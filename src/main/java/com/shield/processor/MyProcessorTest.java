package com.shield.processor;

import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.support.sniper.SniperJavaPrettyPrinter;

public class MyProcessorTest {

    public static void main(String[] arg) {
        final String[] args = {
                "-i", "/Users/f3ol562/IdeaProjects/ucom_customer_services/uComCustomerSvcsParent/uComCustomerSvcs/src/main/java/com/fdc/ucom/customer/endpoint/CustomController.java",
                "-o", "/Users/f3ol562/IdeaProjects/ucom_customer_services/uComCustomerSvcsParent/uComCustomerSvcs/src/main/java/",
                "-p", "com.shield.processor.MyProcessor"//,
                //"--compile"
        };

        final Launcher launcher = new Launcher();
        Environment spoonEnv = launcher.getEnvironment();
        spoonEnv.setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(spoonEnv));
        spoonEnv.setAutoImports(false);

        launcher.setArgs(args);
        launcher.run();
    }
}
