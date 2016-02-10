package edu.ucla.cs.cs144;

import java.io.IOException;
import java.io.StringReader;
import java.io.File;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

    class Item {
        private String itemId;
        private String itemName;
        private String category;
        private String description;
        public String getId() {
            return this.itemId;
        }
        public Item setId(String id) {
            this.itemId = id;
            return this;
        }
        public String getName() {
            return this.itemName;
        }
        public Item setName(String name) {
            this.itemName = name;
            return this;
        }
        public String getCat() {
            return this.category;
        }
        public Item setCat(String cat) {
            this.category = cat;
            return this;
        }
        public String getDesc() {
            return this.description;
        }
        public Item setDesc(String desc) {
            this.description = desc;
            return this;
        }

        public String toString() {
        return "Item "
               + getId()
               +": "
               + getName()
               +"; "
               + getCat()
               +"; ";
        }

        Item() {
        }
    }
    
    /** Creates a new instance of Indexer */
    public Indexer() {
    }

    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean create) throws IOException {
        if (indexWriter == null) {
            Directory indexDir = FSDirectory.open(new File("/var/lib/lucene/index1"));
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2, new StandardAnalyzer());
            indexWriter = new IndexWriter(indexDir, config);
        }
        return indexWriter;
    }    
   
    public void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }

    public void indexItem(Item item) throws IOException {
        System.out.println("Indexing item: " + item);
        IndexWriter writer = getIndexWriter(false);
        Document doc = new Document();
        doc.add(new StringField("itemid", item.getId(), Field.Store.YES));
        doc.add(new StringField("itemname", item.getName(), Field.Store.YES));
        String fullSearchableText = item.getName() + " " + item.getCat() + " " + item.getDesc();
        doc.add(new TextField("content", fullSearchableText, Field.Store.NO));
        writer.addDocument(doc);
    }
 
    public void rebuildIndexes() throws IOException {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
	try {
	    conn = DbManager.getConnection(true);
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    try {
        Statement stmt = conn.createStatement();
        PreparedStatement prepareUpdatePrice = conn.prepareStatement
        (
            "SELECT category FROM Category WHERE itemid = ?"
        );

        ResultSet rs = stmt.executeQuery("SELECT itemid, name, description FROM Item");
        ResultSet catrs;
        while( rs.next() ){
            String itemid = rs.getString("itemid");
            String name = rs.getString("name");
            String description = rs.getString("description");
            Item newItem = new Item().setId(itemid)
                            .setName(name).setDesc(description);

            prepareUpdatePrice.setString(1, itemid);
            catrs = prepareUpdatePrice.executeQuery();
            String catinfo = "";
            while( catrs.next() ){
                String category = catrs.getString("category");
                catinfo = catinfo + " " + category;
            }
            catrs.close();
            newItem.setCat(catinfo);
            indexItem(newItem);
        }

        closeIndexWriter();
	/*
	 * Add your code here to retrieve Items using the connection
	 * and add corresponding entries to your Lucene inverted indexes.
         *
         * You will have to use JDBC API to retrieve MySQL data from Java.
         * Read our tutorial on JDBC if you do not know how to use JDBC.
         *
         * You will also have to use Lucene IndexWriter and Document
         * classes to create an index and populate it with Items data.
         * Read our tutorial on Lucene as well if you don't know how.
         *
         * As part of this development, you may want to add 
         * new methods and create additional Java classes. 
         * If you create new classes, make sure that
         * the classes become part of "edu.ucla.cs.cs144" package
         * and place your class source files at src/edu/ucla/cs/cs144/.
	 * 
	 */


        // close the database connection
    //close the statement, resultset, connection
	    conn.close();
        rs.close();
        prepareUpdatePrice.close();
        stmt.close();
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    }    

    public static void main(String args[]) throws IOException {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }   
}
