package mongodb;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

public class PaymentTransaction {
	private MongoDatabase database;

	public PaymentTransaction(){
		// public constructor
	}
	
	public PaymentTransaction(Connection connection){
		database = connection.getDatabase();
	}
	
	public void makePayment(int wID, int dID, int cID, double payment){
		
		// update warehouse
		Document dcWarehouseQuery = new Document();
		dcWarehouseQuery.put("wId", wID);
		Document dcWarehouseProjection = new Document();
		dcWarehouseProjection.put("wYtd", 1);
		Document dcWarehouse = database.getCollection("warehouse").find(dcWarehouseQuery).projection(dcWarehouseProjection).first();
		double wYtd = dcWarehouse.getDouble("wYtd");
		Document dcWarehouseUpdate = new Document();
		dcWarehouseUpdate.put("wYtd", wYtd + payment);
		database.getCollection("warehouse").updateOne(dcWarehouseQuery, dcWarehouseUpdate);
		
		// update district
		Document dcDistrictQuery = new Document();
		dcDistrictQuery.put("dWId", wID);
		dcDistrictQuery.put("dId", dID);
		Document dcDistrictProjection = new Document();
		dcDistrictProjection.put("dTax", 1);
		Document dcDistrict = database.getCollection("district").find(dcDistrictQuery).projection(dcDistrictProjection).first();
		double dYtd = dcDistrict.getDouble("dTax");
		Document dcDistrictUpdate = new Document();
		dcDistrictUpdate.put("dYtd", dYtd + payment);
		database.getCollection("district").updateOne(dcDistrictQuery, dcDistrictUpdate);
	}
	
}
