package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;

// This class provides some necessary methods for successful communications with front end.
public class RpcHelper {
	// Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
		// (optional) Let browser know what data format we are sending back (e.g. Chrome will auto-detect).
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();	
		out.print(array);  // write the array to "response" first, and finally "response" will pass it to front end.
		out.close();
	}

	// Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {		
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*");  // For front-end access control. Here set to "*" meaning no restrictions.
		PrintWriter out = response.getWriter();	
		out.print(obj);
		out.close();
	}

	// Parses a JSONObject from http request body (for retrieving and deleting user favorites).
	/** 
	 *  When user click the heart icon on the website, a doPost request will be triggered.
	 *  Imagine the input HTTP request looks like:
	 *	{
	 *		'user_id':'1111',
	 *		'favorite':[
	 *   	'abcd',
	 *   	'efgh'
	 *		]
	 *	}
	 */
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line = null;
			while((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} 
		return new JSONObject();
	}
	
	 // Converts a list of Item objects to JSONArray for sending back to front end (JUnit Test).
	  public static JSONArray getJSONArray(List<Item> items) {
	    JSONArray result = new JSONArray();
	    
	    try {
	      for (Item item : items) {
	        result.put(item.toJSONObject());
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return result;
	  }


}
