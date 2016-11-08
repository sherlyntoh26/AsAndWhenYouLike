

import java.sql.Timestamp;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class DeliveryTransaction {
	private MongoDatabase database;

	public DeliveryTransaction() {
		// public constructor
	}

	public DeliveryTransaction(Connection connection) {
		database = connection.getDatabase();
	}

	public void makeDelivery(int wID, int carrierID) {
		int oldCarrierID = 0;
		for (int i = 1; i <= 10; i++) {
			BasicDBObject dcOrderSort = new BasicDBObject().append("oId", 1);
			BasicDBObject dcOrderQuery = new BasicDBObject().append("oWId", wID).append("oDId", i).append("oCarrierId",
					oldCarrierID);
			BasicDBObject dcOrderProjection = new BasicDBObject().append("oTotalAmt", 1).append("oId", 1).append("oCId",
					1);
			MongoCollection<Document> coll = database.getCollection("order");
			MongoCursor<Document> cursor = coll.find(dcOrderQuery).projection(dcOrderProjection).sort(dcOrderSort).limit(1).iterator();
			try {
		        while (cursor.hasNext()) {
		        	System.out.println("IN");
		        	Document dcOrder = cursor.next();
		        	int oId = (Integer) dcOrder.get("oId");
					if (oId != 0) {
						int oCId = (Integer) dcOrder.get("oCId");
						double totalAmt = (Double) dcOrder.get("oTotalAmt");
						// update orders
						BasicDBObject dcOrderQueryUpdate = new BasicDBObject().append("oWId", wID).append("oDId", i)
								.append("oId", oId);
						BasicDBObject dcOrderUpdate = new BasicDBObject()
								.append("$set", new BasicDBObject("oCarrierId", carrierID))
								.append("$set", new BasicDBObject("oDeliveryDate", new Timestamp(System.currentTimeMillis())));
						coll.updateOne(dcOrderQueryUpdate, dcOrderUpdate);

						// update customer
						BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", i).append("cId",
								oCId);
						BasicDBObject dcCustomerUpdate = new BasicDBObject()
								.append("$inc", new BasicDBObject("cBalance", totalAmt))
								.append("$inc", new BasicDBObject("cDeliveryCnt", 1));
						database.getCollection("customer").updateOne(dcCustomerQuery, dcCustomerUpdate);
					}
		        }
		    } finally {
		        cursor.close();
		    }

			
		}
		/*int oldCarrierID = 0;
		for (int i = 1; i <= 10; i++) {
			BasicDBObject dcOrderSort = new BasicDBObject().append("oId", 1);
			BasicDBObject dcOrderQuery = new BasicDBObject().append("oWId", wID).append("oDId", i).append("oCarrierId",
					oldCarrierID);
			BasicDBObject dcOrderProjection = new BasicDBObject().append("oTotalAmt", 1).append("oId", 1).append("oCId",
					1);
			DBObject dcOrder = (DBObject) database.getCollection("order").find(dcOrderQuery)
					.projection(dcOrderProjection).sort(dcOrderSort).limit(1).first();

			int oId = (Integer) dcOrder.get("oId");
			if (oId != 0) {

				int oCId = (Integer) dcOrder.get("oCId");
				double totalAmt = (Double) dcOrder.get("oTotalAmt");

				// update orders
				BasicDBObject dcOrderQueryUpdate = new BasicDBObject().append("oWId", wID).append("oDId", i)
						.append("oId", oId);
				BasicDBObject dcOrderUpdate = new BasicDBObject()
						.append("$set", new BasicDBObject("oCarrierId", carrierID))
						.append("$set", new BasicDBObject("oDeliveryDate", new Timestamp(System.currentTimeMillis())));
				database.getCollection("order").updateOne(dcOrderQueryUpdate, dcOrderUpdate);

				// update customer
				BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", i).append("cId",
						oCId);
				BasicDBObject dcCustomerUpdate = new BasicDBObject()
						.append("$inc", new BasicDBObject("cBalance", totalAmt))
						.append("$inc", new BasicDBObject("cDeliveryCnt", 1));
				database.getCollection("customer").updateOne(dcCustomerQuery, dcCustomerUpdate);
			}
		}*/
	}
}