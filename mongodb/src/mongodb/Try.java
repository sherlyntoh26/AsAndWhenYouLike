package mongodb;

import java.net.UnknownHostException;
import java.util.Date;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Try {

	public static void main(String[] args) {

	    try {

		/**** Connect to MongoDB ****/
		// Since 2.10.0, uses MongoClient
		MongoClient mongo = new MongoClient("localhost", 27017);

		/**** Get database ****/
		// if database doesn't exists, MongoDB will create it for you
		MongoDatabase db = mongo.getDatabase("testdb");

		/**** Get collection / table from 'testdb' ****/
		// if collection doesn't exists, MongoDB will create it for you
		MongoCollection<Document> table = db.getCollection("user");
		//DBCollection table = db.getCollection("user");

		/**** Insert ****/
		// create a document to store key and value
		Document document = new Document();
		document.put("name", "mkyong");
		document.put("age", 30);
		document.put("createdDate", new Date());
		table.insertOne(document);

		/**** Find and display ****/
		Document searchQuery = new Document();
		searchQuery.put("name", "mkyong");

		FindIterable<Document> cursor = table.find(searchQuery);
		//DBCursor cursor = table.find(searchQuery);

		while (cursor.iterator().hasNext()) {
			System.out.println(cursor.iterator().next());
			cursor.iterator().next();
			break;
		}

		/**** Update ****/
		// search document where name="mkyong" and update it with new values
		Document query = new Document();
		query.put("name", "mkyong");

		Document newDocument = new Document();
		newDocument.put("name", "mkyong-updated");

		Document updateObj = new Document();
		updateObj.put("$set", newDocument);

		table.updateMany(query, updateObj);

		/**** Find and display ****/
		Document searchQuery2
		    = new Document().append("name", "mkyong-updated");

		FindIterable<Document> cursor2 = table.find(searchQuery2);

		while (cursor2.iterator().hasNext()) {
			System.out.println(cursor2.iterator().next());
			break;
		}

		/**** Done ****/
		System.out.println("Done");

	    } catch (MongoException e) {
		e.printStackTrace();
	    }

	  }
	}
