
import org.bson.Document;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import java.util.List;

public class StockLevelTransaction {
	private MongoDatabase database;

	public StockLevelTransaction() {
		// public constructor
	}

	public StockLevelTransaction(Connection connection) {
		database = connection.getDatabase();
	}

	public void stockLevel(int wID, int dID, int T, int L) {
		int count = 0;
		// can sort --> don't need to get D_NEXT_O_ID
		BasicDBObject dcOrderSort = new BasicDBObject().append("oId", -1);
		BasicDBObject dcOrderQuery = new BasicDBObject().append("oWId",
				Integer.toString(wID)).append("oDId", Integer.toString(dID));
		BasicDBObject dcOrderProjection = new BasicDBObject().append(
				"oOrderLine", 1);
		// MongoCollection<Document> coll = database.getCollection("order");
		// Document dcOrdersCursor =
		// coll.find(dcOrderQuery).projection(dcOrderProjection).sort(dcOrderSort).limit(L);

		FindIterable<Document> dcOrdersCursor = database.getCollection("order")
				.find(dcOrderQuery).projection(dcOrderProjection)
				.sort(dcOrderSort).limit(L);
		for (Document doc : dcOrdersCursor) {
			@SuppressWarnings("unchecked")
			List<Document> orderLineList = (List<Document>) doc
					.get("oOrderLine");
			for (int i = 0; i < orderLineList.size(); i++) {
				Document orderLine = orderLineList.get(i);
				int itemNo = Integer.parseInt((String) orderLine.get("iId"));

				BasicDBObject dcInventoryQuery = new BasicDBObject().append(
						"iWId", Integer.toString(wID)).append("iId",
						Integer.toString(itemNo));
				BasicDBObject dcInventoryProjection = new BasicDBObject()
						.append("iQty", 1);
				Document dcInventory = database.getCollection("inventory")
						.find(dcInventoryQuery)
						.projection(dcInventoryProjection).first();
				int iQty = Integer.parseInt((String) dcInventory.get("iQty"));
				if (iQty < T) {
					count++;
				}
			}
		}

		System.out
				.println("Number of items whose quantity is less than the threshold is : "
						+ count);

		/*
		 * // can sort --> don't need to get D_NEXT_O_ID BasicDBObject
		 * dcOrderSort = new BasicDBObject().append("oId", -1); BasicDBObject
		 * dcOrderQuery = new BasicDBObject().append("oWId", wID).append("oDId",
		 * dID); BasicDBObject dcOrderProjection = new
		 * BasicDBObject().append("oOrderLine", 1); FindIterable<Document>
		 * dcOrdersCursor =
		 * database.getCollection("order").find(dcOrderQuery).projection
		 * (dcOrderProjection).sort(dcOrderSort).limit(L); for (Document doc :
		 * dcOrdersCursor) {
		 * 
		 * BasicDBList orderLineList = (BasicDBList) doc.get("oOrderLine");
		 * for(int i=0; i<orderLineList.size(); i++){ BasicDBObject orderLine =
		 * (BasicDBObject) orderLineList.get(i); int itemNo =
		 * orderLine.getInt("iId");
		 * 
		 * BasicDBObject dcInventoryQuery = new BasicDBObject().append("iWId",
		 * wID).append("iId", itemNo); BasicDBObject dcInventoryProjection = new
		 * BasicDBObject().append("iQty", 1); DBObject dcInventory = (DBObject)
		 * database
		 * .getCollection("inventory").find(dcInventoryQuery).projection(
		 * dcInventoryProjection).first(); int iQty = (Integer)
		 * dcInventory.get("iQty"); if(iQty < T){ count++; } } }
		 * 
		 * System.out.println(
		 * "Number of items whose quantity is less than the threshold is : " +
		 * count);
		 */}
}
