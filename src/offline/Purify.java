package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import db.mongodb.MongoDBUtil;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

// This class helps store tomcat logs into MongoDB so that the MapReduce feature of MongoDB can be used for
// analyzing peak usage time.
public class Purify {
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		// Path of logs used for testing
		String fileName = "/Users/cyyy24/apache-tomcat-9.0.13/EventButler-test-log/tomcat_log.txt";

		try {
			db.getCollection("logs").drop();  // in case there was logs collection before, drop it.

			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				// Sample input: 
				// 73.223.210.212 - - [19/Aug/2017:22:00:24 +0000] "GET /EventButler/history?user_id=1111 HTTP/1.1" 200 11410
				List<String> values = Arrays.asList(line.split(" "));

				String ip = values.size() > 0 ? values.get(0) : null;
				String timestamp = values.size() > 3 ? values.get(3) : null;
				String method = values.size() > 5 ? values.get(5) : null;
				String url = values.size() > 6 ? values.get(6) : null;
				String status = values.size() > 8 ? values.get(8) : null;
				
				// Parse the date and time in the log (actually need only time not dates for peak time analysis):
				Pattern pattern = Pattern.compile("\\[(.+?):(.+)");  // regx for matching the pattern (2 "()" corresponds to 2 groups below).
				Matcher matcher = pattern.matcher(timestamp);
				matcher.find();
				
				// Put log info into MongoDB:
				db.getCollection("logs")
				.insertOne(new Document().append("ip", ip).append("date", matcher.group(1))
						.append("time", matcher.group(2)).append("method", method.substring(1))
						.append("url", url).append("status", status));
			}
			System.out.println("Import Done!");
			bufferedReader.close();
			mongoClient.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
