package com.pe.allpafood.api.core.utils.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String convertToJsonString(Object objeto){
        try{return objectMapper.writeValueAsString(objeto);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String,Object> convertToMap(String json) {
        try{
            return objectMapper.readValue(json, Map.class);
        }catch (Exception e){
            return new HashMap<>();
        }

    }

    public static <T> T convertToObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        }catch (Exception e){
            log.error("[convertToObject] error to conver to object {}", e.getMessage());
            return null;
        }
    }

    public static <T> T convertToObjectList(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T convertToObjectList(String json) {
        try {
            return objectMapper.readValue(json,  new TypeReference<>() {});
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
