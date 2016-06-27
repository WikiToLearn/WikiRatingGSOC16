package main.java.compute;

/**This class will deal with the procedures to links the pages which points to some other pages.
 * That is this will interconnect all the Backlinks. 
 */
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import main.java.utilities.Connections;
import main.java.utilities.WikiUtil;


public class LinkPages {
	
	//This is the main method will be used to linking all the pages available in the database.
	public static void linkAll(String key,String value){

		OrientGraph graph = Connections.getInstance().getDbGraph();
		String result="";
		int inLinks;
		
		//Iterating on every vertex to check it's backlinks
		
		for (Vertex v : graph.getVertices(key,value)) {
			
			result=getBacklinks((int)(v.getProperty("pid")));	//Getting the JSON formatted String to process.
			inLinks=0;
			
			//JSON interpretation of the fetched String
			
			 try {  
				 	JSONObject js=new JSONObject(result);
				 	JSONObject js2=js.getJSONObject("query");
				 	JSONArray arr=js2.getJSONArray("backlinks");	//This array has all the backlinks the page has.
				 	JSONObject dummy;							
				 	inLinks=arr.length();
				 	
				 	System.out.println(v.getProperty("title").toString()+" has inLinks = "+inLinks);
				 	
				 	//Iterating to get all the backlinks of a particular node(Page)
				 	
				 	for(int i=0;i<arr.length();i++){
				 		dummy=arr.getJSONObject(i);
				 		
				 		try{	
				 			
				 			Vertex backLink=graph.getVertices("pid",dummy.getInt("pageid")).iterator().next();	//Getting the node linked to the current page.
				 			Edge isbackLink = graph.addEdge("Backlink", backLink, v, "Backlink");				//Creating Edge in between the 2 vertices.
				 			
				 			System.out.println(v.getProperty("title").toString()+" is linked to "+backLink.getProperty("title").toString());
				 			
				 		graph.commit();														
				 		} catch( Exception e ) {
				 			e.printStackTrace();
				 			graph.rollback();																	//In case the transaction fails we will rollback.
				 		}
				 		
				 	}
			 } catch (JSONException e) { 
				 e.printStackTrace();
			 }
			 
		}
		//graph.commit();	
		graph.shutdown();
		//Revision.getAllRevisions();
	}
					
	
	
	//This is the helper method to return the a JSON formatted string queried from the MediaWiki API to get all the backlinking pages
	public static String getBacklinks(int pid) {
		  
		
		String result = "";
		ApiConnection con=Connections.getInstance().getApiConnection();
		InputStream in=WikiUtil.reqSend(con,WikiUtil.getLinkParam(pid+""));	//Getting the InputStream object having JSON form MediaWiki API
		result=WikiUtil.streamToString(in);		  							//Converting the InputStream to String
		return result;
		  
	}
	
}