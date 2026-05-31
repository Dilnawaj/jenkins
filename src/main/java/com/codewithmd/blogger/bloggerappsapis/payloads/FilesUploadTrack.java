package com.codewithmd.blogger.bloggerappsapis.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


public enum FilesUploadTrack {
    UPLOADED, PROCESSING, COMPLETED, PARTIAL_FAILED;


    @JsonValue
    public String toValue() {
        return this.name();
    }

    @JsonCreator
    public static FilesUploadTrack fromValue(String value) {
        return valueOf(value.toUpperCase());
    }

}
