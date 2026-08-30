package apiTests.iteration2_senior.dao.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DaoComparator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<String> compare(Object dto, Object dao) {
        String key = dto.getClass().getSimpleName() + ":" + dao.getClass().getSimpleName();

        Set<String> mappings = DaoComparatorConfig.MAPPINGS.get(key);

        if (mappings == null) {
            throw new RuntimeException("No mapping found for " + key);
        }

        JsonNode dtoNode = MAPPER.valueToTree(dto);
        JsonNode daoNode = MAPPER.valueToTree(dao);

        List<String> errors = new ArrayList<>();

        for (String mapping : mappings) {
            String[] parts = mapping.split("=");
            String dtoPath = parts[0];
            String daoPath = parts[1];
            JsonNode dtoValue = dtoNode.get(dtoPath);
            List<JsonNode> daoValues = extract(daoNode, daoPath);

            boolean match = daoValues.stream()
                    .anyMatch(daoValue -> Objects.equals(dtoValue, daoValue));

            if (!match) {
                errors.add(dtoPath + " != " + daoPath + " | dto=" + dtoValue + " dao=" + daoValues);
            }
        }
        return errors;
    }

    private static List<JsonNode> extract(JsonNode node, String path) {
        List<JsonNode> result = new ArrayList<>();

        if (path.contains("[]")) {
            String field = path.replace("[]", "").split("\\.")[0];
            JsonNode array = node.get(field);

            if (array != null && array.isArray()) {
                for (JsonNode item : array) {
                    String nested = path.substring(path.indexOf('.') + 1);
                    result.add(item.get(nested));
                }
            }
        } else if (path.contains(".")) {
            String[] parts = path.split("\\.");
            JsonNode current = node;
            for (String part : parts) {
                current = current.get(part);
            }
            result.add(current);
        } else {
            result.add(node.get(path));
        }
        return result;
    }
}