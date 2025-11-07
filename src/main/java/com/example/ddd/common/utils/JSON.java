package com.example.ddd.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JSON {
    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //为jackjson注册序列化提供能力的对象
        JavaTimeModule javaTimeModule = new JavaTimeModule();
//系列化时间格式化
        javaTimeModule.addSerializer(LocalDateTime.class, new

                LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
//反序列化时间格式化
        javaTimeModule.addDeserializer(LocalDateTime.class, new

                LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
//注册进入jackson
        objectMapper.registerModule(javaTimeModule);
    }

    public static String toJSON(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> List<T> parseListObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }


}
