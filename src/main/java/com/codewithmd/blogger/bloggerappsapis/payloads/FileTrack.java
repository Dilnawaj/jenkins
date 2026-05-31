package com.codewithmd.blogger.bloggerappsapis.payloads;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
public enum FileTrack{
    PENDING ,SUCCESS ,FAILED ,PROCESSING;


    @JsonValue
    public String toValue() {
        return this.name();
    }

    @JsonCreator
    public static FileTrack fromValue(String value) {
        return valueOf(value.toUpperCase());
    }
}
