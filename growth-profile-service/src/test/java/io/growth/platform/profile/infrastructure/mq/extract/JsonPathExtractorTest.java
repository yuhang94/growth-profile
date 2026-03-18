package io.growth.platform.profile.infrastructure.mq.extract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonPathExtractorTest {

    private JsonPathExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new JsonPathExtractor();
    }

    @Test
    void extract_simpleField() {
        String json = """
                {"userId": "user001", "amount": 100}
                """;
        Object result = extractor.extract(json, "$.userId");
        assertEquals("user001", result);
    }

    @Test
    void extract_nestedField() {
        String json = """
                {"data": {"user": {"id": "user001"}}}
                """;
        Object result = extractor.extract(json, "$.data.user.id");
        assertEquals("user001", result);
    }

    @Test
    void extract_arrayElement() {
        String json = """
                {"items": [{"name": "a"}, {"name": "b"}]}
                """;
        Object result = extractor.extract(json, "$.items[0].name");
        assertEquals("a", result);
    }

    @Test
    void extract_arrayField() {
        String json = """
                {"tags": ["java", "spring"]}
                """;
        Object result = extractor.extract(json, "$.tags");
        assertInstanceOf(List.class, result);
        assertEquals(2, ((List<?>) result).size());
    }

    @Test
    void extract_nonExistentPath_returnsNull() {
        String json = """
                {"userId": "user001"}
                """;
        Object result = extractor.extract(json, "$.nonexistent");
        assertNull(result);
    }

    @Test
    void extract_numericValue() {
        String json = """
                {"amount": 99.5}
                """;
        Object result = extractor.extract(json, "$.amount");
        assertEquals(99.5, result);
    }

    @Test
    void extract_booleanValue() {
        String json = """
                {"active": true}
                """;
        Object result = extractor.extract(json, "$.active");
        assertEquals(true, result);
    }
}
