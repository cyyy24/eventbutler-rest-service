package db.mongodb;

import java.text.ParseException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBCollectionCreation {
	// Run as Java application to create MongoDB collections with index.
	public static void main(String[] args) throws ParseException {

		// Step 1, connetion to MongoDB
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);

		// Step 2, remove old collections.
		db.getCollection("users").drop();
		db.getCollection("items").drop();

		// Step 3, create new collections
		IndexOptions options = new IndexOptions().unique(true);  // for later assign to user_id & item_id to be unique (restriction).
		// "1" here means data will store in order from smallest to largest (for binary search).
		db.getCollection("users").createIndex(new Document("user_id", 1), options);  // if "-1" reverse order.
		db.getCollection("items").createIndex(new Document("item_id", 1), options);

		// Step 4, insert fake user data and create index.
		db.getCollection("users").insertOne(
				new Document().append("user_id", "1111").append("password", "3229c1097c00d497a0fd282d586be050")
				.append("first_name", "Human").append("last_name", "Being"));

		mongoClient.close();
		System.out.println("Import is done successfully.");
	}
}
