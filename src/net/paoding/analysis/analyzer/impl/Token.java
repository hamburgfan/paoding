package net.paoding.analysis.analyzer.impl;

public class Token {

	private String termText;
	private int startOffset;
	private int endOffset;

	public Token(String termText, int startOffset, int endOffset) {
		this.termText = termText;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public int startOffset() {
		return this.startOffset;
	}

	public int endOffset() {
		return this.endOffset;
	}

	public String termText() {
		return this.termText;
	}

}
