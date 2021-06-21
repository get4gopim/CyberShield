package com.shield.processor.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrLfMasker implements IMasker {
    private static final Pattern PATTERNCRLF = Pattern.compile("(\r|\n)");

    public CrLfMasker() {
    }

    public String getMaskPattern() {
        return PATTERNCRLF.pattern();
    }

    public static String crLfMask(final String pLogString) {
        if (StringUtils.isEmpty(pLogString)) {
            return pLogString;
        } else {
            Matcher matcher = PATTERNCRLF.matcher(pLogString);
            return !matcher.find() ? pLogString : specialString(pLogString);
        }
    }

    private static String specialString(String result) {
        StringBuilder buffer = new StringBuilder();

        for(int i = 0; i < result.length(); ++i) {
            char c = result.charAt(i);
            if (c == '\r') {
                buffer.append("<CR>");
            } else if (c == '\n' && i == 0) {
                buffer.append("<LF>");
            } else if (c == '\n' && i > 0 && i != result.length() - 1 && result.charAt(i - 1) == '>' && result.charAt(i + 1) == '<') {
                buffer.append(c);
            } else if (c == '\n') {
                buffer.append("<LF>");
            } else {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    public String mask(final String pLogString) {
        return crLfMask(pLogString);
    }
}

