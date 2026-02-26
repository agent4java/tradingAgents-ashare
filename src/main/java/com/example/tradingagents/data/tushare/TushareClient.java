package com.example.tradingagents.data.tushare;

import com.example.tradingagents.config.TushareProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Low-level client for Tushare Pro API (https://api.tushare.pro).
 * POST with api_name, token, params, fields; response: code, msg, data (array of arrays), fields (array of strings).
 */
@Component
public class TushareClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final RestClient restClient;
    private final String token;

    public TushareClient(TushareProperties props) {
        this.token = props.getToken() != null ? props.getToken() : "";
        this.restClient = RestClient.builder()
                .baseUrl(props.getApiUrl() != null ? props.getApiUrl() : "https://api.tushare.pro")
                .build();
    }

    /**
     * Call Tushare API. params and fields can be null.
     * Returns list of rows as Map (field name -> value). Returns empty list on error or missing token.
     */
    public List<Map<String, Object>> request(String apiName, Map<String, String> params, String fields) {
        if (token == null || token.isBlank()) {
            return Collections.emptyList();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("api_name", apiName);
        body.put("token", token);
        if (params != null && !params.isEmpty()) {
            body.put("params", params);
        }
        if (fields != null && !fields.isBlank()) {
            body.put("fields", fields);
        }
        try {
            String response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(response);
            int code = root.path("code").asInt(0);
            if (code != 0) {
                return Collections.emptyList();
            }
            JsonNode dataNode = root.path("data");
            JsonNode fieldsNode = dataNode.path("fields");
            JsonNode itemsNode = dataNode.path("items");
            if (!itemsNode.isArray() || !fieldsNode.isArray()) {
                return Collections.emptyList();
            }
            List<String> fieldList = new ArrayList<>();
            fieldsNode.forEach(f -> fieldList.add(f.asText()));
            List<Map<String, Object>> rows = new ArrayList<>();
            for (JsonNode row : itemsNode) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < fieldList.size() && i < row.size(); i++) {
                    JsonNode v = row.get(i);
                    if (v != null && !v.isNull()) {
                        if (v.isNumber()) {
                            map.put(fieldList.get(i), v.numberValue());
                        } else {
                            map.put(fieldList.get(i), v.asText());
                        }
                    } else {
                        map.put(fieldList.get(i), null);
                    }
                }
                rows.add(map);
            }
            return rows;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isConfigured() {
        return token != null && !token.isBlank();
    }
}
