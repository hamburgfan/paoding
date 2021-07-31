package net.paoding.analysis.examples.gettingstarted.ch2;

import net.paoding.analysis.examples.gettingstarted.ExampleBase;

import java.io.IOException;

import net.paoding.analysis.examples.gettingstarted.ContentReader;

public class Chinese extends ExampleBase{
	
	private String query;
	
	public Chinese(String query) {
		this.query = query;
	}

	public static void main(String[] args) throws Exception {
		Chinese test = new Chinese(args.length == 0 ? "共和国": args[0]);
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