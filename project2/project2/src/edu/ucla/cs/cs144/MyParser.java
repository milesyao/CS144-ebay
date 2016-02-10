/* CS144
 *
 * Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 * At the point noted below, an individual XML file has been parsed into a
 * DOM Document node. You should fill in code to process the node. Java's
 * interface for the Document Object Model (DOM) is in package
 * org.w3c.dom. The documentation is available online at
 *
 * http://java.sun.com/j2se/1.5.0/docs/api/index.html
 *
 * A tutorial of Java's XML Parsing can be found at:
 *
 * http://java.sun.com/webservices/jaxp/
 *
 * Some auxiliary methods have been written for you. You may find them
 * useful.
 */

package edu.ucla.cs.cs144;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;


class MyParser {
    
    static final String columnSeparator = "|*|";
    static DocumentBuilder builder;
    
    static final String[] typeName = {
	"none",
	"Element",
	"Attr",
	"Text",
	"CDATA",
	"EntityRef",
	"Entity",
	"ProcInstr",
	"Comment",
	"Document",
	"DocType",
	"DocFragment",
	"Notation",
    };
    
    static class MyErrorHandler implements ErrorHandler {
        
        public void warning(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void error(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void fatalError(SAXParseException exception)
        throws SAXException {
            exception.printStackTrace();
            System.out.println("There should be no errors " +
                               "in the supplied XML files.");
            System.exit(3);
        }
        
    }
    
    /* Non-recursive (NR) version of Node.getElementsByTagName(...)
     */
    static Element[] getElementsByTagNameNR(Element e, String tagName) {
        Vector< Element > elements = new Vector< Element >();
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
            {
                elements.add( (Element)child );
            }
            child = child.getNextSibling();
        }
        Element[] result = new Element[elements.size()];
        elements.copyInto(result);
        return result;
    }
    
    /* Returns the first subelement of e matching the given tagName, or
     * null if one does not exist. NR means Non-Recursive.
     */
    static Element getElementByTagNameNR(Element e, String tagName) {
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }
    
    /* Returns the text associated with the given element (which must have
     * type #PCDATA) as child, or "" if it contains no text.
     */
    static String getElementText(Element e) {
        if (e.getChildNodes().getLength() == 1) {
            Text elementText = (Text) e.getFirstChild();
            return elementText.getNodeValue();
        }
        else
            return "";
    }
    
    /* Returns the text (#PCDATA) associated with the first subelement X
     * of e with the given tagName. If no such X exists or X contains no
     * text, "" is returned. NR means Non-Recursive.
     */
    static String getElementTextByTagNameNR(Element e, String tagName) {
        Element elem = getElementByTagNameNR(e, tagName);
        if (elem != null)
            return getElementText(elem);
        else
            return "";
    }
    
    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                                   "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }

