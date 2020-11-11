import classes.Simple;
import classes.SimpleArray;
import classes.SimpleNested;
import classes.SimpleNestedNested;
import org.junit.jupiter.api.Test;
import type.DoubleType;
import type.IntegerType;
import type.PhoneType;
import type.StringType;

import javax.xml.bind.ValidationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanitizerTest {
    private static String CORRECT = getResource("correct/simple.json");
    private static String CORRECT_ARRAY = getResource("correct/simple_array.json");
    private static String CORRECT_NESTED = getResource("correct/simple_nested.json");
    private static String CORRECT_NESTED_NESTED = getResource("correct/simple_nested_nested.json");
    private static String CORRECT_NOT_EXISTS_FIELDS = getResource("correct/simple_not_exists_fields.json");

    private static String INCORRECT = getResource("incorrect/simple.json");
    private static String INCORRECT_ARRAY_1 = getResource("incorrect/simple_array_1.json");
    private static String INCORRECT_ARRAY_2 = getResource("incorrect/simple_array_2.json");
    private static String INCORRECT_NESTED = getResource("incorrect/simple_nested.json");
    private static String INCORRECT_NESTED_NESTED = getResource("incorrect/simple_nested_nested.json");

    private static String getResource(String path) {
        return ofNullable(path)
                .map(Sanitizer.class::getResourceAsStream)
                .map(r -> new BufferedReader(new InputStreamReader(r)).lines().collect(Collectors.joining()))
                .orElse("");
    }

    @Test
    void sanitize_SimpleTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {
        Simple simple = Sanitizer.of(CORRECT, Simple.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .get();

        assertNotNull(simple);
        assertEquals(simple.getFoo(), Integer.valueOf(123));
        assertEquals(simple.getBar(), "asd");
        assertEquals(simple.getBaz(), "79502885623");
    }

    @Test
    void sanitize_SimpleArrayTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {
        SimpleArray simple = Sanitizer.of(CORRECT_ARRAY, SimpleArray.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("array", DoubleType[].class)
                .get();

        assertNotNull(simple);
        assertEquals(simple.getFoo(), Integer.valueOf(123));
        assertEquals(simple.getBar(), "asd");
        assertEquals(simple.getBaz(), "79502885623");

        assertEquals(simple.getArray().size(), 3);
        assertTrue(simple.getArray().stream().anyMatch(e -> e.equals(123.1)));
        assertTrue(simple.getArray().stream().anyMatch(e -> e.equals(123.2)));
        assertTrue(simple.getArray().stream().anyMatch(e -> e.equals(123.0)));
    }

    @Test
    void sanitize_SimpleNestedTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {
        Map<String, Object> nested = new HashMap<>();
        nested.put("foo", IntegerType.class);
        nested.put("bar", StringType.class);
        nested.put("baz", PhoneType.class);
        nested.put("array", DoubleType[].class);

        SimpleNested simple = Sanitizer.of(CORRECT_NESTED, SimpleNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested)
                .get();

        assertNotNull(simple);
        assertEquals(simple.getFoo(), Integer.valueOf(123));
        assertEquals(simple.getBar(), "asd");
        assertEquals(simple.getBaz(), "79502885623");

        assertEquals(simple.getNested().getFoo(), Integer.valueOf(123));
        assertEquals(simple.getNested().getBar(), "asd");
        assertEquals(simple.getNested().getBaz(), "79502885623");

        assertEquals(simple.getNested().getArray().size(), 3);
        assertTrue(simple.getNested().getArray().stream().anyMatch(e -> e.equals(123.1)));
        assertTrue(simple.getNested().getArray().stream().anyMatch(e -> e.equals(123.2)));
        assertTrue(simple.getNested().getArray().stream().anyMatch(e -> e.equals(123.0)));
    }

    @Test
    void sanitize_SimpleNestedNestedTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {
        Map<String, Object> nested2 = new HashMap<>();
        nested2.put("foo", IntegerType.class);
        nested2.put("bar", StringType.class);
        nested2.put("baz", PhoneType.class);
        nested2.put("array", DoubleType[].class);

        Map<String, Object> nested1 = new HashMap<>();
        nested1.put("foo", IntegerType.class);
        nested1.put("bar", StringType.class);
        nested1.put("baz", PhoneType.class);
        nested1.put("nested", nested2);

        SimpleNestedNested simple = Sanitizer.of(CORRECT_NESTED_NESTED, SimpleNestedNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested1)
                .get();

        assertNotNull(simple);
        assertEquals(simple.getFoo(), Integer.valueOf(123));
        assertEquals(simple.getBar(), "asd");
        assertEquals(simple.getBaz(), "79502885623");

        assertEquals(simple.getNested().getFoo(), Integer.valueOf(123));
        assertEquals(simple.getNested().getBar(), "asd");
        assertEquals(simple.getNested().getBaz(), "79502885623");

        assertEquals(simple.getNested().getNested().getFoo(), Integer.valueOf(123));
        assertEquals(simple.getNested().getNested().getBar(), "asd");
        assertEquals(simple.getNested().getNested().getBaz(), "79502885623");

        assertEquals(simple.getNested().getNested().getArray().size(), 3);
        assertTrue(simple.getNested().getNested().getArray().stream().anyMatch(e -> e.equals(123.1)));
        assertTrue(simple.getNested().getNested().getArray().stream().anyMatch(e -> e.equals(123.2)));
        assertTrue(simple.getNested().getNested().getArray().stream().anyMatch(e -> e.equals(123.0)));
    }

    @Test
    void sanitize_SimpleNotExistsFieldsTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {
        Map<String, Object> nested2 = new HashMap<>();
        nested2.put("foo", IntegerType.class);
        nested2.put("bar", StringType.class);
        nested2.put("baz", PhoneType.class);
        nested2.put("array", DoubleType[].class);

        Map<String, Object> nested1 = new HashMap<>();
        nested1.put("foo", IntegerType.class);
        nested1.put("bar", StringType.class);
        nested1.put("baz", PhoneType.class);
        nested1.put("nested", nested2);

        SimpleNestedNested simple = Sanitizer.of(CORRECT_NOT_EXISTS_FIELDS, SimpleNestedNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested1)
                .get();

        assertNotNull(simple);
        assertEquals(simple.getFoo(), Integer.valueOf(123));
        assertEquals(simple.getBar(), "asd");
        assertNull(simple.getBaz());

        assertNull(simple.getNested().getFoo());
        assertEquals(simple.getNested().getBar(), "asd");
        assertEquals(simple.getNested().getBaz(), "79502885623");

        assertEquals(simple.getNested().getNested().getFoo(), Integer.valueOf(123));
        assertNull(simple.getNested().getNested().getBar());
        assertEquals(simple.getNested().getNested().getBaz(), "79502885623");

        assertNull(simple.getNested().getNested().getArray());
    }

    @Test
    void sanitize_IncorrectSimpleTest() throws IOException, InstantiationException, IllegalAccessException {
        Sanitizer sanitizer = Sanitizer.of(INCORRECT, Simple.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class);

        Exception exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 2);
        assertTrue(exception.getMessage().contains("foo: Invalid value \"123a\" for type \"IntegerType\""));
        assertTrue(exception.getMessage().contains("baz: Invalid value \"8 (950) 288-56-233\" for type \"PhoneType\""));
    }


    @Test
    void sanitize_IncorrectSimpleArrayTest() throws IOException, InstantiationException, IllegalAccessException {
        Sanitizer sanitizer = Sanitizer.of(INCORRECT_ARRAY_1, SimpleArray.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("array", DoubleType[].class);

        Exception exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 1);
        assertTrue(exception.getMessage().contains("array[1]: Invalid value \"123x\" for type \"DoubleType\""));

        sanitizer = Sanitizer.of(INCORRECT_ARRAY_2, SimpleArray.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("array", DoubleType[].class);

        exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 1);
        assertTrue(exception.getMessage().contains("array: Not array type"));
    }

    @Test
    void sanitize_IncorrectSimpleNestedTest() throws IOException, InstantiationException, IllegalAccessException {
        Map<String, Object> nested = new HashMap<>();
        nested.put("foo", IntegerType.class);
        nested.put("bar", StringType.class);
        nested.put("baz", PhoneType.class);
        nested.put("array", DoubleType[].class);

        Sanitizer sanitizer = Sanitizer.of(INCORRECT_NESTED, SimpleNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested);

        Exception exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 2);
        assertTrue(exception.getMessage().contains("nested/array[1]: Invalid value \"123s\" for type \"DoubleType\""));
        assertTrue(exception.getMessage().contains("nested/foo: Invalid value \"123s\" for type \"IntegerType\""));
    }

    @Test
    void sanitize_IncorrectSimpleNestedNestedTest() throws IOException, InstantiationException, IllegalAccessException, ValidationException {

        Map<String, Object> nested2 = new HashMap<>();
        nested2.put("foo", IntegerType.class);
        nested2.put("bar", StringType.class);
        nested2.put("baz", PhoneType.class);
        nested2.put("array", DoubleType[].class);

        Map<String, Object> nested1 = new HashMap<>();
        nested1.put("foo", IntegerType.class);
        nested1.put("bar", StringType.class);
        nested1.put("baz", PhoneType.class);
        nested1.put("nested", nested2);

        Sanitizer sanitizer = Sanitizer.of(INCORRECT_NESTED_NESTED, SimpleNestedNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested1);

        Exception exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 3);
        assertTrue(exception.getMessage().contains("nested/foo: Invalid value \"123d\" for type \"IntegerType\""));
        assertTrue(exception.getMessage().contains("nested/nested/array[2]: Invalid value \"123x\" for type \"DoubleType\""));
        assertTrue(exception.getMessage().contains("nested/nested/baz: Invalid value \"8 (950) 288\" for type \"PhoneType\""));
    }

    @Test
    void sanitize_UnsupportedTypesTest() throws IOException, InstantiationException, IllegalAccessException {

        Map<String, Object> nested2 = new HashMap<>();
        nested2.put("foo", String.class);
        nested2.put("bar", StringType.class);
        nested2.put("baz", Object.class);
        nested2.put("array", File[].class);

        Map<String, Object> nested1 = new HashMap<>();
        nested1.put("foo", IntegerType.class);
        nested1.put("bar", null);
        nested1.put("baz", PhoneType.class);
        nested1.put("nested", nested2);

        Sanitizer sanitizer = Sanitizer.of(CORRECT_NESTED_NESTED, SimpleNestedNested.class)
                .map("foo", IntegerType.class)
                .map("bar", StringType.class)
                .map("baz", PhoneType.class)
                .map("nested", nested1);

        Exception exception = assertThrows(ValidationException.class, sanitizer::get);
        assertEquals(exception.getMessage().split(System.getProperty("line.separator")).length, 4);
        assertTrue(exception.getMessage().contains("nested/bar: Type \"null\" not support"));
        assertTrue(exception.getMessage().contains("nested/nested/array: Type File[] not support"));
        assertTrue(exception.getMessage().contains("nested/nested/foo: Type String not support"));
        assertTrue(exception.getMessage().contains("nested/nested/baz: Type Object not support"));
    }
}