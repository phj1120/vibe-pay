package com.api.app.emum;

public class CommonCodeUtil {

    public static <T extends Enum<T> & CommonCode> T findByCode(Class<T> enumClass, String code) {
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getCode().equals(code)) {
                return enumConstant;
            }
        }
        return null;
    }
}
