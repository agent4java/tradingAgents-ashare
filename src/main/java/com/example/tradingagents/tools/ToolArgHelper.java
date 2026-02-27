package com.example.tradingagents.tools;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

final class ToolArgHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DATE_ERR = "日期格式应为 YYYY-MM-DD";

    static Map<String, Object> parseArgs(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = MAPPER.readValue(argumentsJson, Map.class);
            return map != null ? map : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    static String getString(Map<String, Object> args, String key) {
        Object v = args.get(key);
        return v != null ? v.toString().trim() : null;
    }

    static LocalDate parseDate(Map<String, Object> args, String key) {
        String s = getString(args, key);
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    static int getInt(Map<String, Object> args, String key, int defaultValue) {
        Object v = args.get(key);
        if (v == null) return defaultValue;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static String errorInvalidDate(String paramName) {
        return "参数 " + paramName + " 无效。" + DATE_ERR;
    }
}
