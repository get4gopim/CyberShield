package com.example.test;

import spoon.Launcher;

public class EmptyMethodBodyProcessorTest {

	public static void main(String[] arg) {
		final String[] args = {
				"-i", "src/main/java/com/example/test/A.java",
				"-o", "target/spooned/"
		};

		final Launcher launcher = new Launcher();
		launcher.setArgs(args);
		final EmptyMethodBodyProcessor processor = new EmptyMethodBodyProcessor();
		launcher.addProcessor(processor);
		launcher.run();

		/*final Factory factory = launcher.getFactory();
		final ProcessingManager processingManager = new QueueProcessingManager(factory);
		final EmptyMethodBodyProcessor processor = new EmptyMethodBodyProcessor();
		processingManager.addProcessor(processor);
		processingManager.process(factory.Class().getAll());*/

	}
}
