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
package net.paoding.analysis.dictionary.support.filewords;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.paoding.analysis.knife.CharSet;

/**
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @since 1.0
 * 
 */
public class FileWordsReader {

	public static Map/* <String, Set<Word>> */ readWords(String fileOrDirectory, String charsetName)
			throws IOException {
		SimpleReadListener l = new SimpleReadListener();
		readWords(fileOrDirectory, l, charsetName);
		return l.getResult();
	}

	public static Map/* <String, Collection<Word>> */ readWords(String fileOrDirectory, String charsetName,
			Class collectionClass, String ext) throws IOException {
		SimpleReadListener2 l = new SimpleReadListener2(collectionClass, ext);
		readWords(fileOrDirectory, l, charsetName);
		return l.getResult();
	}

	public static void readWords(String fileOrDirectory, ReadListener l, String charsetName) throws IOException {
		File file;
		if (fileOrDirectory.startsWith("classpath:")) {
			String name = fileOrDirectory.substring("classpath:".length());
			URL url = FileWordsReader.class.getClassLoader().getResource(name);
			if (url == null) {
				throw new FileNotFoundException("file \"" + name + "\" not found in classpath!");
			}
			file = new File(url.getFile());
		} else {
			file = new File(fileOrDirectory);
			if (!file.exists()) {
				throw new FileNotFoundException("file \"" + fileOrDirectory + "\" not found!");
			}
		}
		ArrayList/* <File> */ dirs = new ArrayList/* <File> */();
		LinkedList/* <File> */ dics = new LinkedList/* <File> */();
		String dir;
		if (file.isDirectory()) {
			dirs.add(file);
			dir = file.getAbsolutePath();
		} else {
			dics.add(file);
			dir = file.getParentFile().getAbsolutePath();
		}
		int index = 0;
		while (index < dirs.size()) {
			File cur = (File) dirs.get(index++);
			File[] files = cur.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.isDirectory()) {
					dirs.add(f);
				} else {
					dics.add(f);
				}
			}
		}
		for (Iterator iter = dics.iterator(); iter.hasNext();) {
			File f = (File) iter.next();
			String name = f.getAbsolutePath().substring(dir.length() + 1);
			name = name.replace('\\', '/');
			if (!l.onFileBegin(name)) {
				continue;
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), charsetName));
			String word;
			boolean firstInDic = true;
			while ((word = in.readLine()) != null) {
				if (firstInDic) {
					firstInDic = false;
					// ref:http://www.w3.org/International/questions/qa-utf8-bom
					// ZERO WIDTH NO-BREAK SPACE
					// notepad??????????????????unitcode???utf-8???????????????????????????bom?????????
					// notepad???????????????bom???????????????????????????utf-8??????????????????
					// ???????????????????????????????????????????????????
					if (word.length() > 0 && CharSet.isBom(word.charAt(0))) {
						word = word.substring(1);
					}
				}
				l.onWord(word);
			}
			l.onFileEnd(name);
			in.close();
		}
	}

}
