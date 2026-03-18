package io.growth.platform.profile.infrastructure.mq.extract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroovyExtractorTest {

    private GroovyExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new GroovyExtractor();
    }

    @Test
    void extract_simpleExpression() {
        String json = """
                {"value": 42}
                """;
        Object result = extractor.extract(json, "msg.value");
        assertEquals(42, result);
    }

    @Test
    void extract_mathOperation() {
        String json = """
                {"price": 100, "quantity": 3}
                """;
        Object result = extractor.extract(json, "msg.price * msg.quantity");
        assertEquals(300, result);
    }

    @Test
    void extract_stringConcatenation() {
        String json = """
                {"firstName": "John", "lastName": "Doe"}
                """;
        Object result = extractor.extract(json, "msg.firstName + ' ' + msg.lastName");
        assertEquals("John Doe", result);
    }

    @Test
    void extract_nestedAccess() {
        String json = """
                {"data": {"user": {"name": "Alice"}}}
                """;
        Object result = extractor.extract(json, "msg.data.user.name");
        assertEquals("Alice", result);
    }

    @Test
    void extract_nullField_returnsNull() {
        String json = """
                {"value": null}
                """;
        Object result = extractor.extract(json, "msg.nonexistent");
        assertNull(result);
    }

    @Test
    void extract_invalidScript_returnsNull() {
        String json = """
                {"value": 1}
                """;
        Object result = extractor.extract(json, "invalid syntax {{{}}}");
        assertNull(result);
    }
}
