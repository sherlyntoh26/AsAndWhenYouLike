
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;

public class PaymentTransaction {
	private MongoDatabase database;

	public PaymentTransaction(){
		// public constructor
	}

	public PaymentTransaction(Connection connection){
		database = connection.getDatabase();
	}

	@SuppressWarnings("deprecation")
	public void makePayment(int wID, int dID, int cID, double payment){
		System.out.println("wID"+wID+" ;dID"+dID+" ;cID"+cID+" ;payment"+payment);
		for(String s : database.listCollectionNames())
			System.out.println("payment: "+s);
		
		
		// update warehouse
		BasicDBObject dcWarehouseQuery = new BasicDBObject().append("wId", wID);
		//BasicDBObject dcWarehouseProjection = new BasicDBObject().append("wYtd", 1).append("wAdd", 1);
		BasicDBObject dcWarehouseProjection = new BasicDBObject().append("wAdd", 1);
		DBObject dcWarehouse = (DBObject) database.getCollection("warehouse").find(dcWarehouseQuery).projection(dcWarehouseProjection).first();
		//double wYtd = (Double) dcWarehouse.get("wYtd");
		BasicDBObject dcWarehouseUpdate = new BasicDBObject().append("$inc", new BasicDBObject("wYtd", payment));
		database.getCollection("warehouse").updateOne(dcWarehouseQuery, dcWarehouseUpdate);

		// update district
		BasicDBObject dcDistrictQuery = new BasicDBObject().append("dWId", wID).append("dId", dID);
		//BasicDBObject dcDistrictProjection = new BasicDBObject().append("dTax", 1).append("dAdd", 1);
		BasicDBObject dcDistrictProjection = new BasicDBObject().append("dAdd", 1);
		DBObject dcDistrict = (DBObject) database.getCollection("district").find(dcDistrictQuery).projection(dcDistrictProjection).first();
		//double dYtd = dcDistrict.getDouble("dTax");
		BasicDBObject dcDistrictUpdate = new BasicDBObject().append("$inc", new BasicDBObject("dYtd", payment));
		database.getCollection("district").updateOne(dcDistrictQuery, dcDistrictUpdate);

		// update customer
		BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", dID).append("cId", cID);
		//BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName", 1).append("cAdd", 1).append("cPhone", 1).append("cSince", 1).append("cCredit", 1).append("cCreditLimit", 1).append("cDiscount", 1).append("cBalance", 1).append("cYtdPayment", 1).append("cPaymentCnt", 1);
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName", 1).append("cAdd", 1).append("cPhone", 1).append("cSince", 1).append("cCredit", 1).append("cCreditLimit", 1).append("cDiscount", 1).append("cBalance", 1);
		DBObject dcCustomer = (DBObject) database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();		
		double cBalance = (Double) dcCustomer.get("cBalance");
		//double cYtd = (Double) dcCustomer.get("cYtdPayment");
		//int cPaymentCnt = (Integer) dcCustomer.get("cPaymentCnt");
		cBalance -= payment;
		//cYtd += payment;
		//cPaymentCnt += 1;
		BasicDBObject dcCustomerUpdate = new BasicDBObject().append("$set", new BasicDBObject("cBalance", cBalance)).append("$inc", new BasicDBObject("cYtd", payment)).append("$inc", new BasicDBObject("cPaymentCnt", 1));
		database.getCollection("customer").updateOne(dcCustomerQuery, dcCustomerUpdate);

		// output 
		System.out.println("Customer Information:");
		System.out.println(String.format("ID: (%d, %d, %d) | Name: (%s, %s, %s) | Address: (%s, %s, %s, %s, %s) | %s, %s, %s, %.2f, %.4f, %.2f", wID, dID, cID, dcCustomer.get("cName.first").toString(), dcCustomer.get("cName.middle").toString(), dcCustomer.get("cName.last").toString(), dcCustomer.get("cAdd.street1").toString(), dcCustomer.get("cAdd.street2").toString(), dcCustomer.get("cAdd.city").toString(), dcCustomer.get("cAdd.state").toString(), dcCustomer.get("cAdd.zip").toString(), dcCustomer.get("cPhone").toString(), dcCustomer.get("cSince").toString(), dcCustomer.get("cCredit").toString(), (Double) dcCustomer.get("cCreditLimit"), (Double) dcCustomer.get("cDiscount"), cBalance));
		System.out.println(String.format("Warehouse Address: (%s, %s, %s, %s, %s)", dcWarehouse.get("wAdd.street1").toString(), dcWarehouse.get("wAdd.street2").toString(), dcWarehouse.get("wAdd.city").toString(), dcWarehouse.get("wAdd.state").toString(), dcWarehouse.get("wAdd.zip").toString()));
		System.out.println(String.format("District Address: (%s, %s, %s, %s, %s)", dcDistrict.get("dAdd.street1").toString(), dcDistrict.get("dAdd.street2").toString(), dcDistrict.get("dAdd.city").toString(), dcDistrict.get("dAdd.state").toString(), dcDistrict.get("dAdd.zip").toString()));
		System.out.println(String.format("Payment Amount: %.2f", payment));
	}

}
