package com.shield.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MyProcessor2 extends AbstractProcessor<CtMethod<?>> {

    private static final String SANITIZE_FN_NAME = "sanitize";

    private static final Logger LOGGER = LoggerFactory.getLogger(MyProcessor2.class);

    @Override
    public boolean isToBeProcessed(CtMethod<?> element) {
        System.out.println(element.getType());
        return element.isPublic();// only public method
    }

    @Override
    public void process(CtMethod<?> element) {
        ModifierKind modifierKind = element.getModifiers().stream().findFirst().get();
        System.out.println(modifierKind.toString() + " " + element.getSignature() + " " + element.getBody());

        System.out.println("***********");

        List<CtStatement> statements = element.getBody().getStatements();

        Set<CtStatement> loggerStmts = statements.stream()
                .filter(x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtInvocationImpl"))
                .filter(x -> x.getReferencedTypes().stream().filter(y -> y.getQualifiedName().equals("org.slf4j.Logger")).count() > 0)
                .collect(Collectors.toSet());

        //loggerStmts.stream().forEach(System.out::println);

        System.out.println("***********");

        loggerStmts.stream().forEach(x -> {

            //System.out.println(x.getDirectChildren() + " --> " + x.getShortRepresentation() + " : " + x.getReferencedTypes());

            /*for (CtElement directChild : x.getDirectChildren()) {
                CtElement ctElement = directChild.clone();
                System.out.println(ctElement.prettyprint());
            }*/

            StringBuilder sb = new StringBuilder();

            //boolean isStrLtrFound = false;
            List<CtElement> paramList = new ArrayList<>();

            for (int i =0; i<x.getDirectChildren().size(); i++) {
                CtElement directChild = x.getDirectChildren().get(i);
                CtElement ctElement = directChild.clone();

                if (i == 0) sb.append(ctElement.prettyprint()).append(".");

                if (i == 1) {
                    String traceLine = ctElement.prettyprint();
                    String trace = traceLine.substring(0, traceLine.indexOf("(")+1);
                    sb.append(trace);
                }

                /*if (i == 2) {
                    paramList.add(ctElement);
                    if (ctElement.toString().indexOf("\"") == -1) {
                        paramList.add(ctElement);
                    } else {
                        isStrLtrFound = true;
                        sb.append(ctElement);
                    }
                }*/
                if (i > 1) {
                    paramList.add(ctElement);
                }
            }

            //System.out.println(sb.toString());

            //System.out.println("***********");

            //x.getDirectChildren().stream().forEach(y -> System.out.println(y.getElements(z -> true)));

            x.replace(getSnippet1(sb, paramList));
        });

        /*statements.forEach(ctStatement -> {

            Set<CtTypeReference> typeReferenceSet = ctStatement.getReferencedTypes().stream()
                    .filter(x -> x.getQualifiedName().equals("org.slf4j.Logger")).collect(Collectors.toSet());

           // System.out.println(typeReferenceSet);

           *//* System.out.println(ctStatement.getElements(element1 -> {
                Optional<CtTypeReference<?>> optRef = element1.getReferencedTypes().stream().findFirst();
                //optRef.ifPresent(x -> System.out.println(x));
                //String privateApiPackage = "ow2con.privateapi";
                //boolean isTypeFromPrivateApi = returnType.getQualifiedName().contains(privateApiPackage);
                return optRef.isPresent() && optRef.get().equals("org.slf4j.Logger");
            }));*//*

            //System.out.println(ctStatement + " --> " + ctStatement.getShortRepresentation() + " : " + ctStatement.getReferencedTypes());

            *//*Optional<CtTypeReference<?>> optStmtTypeRef = ctStatement.getReferencedTypes().stream().findFirst();
            System.out.println(ctStatement + " --> " + ctStatement.getShortRepresentation() + " : " + optStmtTypeRef.get().getQualifiedName());*//*

            *//*if (ctStatement.getShortRepresentation().contains("spoon.support.reflect.code.CtInvocationImpl")) {

                System.out.println(ctStatement + " --> " +
                        ctStatement.getReferencedTypes().stream()
                                .filter(x -> x.getQualifiedName().equals("org.slf4j.Logger")).findFirst());

                List<CtElement> elements = ctStatement.getDirectChildren();

                for (CtElement ctElement : elements) {
                    //System.out.println(ctElement.toString());
                    Optional<CtTypeReference<?>> optRef = ctElement.getReferencedTypes().stream().findFirst();
                    //System.out.println(ctElement + " --> " + ctElement.getShortRepresentation() + " : " + optRef.get().getQualifiedName());
                    if (optRef.isPresent() && optRef.get().getQualifiedName().equals("org.slf4j.Logger")) {
                        System.out.println("***********");

                        System.out.println(ctElement + " --> " + ctElement.getShortRepresentation() + " : " + ctElement.getDirectChildren());

                        System.out.println("***********");

                        //ctElement.asIterable().forEach(x -> System.out.println(x.toString()));
                    }
                }
            }*//*

        });*/

        //statements.stream().forEach(x -> LOGGER.info("{}", x.getDirectChildren()));

        CtClass ctClass = element.getParent(CtClass.class);
        System.out.println(ctClass);


        final CtCodeSnippetStatement statementInConstructor = getFactory().Code().createCodeSnippetStatement("return inputParam");
        final CtBlock<?> ctBlockOfConstructor = getFactory().Code().createCtBlock(statementInConstructor);
        final CtParameter parameter = getFactory().Core().createParameter();
        final CtTypeReference<Object> objRef = getFactory().Code().createCtTypeReference(Object.class);
        //final CtTypeReference<Void> voidRef = getFactory().Code().createCtTypeReference(Void.class);
        parameter.setType(objRef);
        parameter.setSimpleName("inputParam");
        final CtMethod ctMethod = getFactory().Core().createMethod();
        ctMethod.setBody(ctBlockOfConstructor);
        ctMethod.setParameters(Collections.<CtParameter<?>>singletonList(parameter));
        ctMethod.addModifier(ModifierKind.PRIVATE);
        ctMethod.setSimpleName(SANITIZE_FN_NAME);
        ctMethod.setType(objRef);

        ctClass.addMethod(ctMethod);
        //element.getBody().insertBegin(getSnippet1(element));
    }

    private CtCodeSnippetStatement getSnippet1(StringBuilder sb, List<CtElement> paramList) {
        // we declare a new snippet of code to be inserted.
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

        snippet.setDocComment("Spoon auto inserted code started");

        //Predicate<CtElement> ctLiteralPredicate = x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtLiteralImpl");
        Predicate<CtElement> ctVarPredicate = x -> x.getShortRepresentation().contains("spoon.support.reflect.code.CtVariableReadImpl");

        //boolean isStrLiteral = paramList.stream().anyMatch(ctLiteralPredicate);
        //boolean isVariable = paramList.stream().anyMatch(ctVarPredicate);
        //boolean isLitralFound = false;
        //if (isStrLiteral && isVariable) isLitralFound = true;

        //paramList.stream().forEach(x -> System.out.println(x + " :: " + x.getShortRepresentation() + " "));

        if (paramList.size() > 0) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            //if (isLitralFound) stringJoiner = new StringJoiner(", ", ", ", "");
            for (CtElement ctParam : paramList) {
                String paramName = ctParam.prettyprint();
                if (ctVarPredicate.test(ctParam)) paramName = SANITIZE_FN_NAME + "(" + ctParam.prettyprint() + ")";
                stringJoiner.add(paramName);
            }
            sb.append(stringJoiner);
            /*String listParam[] = paramList.stream().map(x -> x.toString()).collect(Collectors.toList()).toArray(String[]::new);
            sb.append(String.join(", ", listParam));*/
        }
        sb.append(")");

        //LOGGER.info(sb.toString());

        snippet.setValue(sb.toString());

        //System.out.println("***********");

        return snippet;
    }


    private CtCodeSnippetStatement getSnippet(CtMethod<?> element) {
        CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

        // Snippet which contains the log.
        final String value = String.format("System.out.println(\" %s \");",
                element.getBody());
        snippet.setValue(value);

        return snippet;
    }
}
