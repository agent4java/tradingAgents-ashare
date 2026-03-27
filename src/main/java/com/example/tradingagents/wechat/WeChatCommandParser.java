package com.example.tradingagents.wechat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Best-effort parser for free-form user messages -> (A-share symbol, tradeDate).
 * Regex first; LLM fallback is handled by the caller.
 */
public class WeChatCommandParser {

    private static final Pattern TS_CODE_PATTERN = Pattern.compile("(?i)\\b(\\d{6})\\s*\\.?\\s*(SH|SZ|BJ)?\\b");
    private static final Pattern PREFIX_CODE_PATTERN = Pattern.compile("(?i)\\b(SH|SZ|BJ)\\s*(\\d{6})\\b");

    private static final Pattern DATE_YMD_DASH = Pattern.compile("\\b(\\d{4})-(\\d{1,2})-(\\d{1,2})\\b");
    private static final Pattern DATE_YMD_SLASH = Pattern.compile("\\b(\\d{4})/(\\d{1,2})/(\\d{1,2})\\b");
    private static final Pattern DATE_YMD_COMPACT = Pattern.compile("\\b(\\d{4})(\\d{2})(\\d{2})\\b");
    private static final Pattern DATE_CN = Pattern.compile("\\b(\\d{4})年(\\d{1,2})月(\\d{1,2})日\\b");

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public Optional<ParsedWeChatCommand> parse(String text, LocalDate now) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        LocalDate base = now != null ? now : LocalDate.now();
        String symbol = extractSymbol(text);
        if (symbol == null) {
            return Optional.empty();
        }
        LocalDate date = extractDate(text, base).orElse(base);
        return Optional.of(new ParsedWeChatCommand(symbol, date));
    }

    private static String extractSymbol(String text) {
        String t = text.trim();

        Matcher prefix = PREFIX_CODE_PATTERN.matcher(t);
        if (prefix.find()) {
            String ex = prefix.group(1).toUpperCase(Locale.ROOT);
            String code = prefix.group(2);
            return code + "." + ex;
        }

        Matcher m = TS_CODE_PATTERN.matcher(t);
        if (!m.find()) {
            return null;
        }
        String code = m.group(1);
        String ex = m.group(2);
        if (ex != null && !ex.isBlank()) {
            return code + "." + ex.toUpperCase(Locale.ROOT);
        }

        // Infer exchange when user only provides 6-digit code.
        // 6xxxxxx -> SH, 0/3xxxxxx -> SZ, 8/4xxxxxx -> BJ (best effort)
        char first = code.charAt(0);
        String inferred = switch (first) {
            case '6' -> "SH";
            case '0', '3' -> "SZ";
            case '8', '4' -> "BJ";
            default -> "SH";
        };
        return code + "." + inferred;
    }

    private static Optional<LocalDate> extractDate(String text, LocalDate now) {
        String t = text.trim();
        String lower = t.toLowerCase(Locale.ROOT);

        if (t.contains("今天")) return Optional.of(now);
        if (t.contains("昨天") || t.contains("昨日")) return Optional.of(now.minusDays(1));
        if (t.contains("前天")) return Optional.of(now.minusDays(2));

        LocalDate d;
        d = matchDate(DATE_YMD_DASH, t);
        if (d != null) return Optional.of(d);
        d = matchDate(DATE_YMD_SLASH, t);
        if (d != null) return Optional.of(d);
        d = matchDate(DATE_CN, t);
        if (d != null) return Optional.of(d);

        Matcher compact = DATE_YMD_COMPACT.matcher(t);
        if (compact.find()) {
            String s = compact.group(1) + "-" + compact.group(2) + "-" + compact.group(3);
            try {
                return Optional.of(LocalDate.parse(s, ISO));
            } catch (DateTimeParseException ignored) {
                return Optional.empty();
            }
        }

        // Support "tradeDate=YYYY-MM-DD" or similar fragments implicitly covered above.
        if (lower.contains("tradedate=")) {
            int idx = lower.indexOf("tradedate=");
            String tail = t.substring(idx + "tradedate=".length()).trim();
            if (tail.length() >= 10) {
                String candidate = tail.substring(0, 10);
                try {
                    return Optional.of(LocalDate.parse(candidate, ISO));
                } catch (DateTimeParseException ignored) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    private static LocalDate matchDate(Pattern p, String t) {
        Matcher m = p.matcher(t);
        if (!m.find()) return null;
        String s = m.group(1) + "-" + m.group(2) + "-" + m.group(3);
        try {
            return LocalDate.parse(s, ISO);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}

