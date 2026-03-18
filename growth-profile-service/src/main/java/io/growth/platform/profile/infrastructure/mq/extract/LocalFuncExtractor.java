package io.growth.platform.profile.infrastructure.mq.extract;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

@Component
public class LocalFuncExtractor implements FieldExtractor {

    private static final Logger log = LoggerFactory.getLogger(LocalFuncExtractor.class);

    private final Map<String, BiFunction<String, String, Object>> registry = new ConcurrentHashMap<>();

    /**
     * Register a local function.
     *
     * @param funcName the function name
     * @param func     BiFunction(rawJson, expression) -> result
     */
    public void register(String funcName, BiFunction<String, String, Object> func) {
        registry.put(funcName, func);
    }

    @Override
    public Object extract(String rawJson, String expression) {
        BiFunction<String, String, Object> func = registry.get(expression);
        if (func == null) {
            log.warn("Local function not found: {}", expression);
            return null;
        }
        return func.apply(rawJson, expression);
    }
}
