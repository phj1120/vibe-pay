package com.api.app.emum;

public interface CommonCode {
    String getCode();
    String getCodeName();
    int getDisplaySequence();
    String getReferenceValue1();
    String getReferenceValue2();

    default boolean isEquals(String code) {
        return this.getCode().equals(code);
    }
}
