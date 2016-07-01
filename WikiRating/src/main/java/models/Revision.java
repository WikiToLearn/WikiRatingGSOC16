package main.java.models;

import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import main.java.controllers.WikiUtil;
import main.java.utilities.Connections;


/**
 * This class will link the revisions to the corresponding pages
 */

public class Revision {
	
	//This 
	/**
	 * This method will compute the revisions for all the pages and link them
	 * @param key	Name of the key here '@class'
	 * @param value	Value of the key here 'Revision'
	 */
	public static void getAllRevisions(String key,String value){
		
		String result="";
		OrientGraph graph=Connections.getInstance().getDbGraph();
		System.out.println("=====Checkpoint for Revisions==========");
		for (Vertex v : graph.getVertices(key,value)) {
			
			result=getRevision(v.getProperty("pid").toString());	//Fetching the revision string for a particular page.
							
			try {
				//JSON interpretation
				 try {  
					 	JSONObject js=new JSONObject(result);
					 	JSONObject js2=js.getJSONObject("query").getJSONObject("pages").getJSONObject(v.getProperty("pid").toString());
					 	JSONArray arr=js2.getJSONArray("revisions");
						JSONObject dummy;
						
						
					 	for(int i=0;i<arr.length();i++){
					 		dummy=arr.getJSONObject(i);
					 		
					 		System.out.println(v.getProperty("title").toString()+dummy.getInt("revid"));
					 		
					 		//Adding pages to database without the duplicates
					 		if(WikiUtil.rCheck("revid", dummy.getInt("revid"), graph)){
					 			
					 		try{
					 			  Vertex ver = graph.addVertex("class:Revision"); // 1st OPERATION: IMPLICITLY BEGINS TRANSACTION
					 			  ver.setProperty( "Page", v.getProperty("title").toString());
					 			  ver.setProperty("revid",dummy.getInt("revid"));
					 			  ver.setProperty("parentid",dummy.getInt("parentid"));
					 			  ver.setProperty("user",dummy.getString("user"));
					 			  ver.setProperty("userid",dummy.getInt("userid"));
					 			  ver.setProperty("size",dummy.getInt("size"));
					 			  ver.setProperty("previousVote",-1.0);
					 			  ver.setProperty("previousReliability", -1.0);
					 			  
					 			  //All the versions are connected to each other like (Page)<-(Latest)<-(Latest-1)<-...<-(Last)
					 			  
					 			  if(i==arr.length()-1){//The latest version will be connected to the Page itself and to the previous revision too
					 				  
					 				  Vertex parentPage=graph.getVertices("pid",v.getProperty("pid")).iterator().next();
					 				  Edge isRevision = graph.addEdge("PreviousVersionOfPage", parentPage,ver,"PreviousVersionOfPage");
					 			  }
					 			  
					 			  if(i!=0){//To link the current revision to the previous versions
					 				Vertex parent=graph.getVertices("revid",dummy.getInt("parentid")).iterator().next();
					 				Edge isRevision = graph.addEdge("PreviousRevision", ver,parent,"PreviousRevision");
					 			  }
					 			  
					 			  
					 			  graph.commit();
					 			} catch( Exception e ) {
					 				e.printStackTrace();
					 			  graph.rollback();
					 			}
					 	}
					 	
					 		}
					} catch (JSONException e) {
						e.printStackTrace();
					}
			 
				 
			 } catch(Exception ee) {
			   ee.printStackTrace();
			 }
			
}		//graph.commit();
		graph.shutdown();
//Pagerank.pageRankCompute();
		
			
		
	}

	//This is the helper method to return the a JSON formatted string queried from the MediaWiki API to get all the revisions for a particular pid.
	
	/**
	 * This method will return the a JSON formatted string queried
	 * from the MediaWiki API get all the pages in the particular Namespace
	 * @param pid	PageID of the Page whose revisions are to be returned
	 * @return	A JSON formatted String containing all the revisions 
	 */
	
	public static String getRevision(String pid ){

		String result = "";
		ApiConnection con=Connections.getInstance().getApiConnection();
		InputStream in=WikiUtil.reqSend(con,WikiUtil.getRevisionParam(pid));
		result=WikiUtil.streamToString(in);
		return result;

	}
	
	
	
	
}
