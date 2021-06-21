package com.shield.processor.model;

import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtElement;

import java.util.function.Predicate;

public class LoggerParam {

    private CtElement ctElement;
    private String ctShortDesc;

    private boolean isStringLiteral;
    private boolean isVariable;
    private boolean isSanitizationNeeded;

    public static final Predicate<CtElement> ctVarPredicate = x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtVariableReadImpl");
    public static final Predicate<CtElement> ctLiteralPredicate = x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtLiteralImpl");
    public static final Predicate<CtElement> ctInvocationPredicate = x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtInvocation");

    public LoggerParam() {}

    public LoggerParam(CtElement ctElement, boolean isSanitizationNeeded) {
        this.ctElement = ctElement;
        this.isSanitizationNeeded = isSanitizationNeeded;

        this.ctShortDesc = getShortDesc(ctElement.getShortRepresentation());
        this.isStringLiteral = ctLiteralPredicate.test(ctElement);
        this.isVariable = ctVarPredicate.test(ctElement);
    }

    private String getShortDesc(String shortRepresentation) {
        String shortDesc = null;
        if (StringUtils.isNotBlank(shortRepresentation)) {
            if (shortRepresentation.indexOf("@") > -1) {
                shortDesc = shortRepresentation.substring(0, shortRepresentation.indexOf("@"));
            }
        }
        return shortDesc;
    }

    public CtElement getCtElement() {
        return ctElement;
    }

    public void setCtElement(CtElement ctElement) {
        this.ctElement = ctElement;
    }

    public String getCtShortDesc() {
        return ctShortDesc;
    }

    public void setCtShortDesc(String ctShortDesc) {
        this.ctShortDesc = ctShortDesc;
    }

    public boolean isStringLiteral() {
        return isStringLiteral;
    }

    public void setStringLiteral(boolean stringLiteral) {
        isStringLiteral = stringLiteral;
    }

    public boolean isSanitizationNeeded() {
        return isSanitizationNeeded;
    }

    public void setSanitizationNeeded(boolean sanitizationNeeded) {
        isSanitizationNeeded = sanitizationNeeded;
    }

    public boolean isVariable() {
        return isVariable;
    }

    public void setVariable(boolean variable) {
        isVariable = variable;
    }
}
