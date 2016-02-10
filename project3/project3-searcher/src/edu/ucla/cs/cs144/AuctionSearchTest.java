package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchRegion;
import edu.ucla.cs.cs144.SearchResult;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

public class AuctionSearchTest {
	public static void main(String[] args1) throws IOException, ParseException
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);
		
		String query = "china";
		SearchResult[] basicResults = as.basicSearch(query, 0, 20);
		System.out.println("Basic Seacrh Query: " + query);
		
		// for(SearchResult result : basicResults) {
		// 	System.out.println(result.getItemId() + ": " + result.getName());
		// }
		System.out.println("Received " + basicResults.length + " results");
		
		SearchRegion region =
		    new SearchRegion(-80, -80, 80, 80); 
		SearchResult[] spatialResults = as.spatialSearch("china", region, 0, 1000);
		System.out.println("Spatial Seacrh");
		// for(SearchResult result : spatialResults) {
		// 	System.out.println(result.getItemId() + ": " + result.getName());
		// }
		System.out.println("Received " + spatialResults.length + " results");
		String itemId = "1044300051";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		// Add your own test here
	}
}
