import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.List;

public class PaymentTransaction {
	private MongoDatabase database;

	public PaymentTransaction() {
		// public constructor
	}

	public PaymentTransaction(Connection connection) {
		database = connection.getDatabase();
	}

	// @SuppressWarnings("deprecation")
	public void makePayment(int wID, int dID, int cID, double payment) {
		System.out.println("wID:"+wID+" dID:"+dID+" cID:"+cID+ " payment:"+payment);
		// update warehouse
		BasicDBObject dcWarehouseQuery = new BasicDBObject().append("wId",Integer.toString(wID));
		// BasicDBObject dcWarehouseProjection = new
		// BasicDBObject().append("wYtd", 1).append("wAdd", 1);
		BasicDBObject dcWarehouseProjection = new BasicDBObject().append(
				"wAdd", 1).append("wYtd", 1);
		MongoCollection<Document> coll = database.getCollection("warehouse");
		Document dcWarehouse = coll.find(dcWarehouseQuery).projection(dcWarehouseProjection).first();
		
		Double wYtd = Double.parseDouble((String) dcWarehouse.get("wYtd"));
		
		BasicDBObject dcWarehouseUpdate = new BasicDBObject().append("$set",
				new BasicDBObject("wYtd", Double.toString((wYtd+payment))));
		coll.updateOne(dcWarehouseQuery, dcWarehouseUpdate);

		// update district
		BasicDBObject dcDistrictQuery = new BasicDBObject().append("dWId",
				Integer.toString(wID)).append("dId", Integer.toString(dID));
		// BasicDBObject dcDistrictProjection = new
		// BasicDBObject().append("dTax", 1).append("dAdd", 1);
		BasicDBObject dcDistrictProjection = new BasicDBObject().append("dAdd",
				1).append("dTax", 1);
		MongoCollection<Document> coll2 = database.getCollection("district");
		Document dcDistrict = coll2.find(dcDistrictQuery)
				.projection(dcDistrictProjection).first();
		double dYtd = Double.parseDouble((String) dcDistrict.get("dTax"));
		BasicDBObject dcDistrictUpdate = new BasicDBObject().append("$set",
				new BasicDBObject("dYtd", Double.toString(dYtd+payment)));
		coll2.updateOne(dcDistrictQuery, dcDistrictUpdate);

		// update customer
		BasicDBObject dcCustomerQuery = new BasicDBObject()
				.append("cWId", Integer.toString(wID))
				.append("cDId", Integer.toString(dID))
				.append("cId", Integer.toString(cID));
		// BasicDBObject dcCustomerProjection = new
		// BasicDBObject().append("cName", 1).append("cAdd", 1).append("cPhone",
		// 1).append("cSince", 1).append("cCredit", 1).append("cCreditLimit",
		// 1).append("cDiscount", 1).append("cBalance", 1).append("cYtdPayment",
		// 1).append("cPaymentCnt", 1);
		BasicDBObject dcCustomerProjection = new BasicDBObject()
				.append("cName", 1).append("cAdd", 1).append("cPhone", 1)
				.append("cSince", 1).append("cCredit", 1)
				.append("cCreditLimit", 1).append("cDiscount", 1)
				.append("cBalance", 1).append("cYtdPayment", 1).append("cPaymentCnt", 1);
		MongoCollection<Document> coll3 = database.getCollection("customer");
		Document dcCustomer = coll3.find(dcCustomerQuery)
				.projection(dcCustomerProjection).first();
		double cBalance = Double.parseDouble((String) dcCustomer
				.get("cBalance"));
		double cYtd = Double.parseDouble((String) dcCustomer.get("cYtdPayment"));
		int cPaymentCnt = Integer.parseInt((String) dcCustomer.get("cPaymentCnt"));
		cBalance -= payment;
		// cYtd += payment;
		// cPaymentCnt += 1;
		BasicDBObject dcCustomerUpdate = new BasicDBObject()
				.append("$set", new BasicDBObject("cBalance", cBalance))
				.append("$set", new BasicDBObject("cYtd", Double.toString(cYtd+payment)))
				.append("$set", new BasicDBObject("cPaymentCnt", Integer.toString(cPaymentCnt+1)));
		coll3.updateOne(dcCustomerQuery, dcCustomerUpdate);

		// output
		@SuppressWarnings("unchecked")
		List<String> nameList = (List<String>) dcCustomer.get("cName");
		@SuppressWarnings("unchecked")
		List<String> addList = (List<String>) dcCustomer.get("cAdd");
		System.out.println("Customer Information:");
		System.out
				.println(String
						.format("ID: (%d, %d, %d) | Name: (%s, %s, %s) | Address: (%s, %s, %s, %s, %s) | %s, %s, %s, %.2f, %.4f, %.2f",
								wID, dID, cID, nameList.get(0),
								nameList.get(1), nameList.get(2),
								addList.get(0), addList.get(1), addList.get(2),
								addList.get(3), addList.get(4),
								(String) dcCustomer.get("cPhone"),
								(String) dcCustomer.get("cSince"),
								(String) dcCustomer.get("cCredit"),
								Double.parseDouble((String) dcCustomer.get("cCreditLimit")),
								Double.parseDouble((String) dcCustomer.get("cDiscount")), cBalance));
		@SuppressWarnings("unchecked")
		List<String> waddList = (List<String>) dcWarehouse.get("wAdd");
		System.out.println(String.format(
				"Warehouse Address: (%s, %s, %s, %s, %s)", waddList.get(0),
				waddList.get(1), waddList.get(2), waddList.get(3),
				waddList.get(4)));
		@SuppressWarnings("unchecked")
		List<String> daddList = (List<String>) dcDistrict.get("dAdd");
		System.out.println(String.format(
				"District Address: (%s, %s, %s, %s, %s)", daddList.get(0),
				daddList.get(1), daddList.get(2), daddList.get(3),
				daddList.get(4)));
		System.out.println(String.format("Payment Amount: %.2f", payment));

		/*
		 * System.out.println("wID"+wID+" ;dID"+dID+" ;cID"+cID+" ;payment"+payment
		 * ); for(String s : database.listCollectionNames())
		 * System.out.println("payment: "+s);
		 */

		/*
		 * // update warehouse BasicDBObject dcWarehouseQuery = new
		 * BasicDBObject().append("wId", wID); //BasicDBObject
		 * dcWarehouseProjection = new BasicDBObject().append("wYtd",
		 * 1).append("wAdd", 1); BasicDBObject dcWarehouseProjection = new
		 * BasicDBObject().append("wAdd", 1); DBObject dcWarehouse = (DBObject)
		 * database
		 * .getCollection("warehouse").find(dcWarehouseQuery).projection(
		 * dcWarehouseProjection).first(); //double wYtd = (Double)
		 * dcWarehouse.get("wYtd"); BasicDBObject dcWarehouseUpdate = new
		 * BasicDBObject().append("$inc", new BasicDBObject("wYtd", payment));
		 * database.getCollection("warehouse").updateOne(dcWarehouseQuery,
		 * dcWarehouseUpdate);
		 * 
		 * // update district BasicDBObject dcDistrictQuery = new
		 * BasicDBObject().append("dWId", wID).append("dId", dID);
		 * //BasicDBObject dcDistrictProjection = new
		 * BasicDBObject().append("dTax", 1).append("dAdd", 1); BasicDBObject
		 * dcDistrictProjection = new BasicDBObject().append("dAdd", 1);
		 * DBObject dcDistrict = (DBObject)
		 * database.getCollection("district").find
		 * (dcDistrictQuery).projection(dcDistrictProjection).first(); //double
		 * dYtd = dcDistrict.getDouble("dTax"); BasicDBObject dcDistrictUpdate =
		 * new BasicDBObject().append("$inc", new BasicDBObject("dYtd",
		 * payment));
		 * database.getCollection("district").updateOne(dcDistrictQuery,
		 * dcDistrictUpdate);
		 * 
		 * // update customer BasicDBObject dcCustomerQuery = new
		 * BasicDBObject().append("cWId", wID).append("cDId", dID).append("cId",
		 * cID); //BasicDBObject dcCustomerProjection = new
		 * BasicDBObject().append("cName", 1).append("cAdd", 1).append("cPhone",
		 * 1).append("cSince", 1).append("cCredit", 1).append("cCreditLimit",
		 * 1).append("cDiscount", 1).append("cBalance", 1).append("cYtdPayment",
		 * 1).append("cPaymentCnt", 1); BasicDBObject dcCustomerProjection = new
		 * BasicDBObject().append("cName", 1).append("cAdd", 1).append("cPhone",
		 * 1).append("cSince", 1).append("cCredit", 1).append("cCreditLimit",
		 * 1).append("cDiscount", 1).append("cBalance", 1); DBObject dcCustomer
		 * = (DBObject)
		 * database.getCollection("customer").find(dcCustomerQuery).
		 * projection(dcCustomerProjection).first(); double cBalance = (Double)
		 * dcCustomer.get("cBalance"); //double cYtd = (Double)
		 * dcCustomer.get("cYtdPayment"); //int cPaymentCnt = (Integer)
		 * dcCustomer.get("cPaymentCnt"); cBalance -= payment; //cYtd +=
		 * payment; //cPaymentCnt += 1; BasicDBObject dcCustomerUpdate = new
		 * BasicDBObject().append("$set", new BasicDBObject("cBalance",
		 * cBalance)).append("$inc", new BasicDBObject("cYtd",
		 * payment)).append("$inc", new BasicDBObject("cPaymentCnt", 1));
		 * database.getCollection("customer").updateOne(dcCustomerQuery,
		 * dcCustomerUpdate);
		 * 
		 * // output System.out.println("Customer Information:");
		 * System.out.println(String.format(
		 * "ID: (%d, %d, %d) | Name: (%s, %s, %s) | Address: (%s, %s, %s, %s, %s) | %s, %s, %s, %.2f, %.4f, %.2f"
		 * , wID, dID, cID, dcCustomer.get("cName.first").toString(),
		 * dcCustomer.get("cName.middle").toString(),
		 * dcCustomer.get("cName.last").toString(),
		 * dcCustomer.get("cAdd.street1").toString(),
		 * dcCustomer.get("cAdd.street2").toString(),
		 * dcCustomer.get("cAdd.city").toString(),
		 * dcCustomer.get("cAdd.state").toString(),
		 * dcCustomer.get("cAdd.zip").toString(),
		 * dcCustomer.get("cPhone").toString(),
		 * dcCustomer.get("cSince").toString(),
		 * dcCustomer.get("cCredit").toString(), (Double)
		 * dcCustomer.get("cCreditLimit"), (Double) dcCustomer.get("cDiscount"),
		 * cBalance)); System.out.println(String.format(
		 * "Warehouse Address: (%s, %s, %s, %s, %s)",
		 * dcWarehouse.get("wAdd.street1").toString(),
		 * dcWarehouse.get("wAdd.street2").toString(),
		 * dcWarehouse.get("wAdd.city").toString(),
		 * dcWarehouse.get("wAdd.state").toString(),
		 * dcWarehouse.get("wAdd.zip").toString()));
		 * System.out.println(String.format
		 * ("District Address: (%s, %s, %s, %s, %s)",
		 * dcDistrict.get("dAdd.street1").toString(),
		 * dcDistrict.get("dAdd.street2").toString(),
		 * dcDistrict.get("dAdd.city").toString(),
		 * dcDistrict.get("dAdd.state").toString(),
		 * dcDistrict.get("dAdd.zip").toString()));
		 * System.out.println(String.format("Payment Amount: %.2f", payment));
		 */
	}

}
