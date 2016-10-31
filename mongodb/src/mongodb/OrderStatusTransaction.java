package mongodb;

import org.bson.Document;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
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
		Document dcCustomerQuery = new Document("cWId", wID).append("cDId", dID).append("cId", cID);
		Document dcCustomerProjection = new Document("cName",1).append("cBalance", 1);
		Document dcCustomer = database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();
		System.out.println(String.format("Customer's Name: (%s, %s, %s) | Balance: %.2f", dcCustomer.getString("cName.first"), dcCustomer.getString("cName.middle"), dcCustomer.getString("cName.last"), dcCustomer.getDouble("cBalance")));

		// get last order
		BasicDBObject dcOrderLast = new BasicDBObject("oId", -1);
		Document dcOrderQuery = new Document("oWId", wID).append("oDId", dID).append("oCId", cID);
		Document dcOrderProjection = new Document("oId",1).append("oEntryDate", 1).append("oCarrierId", 1).append("oOrderLine", 1);
		Document dcOrder = database.getCollection("order").find(dcOrderQuery).projection(dcOrderProjection).sort(dcOrderLast).limit(1).first();
		int oId = dcOrder.getInteger("oId");
		String entryDate = dcOrder.get("oEntryDate").toString();
		int oCId = dcOrder.getInteger("oCarrierId");
		BasicDBList orderLine = (BasicDBList) dcOrder.get("oOrderLine");
		
		String deliveryDate="";
		try{
			deliveryDate = dcOrder.getString("o_delivery_d");
		}catch(NullPointerException e){
			deliveryDate = "Not delivered";
		}

		System.out.println(String.format("Last order number: %d | Entry date & time: %s | Carrier ID: %d", oId, orderRow.getTimestamp("o_entry_d").toString(), orderRow.getInt("o_carrier_id")));

		System.out.println("Ordered Item(s)");
		ResultSet orderLineResult = session.execute(selectOrderLineStmt.bind(wID, dID, orderId));
		List<Row> orderLineRow = orderLineResult.all();
		for(int i=0; i < orderLineRow.size(); i++){
			Row currentRow = orderLineRow.get(i);
			System.out.println(String.format("Item No.: %d | Supplying Warehouse No.: %d | quantity: %.0f | Total price: %.2f | Delivery date & time: %s", currentRow.getInt("ol_i_id"), currentRow.getInt("ol_supply_w_id"), currentRow.getDecimal("ol_quantity"), currentRow.getDecimal("ol_amount"), deliveryDate));
		}
	}
}
