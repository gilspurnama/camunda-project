package com.example.workflow.util;

public interface StringUtils {

    static String nvl(String value, String replacement){
        return value == null?replacement:value;
    }

    static String bie(String value){
        return nvl(value,"");
    }

    static boolean isEmpty(String text){
        return text == null || text.trim().length() == 0;
    }
}
