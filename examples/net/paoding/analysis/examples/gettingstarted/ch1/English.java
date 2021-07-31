package net.paoding.analysis.examples.gettingstarted.ch1;

import net.paoding.analysis.examples.gettingstarted.ExampleBase;

import java.io.IOException;

import net.paoding.analysis.examples.gettingstarted.ContentReader;

public class English extends ExampleBase{
	private final static String QUERY_DEFAULT = "Tomcat";
	
	private String query;
	
	public English(String query) {
		this.query = query;
	}

	public static void main(String[] args) throws Exception {
		English test = new English(args.length == 0 ? QUERY_DEFAULT: args[0]);
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
