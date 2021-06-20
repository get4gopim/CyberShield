package com.shield.processor;

import com.shield.processor.model.LoggerParam;
import com.shield.processor.model.LoggerStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class MyProcessor extends AbstractProcessor<CtMethod<?>> {

    private static final String SANITIZE_FN_NAME = "sanitize";

    private static final Logger LOGGER = LoggerFactory.getLogger(MyProcessor.class);

    private static final List<Class> SPRING_PATTERN_LIST = Arrays.asList(PathVariable.class, RequestBody.class, RequestHeader.class, RequestParam.class, Header.class);

    private static final String RESP_ENTITY_REF_TYPE = "org.springframework.http.ResponseEntity";

    private final BiPredicate<List<CtParameter<?>>, CtElement> doesAnnotationParameterSanitize = (parametersList, ctElement) ->
            parametersList.stream().anyMatch(ctTypeParameter -> ctTypeParameter.getReference().getSimpleName().equalsIgnoreCase(ctElement.prettyprint())
                    && SPRING_PATTERN_LIST.stream().anyMatch(ctTypeParameter::hasAnnotation) );

    @Override
    public boolean isToBeProcessed(CtMethod<?> element) {
        // System.out.println(element.getType());
        return element.isPublic(); // filter only public method
    }

    @Override
    public void process(CtMethod<?> ctMethod) {
        // ModifierKind modifierKind = element.getModifiers().stream().findFirst().get();
        // System.out.println(modifierKind.toString() + " " + element.getSignature() + " " + element.getBody());

        List<CtParameter<?>> ctMethodParams = ctMethod.getParameters();
        List<CtStatement> statements = ctMethod.getBody().getStatements();

        //statements.stream().forEach(x -> System.out.println( x.getShortRepresentation()) );

        //statements.stream().forEach(x -> System.out.println(x + " :: " + x.getShortRepresentation() + " :: " + x.getDirectChildren() ));

        /*Set<CtStatement> invokeStmts = statements.stream()
                .filter(x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtLocalVariableImpl"))
                .peek(x -> {
                    System.out.print(x + " :: ");
                    x.getDirectChildren().forEach(y -> System.out.print(y.getDirectChildren() + " || "));
                    System.out.println("");
                } )
                .collect(Collectors.toSet());*/

        //invokeStmts.forEach(System.out::println);

        Set<CtStatement> loggerStmts = statements.stream()
                .filter(x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtInvocationImpl"))
                .filter(x -> x.getReferencedTypes().stream()
                        .filter(y -> y.getQualifiedName().equals("org.slf4j.Logger"))
                        .count() > 0)
                .collect(Collectors.toSet());

       /* loggerStmts.stream().forEach(x -> {
            StringBuilder sb = new StringBuilder();
            LoggerStatement loggerStatement = getCtElements(x, sb);
            if (loggerStatement.isModificationNeeded()) x.replace(getModifiedLogSnippet(loggerStatement));
        });*/

        loggerStmts.stream().forEach(ctStatement -> {
            StringBuilder sb = new StringBuilder();
            LoggerStatement loggerStatement = getCTStatementsList(ctStatement, sb, ctMethodParams);
            if (loggerStatement.isModificationNeeded()) ctStatement.replace(getModifiedLogSnippet(loggerStatement));
        });

        CtClass ctClass = ctMethod.getParent(CtClass.class);
        final CtMethod sanitizeMethod = getSanitizeMethod();
        ctClass.addMethod(sanitizeMethod);

        // System.out.println(ctClass);
    }

    private boolean isResponseEntity(CtMethod<?> ctMethod, final String localVarName) {
        //System.out.println("Find Variable: { "  + localVarName + " }");

        // Find by methodParam tracking
        Optional<CtParameter<?>> optParamVariable = ctMethod.getParameters().stream()
                .filter(x -> x.getReference().getSimpleName().equals(localVarName))
                //.peek(x -> System.out.println(x.getReferencedTypes()))
                .filter(x -> x.getReference().getReferencedTypes().stream().anyMatch(y -> y.getQualifiedName().equals(RESP_ENTITY_REF_TYPE)))
                .findFirst();
                //.forEach(x -> System.out.println(x.getReference().getSimpleName()));

        if (optParamVariable.isPresent()) {
            //LOGGER.info("{} foundBy methodParam@1", localVarName);
            return true;
        }

        // Find by localVariable tracking
        List<CtLocalVariable> methodLocalVariables = ctMethod.getBody().getElements(new TypeFilter(CtLocalVariable.class));

        CtLocalVariable ctLocalVariable = methodLocalVariables.stream()
                //.peek(x -> System.out.println(x.getReference()))
                .filter(x -> x.getSimpleName().equals(localVarName))
                //.peek(x -> System.out.println(x.getReferencedTypes()))
                .findFirst().orElse(null);

        if (ctLocalVariable == null) return false;

        var ctRefType = ctLocalVariable.getReferencedTypes()
                .stream()
                .filter(x -> x.getQualifiedName().equals(RESP_ENTITY_REF_TYPE))
                .findFirst();

        if (ctRefType.isPresent()) {
            //LOGGER.info("{} foundBy methodLocalVariable@1", localVarName);
            return true;
        }

        // Found by expression back tracking
        var ctTypeReference = ctLocalVariable.getDefaultExpression()
                .getReferencedTypes().stream().filter(y -> y.getQualifiedName().equals(RESP_ENTITY_REF_TYPE))
                .findFirst();

        if (ctTypeReference.isPresent()) {
            //LOGGER.info("{} foundBy methodLocalDefaultExp@1", localVarName);
            return true;
        } else {
            //System.out.println("recursion methodLocalDefaultExp@2 : " + localVarName);
            //if (ctLocalVariable.getAssignment().getDirectChildren().size() > 1) {
                var abc = ctLocalVariable.getAssignment().getDirectChildren().get(0);
                return isResponseEntity(ctMethod, abc.prettyprint());
            //} else return false;
        }
    }

    private LoggerStatement getCTStatementsList(final CtStatement ctStatement, final StringBuilder sb,
                                                      final List<CtParameter<?>> ctMethodParams) {

        final List<LoggerParam> paramList = new ArrayList<>();
        boolean isStmtModified = false;

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
                if(LoggerParam.ctLiteralPredicate.test(ctElement)) {
                    paramList.add(new LoggerParam(ctElement, false));
                }
                if (LoggerParam.ctVarPredicate.test(ctElement)) {
                    isStmtModified = doesAnnotationParameterSanitize.test(ctMethodParams, ctElement);
                    //LOGGER.info("{} annotation : {}", ctElement, ctElement.hasAnnotation(PathVariable.class));
                    if (!isStmtModified) isStmtModified = isResponseEntity (ctStatement.getParent(CtMethod.class), ctElement.prettyprint());
                    paramList.add(new LoggerParam(ctElement, isStmtModified));
                }
                if (LoggerParam.ctInvocationPredicate.test(ctElement)) {
                    //LOGGER.info("ctElement : {} ", ctElement);
                    //LOGGER.info("ctInvocationPredicate : {}", ctElement.getElements(new TypeFilter(CtVariableRead.class)));
                    //LOGGER.info(" {} ", ctStatement.getParent(CtMethod.class));
                    String variableName = ctElement.getElements(new TypeFilter(CtVariableRead.class)).get(0).toString();
                    isStmtModified = isResponseEntity (ctStatement.getParent(CtMethod.class), variableName);
                    paramList.add(new LoggerParam(ctElement, isStmtModified));
                }
            }
        }

        return new LoggerStatement(sb, paramList, isStmtModified);
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
                //System.out.println("paramName sanitize : " + paramName);
                if (logStmt.isSanitizationNeeded() ) { // && logStmt.isVariable()
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

}