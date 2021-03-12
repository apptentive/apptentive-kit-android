/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package apptentive.com.android.feedback.utils;

public abstract class IndentPrinter {
	private static final String INDENT = "  ";
	private final StringBuilder indentBuffer;

	public IndentPrinter() {
		indentBuffer = new StringBuilder();
	}

	protected abstract void printInternal(String message);

	public IndentPrinter print(String value) {
		String message = indentBuffer + value;
		printInternal(message);
		return this;
	}

	public IndentPrinter startBlock() {
		indentBuffer.append(INDENT);
		return this;
	}

	public IndentPrinter endBlock() {
		if (indentBuffer.length() >= INDENT.length()) {
			indentBuffer.setLength(indentBuffer.length() - INDENT.length());
		}
		return this;
	}
}
