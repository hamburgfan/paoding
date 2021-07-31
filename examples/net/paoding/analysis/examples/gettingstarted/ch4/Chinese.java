package net.paoding.analysis.examples.gettingstarted.ch4;

import net.paoding.analysis.examples.gettingstarted.ExampleBase;

import java.io.IOException;

import net.paoding.analysis.examples.gettingstarted.ContentReader;

public class Chinese extends ExampleBase{
	
	private final static String QUERY_DEFAULT = "中华";
	
	private String query;
	
	public Chinese(String query) {
		this.query = query;
	}

	public static void main(String[] args) throws Exception {
		Chinese test = new Chinese(args.length == 0 ? QUERY_DEFAULT: args[0]);
		test.execute();
	}

	@Override
	public String getContent() throws IOException {
		return ContentReader.readText(this.getClass());
	}

	@Override
	public String getQuery() {
		return this.query;
	}
}