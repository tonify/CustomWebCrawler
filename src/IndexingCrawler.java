/**
 * @author Diego Perez Botero
 * 
 * This is a custom SPHINX-based crawler class. It parses page contents with the
 * help of jsoup and sends them to various uClassify classifiers. The uClassify
 * results are then stored alongside each page's human-readable content in a
 * Lucene index.
 * 
 * Very closed based on this tutorial:
 * http://semanticwebhow-tos.blogspot.com/2011/10/websphinx-and-lucene-example.html
 */
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.onesun.textmining.uclassify.DefaultResult;
import org.onesun.textmining.uclassify.ResultHandler;
import org.onesun.textmining.uclassify.ServiceType;
import org.onesun.textmining.uclassify.UClassifyService;

import websphinx.Crawler;
import websphinx.Element;
import websphinx.Link;
import websphinx.Page;

public class IndexingCrawler extends Crawler {

    private static final long serialVersionUID = 1L;
    
    // Lucene index writer
    private IndexWriter writer;
    public IndexingCrawler(IndexWriter writer, Link[] roots) {
        super();
        this.setRoots(roots);
        this.writer = writer;
        // One page at a time
        this.setSynchronous(true);
        this.setDomain(Crawler.WEB);
        // Maximum depth of the crawl
        this.setMaxDepth(2);
        // What sort of links do we use to crawl deeper in the web?
        this.setLinkType(Crawler.HYPERLINKS);
        // DFS or BFS algorithm
        this.setDepthFirst(true);
        // Naughty: ignore robot exlusion files
        this.getDownloadParameters().changeObeyRobotExclusion(false);
        // Change user agent to hide the web crawler's identity
        this.getDownloadParameters().changeUserAgent("Mozilla/5.0 (Windows NT 5.1; rv:14.0) Gecko/20120405 Firefox/14.0a1");
    }

    // Method that is visited every time a web site is fetched
    public void visit(Page p) {

        System.out.println("Visiting [" + p.getURL() + "]");

        if(p.getTitle() == null){
            noindex(p);// skip pdf files
        }else{
            index(p);// process text
        }

        System.out.println("    Done.");
    }

    // Called when a web site's text is to be processed
    public void index(Page p) {
        //StringBuffer contents = new StringBuffer();
        // Lucene Document entity to be stored
        final Document doc = new Document();
        doc.add(new Field("path", p.getURL().toString(), Field.Store.YES, Field.Index.ANALYZED));
        //doc.add(new Field("title", value, Field.Store.YES, Field.Index.ANALYZED));
        //doc.add(Field.Keyword("modified",DateField.timeToString(p.getLastModified())));

        if (p.getTitle() != null) {
            doc.add(new Field("title", p.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
        }

        /*System.out.println("    Indexing...");
        System.out.println("        depth [" + p.getDepth() + "]");
        System.out.println("        title [" + p.getTitle() + "]");
        System.out.println("        modified [" + p.getLastModified() + "]");*/
        // Read and store page meta-tags
        Element[] elements = p.getElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getTagName().equalsIgnoreCase("meta")) {
                String name = elements[i].getHTMLAttribute("name", "");
                String content = elements[i].getHTMLAttribute("content", "");
                if (!name.equals("")) {
                    doc.add(new Field(name, content, Field.Store.YES, Field.Index.ANALYZED));
                    //System.out.println("        meta [" + name + ":" + content + "]");
                }
            }
        }
        /*Text[] texts = p.getWords();
        for (int i = 0; i < texts.length; i++) {
            contents.append(texts[i].toText());
            contents.append(" ");
        }*/
        
        // Parse the page's content, removing any scripting code and HTML tags
        String contents = "";        
        try
        {
            contents = Jsoup.parse(p.getContent()).body().text();
        }
        catch (Exception e)
        {
            return;
        }
        // Store the page's huma-readable content
        doc.add(new Field("contents", contents, Field.Store.YES, Field.Index.ANALYZED));
        
        // *******************************************************************
        // DO NOT FORGET TO SET YOUR OWN KEY HERE BEFORE RUNNING APP
        // You can get a key from: http://www.uclassify.com/Register.aspx
        // *******************************************************************
        UClassifyService.setUClassifyReadAccessKey("SYIvgYW9cV38qOQytKJlL6oYdbI");
        // *******************************************************************

        // Initialize the uClassify classifier services to be used
        UClassifyService uClassifyService1 = createService(doc, ServiceType.SENTIMENT);
        UClassifyService uClassifyService2 = createService(doc, ServiceType.TONALITY);
        UClassifyService uClassifyService3 = createService(doc, ServiceType.MOOD);

        // Process the tone scores and then store the document in the Lucene index
        try{
            uClassifyService1.process();
            uClassifyService2.process();
            uClassifyService3.process();
            writer.addDocument(doc);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        // Cleanup so that crawler doesn't take up too much RAM
        p.discardContent();
        p.getOrigin().setPage(null);
    }

    // Page is useless
    public void noindex(Page p) {
        System.out.println("    Skipping...");
    }
    
    // Initializes a uClassify classifier service abstraction
    private UClassifyService createService (final Document doc, ServiceType serviceType)
    {
        return new UClassifyService(doc.get("contents"), serviceType, new ResultHandler() {


            @Override
            public void process(ServiceType service, Map<String, DefaultResult> results) {
                
                // Grab each tone's score and store it under the name of the tone
                for(String key : results.keySet()){
                    DefaultResult result = results.get(key);
                    
                    doc.add(new Field(key, ""+result, Field.Store.YES, Field.Index.NO));
                    
                    // System.out.println(key+": "+result);

                    // interested in match >= 25%
                    // if(result >= 25) System.out.format("%1$-50s %2$10.2f\n", key, result);
                }
            }
        });
    }

}