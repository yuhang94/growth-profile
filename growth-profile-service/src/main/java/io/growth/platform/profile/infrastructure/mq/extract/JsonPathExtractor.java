package io.growth.platform.profile.infrastructure.mq.extract;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JsonPathExtractor implements FieldExtractor {

    @Override
    public Object extract(String rawJson, String expression) {
        try {
            return JsonPath.parse(rawJson).read(expression);
        } catch (PathNotFoundException e) {
            return null;
        }
    }
}
