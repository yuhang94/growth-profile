package io.growth.platform.profile.infrastructure.mq.extract;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.SimpleBindings;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GroovyExtractor implements FieldExtractor {

    private static final Logger log = LoggerFactory.getLogger(GroovyExtractor.class);
    private static final long TIMEOUT_MS = 500;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final GroovyScriptEngineFactory ENGINE_FACTORY = new GroovyScriptEngineFactory();

    private final ExecutorService executor;

    public GroovyExtractor() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "groovy-extractor");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public Object extract(String rawJson, String expression) {
        try {
            Map<String, Object> msg = OBJECT_MAPPER.readValue(rawJson, new TypeReference<>() {});

            Bindings bindings = new SimpleBindings();
            bindings.put("msg", msg);

            ScriptEngine engine = ENGINE_FACTORY.getScriptEngine();
            Callable<Object> task = () -> engine.eval(expression, bindings);
            Future<Object> future = executor.submit(task);

            return future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Groovy script execution timeout ({}ms): {}", TIMEOUT_MS, expression);
            return null;
        } catch (ExecutionException e) {
            log.warn("Groovy script execution error: {}", e.getCause().getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Groovy extractor error: {}", e.getMessage());
            return null;
        }
    }
}
