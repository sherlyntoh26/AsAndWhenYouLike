

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCollection;

public class NewOrderTransaction {
	private MongoDatabase database;

	public NewOrderTransaction(){
		// public constructor
	}
	
	public NewOrderTransaction(Connection connection){
		database = connection.getDatabase();
	}
	
	public void newOrder(int wID, int dID, int cID, int numOfItems, int[] itemNumberArr, int[] sWarehouseID,
			int[] quantities) {
		// get d_next_o_id from given wid, did
				BasicDBObject dcDistrictQuery = new BasicDBObject().append("dWId", Integer.toString(wID)).append("dId", Integer.toString(dID));
				BasicDBObject dcDistrictProjection = new BasicDBObject().append("dNextOId", 1).append("dTax", 1);
				System.out.println("dWId:"+wID+" dId:"+dID);	
				MongoCollection<Document> coll = database.getCollection("district");
				Document dcDistrict = coll.find(dcDistrictQuery).projection(dcDistrictProjection).first();
				int dNextOId = Integer.parseInt((String) dcDistrict.get("dNextOId"));
				double dTax = Double.parseDouble((String) dcDistrict.get("dTax"));
				// update d_next_o_id by 1
				// $inc --> increase
				BasicDBObject dcDistrictUpdate = new BasicDBObject("$set", new BasicDBObject("dNextOId", Integer.toString((dNextOId+1))));
				//dcDistrictUpdate.put("dNextOId", dNextOId+1);
				coll.updateOne(dcDistrictQuery, dcDistrictUpdate);
				// create newOrder
				int allLocal = 1;
				double totalAmt = 0.0;
				ArrayList<String> arrayListOutput = new ArrayList<String>();
				ArrayList<Map<String, Object>> orderLineArr = new ArrayList<Map<String, Object>>();
				
				BasicDBObject dcInvProjection = new BasicDBObject().append("iQty", 1).append("iYtd", 1).append("iOrderCnt", 1).append("iRemoteCnt", 1).append("iPrice", 1).append("iName", 1);
				
				for (int i=0; i<numOfItems; i++){
					BasicDBObject dcInvQuery = new BasicDBObject().append("iWId", Integer.toString(wID)).append("iId", Integer.toString(itemNumberArr[i]));
					MongoCollection<Document> coll2 = database.getCollection("inventory");
					Document dcInv = coll2.find(dcInvQuery).projection(dcInvProjection).first();
					int invQty = Integer.parseInt((String) dcInv.get("iQty"));
					
					//double invYtd = (Double) dcInv.get("iYtd");
					//int invOrderCnt = (Integer) dcInv.get("iOrderCnt");
					int invRemoteCnt;
					try{
						invRemoteCnt = (Integer) dcInv.get("iRemoteCnt");
					}catch(ClassCastException e){
						invRemoteCnt = Integer.parseInt((String) dcInv.get("iRemoteCnt"));
					}	
					
					double invPrice = Double.parseDouble((String) dcInv.get("iPrice"));					
					String invName = (String) dcInv.get("iName");					
					double itemAmt = (quantities[i] * invPrice);
					totalAmt += itemAmt;
					
					int adjustedQty = invQty - quantities[i];
					if(adjustedQty < 10){
						adjustedQty +=100;
					}
					if(sWarehouseID[i] != wID){
						allLocal=0;
						invRemoteCnt +=1;
					}
					
					// update Inventory
					BasicDBObject dcInvUpdate = new BasicDBObject().append("$set", new BasicDBObject("iQty", adjustedQty)).append("$inc", new BasicDBObject("iYtd", quantities[i])).append("$inc", new BasicDBObject("invOrderCnt", 1)).append("$set", new BasicDBObject("iRemoteCnt", invRemoteCnt));
					coll2.updateOne(dcInvQuery, dcInvUpdate);
					// create document for 1 orderLine --> place it in array first
					Map<String, Object> itemMap = new HashMap<String, Object>();
					itemMap.put("num", i+1);
					itemMap.put("iId", itemNumberArr[i]);
					itemMap.put("amt", itemAmt);
					itemMap.put("supplyWId", sWarehouseID[i]);
					itemMap.put("qty", quantities[i]);
					itemMap.put("distInfo", "S_DIST_" + dID);
					itemMap.put("iName", invName);
					orderLineArr.add(itemMap);
					
					// prepare printing statement
					arrayListOutput.add(String.format("Item: %d | %s, Warehouse %d. Quantity: %d. Amount: %.2f, Stock: %d",
							itemNumberArr[i], invName, sWarehouseID[i], quantities[i], itemAmt, adjustedQty));
				}
				// insert into collection Order
				Timestamp orderDate = new Timestamp(System.currentTimeMillis());
				Map<String, Object> orderMap = new HashMap<String, Object>();
				orderMap.put("oWId", wID);
				orderMap.put("oDId", dID);
				orderMap.put("oId", dNextOId);
				orderMap.put("oCId", cID);
				orderMap.put("oCarrierId", 0);
				orderMap.put("oOlCnt", numOfItems);
				orderMap.put("oAllLocal", allLocal);
				orderMap.put("oEntryDate", orderDate);
				orderMap.put("oDeliveryDate", null);
				orderMap.put("oTotalAmt", totalAmt);
				orderMap.put("oOrderLine", orderLineArr);
				MongoCollection<Document> collOrder = database.getCollection("order");
				Document temp = new Document();
				temp.putAll(orderMap);
				collOrder.insertOne(temp);
				
				// get customer information
				BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", Integer.toString(wID)).append("cDId", Integer.toString(dID)).append("cId", Integer.toString(cID));
				BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName", 1).append("cCredit", 1).append("cDiscount", 1);
				MongoCollection<Document> coll3 = database.getCollection("customer");
				Document dcCustomer = coll3.find(dcCustomerQuery).projection(dcCustomerProjection).first();	
			
				double cDiscount = Double.parseDouble((String) dcCustomer.get("cDiscount"));

				// get warehouse information
				BasicDBObject dcWarehouseQuery = new BasicDBObject().append("wId", Integer.toString(wID));
				BasicDBObject dcWarehouseProjection = new BasicDBObject().append("wTax", 1);
				MongoCollection<Document> coll4 = database.getCollection("warehouse");
				Document dcWarehouse = coll4.find(dcWarehouseQuery).projection(dcWarehouseProjection).first();

				
				double wTax = Double.parseDouble((String) dcWarehouse.get("wTax"));

				totalAmt = totalAmt * (1 + dTax + wTax) * (1 - cDiscount);
				@SuppressWarnings("unchecked")
		    	List<String> nameList = (List<String>) dcCustomer.get("cName");  
				String cCredit = (String) dcCustomer.get("cCredit");
				System.out.println(String.format("Customer %s, %s, %.4f", nameList.get(2), cCredit, cDiscount));
				System.out.println(String.format("Warehouse Tax: %.4f, District tax: %.4f", wTax, dTax));
				System.out.println(String.format("Order Number: %d, Entry Date: %s", dNextOId, orderDate));
				System.out.println(String.format("Number of Item: %d, Total amount: %.2f", numOfItems, totalAmt));

				for (String str : arrayListOutput) {
					System.out.println(str);
				}		
		/*// get d_next_o_id from given wid, did
		BasicDBObject dcDistrictQuery = new BasicDBObject().append("dWId", wID).append("dId", dID);
		BasicDBObject dcDistrictProjection = new BasicDBObject().append("dNextOId", 1).append("dTax", 1);
		DBObject dcDistrict = (DBObject) database.getCollection("district").find(dcDistrictQuery).projection(dcDistrictProjection).first();
		int dNextOId = (Integer) dcDistrict.get("dNextOId");
		double dTax = (Double) dcDistrict.get("dTax");
		
		// update d_next_o_id by 1
		// $inc --> increase
		BasicDBObject dcDistrictUpdate = new BasicDBObject("$inc", new BasicDBObject("dNextOId", 1));
		//dcDistrictUpdate.put("dNextOId", dNextOId+1);
		database.getCollection("district").updateOne(dcDistrictQuery, dcDistrictUpdate);
		
		// create newOrder
		int allLocal = 1;
		double totalAmt = 0.0;
		ArrayList<String> arrayListOutput = new ArrayList<String>();
		ArrayList<Map<String, Object>> orderLineArr = new ArrayList<Map<String, Object>>();
		
		BasicDBObject dcInvProjection = new BasicDBObject().append("iQty", 1).append("iYtd", 1).append("iOrderCnt", 1).append("iRemoteCnt", 1).append("iPrice", 1).append("iName", 1);
		
		for (int i=0; i<numOfItems; i++){
			BasicDBObject dcInvQuery = new BasicDBObject().append("iWId", wID).append("iId", itemNumberArr[i]);
			DBObject dcInv = (DBObject) database.getCollection("inventory").find(dcInvQuery).projection(dcInvProjection).first();
			int invQty = (Integer) dcInv.get("iQty");
			//double invYtd = (Double) dcInv.get("iYtd");
			//int invOrderCnt = (Integer) dcInv.get("iOrderCnt");
			int invRemoteCnt = (Integer) dcInv.get("iRemoteCnt");
			double invPrice = (Double) dcInv.get("iPrice");
			String invName = dcInv.get("iName").toString();
			double itemAmt = (quantities[i] * invPrice);
			totalAmt += itemAmt;
			
			int adjustedQty = invQty - quantities[i];
			if(adjustedQty < 10){
				adjustedQty +=100;
			}
			if(sWarehouseID[i] != wID){
				allLocal=0;
				invRemoteCnt +=1;
			}
			
			// update Inventory
			BasicDBObject dcInvUpdate = new BasicDBObject().append("$set", new BasicDBObject("iQty", adjustedQty)).append("$inc", new BasicDBObject("iYtd", quantities[i])).append("$inc", new BasicDBObject("invOrderCnt", 1)).append("$set", new BasicDBObject("iRemoteCnt", invRemoteCnt));
			database.getCollection("inventory").updateOne(dcInvQuery, dcInvUpdate);
			
			// create document for 1 orderLine --> place it in array first
			Map<String, Object> itemMap = new HashMap<String, Object>();
			itemMap.put("num", i+1);
			itemMap.put("iId", itemNumberArr[i]);
			itemMap.put("amt", itemAmt);
			itemMap.put("supplyWId", sWarehouseID[i]);
			itemMap.put("qty", quantities[i]);
			itemMap.put("distInfo", "S_DIST_" + dID);
			itemMap.put("iName", invName);
			orderLineArr.add(itemMap);
			
			// prepare printing statement
			arrayListOutput.add(String.format("Item: %d | %s, Warehouse %d. Quantity: %d. Amount: %.2f, Stock: %d",
					itemNumberArr[i], invName, sWarehouseID[i], quantities[i], itemAmt, adjustedQty));
		}
		
		// insert into collection Order
		Timestamp orderDate = new Timestamp(System.currentTimeMillis());
		Map<String, Object> orderMap = new HashMap<String, Object>();
		orderMap.put("oWId", wID);
		orderMap.put("oDId", dID);
		orderMap.put("oId", dNextOId);
		orderMap.put("oCId", cID);
		orderMap.put("oCarrierId", 0);
		orderMap.put("oOlCnt", numOfItems);
		orderMap.put("oAllLocal", allLocal);
		orderMap.put("oEntryDate", orderDate);
		orderMap.put("oDeliveryDate", null);
		orderMap.put("oTotalAmt", totalAmt);
		orderMap.put("oOrderLine", orderLineArr);
		database.getCollection("order").insertOne((Document)orderMap);
		
		// get customer information
		BasicDBObject dcCustomerQuery = new BasicDBObject().append("cWId", wID).append("cDId", dID).append("cId", cID);
		BasicDBObject dcCustomerProjection = new BasicDBObject().append("cName.last", 1).append("credit", 1).append("cDiscount", 1);
		DBObject dcCustomer = (DBObject) database.getCollection("customer").find(dcCustomerQuery).projection(dcCustomerProjection).first();
		double cDiscount = (Double) dcCustomer.get("cDiscount");
		
		// get warehouse information
		BasicDBObject dcWarehouseQuery = new BasicDBObject().append("wId", wID);
		BasicDBObject dcWarehouseProjection = new BasicDBObject().append("wTax", 1);
		DBObject dcWarehouse = (DBObject) database.getCollection("warehouse").find(dcWarehouseQuery).projection(dcWarehouseProjection).first();
		double wTax = (Double) dcWarehouse.get("wTax");
		totalAmt = totalAmt * (1 + dTax + wTax) * (1 - cDiscount);
		
		System.out.println(String.format("Customer %s, %s, %.4f", dcCustomer.get("cName.last").toString(), dcCustomer.get("cCredit").toString(), cDiscount));
		System.out.println(String.format("Warehouse Tax: %.4f, District tax: %.4f", wTax, dTax));
		System.out.println(String.format("Order Number: %d, Entry Date: %s", dNextOId, orderDate));
		System.out.println(String.format("Number of Item: %d, Total amount: %.2f", numOfItems, totalAmt));

		for (String str : arrayListOutput) {
			System.out.println(str);
		}*/
	}
}
