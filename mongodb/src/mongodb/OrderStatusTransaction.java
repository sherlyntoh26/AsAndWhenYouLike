package mongodb;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;

public class OrderStatusTransaction {
	private MongoDatabase database;

	public OrderStatusTransaction(){
		// public constructor
	}

	public OrderStatusTransaction(Connection connection){
		database = connection.getDatabase();
	}

	public void getOrderStatus(int wID, int dID, int cID){

		// print customer
		BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", dID).append("cId", cID);
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName",1).append("cBalance", 1);
		DBObject dcCustomer = (DBObject) database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();
		System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", dcCustomer.get("cName.first").toString(), dcCustomer.get("cName.middle").toString(), dcCustomer.get("cName.last").toString(), (Double) dcCustomer.get("cBalance")));

		// get last order
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
		}
	}
}
