
package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

// This class is responsible for communicating with MySQL database.
// It includes method such as setting/unsetting, retrieving, and storing favorite items, etc.  
public class MySQLConnection implements DBConnection{
	// Fields:
	private Connection conn;

	// Constructor:
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Methods:
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method that takes user_id and a list of item_ids, and stores them into "history" table in database.
	 */
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		// Start putting user_id and item_id into history table:
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that takes user_id and a list of item_ids, and removes them from the "history" table in database.
	 */
	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
		}

		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that takes user_id and returns a set of all item_id this user has saved
	 * (for later looking up items in "items" table and item categories in "categories" table given item_id).
	 */
	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}

	/**
	 * Method that takes user_id and returns a set of all the items this user has saved.
	 * It will call "getFavoriteItemIds(String userId)" first to get all item_id and use them to construct items 
	 * with info retrieved from "items" table.
	 */
	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}

		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);

		// This is the reason we need "items" table (when in "favorite" tab, we can search the db to get all info to display there).
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, itemId);

				ResultSet rs = stmt.executeQuery();

				ItemBuilder builder = new ItemBuilder();

				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));

					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;

	}

	/**
	 * Method that takes item_id to look up corresponding categories of this item_id in "categories" table in database.
	 */
	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE item_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String category = rs.getString("category");
				categories.add(category);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	/**
	 * Method that takes a user's latitude, longitude, and the given term to search for corresponding events,
	 * and returns a list of items (model object) while storing all the items into database.
	 * 
	 * It will call "search" method defined in TicketMasterAPI class to do HTTP request for the actual search. 
	 */
	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI ticketMasterAPI = new TicketMasterAPI();
		List<Item> items = ticketMasterAPI.search(lat, lon, term);

		for(Item item : items) {
			saveItem(item);
		}

		return items;
	}

	/**
	 * Method that takes an item (model object) and stores it into the "items" table in database.
	 */
	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		// sql injection
		// select * from users where username = '' AND password = '';

		// username: fakeuser ' OR 1 = 1; DROP  --
		// select * from users where username = 'fakeuser ' OR 1 = 1 --' AND password = ''; 
		try {
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);  // Here: use PreparedStatement and placeholder ("?") to prevent SQL injection.
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getImageUrl());
			ps.setString(6, item.getUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();

			// Put item_id and categories into categories table:
			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for(String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// -------------------------------- For Authentication & Authorization ------------------------------------
	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return "";
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {  // user_id is primary key, so there must be only on row for this user_id (or none). So can use "if" ("while" also ok).
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {  // front-end JS already encrypted these.
		if (conn == null) {
			return false;
		}
		
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ? ";  // no need to SELECT * (but also OK).
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {  // rs.next() == true if userId & password matches a record in database.
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}
	
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (conn == null || userExisted(userId)) {
			return false;
		}
		
		try {
			String sql = "INSERT INTO users VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);  // Here: use PreparedStatement and placeholder ("?") to prevent SQL injection.
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			ps.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	private boolean userExisted(String userId) {
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? ";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {  // username already existed.
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return false;
	}

}
