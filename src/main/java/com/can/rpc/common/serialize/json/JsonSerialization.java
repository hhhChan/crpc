package com.can.rpc.common.serialize.json;

import com.can.rpc.common.serialize.Serialization;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

/**
 * @author ccc
 */
public class JsonSerialization implements Serialization {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的全部字段全部写入
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //取消默认转换timestamp
        objectMapper.disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        //忽略空bean转json的错误
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //所有日期同一格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //忽略在json字符串中存在，但是在java对象中不存在对应属性的情况，防止出错
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public byte[] serialize(Object output) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(output);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] input, Class c) throws Exception {
        return objectMapper.readValue(input, c);
    }
}
