

import java.util.Collection;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class Connection {
	private MongoClient mongo;
	private MongoDatabase db;

	public Connection(){
		//mongo = new MongoClient("localhost", 27017);
		//db = mongo.getDatabase("project");
	}
	
	public void connect(String node, String dbName){
		mongo = new MongoClient(node, 27017);
		db = mongo.getDatabase(dbName);
	}
	
	public MongoDatabase getDatabase(){
		return this.db;
	}
	
	public void close(){
		mongo.close();
	}
	
	public static void main(String[] args) {
		Connection connection = new Connection();
		Collection<DB> dbs = connection.mongo.getUsedDatabases();
		for(DB db:dbs){
			System.out.println(db.getName());
		}
	}
}
