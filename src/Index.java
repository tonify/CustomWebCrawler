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
            index = new SimpleFSDirectory(new File("C:\\Users\\Diego\\Desktop\\index"));
            IndexWriter writer = new IndexWriter(index, analyzer, false,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            // The mergeFactor value tells Lucene how many segments of equal size to build
            // before merging them into a single segment
            writer.setMergeFactor(20);
            // Setup a new IndexCrawler instance
            Link[] roots = new Link[4];
            roots[0] = new Link("http://www.princeton.edu/");
            roots[1] = new Link("http://www.britannica.com/");
            roots[2] = new Link("http://www.cnn.com/");
            roots[3] = new Link("http://www.cheezburger.com/");
            
            /*File[] files = new File[3];
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
            
            IndexingCrawler c = new IndexingCrawler(writer, roots);
            c.run();
            //writer.optimize();
            // Close the writer when done
            writer.close();
        } catch (MalformedURLException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }        
    }
}