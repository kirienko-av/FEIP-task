import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import type.BaseType;

import javax.xml.bind.ValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class Sanitizer<T> {
    private final JsonNode node;
    private final T target;
    private final StringBuffer error;

    private Sanitizer(String json, Class<T> type) throws IOException, IllegalAccessException, InstantiationException {
        this.node = new ObjectMapper().readTree(json);
        this.target = type.newInstance();
        this.error = new StringBuffer();
    }

    public static <T> Sanitizer<T> of(String json, Class<T> type) throws IOException, InstantiationException, IllegalAccessException {
        return new Sanitizer<>(json, type);
    }

    public Sanitizer<T> map(String key, Class type) throws IllegalAccessException, IOException, InstantiationException {
        final BaseType baseType;
        final JsonNode currentNode = node.get(key);
        final Field field = getField(key);
        String currentField = key;

        if (ofNullable(currentNode).isPresent()) {
            try {
                if (Types.BASE.is(type)) {
                    baseType = (BaseType) type.newInstance();
                    applyFieldValue(field, baseType.transform(currentNode.asText()));
                } else if (Types.BASE_ARRAY.is(type)) {
                    final List<Object> list = new ArrayList<>();
                    baseType = (BaseType) type.getComponentType()
                            .newInstance();

                    if (currentNode.isArray()) {
                        int i = 0;
                        for (Iterator<JsonNode> it = currentNode.iterator(); it.hasNext(); i++) {
                            currentField = String.format("%s[%d]", key, i);
                            list.add(baseType.transform(it.next().asText()));
                        }
                        applyFieldValue(field, list);
                    } else {
                        throw new ValidationException("Not array type");
                    }
                } else {
                    throw new ValidationException(String.format("Type %s not support",
                            ofNullable((Class<?>) type).map(Class::getSimpleName)
                                    .orElse("\"null\"")));
                }
            } catch (ValidationException e) {
                error.append(String.format("%s: %s", currentField, e.getMessage()))
                        .append(System.getProperty("line.separator"));
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public Sanitizer<T> map(String key, Map<String, Object> map) throws IllegalAccessException, IOException, InstantiationException {
        final JsonNode currentNode = node.get(key);
        final Field field = getField(key);

        if (ofNullable(currentNode).isPresent()) {
            if (Types.MAP.is(map)) {
                Sanitizer sanitizer = Sanitizer.of(currentNode.toString(), field.getType());

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (Types.MAP.is(entry.getValue())) {
                        sanitizer.map(entry.getKey(), (Map<String, Object>) entry.getValue());
                    } else {
                        sanitizer.map(entry.getKey(), (Class) entry.getValue());
                    }
                }
                try {
                    applyFieldValue(field, sanitizer.get());
                } catch (ValidationException e) {
                    new BufferedReader(new StringReader(e.getMessage())).lines().forEach(l ->
                            error.append(String.format("%s/%s", key, l))
                                    .append(System.getProperty("line.separator"))
                    );
                }
            }
        }
        return this;
    }

    private Field getField(String key) {
        Class tmp = this.target.getClass();
        Stream<Field> fields = Stream.of(tmp.getDeclaredFields());

        while (ofNullable(tmp.getSuperclass()).isPresent()) {
            tmp = tmp.getSuperclass();
            fields = Stream.concat(fields, Stream.of(tmp.getDeclaredFields()));
        }
        return fields
                .filter(f -> key.equals(f.getName()))
                .findFirst().orElse(null);
    }

    private void applyFieldValue(Field field, Object value) throws IllegalAccessException {
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(this.target, value);
        field.setAccessible(accessible);
    }

    public T get() throws ValidationException {
        if (error.length() != 0) {
            throw new ValidationException(error.toString());
        }
        return this.target;
    }

    public enum Types {
        BASE {
            @Override
            public boolean is(Object object) {
                boolean result;
                try {
                    result = ofNullable(object)
                            .map(c -> (Class) c)
                            .filter(BaseType.class::isAssignableFrom)
                            .isPresent();
                } catch (ClassCastException e) {
                    result = false;
                }
                return result;
            }
        },
        BASE_ARRAY {
            @Override
            public boolean is(Object object) {
                boolean result;
                try {
                    result = ofNullable(object)
                            .map(c -> (Class) c)
                            .filter(BaseType[].class::isAssignableFrom)
                            .isPresent();
                } catch (ClassCastException e) {
                    result = false;
                }
                return result;
            }
        },
        MAP {
            @Override
            public boolean is(Object object) {
                return object instanceof Map;
            }
        };

        public abstract boolean is(Object object);
    }
}
