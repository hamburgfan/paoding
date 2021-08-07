package net.paoding.analysis.examples.gettingstarted;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

public abstract class ExampleBase {
	
	private final static String FIELD_NAME = "content";
	
	public abstract String getContent() throws IOException;	
	public abstract String getQuery();	
	
	public void execute() throws Exception {
		String query = getQuery();		
		String content = getContent();

		Directory ramDir = FSDirectory.open(Paths.get("work"));

		Analyzer analyzer = new PaodingAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(ramDir, iwc);
		Document doc = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		type.setStored(true);
		type.setStoreTermVectors(true);
		type.setTokenized(true);
		type.setStoreTermVectorOffsets(true);

		Field field = new Field(FIELD_NAME, content, type);
		doc.add(field);
		writer.addDocument(doc);
		writer.commit();
		writer.close();

		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);

		String queryString = query;
		QueryParser parser = new QueryParser(FIELD_NAME, analyzer);
		Query q = parser.parse(queryString);

		System.out.println("Searching for: " + q.toString(FIELD_NAME));

		TopDocs hits = searcher.search(q, 10);
		System.out.println("hits.scoreDocs.length: " + hits.scoreDocs.length);
		QueryScorer scorer = new QueryScorer(q);
		BoldFormatter formatter = new BoldFormatter();
		Highlighter highlighter = new Highlighter(formatter, scorer);
		Fragmenter fragmenter = new SimpleFragmenter(50);
		highlighter.setTextFragmenter(fragmenter);

		for (int i = 0; i < hits.scoreDocs.length; i++) {
			int docId = hits.scoreDocs[i].doc;
			Document d = searcher.doc(docId);
			String text = d.get(FIELD_NAME);

			TokenStream tokenStream = TokenSources.getTokenStream(FIELD_NAME, null, text, analyzer, 10);

			String frag = highlighter.getBestFragment(analyzer, FIELD_NAME, text);
			System.out.println(frag);

		}
		reader.close();

	}

}
