package edu.ucla.cs.cs144;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Vector;
import java.util.Arrays; 

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */
    private IndexSearcher searcher = null;
    private QueryParser parser = null;

    //According to http://www.hdfgroup.org/HDF5/XML/xml_escape_chars.htm
    public String escape_parsed(String input) {
        return input.replaceAll("&quot","\\\\\"").replaceAll("&apos", "'").replaceAll("&amp","&")
                    .replaceAll("&lt","<").replaceAll("&gt",">");
    }

    public String escape_unparsed(String input) {
        return input.replaceAll("\\\\\\\"", "\\\\\"").replaceAll("&apos", "'").replaceAll("&amp", "&")
                    .replaceAll("&lt","<").replaceAll("&gt", ">").replaceAll("\\\\\\\\", "\\\\");
    }

    public static String toXMLTime(String TimeInfo) {
        try {
            SimpleDateFormat newform = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
            SimpleDateFormat oldform = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            TimeInfo = newform.format(oldform.parse(TimeInfo));
        
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return TimeInfo;
    }

    public AuctionSearch() throws IOException {
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File("/var/lib/lucene/index1/"))));
        parser = new QueryParser("content", new StandardAnalyzer());
    }

	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) throws IOException, ParseException {
		// TODO: Your code here!
        Query queryToken = parser.parse(query);
        TopDocs searchResult = searcher.search(queryToken, numResultsToSkip + numResultsToReturn);
        ScoreDoc[] hits = searchResult.scoreDocs;
        SearchResult[] rs;
        if(hits.length < numResultsToSkip + numResultsToReturn) {
        	rs = new SearchResult[hits.length - numResultsToSkip];
        } else {
        	rs = new SearchResult[numResultsToReturn];
        }
        for(int i=numResultsToSkip; i<numResultsToSkip+numResultsToReturn && i<numResultsToSkip+hits.length; i++) {
        	org.apache.lucene.document.Document doc = searcher.doc(hits[i].doc);
        	String itemid = doc.get("itemid");
        	String itemname = doc.get("itemname");
        	rs[i-numResultsToSkip] = new SearchResult(itemid, itemname);
        }
		return rs;
	}

	public SearchResult[] spatialSearch(String query, SearchRegion region,
			int numResultsToSkip, int numResultsToReturn) throws IOException, ParseException {
		// TODO: Your code here!
        Connection conn = null;

        int counter=0;
        SearchResult[] rs = new SearchResult[numResultsToReturn];
        try {
            conn = DbManager.getConnection(true);

            Statement stmt = conn.createStatement();
            // System.out.println("SELECT itemid FROM geom WHERE MBRWithin(position, GeomFromText('Polygon((" + region.getLx() + " " + region.getLy() + "," + region.getLx() + " " + region.getRy() + "," + region.getRx() + " " + region.getRy() + "," + region.getRx() + " " + region.getLy() + "))'))");

            ResultSet rs1 = stmt.executeQuery("SELECT itemid FROM geom WHERE MBRWithin(position, GeomFromText('Polygon((" + region.getLx() + " " + region.getLy() + "," + region.getLx() + " " + region.getRy() + "," + region.getRx() + " " + region.getRy() + "," + region.getRx() + " " + region.getLy() + "," + region.getLx() + " " + region.getLy() + "))'))");
            HashSet<String> helper = new HashSet<String>();
            while(rs1.next()) {
                String itemid = rs1.getString("itemid");
                helper.add(itemid);
            }
            Query queryToken = parser.parse(query);
            TopDocs searchResult = searcher.search(queryToken, Integer.MAX_VALUE);
            ScoreDoc[] hits = searchResult.scoreDocs;
            for(ScoreDoc hit:hits) {
                if(counter-numResultsToSkip >= numResultsToReturn) break;
                org.apache.lucene.document.Document doc = searcher.doc(hit.doc);
                String itemid = doc.get("itemid");
                if(helper.contains(itemid)) {
                    if(counter >= numResultsToSkip) {
                        String itemname = doc.get("itemname");
                        rs[counter-numResultsToSkip] = new SearchResult(itemid, itemname);
                    }
                    counter++;
                }   
            }
            conn.close();
            stmt.close();
            rs1.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
		return Arrays.copyOfRange(rs, 0, counter);
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
        Connection conn = null;
        String result = "";
        // create a connection to the database to retrieve Items from MySQL
        try {
            conn = DbManager.getConnection(true);

            ResultSet rs1 = conn.createStatement().executeQuery("SELECT * FROM Item WHERE itemid = " + "'" + itemId + "'");
            if(!rs1.next()) return "";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Item");
            doc.appendChild(rootElement);
            rootElement.setAttribute("ItemID", rs1.getString("itemid"));

            Element itemName = doc.createElement("Name");
            itemName.appendChild(doc.createTextNode(escape_unparsed(rs1.getString("name"))));
            rootElement.appendChild(itemName);

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT category FROM Category WHERE itemid = " + "'"+itemId+"'");
            while(rs2.next()) {
                Element category = doc.createElement("Category");
                category.appendChild(doc.createTextNode(escape_unparsed(rs2.getString("category"))));
                rootElement.appendChild(category);
            }
            Element currently = doc.createElement("Currently");
            currently.appendChild(doc.createTextNode("$" + rs1.getString("currently")));
            rootElement.appendChild(currently);

            Element firstBid = doc.createElement("First_Bid");
            firstBid.appendChild(doc.createTextNode("$" + rs1.getString("first_bid")));
            rootElement.appendChild(firstBid);

            String buy_price = rs1.getString("buy_price");
            if(buy_price != null) {
                Element buyPrice = doc.createElement("Buy_Price");
                buyPrice.appendChild(doc.createTextNode("$" + buy_price));
                rootElement.appendChild(buyPrice);            
            }

            Element numOfBids = doc.createElement("Number_of_Bids");
            numOfBids.appendChild(doc.createTextNode(rs1.getString("numbids")));
            rootElement.appendChild(numOfBids);       

            Element bids = doc.createElement("Bids");
            rootElement.appendChild(bids);
            ResultSet rs3 = conn.createStatement().executeQuery("SELECT userid, time, amount FROM Bids WHERE itemid = " + "'"+itemId+"'");
            ResultSet rsBidder;
            while(rs3.next()) {
                String userid = rs3.getString("userid");
                String time = toXMLTime(rs3.getString("time"));
                String amount = "$" + rs3.getString("amount");
                Element bid = doc.createElement("Bid");
                bids.appendChild(bid);

                rsBidder = conn.createStatement().executeQuery("SELECT lname, country, rating FROM Bidder WHERE userid = " + "'"+userid+"'");
                rsBidder.next();
                String location = rsBidder.getString("lname");
                String country = rsBidder.getString("country");
                String rating = rsBidder.getString("rating");
                rsBidder.close();

                Element bidder = doc.createElement("Bidder");
                bid.appendChild(bidder);
                bidder.setAttribute("Rating", rating);
                bidder.setAttribute("UserID", escape_parsed(userid));

                Element locNode = doc.createElement("Location");
                locNode.appendChild(doc.createTextNode(escape_unparsed(location)));
                bidder.appendChild(locNode);

                Element countryNode = doc.createElement("Country");
                countryNode.appendChild(doc.createTextNode(escape_unparsed(country)));
                bidder.appendChild(countryNode);

                Element bidTime = doc.createElement("Time");
                bidTime.appendChild(doc.createTextNode(time));
                bid.appendChild(bidTime);

                Element bidAmount = doc.createElement("Amount");
                bidAmount.appendChild(doc.createTextNode(amount));
                bid.appendChild(bidAmount);
            }

            Element itemLoc = doc.createElement("Location");
            rootElement.appendChild(itemLoc);
            String lla = rs1.getString("lla");
            if(lla != null) {
                itemLoc.setAttribute("Latitude", lla);
            }
            String llong = rs1.getString("llong");
            if(llong != null) {
                itemLoc.setAttribute("Longitude", llong);
            }
            itemLoc.appendChild(doc.createTextNode(escape_unparsed(rs1.getString("lname"))));

            Element itemCountry = doc.createElement("Country");
            itemCountry.appendChild(doc.createTextNode(escape_unparsed(rs1.getString("country"))));
            rootElement.appendChild(itemCountry);   

            Element started = doc.createElement("Started");
            started.appendChild(doc.createTextNode(toXMLTime(rs1.getString("started"))));                     
            rootElement.appendChild(started);

            Element ends = doc.createElement("Ends");
            ends.appendChild(doc.createTextNode(toXMLTime(rs1.getString("ends"))));                     
            rootElement.appendChild(ends);

            String sellerId = rs1.getString("sellerid");
            ResultSet seller = conn.createStatement().executeQuery("SELECT rating FROM Seller WHERE userid = " + "'"+sellerId+"'");
            seller.next();
            String sellerRating = seller.getString("rating");
            Element sellerNode = doc.createElement("Seller");
            rootElement.appendChild(sellerNode);
            sellerNode.setAttribute("UserID", escape_parsed(sellerId));
            sellerNode.setAttribute("Rating", sellerRating);

            Element desc = doc.createElement("Description");
            desc.appendChild(doc.createTextNode(escape_unparsed(rs1.getString("description"))));
            rootElement.appendChild(desc);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            result = writer.getBuffer().toString();

            conn.close();
            rs1.close();
            rs2.close();
            rs3.close();
            seller.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

        return result;
	}

	
	public String echo(String message) {
		return message;
	}

}
