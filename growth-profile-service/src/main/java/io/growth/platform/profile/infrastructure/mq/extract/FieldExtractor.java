package io.growth.platform.profile.infrastructure.mq.extract;

public interface FieldExtractor {

    /**
     * Extract a value from raw JSON using the given expression.
     *
     * @param rawJson    the raw JSON message
     * @param expression the extraction expression (JSONPath, Groovy script, function name, etc.)
     * @return extracted value, or null if not found
     */
    Object extract(String rawJson, String expression);
}
