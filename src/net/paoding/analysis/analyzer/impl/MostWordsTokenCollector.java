/**
 * Copyright 2007 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.analysis.analyzer.impl;

import java.util.Iterator;

import net.paoding.analysis.analyzer.TokenCollector;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 1.1
 */
public class MostWordsTokenCollector implements TokenCollector, Iterator {

	private LinkedToken firstToken;
	private LinkedToken lastToken;
	private LinkedToken nextLinkedToken;

	/**
	 * Collector接口实现。<br>
	 * 构造词语Token对象，并放置在tokens中
	 * 
	 */
	public void collect(String word, int begin, int end) {
		// System.out.println("[INFO1] begin: " + begin + ", end: " + end + ", " +
		// word);
		LinkedToken tokenToAdd = new LinkedToken(word, begin, end);
		if (firstToken == null) {
			firstToken = tokenToAdd;
			lastToken = tokenToAdd;
			return;
		}
		if (tokenToAdd.compareTo(lastToken) > 0) {
			tokenToAdd.pre = lastToken;
			lastToken.next = tokenToAdd;
			lastToken = tokenToAdd;
			//
		} else {
			LinkedToken curTokenToTry = lastToken.pre;
			while (curTokenToTry != null && tokenToAdd.compareTo(curTokenToTry) < 0) {
				curTokenToTry = curTokenToTry.pre;
			}
			if (curTokenToTry == null) {
				firstToken.pre = tokenToAdd;
				tokenToAdd.next = firstToken;
				firstToken = tokenToAdd;
			} else {
				tokenToAdd.next = curTokenToTry.next;
				curTokenToTry.next.pre = tokenToAdd;
				tokenToAdd.pre = curTokenToTry;
				curTokenToTry.next = tokenToAdd;

			}
		}
	}

	public Iterator/* <Token> */ iterator() {
		nextLinkedToken = firstToken;
		firstToken = null;
		return this;
	}

	public boolean hasNext() {
		return nextLinkedToken != null;
	}

	public Object next() {
		LinkedToken ret = nextLinkedToken;
		nextLinkedToken = nextLinkedToken.next;
		return ret;
	}

	public void remove() {
	}

	private static class LinkedToken extends Token implements Comparable {
		public LinkedToken pre;
		public LinkedToken next;

		public LinkedToken(String word, int begin, int end) {
			super(word, begin, end);
		}

		public int compareTo(Object obj) {
			LinkedToken that = (LinkedToken) obj;
			// System.out.println(" this: " + this);
			// System.out.println(" that: " + that);
			// 简单/单单/简简单单/
			if (this.startOffset() > that.startOffset())
				return 1;
			if (this.startOffset() == that.startOffset()) {
				return this.endOffset() - that.endOffset();
			}
			return -1;
		}

		public int compareTo_origin(Object obj) {
			LinkedToken that = (LinkedToken) obj;
			// System.out.println(" this: " + this);
			// System.out.println(" that: " + that);
			// 简单/单单/简简单单/
			if (this.endOffset() > that.endOffset())
				return 1;
			if (this.endOffset() == that.endOffset()) {
				return that.startOffset() - this.startOffset();
			}
			return -1;
		}

		@Override
		public String toString() {
			return "begin: " + super.startOffset() + ", end: " + super.endOffset() + ", " + super.termText();
		}
	}
}
