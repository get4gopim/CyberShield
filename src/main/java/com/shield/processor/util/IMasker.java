package com.shield.processor.util;

public interface IMasker {
    String getMaskPattern();

    String mask(String pLogString);

    default boolean isValid() {
        return true;
    }
}
