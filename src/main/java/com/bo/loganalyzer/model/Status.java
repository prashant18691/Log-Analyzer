package com.bo.loganalyzer.model;

import java.util.HashMap;
import java.util.Map;

public enum Status {
    SUCCESS(200, "SUCCESS"),
    CREATED(201, "CREATED"),
    BAD_REQUEST(400, "BAD_REQUEST"),
    NOT_AUTHORISED(401, "NOT_AUTHORISED"),
    NOT_FOUND(404, "NOT_FOUND"),
    INTERNAL_ERROR(503, "INTERNAL_ERROR");

    private int key;
    private String value;

    private Status(int key, String value){
        this.key = key;
        this.value = value;
    }

    private static Map<Integer, Status> keyToValueMapping;

    private static Map<String, Status> keyStringToValueMapping;

    public static Status getStatus(int i){
        if(keyToValueMapping == null){
            initMapping();
        }
        return keyToValueMapping.get(i);
    }

    public static Status getStatusFromStr(String s){
        if(keyStringToValueMapping == null){
            initMapping();
        }
        return keyStringToValueMapping.get(s);
    }

    private static void initMapping(){
        keyToValueMapping = new HashMap<>();
        keyStringToValueMapping = new HashMap<>();
        for(Status s : values()){
            keyToValueMapping.put(s.key, s);
            keyStringToValueMapping.put(s.value, s);
        }
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
