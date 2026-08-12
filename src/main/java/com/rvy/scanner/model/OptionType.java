package com.rvy.scanner.model;

public enum OptionType {
    CALL,
    PUT;

    public static OptionType fromOcc(char flag) {
        return flag == 'P' || flag == 'p' ? PUT : CALL;
    }

    public static OptionType fromApi(String value) {
        if (value == null) {
            return CALL;
        }
        return "put".equalsIgnoreCase(value) ? PUT : CALL;
    }
}
