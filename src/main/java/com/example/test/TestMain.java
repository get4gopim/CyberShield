package com.example.test;

import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.factory.TypeFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class TestMain {

    public static void main(String[] args) throws Exception {
        Launcher launcher = new Launcher();
        launcher.addInputResource("./src/main/java/" + A.class.getName().replace('.', '/') + ".java");
        launcher.buildModel();
        CoreFactory core = launcher.getFactory().Core();
        CodeFactory code = launcher.getFactory().Code();
        TypeFactory type = launcher.getFactory().Type();

        final CtClass<?> clazzA = (CtClass<?>) launcher.getFactory().Type().get(A.class);


        SpoonModelBuilder modelBuilder = launcher.getModelBuilder();

        // first compilation
        System.out.println(clazzA);
        modelBuilder.compile();

        // get method a1
        CtMethod<?> a1 = clazzA.getMethodsByName("a1").get(1);
        CtMethod<?> a1Clone = core.clone(a1);

        // create field s1
        CtField f1 = (CtField) core.createField().<CtField>setSimpleName("s1").setType(type.createReference(String.class));
        clazzA.addField(f1);

        // create s1 assignment
        CtStatement test = (CtStatement) core.createAssignment().setAssigned(core.createFieldWrite().setVariable(f1.getReference())).setAssignment(code.createLiteral("test"));
        a1Clone.getBody().insertBegin(test);

        // change invocation parameter
        ((CtInvocation)a1Clone.getBody().getLastStatement()).setArguments(Arrays.asList(code.createLiteral(5)));

        // change a1
        clazzA.removeMethod(a1);
        clazzA.addMethod(a1Clone);

        // second compilation
        System.out.println(clazzA);
        clearCompilationCache(modelBuilder);
        modelBuilder.compile();

        // rollback
        clazzA.removeMethod(a1Clone);
        clazzA.addMethod(a1);
        clazzA.removeField(f1);

        // third compilation
        System.out.println(clazzA);
        clearCompilationCache(modelBuilder);
        modelBuilder.compile();
    }

    private static void clearCompilationCache(SpoonModelBuilder modelBuilder)
            throws NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Field loadedContent = modelBuilder.getClass().getDeclaredField("loadedContent");
        loadedContent.setAccessible(true);
        Object loadedContentObj = loadedContent.get(modelBuilder);
        Method clear = loadedContentObj.getClass().getDeclaredMethod("clear");
        clear.invoke(loadedContentObj);
    }

}
