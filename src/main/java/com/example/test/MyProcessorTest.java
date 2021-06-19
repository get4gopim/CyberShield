package com.example.test;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.support.sniper.SniperJavaPrettyPrinter;

public class MyProcessorTest {

    public static void main(String[] arg) {
        final String[] args = {
                "-i", "src/main/java/com/example/test/A.java",
                "-o", "target/spooned/",
                "-p", "com.example.test.MyProcessor",
                "--compile"
        };

        final Launcher launcher = new Launcher();
        Environment spoonEnv = launcher.getEnvironment();
        spoonEnv.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(spoonEnv));
        spoonEnv.setAutoImports(false);

        launcher.setArgs(args);
        launcher.run();
    }
}
