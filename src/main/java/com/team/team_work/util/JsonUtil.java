package com.team.team_work.util;
import java.io.IOException;
import tools.jackson.databind.json.JsonMapper;
public final class JsonUtil {
    private static final JsonMapper MAPPER=JsonMapper.builder().build();
    private JsonUtil() { }
    public static <T> T jsonToObj(Class<T> type,String json) throws IOException {
        try {return MAPPER.readValue(json,type);}catch(tools.jackson.core.JacksonException e){throw new IOException(e);}
    }
    public static String objToJson(Object value) {return MAPPER.writeValueAsString(value);}
}
