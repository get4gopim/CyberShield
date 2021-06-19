package com.example.test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

/**
 * Adds a not-null check for all method parameters which are objects
 *
 * void m(Object o} { ...(method body) .... }
 *
 * is transformed into
 *
 *  void m(Object o} {
 *      if (o == null) { throw new IllegalArgumentException("o is null"); }
 *      // rest of the method body
 *  }
 *
 * @author Martin Monperrus
 */
public class NotNullCheckAdderProcessor extends AbstractProcessor<CtParameter<?>> {

	@Override
	public boolean isToBeProcessed(CtParameter<?> element) {
		System.out.println(element.getSimpleName());
		return !element.getType().isPrimitive();// only public and for objects
		//return element.getType().getSimpleName().equals("a2");
	}

	@Override
	public void process(CtParameter<?> element) {
		CtCodeSnippetStatement snippet = getSnippet(element);

		/*List<CtComment> commentList = new ArrayList<>();
		CtComment comment = new CtCommentImpl();
		comment.setContent("Spoon inserted check");
		commentList.add(comment);
		snippet.setComments(commentList);*/

		// we insert the snippet at the beginning of the method body.
		if (element.getParent(CtExecutable.class).getBody() != null) {
			element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
		}
	}

	private CtCodeSnippetStatement getSnippet(CtParameter<?> element) {
		// we declare a new snippet of code to be inserted.
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

		snippet.setDocComment("Spoon auto inserted code");

		// this snippet contains an if check.
		final String value = String.format("if (%s == null) "
				+ "throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\");",
				element.getSimpleName());
		snippet.setValue(value);

		return snippet;
	}
}