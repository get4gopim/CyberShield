package com.example.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class MyProcessor extends AbstractProcessor<CtMethod<?>> {

    private static final String SANITIZE_FN_NAME = "sanitize";

    private static final Logger LOGGER = LoggerFactory.getLogger(MyProcessor.class);

    private static final List<Class> SPRING_PATTERN_LIST = Arrays.asList(PathVariable.class, RequestBody.class, RequestHeader.class, Header.class);

    @Override
    public boolean isToBeProcessed(CtMethod<?> element) {
        // System.out.println(element.getType());
        return element.isPublic(); // filter only public method
    }

    @Override
    public void process(CtMethod<?> element) {
        // ModifierKind modifierKind = element.getModifiers().stream().findFirst().get();
        // System.out.println(modifierKind.toString() + " " + element.getSignature() + " " + element.getBody());

        List<CtStatement> statements = element.getBody().getStatements();

        Set<CtStatement> loggerStmts = statements.stream()
                .filter(x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtInvocationImpl"))
                .filter(x -> x.getReferencedTypes().stream()
                        .filter(y -> y.getQualifiedName().equals("org.slf4j.Logger"))
                        .count() > 0)
                .collect(Collectors.toSet());

        loggerStmts.stream().forEach(x -> {
            StringBuilder sb = new StringBuilder();
            LoggerStatement loggerStatement = getCtElements(x, sb);
            if (loggerStatement.isModificationNeeded()) x.replace(getModifiedLogSnippet(loggerStatement));
        });

        CtClass ctClass = element.getParent(CtClass.class);
        final CtMethod ctMethod = getSanitizeMethod();
        ctClass.addMethod(ctMethod);

        // System.out.println(ctClass);
    }

    private LoggerStatement getCtElements(CtStatement ctStatement, StringBuilder sb) {
        List<LoggerParam> paramList = new ArrayList<>();

        for (int i = 0; i< ctStatement.getDirectChildren().size(); i++) {
            CtElement directChild = ctStatement.getDirectChildren().get(i);
            CtElement ctElement = directChild.clone();

            if (i == 0) sb.append(ctElement.prettyprint()).append(".");

            if (i == 1) {
                String traceLine = ctElement.prettyprint();
                String trace = traceLine.substring(0, traceLine.indexOf("(")+1);
                sb.append(trace);
            }

            if (i > 1) {
                paramList.add(new LoggerParam(ctElement, true));
            }
        }

        return new LoggerStatement(sb, paramList, true);
    }

    private List<LoggerStatement> getCTStatementsList(final CtStatement ctStatement, final StringBuilder sb,
                                                      final List<CtParameter<?>> ctParameters){
        final List<LoggerParam> paramList = new ArrayList<>();
        final List<LoggerStatement> loggerStatements = new ArrayList<>();
        for (int i = 0; i< ctStatement.getDirectChildren().size(); i++) {
            CtElement directChild = ctStatement.getDirectChildren().get(i);
            CtElement ctElement = directChild.clone();
            if (i == 0) sb.append(ctElement.prettyprint()).append(".");

            if (i == 1) {
                String traceLine = ctElement.prettyprint();
                String trace = traceLine.substring(0, traceLine.indexOf("(")+1);
                sb.append(trace);
            }

            if (i > 1) {
                if(LoggerParam.ctLiteralPredicate.test(ctElement)){
                    paramList.add(new LoggerParam(ctElement, false));
                }
                if(LoggerParam.ctVarPredicate.test(ctElement) && doesAnnotationParameterSanitize.test(ctParameters,ctElement)){
                    LOGGER.info("parameter {} should be sanitized",ctElement);
                    paramList.add(new LoggerParam(ctElement, true));
                    loggerStatements.add(new LoggerStatement(sb,paramList,true));
                }

            }
        }
        return loggerStatements;
    }

    private CtMethod getSanitizeMethod() {
        final CtCodeSnippetStatement statementInConstructor = getFactory().Code().createCodeSnippetStatement("return inputParam.toString()");
        final CtBlock<?> ctBlockOfConstructor = getFactory().Code().createCtBlock(statementInConstructor);
        final CtParameter parameter = getFactory().Core().createParameter();
        final CtTypeReference<Object> objRef = getFactory().Code().createCtTypeReference(Object.class);

        parameter.setType(objRef);
        parameter.setSimpleName("inputParam");

        final CtMethod ctMethod = getFactory().Core().createMethod();
        ctMethod.setBody(ctBlockOfConstructor);
        ctMethod.setParameters(Collections.<CtParameter<?>>singletonList(parameter));
        ctMethod.addModifier(ModifierKind.PRIVATE);
        ctMethod.setSimpleName(SANITIZE_FN_NAME);

        final CtTypeReference<String> strRef = getFactory().Code().createCtTypeReference(String.class);
        ctMethod.setType(strRef);
        return ctMethod;
    }

    private CtCodeSnippetStatement getModifiedLogSnippet(LoggerStatement loggerStatement) {
        StringBuilder loggerStmt = loggerStatement.getLoggerStmt();
        List<LoggerParam> paramList = loggerStatement.getLoggerParams();

        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
        snippet.setDocComment("CyberShield Auto Generated Code");

        if (paramList.size() > 0) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            for (LoggerParam logStmt : paramList) {
                CtElement ctParam = logStmt.getCtElement();
                String paramName = ctParam.prettyprint();

                if (logStmt.isSanitizationNeeded() && logStmt.isVariable()) {
                    paramName = SANITIZE_FN_NAME + "(" + ctParam.prettyprint() + ")";
                }

                stringJoiner.add(paramName);
            }
            loggerStmt.append(stringJoiner);
        }

        loggerStmt.append(")");

        //LOGGER.info(sb.toString());

        snippet.setValue(loggerStmt.toString());

        return snippet;
    }

    private final BiPredicate<List<CtParameter<?>>, CtElement> doesAnnotationParameterSanitize = (parametersList, ctElement)->
            parametersList.stream().anyMatch(ctTypeParameter -> ctTypeParameter.getReference().getSimpleName().equalsIgnoreCase(ctElement.prettyprint())
                    && SPRING_PATTERN_LIST.stream().anyMatch(ctTypeParameter::hasAnnotation));

}
