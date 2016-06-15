package test.java;
/**This class is made to link the revisions to the corresponding pages
 * 
 */
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wikidata.wdtk.wikibaseapi.ApiConnection;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;


public class Revision {
	
	//This method will compute the revisions for all the pages and link them
	public static void getAllRevisions(){
		
		String result="";
		OrientGraph graph=Connections.getInstance().getDbGraph();
		String res="==";
		System.out.println("=====Checkpoint for Revisions==========");
		for (Vertex v : graph.getVertices("@class","Page")) {
			
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
					 		
					 		System.out.println(v.getProperty("name").toString()+dummy.getInt("revid"));
					 		
					 		//Adding pages to database without the duplicates
					 		if(WikiUtil.rCheck("revid", dummy.getInt("revid"), graph)){
					 			
					 		try{
					 			  Vertex ver = graph.addVertex("class:Revision"); // 1st OPERATION: IMPLICITLY BEGINS TRANSACTION
					 			  ver.setProperty( "Page", v.getProperty("name").toString());
					 			  ver.setProperty("revid",dummy.getInt("revid"));
					 			  ver.setProperty("parentid",dummy.getInt("parentid"));
					 			  ver.setProperty("user",dummy.getString("user"));
					 			  ver.setProperty("size",dummy.getInt("size"));
					 			 					 			  
					 			  if(i!=0){//since the latest version will be connected to the Page node itself and rest of them like a linklist fashion.
					 				Vertex parent=graph.getVertices("revid",dummy.getInt("parentid")).iterator().next();
					 				Edge isRevision = graph.addEdge("link", parent, ver, "version");
					 			  }
					 			  else{
					 				  Vertex parentPage=graph.getVertices("pid",v.getProperty("pid")).iterator().next();
					 				  Edge isRevision = graph.addEdge("link", parentPage, ver, "Pversion");
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
			 
				 
			 } finally {
			   
			 }
			
}		graph.shutdown();
		
			
		
	}

	//This is the helper method to return the a JSON formatted string queried from the MediaWiki API to get all the revisions for a particular pid.
	public static String getRevision(String pid ){
		String result = "";
		  
		  ApiConnection con=Connections.getInstance().getApiConnection();
		  InputStream in=WikiUtil.reqSend(con,WikiUtil.getRevisionParam(pid));
		  result=WikiUtil.streamToString(in);
		  return result;
		  
		 
			 
	}
	
	
	
	
	
	
	
	
	
	
}
