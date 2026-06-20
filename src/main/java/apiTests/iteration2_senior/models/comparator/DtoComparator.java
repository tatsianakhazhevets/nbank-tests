package apiTests.iteration2_senior.models.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class DtoComparator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<String> compare(Object left, Object right) {

        String key = left.getClass().getSimpleName() + ":" + right.getClass().getSimpleName();

        Set<String> mappings = ComparatorConfig.MAPPINGS.get(key);

        if (mappings == null) {
            throw new RuntimeException("No mapping found for " + key);
        }

        JsonNode leftNode = MAPPER.valueToTree(left);
        JsonNode rightNode = MAPPER.valueToTree(right);

        List<String> errors = new ArrayList<>();

        for (String mapping : mappings) {

            String[] parts = mapping.split("=");
            String leftPath = parts[0];
            String rightPath = parts[1];

            JsonNode leftValue = leftNode.get(leftPath);

            List<JsonNode> rightValues = extract(rightNode, rightPath);

            boolean match = rightValues.stream()
                    .anyMatch(r -> Objects.equals(leftValue, r));

            if (!match) {
                errors.add(leftPath + " != " + rightPath +
                        " | left=" + leftValue + " right=" + rightValues);
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

            for (String p : parts) {
                current = current.get(p);
            }
            result.add(current);
        } else {
            result.add(node.get(path));
        }

        return result;
    }
}