package db.mysql;

import java.sql.DriverManager;
import java.sql.Statement;  // class for executing sql commands.
import java.sql.Connection;


/**
 * This class is used for automatically resetting all the tables in the database.
 * So in the future, right click this class to "Run As Java Application" every time if the data stored in DB is messed up.
 */
public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			// Step 1 Connect to MySQL.
			System.out.println("Connecting to " + MySQLDBUtil.URL);	
			// Details: https://stackoverflow.com/questions/15039265/what-exactly-does-this-do-class-fornamecom-mysql-jdbc-driver-newinstance
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();  // ensures the driver is registered. 

			Connection conn = DriverManager.getConnection(MySQLDBUtil.URL);

			if (conn == null) {
				return;
			}

			// Step 2 Drop tables in case they exist (*note: be careful about the order when dropping tables b/c some tables have foreign keys).
			Statement statement = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS categories";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS history";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			statement.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			statement.executeUpdate(sql);

			// Step 3 Create new tables (reverse order of dropping tables).
			sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL,"  // set it must NOT be NULL b/c we'll use it as PRIMARY KEY.
					+ "name VARCHAR(255),"
					+ "rating FLOAT,"
					+ "address VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"
					+ "PRIMARY KEY (item_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id)"
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE categories ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, category),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"  // set "item_id" as foreign key and let it point to "item_id" column in "items" table.
					+ ")";
			statement.executeUpdate(sql);

			sql = "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"  // special type.
					+ "PRIMARY KEY (user_id, item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id)"
					+ ")";
			statement.executeUpdate(sql);

			// Step 4: insert fake user 1111/3229c1097c00d497a0fd282d586be050 （for testing).
			sql = "INSERT INTO users VALUES('1111', '3229c1097c00d497a0fd282d586be050', 'Human', 'Being')";
			statement.executeUpdate(sql);

			System.out.println("Import done successfully");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
