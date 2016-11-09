

import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class TopBalanceTransaction {
	private MongoDatabase database;

	public TopBalanceTransaction(){
		// public constructor
	}
	
	public TopBalanceTransaction(Connection connection){
		database = connection.getDatabase();
	}
	
	public void getTopbalance(){
		BasicDBObject dcCustomerSort = new BasicDBObject().append("cBalance", -1);
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName", 1).append("cBalance",1).append("cWName", 1).append("cDName", 1);
		FindIterable<Document> dcCustomer = database.getCollection("customer").find().projection(dcCustomerProjection).sort(dcCustomerSort).limit(10);
		String printStmt = "%d. Name: (%s, %s, %s) | Balance: %.2f | Warehouse name: %s | District name: %s";
		int noOfCust = 1;
		
		for(Document doc : dcCustomer){
			// for top 10 customer
			@SuppressWarnings("unchecked")
	    	List<String> nameList = (List<String>) doc.get("cName");    
			System.out.println(String.format(printStmt, noOfCust, nameList.get(0), nameList.get(1), nameList.get(2), Double.parseDouble((String)doc.get("cBalance")), (String) doc.get("cWName"), (String) doc.get("cDName")));
			noOfCust++;
		}
	}
}
