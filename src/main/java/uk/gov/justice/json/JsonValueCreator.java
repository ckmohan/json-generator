package uk.gov.justice.json;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class JsonValueCreator {

    private final Random random = new Random();

    public String createJsonArray(final Map jsonSchema) {
        Boolean uniqueItems = null;
        if (jsonSchema.containsKey("uniqueItems")) {
            uniqueItems = (Boolean) jsonSchema.get("uniqueItems");
        }
        Integer minItems = null;
        if (jsonSchema.containsKey("minItems")) {
            minItems = ((Double) jsonSchema.get("minItems")).intValue();
        }

        final Object items;
        if (jsonSchema.containsKey("items")) {
            items = jsonSchema.get("items");
        } else if (jsonSchema.containsKey("properties")) {
            items = jsonSchema.get("properties");
        } else {
            throw new RuntimeException("Unknown Array Type:");
        }

        String arrayValue;

        if (items instanceof Map) {
            arrayValue = new JsonFromSchemaMapCreator().createJson((Map) items);
        } else if (items instanceof List) {
            List<Map> listOfItems = (List<Map>) items;
            arrayValue = listOfItems.stream().map(this::createJsonObject).collect(joining(", "));

        } else {
            throw new RuntimeException("Unknown Array Type:" + items);
        }

        return format("[%s]", arrayValue);

    }

    public Integer createRandomInteger(Map jsonSchema) {
        int minimum = Integer.MIN_VALUE, maximum = Integer.MAX_VALUE;

        if (jsonSchema.containsKey("minimum")) {
            minimum = ((Double) jsonSchema.get("minimum")).intValue();
        }
        if (jsonSchema.containsKey("maximum")) {
            maximum = ((Double) jsonSchema.get("maximum")).intValue();
        }
//        return new IntegerGenerator(minimum, maximum).next();
        return -1;
    }



    public String createJsonObject(final Map jsonSchema) {
        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        if (jsonSchema.containsKey("required")) {
            final List<String> required = (List<String>) jsonSchema.get("required");

            if (!required.isEmpty()) {
                assert (jsonSchema.containsKey("properties"));
                final Map childJsonObj = (Map) jsonSchema.get("properties");
                required.forEach(s -> {
                    assert childJsonObj.containsKey(s);
                });

            }
        }

        if (jsonSchema.containsKey("oneOf")) {
            final List oneOf = (List) jsonSchema.get("oneOf");
            return createJsonObject((Map) oneOf.get(random.nextInt(oneOf.size())));
        }

        if (jsonSchema.containsKey("anyOf")) {
            final List anyOf = (List) jsonSchema.get("anyOf");
            return createJsonObject((Map) anyOf.get(random.nextInt(anyOf.size())));
        }

        final Map childJsonObj = (Map) jsonSchema.get("properties");
        //TODO generate all required fields and optional fields :)
        if (childJsonObj != null) {
            sb.append(childJsonObj.keySet().stream().map(key -> format("\"%s\":", key) + processValue(childJsonObj, key)).collect(joining(", ")));
        }

        sb.append('}');


        return sb.toString();
    }

    public boolean createRandomBoolean() {
        return random.nextBoolean();
    }

    public String processValue(Map childJsonObj, Object key) {
        final Map value = (Map) childJsonObj.get(key);
        if (value.containsKey("enum")) {
            return processSchemaJsonObjectForEnum(value);
        } else if (value.containsKey("type")) {
            return new JsonFromSchemaMapCreator().createJson(value);
        } else if (value.containsKey("$ref")) {
            return processSchemaJsonObjectForRef(value);
        } else {
            return "";
        }
    }

    private String processSchemaJsonObjectForEnum(Map jsonObj) {
        final Object anEnum = jsonObj.get("enum");
        final List<String> items = (List<String>) anEnum;

        final Object next1 = items.get(random.nextInt(items.size()));
        if (next1 instanceof Boolean) {
            return next1.toString();
        }
        final String next = (String) next1;
        if (next.equalsIgnoreCase("true") || next.equalsIgnoreCase("false")) {
            return next;
        }
        return format("\"%s\"", next);
    }

    private String processSchemaJsonObjectForRef(final Map schemaMap) {
        final String ref = (String) schemaMap.get("$ref");
        final String definitionKey = ref.replace("#/definitions/", "");
        return getJsonForDefinition(definitionKey, schemaMap);
    }

    private String getJsonForDefinition(final String definitionKey, final Map schemaMap) {
        final Map definitions = (Map) schemaMap.get("definitions");
        final Map definition = (Map) definitions.get(definitionKey);
        return new JsonFromSchemaMapCreator().createJson(definition);
    }
}