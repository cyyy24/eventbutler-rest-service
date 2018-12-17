package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    // Agreement with front end. Whenever a GET is sent here, check if a session is valid (but will not create new if none exists).
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			HttpSession session = request.getSession(false);  // check if there's a session binded to this request ("false" will not let it create a new session b/c we just want to check.)		
			JSONObject obj = new JSONObject();
			
			if (session != null) {  // session does exist.
				String userId = session.getAttribute("user_id").toString();
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				response.setStatus(403);  // authorization failed (session expired).
				obj.put("status", "Invalid Session");
			}
			
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	// When user first-time typed in username & password and hit login.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			
			JSONObject obj = new JSONObject();
			if (connection.verifyLogin(userId, password)) {
				// Tomcat will bind the session to request & response (will create a new session if none exists). No need to handle session id and other stuff.
				HttpSession session = request.getSession(); 
				session.setAttribute("user_id", userId);  // put userId into the session so that it can later be retrieved from session.
				session.setMaxInactiveInterval(600);  // after 600s (10min), need to re-login.
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				response.setStatus(401);  // authentication failed.
				obj.put("status", "User Doesn't Exist");
			}
			
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

}
