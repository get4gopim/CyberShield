package com.example.test;

import spoon.Launcher;

public class NotNullCheckAdderProcessorTest {

	public static void main(String[] arg) throws Exception {
		final String[] args = {
				"-i", "src/main/java/com/example/test/A.java",
				"-o", "target/spooned/",
				"-p", "com.example.test.NotNullCheckAdderProcessor",
				"--compile"
		};

		final Launcher launcher = new Launcher();
		//launcher.getEnvironment().setPrettyPrintingMode(Environment.PRETTY_PRINTING_MODE.FULLYQUALIFIED); //.setPreserveLineNumbers(true); //.setAutoImports(true);
		launcher.setArgs(args);
		launcher.run();
	}

}
