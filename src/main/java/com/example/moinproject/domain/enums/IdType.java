package com.example.moinproject.domain.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IdType {
    @JsonProperty("REG_NO")
    REG_NO,
    @JsonProperty("BUSINESS_NO")
    BUSINESS_NO
}