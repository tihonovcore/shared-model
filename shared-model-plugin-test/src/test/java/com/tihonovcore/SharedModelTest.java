package com.tihonovcore;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tihonovcore.inheritance.B;
import com.tihonovcore.nested.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SharedModelTest {

    private final ObjectMapper mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void singleClass() throws IOException {
        var bytes = """
            {
                "name": "petr",
                "age": 123,
                "flag": true,
                "phones": ["1234321", "000"]
            }
            """.getBytes();

        var user = mapper.readValue(bytes, User.class);
        var string = mapper.writeValueAsString(user);

        var expected = mapper.readValue(bytes, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }

    @Test
    void nestedClasses() throws IOException {
        var bytes = """
            {
                "foo": "foo",
                "outer": 123,
                "inner": {
                    "bar": "bar",
                    "inner": 123
                }
            }
            """.getBytes();

        var outer = mapper.readValue(bytes, Outer.class);
        var string = mapper.writeValueAsString(outer);

        var expected = mapper.readValue(bytes, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }

    @Test
    void update() throws IOException {
        var bytes = """
            {
                "name": "petr",
                "age": 123,
                "flag": true
            }
            """.getBytes();

        var user = mapper.readValue(bytes, User.class);
        user.setName(user.getName().toUpperCase());
        user.setAge(user.getAge() + 1);

        var string = mapper.writeValueAsString(user);

        var expected = mapper.readValue("""
            {
                "name": "PETR",
                "age": 124,
                "flag": true
            }
            """, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }

    @Test
    void unannotated() throws IOException {
        var bytes = """
            {
                "name": "petr",
                "age": 123
            }
            """.getBytes();

        var user = mapper.readValue(bytes, Unannotated.class);
        var string = mapper.writeValueAsString(user);

        var expected = mapper.readValue("""
            {
                "age": 123
            }
            """, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }

    @Test
    void inheritance() throws IOException {
        var bytes = """
            {
                "u": 0,
                "x": 1,
                "y": 2,
                "z": 3
            }
            """.getBytes();

        var b = mapper.readValue(bytes, B.class);
        var string = mapper.writeValueAsString(b);

        var expected = mapper.readValue(bytes, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }


    @Test
    void nested() throws IOException {
        var bytes = """
            {
                "id": "3ab807ab-557a-4e66-a3ea-aa46767d0cc3",
                "name": "process users",
                "additionalInfo": 123,
                "context": {
                    "owner": "customer-service",
                    "startTime": "2024-03-03T10:10:10Z",
                    "additionalInfo": 123
                }
            }
            """.getBytes();

        var task = mapper.readValue(bytes, Task.class);
        var string = mapper.writeValueAsString(task);

        var expected = mapper.readValue(bytes, Map.class);
        var actual = mapper.readValue(string.getBytes(), Map.class);
        assertEquals(expected, actual);
    }
}
