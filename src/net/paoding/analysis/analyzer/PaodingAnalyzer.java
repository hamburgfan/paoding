package net.paoding.analysis.analyzer;

import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

import net.paoding.analysis.Constants;
import net.paoding.analysis.analyzer.estimate.TryPaodingAnalyzer;
import net.paoding.analysis.analyzer.impl.MaxWordLengthTokenCollector;
import net.paoding.analysis.analyzer.impl.MostWordsTokenCollector;
import net.paoding.analysis.knife.Knife;
import net.paoding.analysis.knife.Paoding;
import net.paoding.analysis.knife.PaodingMaker;

/**
 * PaodingAnalyzer是基于“庖丁解牛”框架的Lucene词语分析器，是“庖丁解牛”框架对Lucene的适配器。
 * <p>
 * 
 * PaodingAnalyzer是线程安全的：并发情况下使用同一个PaodingAnalyzer实例是可行的。<br>
 * PaodingAnalyzer是可复用的：推荐多次同一个PaodingAnalyzer实例。
 * <p>
 * 
 * PaodingAnalyzer自动读取类路径下的paoding-analysis.properties属性文件，装配PaodingAnalyzer
 * <p>
 * 
 * @author Zhiliang Wang [qieqie.wang@gmail.com]
 * 
 * @see PaodingAnalyzer
 * 
 * @since 1.0
 * 
 */
public class PaodingAnalyzer extends Analyzer {

	/**
	 * 最多切分
	 */
	public static final int MOST_WORDS_MODE = 1;

	/**
	 * 按最大切分
	 */
	public static final int MAX_WORD_LENGTH_MODE = 2;

	/**
	 * 用于向PaodingTokenizer提供，分解文本字符
	 * 
	 * @see PaodingTokenizer#next()
	 * 
	 */
	private Knife knife;

	/**
	 * @see #MOST_WORDS_MODE
	 * @see #MAX_WORD_LENGTH_MODE
	 */
	private int mode = MOST_WORDS_MODE;

	private Class modeClass;

	/**
	 * 根据类路径下的paoding-analysis.properties构建一个PaodingAnalyzer对象
	 * <p>
	 * 在一个JVM中，可多次创建，而并不会多次读取属性文件，不会重复读取字典。
	 */
	public PaodingAnalyzer() {
		this(PaodingMaker.DEFAULT_PROPERTIES_PATH);
	}

	/**
	 * @param propertiesPath null表示使用类路径下的paoding-analysis.properties
	 */
	public PaodingAnalyzer(String propertiesPath) {
		init(propertiesPath);
	}

	/**
	 * @see #setKnife(Knife)
	 * @param knife
	 */
	public PaodingAnalyzer(Knife knife) {
		this.knife = knife;
	}

	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzer(Knife knife, int mode) {
		this.knife = knife;
		this.mode = mode;
	}

	/**
	 * @see #setKnife(Knife)
	 * @see #setMode(int)
	 * @param knife
	 * @param mode
	 */
	public PaodingAnalyzer(Knife knife, String mode) {
		this.knife = knife;
		this.setMode(mode);
	}

	protected void init(String propertiesPath) {
		// 根据PaodingMaker说明，
		// 1、多次调用getProperties()，返回的都是同一个properties实例(只要属性文件没发生过修改)
		// 2、相同的properties实例，PaodingMaker也将返回同一个Paoding实例
		// 根据以上1、2点说明，在此能够保证多次创建PaodingAnalyzer并不会多次装载属性文件和词典
		if (propertiesPath == null) {
			propertiesPath = PaodingMaker.DEFAULT_PROPERTIES_PATH;
		}
		Properties properties = PaodingMaker.getProperties(propertiesPath);
		String mode = Constants.getProperty(properties, Constants.ANALYZER_MODE);
		Paoding paoding = PaodingMaker.make(properties);
		setKnife(paoding);
		setMode(mode);
	}

	public Knife getKnife() {
		return knife;
	}

	public void setKnife(Knife knife) {
		this.knife = knife;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * 设置分析器模式.
	 * <p>
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		if (mode != MOST_WORDS_MODE && mode != MAX_WORD_LENGTH_MODE) {
			throw new IllegalArgumentException("wrong mode:" + mode);
		}
		this.mode = mode;
		this.modeClass = null;
	}

	/**
	 * 设置分析器模式类。
	 * 
	 * @param modeClass TokenCollector的实现类。
	 */
	public void setModeClass(Class modeClass) {
		this.modeClass = modeClass;
	}

	public void setModeClass(String modeClass) {
		try {
			this.modeClass = Class.forName(modeClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("not found mode class:" + e.getMessage());
		}
	}

	public void setMode(String mode) {
		if (mode.startsWith("class:")) {
			setModeClass(mode.substring("class:".length()));
		} else {
			if ("most-words".equalsIgnoreCase(mode) || "default".equalsIgnoreCase(mode)
					|| ("" + MOST_WORDS_MODE).equals(mode)) {
				setMode(MOST_WORDS_MODE);
			} else if ("max-word-length".equalsIgnoreCase(mode) || ("" + MAX_WORD_LENGTH_MODE).equals(mode)) {
				setMode(MAX_WORD_LENGTH_MODE);
			} else {
				throw new IllegalArgumentException("不合法的分析器Mode参数设置:" + mode);
			}
		}
	}

//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		if (knife == null) {
//			throw new NullPointerException("knife should be set before token");
//		}
//		// PaodingTokenizer是TokenStream实现，使用knife解析reader流入的文本
//		return new PaodingTokenizer(reader, knife, createTokenCollector());
//	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		if (knife == null) {
			throw new NullPointerException("knife should be set before token");
		}
		// PaodingTokenizer是TokenStream实现，使用knife解析reader流入的文本
		Tokenizer source = new PaodingTokenizer(knife, createTokenCollector());
		return new TokenStreamComponents(source, source);
	}

	protected TokenCollector createTokenCollector() {
		if (modeClass != null) {
			try {
				return (TokenCollector) modeClass.newInstance();
			} catch (InstantiationException e) {
				throw new IllegalArgumentException("wrong mode class:" + e.getMessage());
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("wrong mode class:" + e.getMessage());
			}
		}
		switch (mode) {
		case MOST_WORDS_MODE:
			return new MostWordsTokenCollector();
		case MAX_WORD_LENGTH_MODE:
			return new MaxWordLengthTokenCollector();
		default:
			throw new Error("never happened");
		}
	}

	/**
	 * 本方法为PaodingAnalyzer附带的测试评估方法。 <br>
	 * 执行之可以查看分词效果。以下任选一种方式进行:
	 * <p>
	 * 
	 * java net...PaodingAnalyzer<br>
	 * java net...PaodingAnalyzer --help<br>
	 * java net...PaodingAnalyzer 中华人民共和国<br>
	 * java net...PaodingAnalyzer -m max 中华人民共和国<br>
	 * java net...PaodingAnalyzer -f c:/text.txt<br>
	 * java net...PaodingAnalyzer -f c:/text.txt -c utf-8<br>
	 * 
	 * @param args
	 */

	public static void main(String[] args) {
		if (System.getProperty("paoding.try.app") == null) {
			System.setProperty("paoding.try.app", "PaodingAnalyzer");
			System.setProperty("paoding.try.cmd", "java PaodingAnalyzer");
		}
		TryPaodingAnalyzer.main(args);
	}
}
