package com.example.test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Reports warnings when empty methods are found.
 */
public class EmptyMethodBodyProcessor extends AbstractProcessor<CtMethod<?>> {

	public final List<CtMethod> emptyMethods = new ArrayList<>();

	public void process(CtMethod<?> element) {
		System.out.println(element.getSimpleName());

		// we declare a new snippet of code to be inserted.
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

		// this snippet contains an if check.
		final String value = String.format("if (%s == null) "
						+ "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
				element.getSimpleName());
		snippet.setValue(value);

		// we insert the snippet at the beginning of the method body.
		if (element.getParent(CtExecutable.class).getBody() != null) {
			element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
		}
	}

}
