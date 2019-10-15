package com.bo.loganalyzer.model;

import java.util.HashMap;
import java.util.Map;

public enum RequestType {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH");

    private String value;

    private RequestType(String value){
        this.value = value;
    }

    private static Map<String, RequestType> keyToValueMapping;

    public static RequestType getRequestType(String i){
        if(keyToValueMapping == null){
            initMapping();
        }
        return keyToValueMapping.get(i);
    }

    private static void initMapping(){
        keyToValueMapping = new HashMap<>();
        for(RequestType s : values()){
            keyToValueMapping.put(s.value, s);
        }
    }

    public String getValue() {
        return value;
    }
}
