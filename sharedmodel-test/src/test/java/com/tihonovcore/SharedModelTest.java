package com.tihonovcore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SharedModelTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
}