    public static String toSQLTime(String TimeInfo) {
        try {
            SimpleDateFormat oldform = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
            SimpleDateFormat newform = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            TimeInfo = newform.format(oldform.parse(TimeInfo));
        } catch(ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return TimeInfo;
    }

    public static String truncString(String des) {
        if(des.length() > 4000) {
            return des.substring(0, 4000-6) + "[MORE]";
        }
        return des;
    }
    
    /* Process one items-???.xml file.
     */
    static void processFile(File xmlFile) {
        Document doc = null;
        try {
            doc = builder.parse(xmlFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
        catch (SAXException e) {
            System.out.println("Parsing error on file " + xmlFile);
            System.out.println("  (not supposed to happen with supplied XML files)");
            e.printStackTrace();
            System.exit(3);
        }
        
        /* Fill in code here (you will probably need to write auxiliary
            methods). */
        try {
            FileWriter itemCategory = new FileWriter("category.csv", true);
            FileWriter itemInfo = new FileWriter("item.csv", true);
            FileWriter bids = new FileWriter("bids.csv", true);
            FileWriter bidderInfo = new FileWriter("bidder.csv", true);
            FileWriter sellerInfo = new FileWriter("seller.csv", true);

            BufferedWriter itemCategoryOut = new BufferedWriter(itemCategory);
            BufferedWriter itemInfoOut = new BufferedWriter(itemInfo);
            BufferedWriter bidsOut = new BufferedWriter(bids);
            BufferedWriter bidderInfoOut = new BufferedWriter(bidderInfo);
            BufferedWriter sellerInfoOut = new BufferedWriter(sellerInfo);

            Node curNode = getElementByTagNameNR(doc.getDocumentElement(), "Item");
            Element curElement;
            while(curNode != null) {
                if(curNode instanceof Element) {
                    curElement = (Element) curNode;
                    String itemID = curElement.getAttributes().item(0).getNodeValue();

                    // String itemName = addEscape(getElementTextByTagNameNR(curNode, "Name"));
                    String itemName = getElementTextByTagNameNR(curElement, "Name");

                    String currently = strip(getElementTextByTagNameNR(curElement, "Currently"));
                    String buyPrice = strip(getElementTextByTagNameNR(curElement,"Buy_Price"));
                    if(buyPrice.equals("")) {
                        buyPrice = "\\N";
                    }
                    String firstBid = strip(getElementTextByTagNameNR(curElement, "First_Bid"));
                    String numberBids = getElementTextByTagNameNR(curElement, "Number_of_Bids");
                    Element location = getElementByTagNameNR(curElement, "Location");
                    String locName = getElementText(location);
                    Node laNode = location.getAttributes().getNamedItem("Latitude");
                    String locLatitude="";
                    if(laNode != null) locLatitude = laNode.getNodeValue();
                    // String locLatitude = getElementTextByTagNameNR(location, "Latitude");
                    if(locLatitude.equals("")) {
                        locLatitude = "\\N";
                    }
                    // String locLongitude = getElementTextByTagNameNR(location, "Longitude");
                    Node longNode = location.getAttributes().getNamedItem("Longitude");
                    String locLongitude = "";
                    if(longNode != null) locLongitude = longNode.getNodeValue();

                    if(locLongitude.equals("")) {
                        locLongitude = "\\N";
                    }
                    // String country = addEscape(getElementTextByTagNameNR(curNode, "Country"));
                    String country = getElementTextByTagNameNR(curElement, "Country");
                 
                    String started = toSQLTime(getElementTextByTagNameNR(curElement, "Started"));
                    String ends = toSQLTime(getElementTextByTagNameNR(curElement, "Ends"));
                    String sellerId = getElementByTagNameNR(curElement, "Seller").getAttributes().getNamedItem("UserID").getNodeValue();
                    String desc = truncString(getElementTextByTagNameNR(curElement, "Description"));
                    //Item-Info
                    itemInfoOut.append(itemID + columnSeparator + itemName + columnSeparator + 
                                        currently + columnSeparator + buyPrice + columnSeparator + 
                                        firstBid + columnSeparator + numberBids + columnSeparator + 
                                        locName + columnSeparator + locLatitude + columnSeparator + 
                                        locLongitude + columnSeparator + country + columnSeparator + 
                                        started + columnSeparator + ends + columnSeparator + sellerId +
                                        columnSeparator + desc + '\n');

                    Node[] aimNodeList = getElementsByTagNameNR(curElement, "Category");
                    Element curAimElement;
                    //Item-Category
                    for(Node aimNode:aimNodeList) {
                        curAimElement = (Element) aimNode;
                        itemCategoryOut.append(itemID + columnSeparator + getElementText(curAimElement) + '\n');
                    }
                    //Bids
                    Node aimNode = getElementByTagNameNR(curElement, "Bids");
                    aimNode = getElementByTagNameNR((Element) aimNode, "Bid");
                    while(aimNode != null) {
                        if(aimNode instanceof Element) {
                            curAimElement = (Element) aimNode;
                            Element bidder = getElementByTagNameNR(curAimElement, "Bidder");
                            String bidderID = bidder.getAttributes().getNamedItem("UserID").getNodeValue();
                            String bidderRating = bidder.getAttributes().getNamedItem("Rating").getNodeValue();
                            String bidderLocation = getElementTextByTagNameNR(bidder, "Location");

                            if(bidderLocation.equals("")) {
                                bidderLocation = "\\N";
                            }

                            String bidderCountry = getElementTextByTagNameNR(bidder, "Country");
                            if(bidderCountry.equals("")) {
                                bidderCountry = "\\N";
                            }
                            String bidTime = toSQLTime(getElementTextByTagNameNR(curAimElement, "Time"));
                            String bidAmount = strip(getElementTextByTagNameNR(curAimElement, "Amount"));

                            bidsOut.append(bidderID + columnSeparator + bidTime + columnSeparator 
                                            + itemID + columnSeparator + bidAmount + '\n');
                            bidderInfoOut.append(bidderID + columnSeparator + bidderLocation + columnSeparator 
                                                + bidderCountry + columnSeparator + bidderRating + '\n');
                        }   
                        aimNode = aimNode.getNextSibling();
                    }
                    String sellerRating = getElementByTagNameNR(curElement, "Seller").getAttributes().getNamedItem("Rating").getNodeValue();
                    sellerInfoOut.append(sellerId + columnSeparator + sellerRating + '\n');

                }
                curNode = curNode.getNextSibling();
                
            }

            /* At this point 'doc' contains a DOM representation of an 'Items' XML
             * file. Use doc.getDocumentElement() to get the root Element. */
            System.out.println("Successfully parsed - " + xmlFile);
     
            itemInfoOut.close();
            itemCategoryOut.close();
            bidsOut.close();
            bidderInfoOut.close();
            sellerInfoOut.close();

        } catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        
        
        /**************************************************************/
        
    }
    
    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java MyParser [file] [file] ...");
            System.exit(1);
        }
        
        /* Initialize parser. */
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);      
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
        }
        catch (FactoryConfigurationError e) {
            System.out.println("unable to get a document builder factory");
            System.exit(2);
        } 
        catch (ParserConfigurationException e) {
            System.out.println("parser was unable to be configured");
            System.exit(2);
        }
        
        /* Process all files listed on command line. */
        for (int i = 0; i < args.length; i++) {
            File currentFile = new File(args[i]);
            processFile(currentFile);
        }
    }
}
