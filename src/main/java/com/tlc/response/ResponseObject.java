package com.tlc.response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ResponseObject extends HashMap<String, Object> {
    public ResponseObject() {
        super();

        this.put("success", true);
        this.put("code", ResponseCode.SUCCESS.getCode());
        this.put("http_status_code", 200);
    }

    public Map<String, Object> toMap(JSONObject object) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.opt(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        for(int i=0; i<array.length(); i++) {
            Object value = array.opt(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public void putResult(Object obj) {
        if (obj instanceof JSONArray) {
            this.put("result", toList((JSONArray)obj));
        } else if (obj instanceof JSONObject) {
            this.put("result", toMap((JSONObject)obj));
        } else {
            this.put("result", obj);
        }
    }
}
