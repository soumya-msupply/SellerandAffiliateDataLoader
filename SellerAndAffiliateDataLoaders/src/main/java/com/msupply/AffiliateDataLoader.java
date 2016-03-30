package com.msupply;

import static com.msupply.LoaderUtil.getBoolean;
import static com.msupply.LoaderUtil.getNumericCellValue;
import static com.msupply.LoaderUtil.getTextCellValue;
import static com.msupply.LoaderUtil.isValid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class AffiliateDataLoader {

	private static XSSFSheet dataSheet;
	private static MongoClient mongoClient;
	private static MongoDatabase db;
	
	private static String env = "prd";
	

	// private static boolean runLoaderForDataUpsert = false;
	private static boolean runLoaderForDataLoad = true;
	private static boolean runLoaderForImageUpdate = false;

	public static void main(String[] args) {

		// Creating mongo connection
		// MongoClient mongoClient = new MongoClient();
		// "52.30.181.28"
//		mongoClient = new MongoClient();
//		db = mongoClient.getDatabase("msupplyDB");
		
		
		/*
		 * create a DB Connection.
		 */
		
		HashMap<String, Object> connData = DBConnector.getMongoConnData(env);
		
		@SuppressWarnings("unchecked")
		List<ServerAddress> seeds =  (List<ServerAddress>)connData.get("seeds");
		
		@SuppressWarnings("unchecked")
		List<MongoCredential> credentials = (List<MongoCredential>)connData.get("credentials");
		
		String DB = (String)connData.get("DB");
		
		if (credentials.size()>0){
			mongoClient = new MongoClient(seeds,credentials);
		} else {
			mongoClient = new MongoClient(seeds);
		}
		db = mongoClient.getDatabase(DB);
		
		

		// Getting timestamp for report file
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String timestamp = sdf.format(calendar.getTime());

		try {
			// Creating report file
			File file = new File("/home/soumyabardhan/work/loaders/Affiliate/demo/Affiliate_Data_Upload_Report_" + timestamp + ".txt");
			FileOutputStream fos = new FileOutputStream(file);
			TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(myOut);
			System.setOut(ps);

			/*
			 * Data file of upload
			 */
			File inputFile = new File("/home/soumyabardhan/work/loaders/Affiliate/demo/Affiliate Data Collection Sheet.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(inputFile));
			dataSheet = wb.getSheet("Data"); /* wb.getSheetAt(2); */
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (runLoaderForDataLoad) {
			dataLoad();
		} else if (runLoaderForImageUpdate) {
			imageLoad();
		}

	}

	private static void imageLoad() {
		
		String map = "function(){var customerID = this.serviceProviderEntity.profileInfo.accountInfo.customerId;"
				+ "var profileURL;"
				+ "if(this.serviceProviderEntity.profileInfo.basicInfo === undefined){"
				+ "profileURL = '';"
				+ "} else {"
				+ "profileURL = this.serviceProviderEntity.profileInfo.basicInfo.photoURL;"
				+ "}"
				+ "var projects = this.serviceProviderEntity.projectsInfo;"
				+ "for(i in projects){"
				+ "if(projects[i].imageURL == ''){"
				+ "emit(customerID, {'profileURL':profileURL, 'project': projects[i].name});"
				+ "}"
				+ "}"
				+ "}";
		
		
		String reduce = "function(keyCustId, values){"
				+ "var returnObj = {'customerID': keyCustId, 'profileURL':values[0].profileURL, projects:[]};"
				+ "for(i in values){"
				+ "returnObj.projects.push(values[i].project);"
				+ "}"
				+ "return returnObj;"
				+ "}";
		
//		System.out.println("Map : " + map);
//		System.out.println("Reduce : " + reduce);
		
		// Used for storing projects with no images
		final Map<Integer, ProjectImageDetails> DBprojects = new HashMap<>();
		
		MapReduceIterable<Document> iterable = db.getCollection("ServiceProvider").mapReduce(map, reduce);
		
		iterable.forEach(new Block<Document>() {
		@Override
		public void apply(final Document document) {
//			System.out.println(document);
//			Document project = (Document) document.get("_id");
//			
//				List<String> projectList = asList(document.getString("project").split(","));
			ProjectImageDetails details = new ProjectImageDetails(document);
			DBprojects.put(details.getCustomerID(), details );
		}
	});
		
		
		Iterator<Row> rowIterator = dataSheet.rowIterator();
		int rowCounter = 0;

		// MongoDB document for data updation
		while (rowIterator.hasNext()) {
			Row row = (Row) rowIterator.next();
			if (rowCounter < 4) {
				rowCounter++;
				continue;
			}

			if (isValid(row.getCell(1)) && isValid(row.getCell(14))) {
				Integer customerId = getNumericCellValue(row.getCell(1));
				
				Map<String, String> projectImages = getProjectsImage(row);
				
				for (String project : projectImages.keySet()) {
					if (DBprojects.containsKey(customerId) && DBprojects.get(customerId).getProjects().contains(project)) {
						
						// System.out.println(document.toJson());
						UpdateResult result = db.getCollection("ServiceProvider")
								.updateOne(new Document("serviceProviderEntity.profileInfo.accountInfo.customerId",	customerId)
										.append("serviceProviderEntity.projectsInfo.name",project)
										, new Document("$set", new Document("serviceProviderEntity.projectsInfo.$.imageURL",projectImages.get(project))));

						if (result.getMatchedCount() == 0) {
							System.out.println("Skipped row : " + (row.getRowNum() + 1)
									+ " as no match found for Affiliate ID / Customer ID");
						} else if (result.getModifiedCount() == 1) {
							System.out.println("Image updated successfully for project " + project + " for customer ID : " + customerId);
						}
					} else {
						System.out.println("Skipped image for project " + project + " as image already exixts in DB or no match found for customer ID : " + customerId);
					}
				}
				
				if (DBprojects.containsKey(customerId)) {
					if (DBprojects.get(customerId).getProfileImage().equalsIgnoreCase("")) {
						
						UpdateResult result = db.getCollection("ServiceProvider")
								.updateOne(new Document("serviceProviderEntity.basicInfo.accountInfo.customerId",	customerId)										
										, new Document("$set", new Document("serviceProviderEntity.basicInfo.photoURL",getTextCellValue(row.getCell(11)))));

						if (result.getMatchedCount() == 0) {
							System.out.println("Skipped row : " + (row.getRowNum() + 1)
									+ " as no match found for Affiliate ID / Customer ID");
						} else if (result.getModifiedCount() == 1) {
							System.out.println("Profile image updated successfully for customer ID : " + customerId);
						}
						
					} else {
						System.out.println("Profile image skipped for customer ID : " + customerId + " as image already present in DB.");
					}
				}

				
			} else {
				System.out.println("Skipped row : " + (row.getRowNum() + 1)
						+ ". Either blank or missing mandatory field PAN number.");
			}

		}
}

	private static void dataLoad() {

		// Used for maintaining customer ids of all legitimate docs
			List<Integer> legitimateDocuments = new ArrayList<>();

			FindIterable<Document> legitimateDocs = db.getCollection("ServiceProvider")
					.find(new Document("serviceProviderEntity.profileInfo.accountInfo.PAN", null)
							.append("serviceProviderEntity.profileInfo.accountInfo.AadharNumber", null))
					.projection(new Document("serviceProviderEntity.profileInfo.accountInfo.customerId", 1)
							.append("_id", 0));
			
			System.out.println("Legitimate Docs");

			for (Document document : legitimateDocs) {
				System.out.println(document.toJson());
				legitimateDocuments
						.add((Integer) ((Document) ((Document) ((Document) document.get("serviceProviderEntity"))
								.get("profileInfo")).get("accountInfo")).get("customerId")

				);
			}

			Iterator<Row> rowIterator = dataSheet.rowIterator();
			int rowCounter = 0;

			// MongoDB document for data updation
			Document document = null;
			while (rowIterator.hasNext()) {
				Row row = (Row) rowIterator.next();
				if (rowCounter < 4) {
					rowCounter++;
					continue;
				}

				if (isValid(row.getCell(1)) && ( isValid(row.getCell(14)) || isValid(row.getCell(16)) ) ) {
					if ( !legitimateDocuments.contains(getNumericCellValue(row.getCell(1))) ) {
						System.out.println("Skipped row : " + (row.getRowNum() + 1) + ". Row data already exists in DB or no match found for Affiliate ID / Customer ID. : " + getNumericCellValue(row.getCell(1)));
						continue;
					}

					// Updating account info
					document = new Document("serviceProviderEntity.profileInfo.accountInfo.serviceTaxNumber",
							getTextCellValue(row.getCell(15)))
									.append("serviceProviderEntity.profileInfo.accountInfo.PAN",
											getTextCellValue(row.getCell(14)))
									.append("serviceProviderEntity.profileInfo.accountInfo.TIN",
											getTextCellValue(row.getCell(17)))
									.append("serviceProviderEntity.profileInfo.accountInfo.AadharNumber",
											getTextCellValue(row.getCell(16)))
									.append("serviceProviderEntity.profileInfo.accountInfo.verificationStatus",
											getTextCellValue(row.getCell(7)))
									.append("serviceProviderEntity.profileInfo.accountInfo.paymentStatus",
											getTextCellValue(row.getCell(6)))

									// Updating basic info
									.append("serviceProviderEntity.profileInfo.basicInfo",
											new Document().append("photoURL", getTextCellValue(row.getCell(11)))
													.append("proprietorFirstName", getTextCellValue(row.getCell(8)))
													.append("proprietorLastName", getTextCellValue(row.getCell(9)))
													.append("company", getTextCellValue(row.getCell(10)))
													.append("typeOfFirm", getTextCellValue(row.getCell(13)))
													.append("establishment", getDateFromText(row.getCell(12)))
													.append("manPower",
															getNumericCellValue(
																	row.getCell(36)))
													.append("contactPerson",
															new Document()
																	.append("Name", getTextCellValue(row.getCell(18)))
																	.append("designation",
																			getTextCellValue(row.getCell(19)))
													.append("phoneNumber", getTextCellValue(row.getCell(20)))
													.append("email", getTextCellValue(row.getCell(21))))
											.append("customerDetails", getCustomerDetails(row))
											.append("website", getWebsite(row)).append("email", getEmail(row))
											.append("telephone", getTelephone(row)).append("mobile", getMobile(row))
											.append("expertise", getExpertise(row))
											.append("billingSameAsOfficialAddress", getBoolean(row.getCell(96)))
											.append("address", getAddresses(row)))

									// Updating businnes info
									.append("serviceProviderEntity.profileInfo.businessInfo",
											new Document().append("operatingHours", getOperatingHours(row))
													.append("contractSize", getContracts(row))
													.append("paymentModes", getPaymentModes(row))
													.append("serviceAreas", getServiceAreas(row))
													.append("natureOfBusiness", getNatureOfBusiness(row))
													.append("noOfProjectsCompleted",
															getNumericCellValue(row.getCell(53)))
											.append("noOfBranches", getNumericCellValue(row.getCell(115)))
											.append("legalApproval", getBoolean(row.getCell(43)))
											.append("doesRenovation", getTextCellValue(row.getCell(679)))
											.append("branchList", getBranchList(row))
											.append("consultationCharges", getConsultationCharges(row))
											.append("maxProjectValue", getMaxProjectValue(row))
											.append("minProjectValue", getMinProjectValue(row))
											.append("visitingCharges", getVisitingCharges(row))
											.append("materialsAndLabourInfo", getTextCellValue(row.getCell(52))))
									.append("serviceProviderEntity.projectsInfo", getProjects(row));

					
					 System.out.println("\n" + document.toJson());
					// ******************************************************** Comment from here ******************************************************************
					
//					UpdateResult result = db.getCollection("ServiceProvider")
//							.updateOne(new Document("serviceProviderEntity.profileInfo.accountInfo.customerId",
//									getNumericCellValue(row.getCell(1))), new Document("$set", document));
//
//					if (result.getMatchedCount() == 0) {
//						System.out.println("Skipped row : " + (row.getRowNum() + 1)
//								+ " as no match found for Affiliate ID / Customer ID");
//					} else if (result.getModifiedCount() == 1) {
//						System.out.println("Row : " + (row.getRowNum() + 1) + " updated successfully");
//					}
					
					// ******************************************************* Comment till here *********************************************************************
				} else {
					System.out.println("Skipped row : " + (row.getRowNum() + 1)
							+ ". Either blank or missing mandatory field PAN/Aadhar number.");
				}

			}
	}

	private static List<Document> getCustomerDetails(Row row) {
		int CUST_FIELDS = 6;
		int START_INDEX = 180;
		List<Document> customerDetailsList = new ArrayList<Document>();

		for (int i = 0; i < 4; i++) {
			if (!isValid(row.getCell(START_INDEX + CUST_FIELDS * i))) {
				continue;
			}
			customerDetailsList.add(new Document()
					.append("contactPerson", getTextCellValue(row.getCell(START_INDEX + CUST_FIELDS * i)))
					.append("designation", getTextCellValue(row.getCell(START_INDEX + 1 + CUST_FIELDS * i)))
					.append("phoneNumber", getTextCellValue(row.getCell(START_INDEX + 2 + CUST_FIELDS * i)))
					.append("emailId", getTextCellValue(row.getCell(START_INDEX + 3 + CUST_FIELDS * i)))
					.append("projectDone", getTextCellValue(row.getCell(START_INDEX + 4 + CUST_FIELDS * i)))
					.append("dateCompleted", getDateFromText((row.getCell(START_INDEX + 5 + CUST_FIELDS * i)))));
		}
		return customerDetailsList;
	}

	private static List<String> getWebsite(Row row) {
		List<String> websiteList = new ArrayList<String>();
		int START_INDEX = 24;
		for (int i = 0; i < 2; i++) {
			String website = getTextCellValue(row.getCell(START_INDEX + 1 * i));
			if (website.length() > 0) {
				websiteList.add(website);
			}
		}
		return websiteList;
	}

	private static List<String> getEmail(Row row) {
		List<String> emilList = new ArrayList<String>();
		int START_INDEX = 22;
		for (int i = 0; i < 2; i++) {
			String email = getTextCellValue(row.getCell(START_INDEX + 1 * i));
			if (email.length() > 0) {
				emilList.add(email);
			}
		}
		return emilList;
	}

	private static List<String> getTelephone(Row row) {
		int START_INDEX = 26;
		List<String> telephoneList = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			String tel = getTextCellValue(row.getCell(START_INDEX + 1 * i));
			if (tel.length() > 0) {
				telephoneList.add(tel);
			}
		}
		return telephoneList;
	}

	private static List<String> getMobile(Row row) {
		int START_INDEX = 31;
		List<String> mobileList = new ArrayList<String>();
		for (int i = 0; i < 5; i++) {
			String mob = getTextCellValue(row.getCell(START_INDEX + 1 * i));
			if (mob.length() > 0) {
				mobileList.add(mob);
			}
		}
		return mobileList;
	}

	private static List<Document> getExpertise(Row row) {
		int EXPERT_FIELDS = 2;
		int START_INDEX = 140;
		List<Document> expertiseList = new ArrayList<Document>();

		for (int i = 0; i < 20; i++) {
			if (!isValid(row.getCell(START_INDEX + EXPERT_FIELDS * i))) {
				continue;
			}
			expertiseList
					.add(new Document().append("type", getTextCellValue(row.getCell(START_INDEX + EXPERT_FIELDS * i)))
							.append("description", getTextCellValue(row.getCell(START_INDEX + 1 + EXPERT_FIELDS * i))));
		}
		return expertiseList;
	}

	private static List<Document> getAddresses(Row row) {
		int ADDRESS_FIELDS = 6;
		int START_INDEX = 97;
		List<Document> addressList = new ArrayList<Document>() {
			private static final long serialVersionUID = 1L;

			{
				add(new Document().append("type", "OFFICIAL"));
				add(new Document().append("type", "BILLING"));
				add(new Document().append("type", "PERSONAL"));
			}
		};

		for (int i = 0; i < 3; i++) {
			if (!isValid(row.getCell(START_INDEX + ADDRESS_FIELDS * i))) {
				continue;
			}
			addressList.get(i).append("address1", getTextCellValue(row.getCell(START_INDEX + ADDRESS_FIELDS * i)))
					.append("address2", getTextCellValue(row.getCell(START_INDEX + 1 + ADDRESS_FIELDS * i)))
					.append("address3", getTextCellValue(row.getCell(START_INDEX + 2 + ADDRESS_FIELDS * i)))
					.append("city", getTextCellValue(row.getCell(START_INDEX + 3 + ADDRESS_FIELDS * i)))
					.append("state", getTextCellValue(row.getCell(START_INDEX + 4 + ADDRESS_FIELDS * i)))
					// .append("country",getTextCellValue(row.getCell(START_INDEX
					// + 5 + ADDRESS_FIELDS * i)))
					.append("country", "India")
					.append("pincode", getTextCellValue(row.getCell(START_INDEX + 5 + ADDRESS_FIELDS * i)));
		}
		return addressList;
	}

	private static List<Document> getOperatingHours(Row row) {
		int OPERATING_FIELDS = 4;
		int START_INDEX = 54;
		List<Document> operationsList = new ArrayList<Document>() {
			private static final long serialVersionUID = 1L;

			{
				add(new Document().append("day", "Monday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Tuesday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Wednesday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Thursday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Friday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Saturday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
				add(new Document().append("day", "Sunday").append("isHoliday", null).append("start", "")
						.append("close", "").append("remarks", ""));
			}
		};

		for (int i = 0; i < 7; i++) {
			if (!isValid(row.getCell(START_INDEX + OPERATING_FIELDS * i))) {
				continue;
			}

			if (!getBoolean(row.getCell(START_INDEX + OPERATING_FIELDS * i))) {
				operationsList.get(i)
						.append("start", getTextCellValue(row.getCell(START_INDEX + 1 + OPERATING_FIELDS * i)))
						.append("close", getTextCellValue(row.getCell(START_INDEX + 2 + OPERATING_FIELDS * i)))
						.append("remarks", getTextCellValue(row.getCell(START_INDEX + 3 + OPERATING_FIELDS * i)));
			}
			operationsList.get(i).append("isHoliday", getBoolean(row.getCell(START_INDEX + OPERATING_FIELDS * i)));
		}
		return operationsList;
	}

	private static List<Document> getContracts(Row row) {
		int CONTRACT_FIELDS = 2;
		int START_INDEX = 37;
		List<Document> contractList = new ArrayList<Document>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				add(new Document().append("type", "small").append("isActive", null).append("description", ""));
				add(new Document().append("type", "medium").append("isActive", null).append("description", ""));
				add(new Document().append("type", "large").append("isActive", null).append("description", ""));

			}

		};

		for (int i = 0; i < 3; i++) {
			if (!isValid(row.getCell(START_INDEX + CONTRACT_FIELDS * i))) {
				continue;
			}
			if (getBoolean(row.getCell(START_INDEX + CONTRACT_FIELDS * i))) {
				contractList.get(i).append("description",
						getTextCellValue(row.getCell(START_INDEX + 1 + CONTRACT_FIELDS * i)));
			}
			contractList.get(i).append("isActive", getBoolean(row.getCell(START_INDEX + CONTRACT_FIELDS * i)));
		}

		return contractList;
	}

	private static List<String> getPaymentModes(Row row) {
		int PAYMENT_FIELDS = 1;
		int START_INDEX = 82;
		List<String> paymentList = new ArrayList<String>();

		for (int i = 0; i < 11; i++) {
			if (!isValid(row.getCell(START_INDEX + PAYMENT_FIELDS * i))) {
				continue;
			}
			paymentList.add(getTextCellValue(row.getCell(START_INDEX + PAYMENT_FIELDS * i)));
		}
		return paymentList;
	}

	private static List<Document> getServiceAreas(Row row) {
		int BANGALORE_SERVICE_FIELDS_COUNT = 299;
		int BANGALORE_START_INDEX = 379;

		// int MYSORE_SERVICE_FIELDS_COUNT = 299;
		// int MYSORE_START_INDEX = 854;

		List<Document> serviceAreaList = new ArrayList<Document>();
		if (isValid(row.getCell(BANGALORE_START_INDEX))
				&& getTextCellValue(row.getCell(BANGALORE_START_INDEX)).equalsIgnoreCase("Bangalore")) {
			serviceAreaList.add(new Document("city", "Bangalore").append("cityServiceAreas",
					getServiceAreaList(row, BANGALORE_START_INDEX + 1, BANGALORE_SERVICE_FIELDS_COUNT)));
		}
		// if (isValid(row.getCell(MYSORE_START_INDEX)) &&
		// getTextCellValue(row.getCell(MYSORE_START_INDEX)).equalsIgnoreCase("Mysore"))
		// {
		// serviceAreaList.add(new Document("Mysore",getServiceAreaList(row,
		// MYSORE_START_INDEX + 1, MYSORE_SERVICE_FIELDS_COUNT)));
		// }
		return serviceAreaList;
	}

	private static List<Document> getServiceAreaList(Row row, int startIndex, int serviceAreaCount) {
		int SERVICE_FIELDS = 2;
		List<Document> serviceAreaList = new ArrayList<Document>();
		for (int i = 0; i < serviceAreaCount; i++) {
			String serviceArea = getTextCellValue(row.getCell(startIndex + i));
			if (serviceArea.length() > 0) {
				serviceAreaList.add(new Document().append("name", serviceArea).append("description", ""));
			}

		}
		return serviceAreaList;
	}

	private static List<String> getNatureOfBusiness(Row row) {
		int PAYMENT_FIELDS = 1;
		int START_INDEX = 93;
		List<String> businnesList = new ArrayList<String>();

		for (int i = 0; i < 3; i++) {
			if (!isValid(row.getCell(START_INDEX + PAYMENT_FIELDS * i))) {
				continue;
			}
			businnesList.add(getTextCellValue(row.getCell(START_INDEX + PAYMENT_FIELDS * i)));
		}
		return businnesList;
	}

	private static Document getConsultationCharges(Row row) {
		int FIXED_INDEX = 44;
		Document consultaionCharge = new Document().append("fixed", null).append("percentage", null);
		if (isValid(row.getCell(FIXED_INDEX))) {
			consultaionCharge.append("fixed", row.getCell(FIXED_INDEX).getNumericCellValue());
		}
		if (isValid(row.getCell(FIXED_INDEX + 1))) {
			consultaionCharge.append("percentage", row.getCell(FIXED_INDEX + 1).getNumericCellValue());
		}
		return consultaionCharge;
	}

	private static Document getMaxProjectValue(Row row) {
		int FIXED_INDEX = 46;
		Document consultaionCharge = new Document().append("fixed", null).append("percentage", null);
		if (isValid(row.getCell(FIXED_INDEX))) {
			consultaionCharge.append("fixed", row.getCell(FIXED_INDEX).getNumericCellValue());
		}
		if (isValid(row.getCell(FIXED_INDEX + 1))) {
			consultaionCharge.append("percentage", row.getCell(FIXED_INDEX + 1).getNumericCellValue());
		}
		return consultaionCharge;
	}

	private static Document getMinProjectValue(Row row) {
		int FIXED_INDEX = 48;
		Document consultaionCharge = new Document().append("fixed", null).append("percentage", null);
		if (isValid(row.getCell(FIXED_INDEX))) {
			consultaionCharge.append("fixed", row.getCell(FIXED_INDEX).getNumericCellValue());
		}
		if (isValid(row.getCell(FIXED_INDEX + 1))) {
			consultaionCharge.append("percentage", row.getCell(FIXED_INDEX + 1).getNumericCellValue());
		}
		return consultaionCharge;
	}

	private static Document getVisitingCharges(Row row) {
		int FIXED_INDEX = 50;
		Document consultaionCharge = new Document().append("fixed", null).append("percentage", null);
		if (isValid(row.getCell(FIXED_INDEX))) {
			consultaionCharge.append("fixed", row.getCell(FIXED_INDEX).getNumericCellValue());
		}
		if (isValid(row.getCell(FIXED_INDEX + 1))) {
			consultaionCharge.append("percentage", row.getCell(FIXED_INDEX + 1).getNumericCellValue());
		}
		return consultaionCharge;
	}

	private static List<Document> getBranchList(Row row) {
		int BRANCH_FIELDS = 3;
		int START_INDEX = 116;
		List<Document> branchList = new ArrayList<Document>();

		for (int i = 0; i < 8; i++) {
			if (!isValid(row.getCell(START_INDEX + BRANCH_FIELDS * i))) {
				continue;
			}
			branchList.add(
					new Document().append("branchName", getTextCellValue(row.getCell(START_INDEX + BRANCH_FIELDS * i)))
							.append("branchPhoneNumber",
									getTextCellValue(row.getCell(START_INDEX + 1 + BRANCH_FIELDS * i)))
					.append("branchAddress", getTextCellValue(row.getCell(START_INDEX + 2 + BRANCH_FIELDS * i))));
		}
		return branchList;
	}

	private static List<Document> getProjects(Row row) {
		int PROJECT_FIELDS = 35;
		int START_INDEX = 204;
		List<Document> projectList = new ArrayList<Document>();

		for (int i = 0; i < 5; i++) {
			if (!isValid(row.getCell(START_INDEX + PROJECT_FIELDS * i))) {
				continue;
			}

			projectList
					.add(new Document()
							.append("imageURL", getTextCellValue(row.getCell(START_INDEX + 1 + PROJECT_FIELDS * i)))
							.append("name", getTextCellValue(row.getCell(START_INDEX + PROJECT_FIELDS * i)))
							.append("startDate", getDateFromText(row.getCell(START_INDEX + 2 + PROJECT_FIELDS * i)))
							.append("endDate", getDateFromText(row.getCell(START_INDEX + 3 + PROJECT_FIELDS * i)))
							.append("description", getTextCellValue(row.getCell(START_INDEX + 4 + PROJECT_FIELDS * i)))
							.append("city", getTextCellValue(row.getCell(START_INDEX + 5 + PROJECT_FIELDS * i)))
							.append("location", getTextCellValue(row.getCell(START_INDEX + 6 + PROJECT_FIELDS * i)))
							.append("value", getNumericCellValue(row.getCell(START_INDEX + 7 + PROJECT_FIELDS * i)))
							.append("valueCurrency",
									getTextCellValue(row.getCell(START_INDEX + 8 + PROJECT_FIELDS * i)))
					.append("materialAndLaborInfo", getTextCellValue(row.getCell(START_INDEX + 9 + PROJECT_FIELDS * i)))
					.append("customerReviews", getCustomerReview(row, START_INDEX + 29 + PROJECT_FIELDS * i))
					.append("verificationStatus", getTextCellValue(row.getCell(START_INDEX + 10 + PROJECT_FIELDS * i)))
					.append("customerDetails", getCustomerReference(row, START_INDEX + 11 + PROJECT_FIELDS * i))

			);

		}
		return projectList;
	}
	
	private static Map<String,String> getProjectsImage(Row row) {
		int PROJECT_FIELDS = 35;
		int START_INDEX = 204;
		
		//map of project name and corresponding image url
		Map<String,String> projectImage = new HashMap<>();

		for (int i = 0; i < 5; i++) {
			if (!isValid(row.getCell(START_INDEX + PROJECT_FIELDS * i))) {
				continue;
			}
			
			if (!getTextCellValue(row.getCell(START_INDEX + 1 + PROJECT_FIELDS * i)).equalsIgnoreCase("")) {
				projectImage.put(getTextCellValue(row.getCell(START_INDEX + PROJECT_FIELDS * i)), getTextCellValue(row.getCell(START_INDEX + 1 + PROJECT_FIELDS * i)));
			}
		}
		return projectImage;
	}

	private static List<Document> getCustomerReference(Row row, int index) {
		int REFERENCE_FIELDS = 6;
		int START_INDEX = index;
		List<Document> branchList = new ArrayList<Document>();

		for (int i = 0; i < 3; i++) {
			if (!isValid(row.getCell(START_INDEX + REFERENCE_FIELDS * i))) {
				continue;
			}
			branchList
					.add(new Document()
							.append("contactPerson", getTextCellValue(row.getCell(START_INDEX + REFERENCE_FIELDS * i)))
							.append("designation",
									getTextCellValue(row.getCell(START_INDEX + 1 + REFERENCE_FIELDS * i)))
					.append("phoneNumber", getTextCellValue(row.getCell(START_INDEX + 2 + REFERENCE_FIELDS * i)))
					.append("emailId", getTextCellValue(row.getCell(START_INDEX + 3 + REFERENCE_FIELDS * i)))
					.append("projectDone", getTextCellValue(row.getCell(START_INDEX + 4 + REFERENCE_FIELDS * i)))
					.append("dateCompleted", getDateFromText(row.getCell(START_INDEX + 5 + REFERENCE_FIELDS * i))));
		}
		return branchList;
	}

	private static List<Document> getCustomerReview(Row row, int index) {
		int REVIEW_FIELDS = 3;
		int START_INDEX = index;
		List<Document> branchList = new ArrayList<Document>();

		for (int i = 0; i < 2; i++) {
			if (!isValid(row.getCell(START_INDEX + REVIEW_FIELDS * i))) {
				continue;
			}
			branchList.add(new Document().append("name", getTextCellValue(row.getCell(START_INDEX + REVIEW_FIELDS * i)))
					.append("review", getTextCellValue(row.getCell(START_INDEX + 2 + REVIEW_FIELDS * i)))
					.append("imageURL", getTextCellValue(row.getCell(START_INDEX + 1 + REVIEW_FIELDS * i))));
		}
		return branchList;
	}

	public static String getHeader(Cell cell) {
		return dataSheet.getRow(3).getCell(cell.getColumnIndex()) + " / "
				+ dataSheet.getRow(2).getCell(cell.getColumnIndex())
				+ dataSheet.getRow(1).getCell(cell.getColumnIndex())
				+ dataSheet.getRow(0).getCell(cell.getColumnIndex());
	}

	public static Date getDateFromText(Cell cell) {
		if (!isValid(cell)) {
			return null;
		} else {
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}

		if (cell.getStringCellValue().trim().matches("^(19|20)[0-9][0-9].?0*")) {
			return getDefaultDateFromText(cell.getStringCellValue().trim().split("\\.")[0]);
		} else if (getTextCellValue(cell).trim()
				.matches("^(0?[1-9]|[12][0-9]|3[01])[-/](0?[1-9]|1[012])[-/](19|20)[0-9][0-9]$")) {
			String[] splits = null;
			if (getTextCellValue(cell).trim().split("/").length == 3) {
				splits = getTextCellValue(cell).trim().split("/");
			} else {
				splits = getTextCellValue(cell).trim().split("-");
			}

			Calendar date = new GregorianCalendar(Integer.parseInt(splits[2]), Integer.parseInt(splits[1]) - 1,
					Integer.parseInt(splits[0]));
			date.setTimeZone(TimeZone.getTimeZone("GMT"));

			return date.getTime();
		} else {
			System.out.println("Received Data : " + getTextCellValue(cell));
			System.out.println("Invalid date format in row : " + (cell.getRowIndex() + 1) + " column : "
					+ getHeader(cell) + ". Inserting null. Date should be in format dd/mm/yyyy or dd-mm-yy");
		}
		return null;

	}

	private static Date getDefaultDateFromText(String dateStr) {
		Calendar date = new GregorianCalendar(Integer.parseInt(dateStr), Calendar.JANUARY, 1);
		date.setTimeZone(TimeZone.getTimeZone("GMT"));
		return date.getTime();
	}

}
