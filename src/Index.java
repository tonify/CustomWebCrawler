/**
 * @author Diego Perez Botero
 * 
 * Java class that creates a Lucene index with web pages (including tone metadata)
 * by initiating a custom web crawling procedure from a given set of origin nodes.
 * 
 * Needs to be run with huge amounts of heap space (e.g. "-Xmx4096m")
 * 
 * Very closed based on this tutorial:
 * http://semanticwebhow-tos.blogspot.com/2011/10/websphinx-and-lucene-example.html
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import websphinx.Link;


public class Index {
    private static StandardAnalyzer analyzer;
    private static Directory index;

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            
            // Setup an Analyzer and prepare a new IndexWriter
            analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
            index = new SimpleFSDirectory(new File("C:\\Users\\Diego\\Desktop\\index2"));
            IndexWriter writer = new IndexWriter(index, analyzer, true,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            // The mergeFactor value tells Lucene how many segments of equal size to build
            // before merging them into a single segment
            writer.setMergeFactor(20);
            // Setup a new IndexCrawler instance
            // Root nodes
            Link[] roots = new Link[4];
            roots[0] = new Link("http://www.princeton.edu/");
            roots[1] = new Link("http://www.britannica.com/");
            roots[2] = new Link("http://www.cnn.com/");
            roots[3] = new Link("http://www.cheezburger.com/");
            
            /* This commented code was used to fetch Google results
             * for the queries mentioned in the project report.
             * The scraped URLs are in the "datasets" folder.
            File[] files = new File[3];
            files[0] = new File("./datasets/french_election.txt");
            files[1] = new File("./datasets/ipad_review.txt");
            files[2] = new File("./datasets/us_economy.txt");
            ArrayList<Link> datasets = new ArrayList<Link>();            
            
            for (int i = 0; i < files.length; i++)
            {
                BufferedReader br = new BufferedReader(new FileReader(files[i]));
                String url = br.readLine();
                
                while (url != null)
                {
                    datasets.add(new Link(url));                    
                    url = br.readLine();
                }
                
                br.close();
            }
            roots = new Link[datasets.size()];
            roots = datasets.toArray(roots);*/
            
            // Initialize the custom crawler and start crawling
            IndexingCrawler c = new IndexingCrawler(writer, roots);
            c.run();
            //writer.optimize(); (unnecessary in new versions of Lucene)
            // Close the writer when done
            writer.close();
        } catch (MalformedURLException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }        
    }
}