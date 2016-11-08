

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;




public class OrderStatusTransaction {
	private MongoDatabase database;

	public OrderStatusTransaction(){
		// public constructor
	}

	public OrderStatusTransaction(Connection connection){
		database = connection.getDatabase();
	}


	public void getOrderStatus(int wID, int dID, int cID){
		System.out.println("wID"+wID+" ;dID"+dID+" ;cID"+cID);
/*		//@SuppressWarnings("deprecation")	
 * 		for(String s : database.listCollectionNames())
			System.out.println("OrderStatus: "+s);*/
		// print customer
		BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", Integer.toString(wID)).append("cDId", Integer.toString(dID)).append("cId", Integer.toString(cID));
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName",1).append("cBalance", 1);
		MongoCollection<Document> coll = database.getCollection("customer");
		//MongoCursor<Document> cursor = coll.find(dcCustomerQuery).projection(dcCustomerProjection).iterator();
		Document dcCustomer = coll.find(dcCustomerQuery).projection(dcCustomerProjection).first();
		@SuppressWarnings("unchecked")
    	List<String> nameList = (List<String>) dcCustomer.get("cName");      	
		System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", nameList.get(0), nameList.get(1), nameList.get(2), Double.parseDouble(dcCustomer.getString("cBalance"))));		
    
		/*try {
	        while (cursor.hasNext()) {
	        	Document dcCustomer = cursor.next();
	        	@SuppressWarnings("unchecked")
	        	List<String> nameList = (List<String>) dcCustomer.get("cName");      	
				System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", nameList.get(0), nameList.get(1), nameList.get(2), Double.parseDouble(dcCustomer.getString("cBalance"))));		
	        }
	    } finally {
	        cursor.close();
	    }*/
/*		// print customer
		BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", Integer.toString(wID)).append("cDId", Integer.toString(dID)).append("cId", Integer.toString(cID));
		//BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", dID).append("cId", cID);
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName",1).append("cBalance", 1);
		//DBObject dcCustomer = (DBObject) database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();
		Document dcCustomer = database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();
		System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", dcCustomer.getString("cName.first"), dcCustomer.getString("cName.middle"), dcCustomer.getString("cName.last"), Double.parseDouble(dcCustomer.getString("cBalance"))));
		//System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", dcCustomer.get("cName.first").toString(), dcCustomer.get("cName.middle").toString(), dcCustomer.get("cName.last").toString(), (Double) dcCustomer.get("cBalance")));
*/
		// get last order
		BasicDBObject dcOrderSort = new BasicDBObject().append("oId", -1);
		BasicDBObject dcOrderQuery = new BasicDBObject().append("oWId", wID).append("oDId", dID).append("oCId", cID);
		BasicDBObject dcOrderProjection = new BasicDBObject().append("oId",1).append("oEntryDate", 1).append("oDeliveryDate", 1).append("oCarrierId", 1).append("oOrderLine", 1);
		MongoCollection<Document> coll2 = database.getCollection("order");
		MongoCursor<Document> cursor2 = coll2.find(dcOrderQuery).projection(dcOrderProjection).sort(dcOrderSort).limit(1).iterator();
		try {
	        while (cursor2.hasNext()) {
	        	Document dcOrder = cursor2.next();
	        	@SuppressWarnings("unchecked")
	        	int oId = (Integer) dcOrder.get("oId");
	        	System.out.println("OLD IS: "+oId);
	        	String entryDate = dcOrder.get("oEntryDate").toString();
	        	System.out.println("entryDate IS: "+entryDate);
	    		int oCarrierId = (Integer) dcOrder.get("oCarrierId");
	    		System.out.println("oCarrierId IS: "+oCarrierId);
	    		@SuppressWarnings("unchecked")
	    		List<Document> orderLineList = (List<Document>) dcOrder.get("oOrderLine");
	    		System.out.println("Length IS: "+orderLineList.size());
	    		String deliveryDate="";
	    		try{
	    			deliveryDate = dcOrder.get("oDeliveryDate").toString();
	    		}catch(NullPointerException e){
	    			deliveryDate = "Not delivered";
	    		}
	    		System.out.println("deliveryDate IS: "+deliveryDate);
	    		System.out.println(String.format("Last order number: %d | Entry date & time: %s | Carrier ID: %d", oId, entryDate, oCarrierId));

	    		System.out.println("Ordered Item(s)");
	    		
	    		for(int i=0; i < orderLineList.size(); i++){
	    			Document orderLine = orderLineList.get(i);
	    			System.out.println(String.format("Item No.: %d | Supplying Warehouse No.: %d | quantity: %.0f | Total price: %.2f | Delivery date & time: %s", orderLine.get("iId"), orderLine.get("supplyWId"), orderLine.get("qty"), orderLine.getDouble("amt"), deliveryDate));
	    		}
	        }
	    } finally {
	        cursor2.close();
	    }
		/*// get last order
		BasicDBObject dcOrderSort = new BasicDBObject().append("oId", -1);
		BasicDBObject dcOrderQuery = new BasicDBObject().append("oWId", wID).append("oDId", dID).append("oCId", cID);
		BasicDBObject dcOrderProjection = new BasicDBObject().append("oId",1).append("oEntryDate", 1).append("oDeliveryDate", 1).append("oCarrierId", 1).append("oOrderLine", 1);
		DBObject dcOrder = (DBObject) database.getCollection("order").find(dcOrderQuery).projection(dcOrderProjection).sort(dcOrderSort).limit(1).first();
		int oId = (Integer) dcOrder.get("oId");
		String entryDate = dcOrder.get("oEntryDate").toString();
		int oCarrierId = (Integer) dcOrder.get("oCarrierId");
		BasicDBList orderLineList = (BasicDBList) dcOrder.get("oOrderLine");
		
		String deliveryDate="";
		try{
			deliveryDate = dcOrder.get("oDeliveryDate").toString();
		}catch(NullPointerException e){
			deliveryDate = "Not delivered";
		}

		System.out.println(String.format("Last order number: %d | Entry date & time: %s | Carrier ID: %d", oId, entryDate, oCarrierId));

		System.out.println("Ordered Item(s)");
		
		for(int i=0; i < orderLineList.size(); i++){
			BasicDBObject orderLine = (BasicDBObject) orderLineList.get(i);
			System.out.println(String.format("Item No.: %d | Supplying Warehouse No.: %d | quantity: %.0f | Total price: %.2f | Delivery date & time: %s", orderLine.getInt("iId"), orderLine.getInt("supplyWId"), orderLine.getInt("qty"), orderLine.getDouble("amt"), deliveryDate));
		}*/
	}
}
