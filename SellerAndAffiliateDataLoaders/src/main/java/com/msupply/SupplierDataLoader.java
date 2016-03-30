package com.msupply;

import static com.msupply.LoaderUtil.getCurrentTimestamp;
import static com.msupply.LoaderUtil.getTextCellValue;
import static com.msupply.LoaderUtil.isValid;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

public class SupplierDataLoader {

	private static XSSFWorkbook wb;
	private static HashSet<String> duplicateSet = new HashSet<String>();
	private static MongoDatabase db;
	private static Row headerRow;
	private static String env = "stg";
	private static int successRows = 0;
	private static int rowCounter = 0;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// "52.30.181.28"
		try {

			MongoClient mongoClient;

			// Creating mongo connection
			// db = mongoClient.getDatabase("msupplyDB");

			/*
			 * create a DB Connection.
			 */

			HashMap<String, Object> connData = DBConnector.getMongoConnData(env);

			@SuppressWarnings("unchecked")
			List<ServerAddress> seeds = (List<ServerAddress>) connData.get("seeds");

			@SuppressWarnings("unchecked")
			List<MongoCredential> credentials = (List<MongoCredential>) connData.get("credentials");

			String DB = (String) connData.get("DB");

			if (credentials.size() > 0) {
				mongoClient = new MongoClient(seeds, credentials);
			} else {
				mongoClient = new MongoClient(seeds);
			}
			db = mongoClient.getDatabase(DB);

			// Getting timestamp for report file
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String timestamp = sdf.format(calendar.getTime());

			// Creating report file
			File file = new File(
					"/home/soumyabardhan/work/loaders/Supplier/new/Supplier_Data_Upload_Report - " + timestamp + ".txt");
			FileOutputStream fos = new FileOutputStream(file);
			TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(myOut);
			System.setOut(ps);

			/*
			 * Data file of upload
			 */
			File inputFile = new File("/home/soumyabardhan/work/loaders/Supplier/new/Supplier_test_data (2).xlsx");
			wb = new XSSFWorkbook(new FileInputStream(inputFile));
			XSSFSheet firstSheet = wb.getSheetAt(0);
			Iterator<Row> rowIterator = firstSheet.iterator();
			

			// MongoDB document
			Document document = null;
			while (rowIterator.hasNext()) {
				Row row = (Row) rowIterator.next();
				if (rowCounter < 1) {
					// Setting header row for error reporting
					if (rowCounter == 0) {
						headerRow = row;
					}
					rowCounter++;
					continue;
				}
				//Counting total number of rows
				rowCounter++;
				//validate(row)
				if (validate(row)) {
					document = new Document("supplierEntity",
							new Document("identifier",
									new Document().append("sellerId", getTextCellValue(row.getCell(0)))
											.append("magentoCustID", getTextCellValue(row.getCell(1)))
											.append("persona", getTextCellValue(row.getCell(44))))
											.append("companyInfo", new Document()
													.append("companyName",getTextCellValue(	row.getCell(7)))
													.append("displayName",getTextCellValue(row.getCell(8)))
													.append("establishment", getTextCellValue(row.getCell(9)))
													.append("website",getTextCellValue(row.getCell(10)))
													.append("status",getTextCellValue(row.getCell(45)))//new addition
													.append("address",asList(new Document()
															.append("type",getTextCellValue(row.getCell(11)))
															.append("address1",getTextCellValue(row.getCell(12)))
															.append("address2", getTextCellValue(row.getCell(13)))
															.append("city", getTextCellValue(row.getCell(14)))
															.append("state", getTextCellValue(row.getCell(15)))
															.append("country", getTextCellValue(row.getCell(16)))
															.append("pincode", getTextCellValue(row.getCell(17))),
															new Document()
															.append("type",getTextCellValue(row.getCell(18)))
															.append("address1",getTextCellValue(row.getCell(19)))
															.append("address2",getTextCellValue(row.getCell(20)))
															.append("city",getTextCellValue(row.getCell(21)))
															.append("state",getTextCellValue(row.getCell(22)))
															.append("country",getTextCellValue(row.getCell(23)))
															.append("pincode",getTextCellValue(row.getCell(24)))
					))).append("contactInfo",
							new Document().append("primaryFirstName", getTextCellValue(row.getCell(25)))
									.append("primaryLastName", getTextCellValue(row.getCell(26)))
									.append("image", getTextCellValue(row.getCell(27)))
									.append("secondaryFirstName", getTextCellValue(row.getCell(28)))
									.append("secondaryLastName", getTextCellValue(row.getCell(29)))
									.append("primaryMobile", getTextCellValue(row.getCell(30)))
									.append("secondaryMobile", getTextCellValue(row.getCell(31)))
									.append("primaryEmail", getTextCellValue(row.getCell(32)))
									.append("secondaryEmail", getTextCellValue(row.getCell(33))))
											.append("firstTimeLogin", Boolean.TRUE)
											.append("lastLoginTime", getCurrentTimestamp())
											.append("passwords",new Document()
													.append("previousPasswordHash", "")
													.append("passwordHash","42f749ade7f9e195bf475f37a44cafcb")
													.append("OTP", ""))
											.append("bankInfo",new Document()
													.append("accountHolderName",getTextCellValue(row.getCell(37)))
													.append("branch",getTextCellValue(row.getCell(39)))
													.append("bankName",getTextCellValue(row.getCell(38)))
													.append("accountNumber",getTextCellValue(row.getCell(40)))
													.append("IFSC",getTextCellValue(row.getCell(41))))
											.append("taxInfo",new Document()
													.append("VAT_TIN",getTextCellValue(row.getCell(34)))
													.append("STNumber",getTextCellValue(row.getCell(35)))
													.append("PAN",getTextCellValue(row.getCell(36))))
													.append("agreementInfo",new Document()
															.append("T&CAcceptance",getTextCellValue(row.getCell(42)))
															.append("timeStamp",getDateFromTimestamp(row.getCell(43))))
											.append("interestCategories",getInterest(row)));

					// Comment these lines for testing the data file
					
					System.out.println(document.toJson());
					
					 try {
					 db.getCollection("Supplier").insertOne(document);
					 System.out.println("Successfully inserted row : " +
					 (row.getRowNum() + 1) );
					successRows++;
					 } catch (Exception e) {
					 System.out.println("Row " + (row.getRowNum() + 1) + " skipped with error from DB. ERROR : " + e.getMessage());
					 }

				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Statictics : " + successRows +"/" + rowCounter + " rows inserted successfully. " );
		}
	}
	
	private static List<String> getInterest(Row row) {
		List<String> websiteList = new ArrayList<String>();
		int START_INDEX = 46;
		for (int i = 0; i < 20; i++) {
			String website = getTextCellValue(row.getCell(START_INDEX + 1 * i));
			if (website.length() > 0) {
				websiteList.add(website);
			}
		}
		return websiteList;
	}

	private static Boolean validate(Row row) {
		int sellerID_idx = 0;
		int primaryMob_idx = 30;
		int primaryEmail_idx = 32;
//		int VAT_idx = 34;
//		int PAN_idx = 36;
		int tnc_idx = 42;
		int status_idx = 45;

		String missingFields = "";

		// Validating mandatory fields
		missingFields += isValid(row.getCell(sellerID_idx)) ? "" : getTextCellValue(headerRow.getCell(sellerID_idx)) + ",";
		missingFields += isValid(row.getCell(primaryMob_idx)) ? "" : getTextCellValue(headerRow.getCell(primaryMob_idx)) + ",";
		missingFields += isValid(row.getCell(primaryEmail_idx)) ? "" : getTextCellValue(headerRow.getCell(primaryEmail_idx)) + ",";
		//missingFields += isValid(row.getCell(VAT_idx)) ? "" : getTextCellValue(headerRow.getCell(VAT_idx)) + ",";
		//missingFields += isValid(row.getCell(PAN_idx)) ? "" : getTextCellValue(headerRow.getCell(PAN_idx)) + ",";
		missingFields += isValid(row.getCell(tnc_idx)) && ( getTextCellValue(row.getCell(tnc_idx)).equalsIgnoreCase("Y") || getTextCellValue(row.getCell(tnc_idx)).equalsIgnoreCase("Yes")  )  ? "" : getTextCellValue(headerRow.getCell(tnc_idx)) + ",";
		missingFields += isValid(row.getCell(status_idx)) ? "" : getTextCellValue(headerRow.getCell(status_idx)) + ",";

		if (missingFields.length() == 0) {

			// Creating key for duplicate check
			String key = getTextCellValue(row.getCell(sellerID_idx)) + getTextCellValue(row.getCell(primaryEmail_idx))
					+ getTextCellValue(row.getCell(primaryMob_idx));
//			+ getTextCellValue(row.getCell(VAT_idx))
//					+ getTextCellValue(row.getCell(PAN_idx));

			// Checking for duplicate in excel sheet
			if (duplicateSet.contains(key)) {
				System.out.println("Row " + (row.getRowNum() + 1) + " skipped due to duplicate row in upload file.");
				return false;

			} else {

				// Checking for duplicate entry in DB
				Document document = new Document("supplierEntity.identifier.sellerId",
						getTextCellValue(row.getCell(sellerID_idx)))
								.append("supplierEntity.contactInfo.primaryEmail",
										getTextCellValue(row.getCell(primaryEmail_idx)))
								.append("supplierEntity.contactInfo.primaryMobile",
										getTextCellValue(row.getCell(primaryMob_idx)));
//								.append("supplierEntity.taxInfo.VAT_TIN", getTextCellValue(row.getCell(VAT_idx)))
//								.append("supplierEntity.taxInfo.PAN", getTextCellValue(row.getCell(PAN_idx)));

				// System.out.println("Query Doc : " + document.toJson());
				long count = db.getCollection("Supplier").count(document);
				if (count != 0) {
					System.out.println("Row " + (row.getRowNum() + 1) + " skipped as seller already present in DB.");
					return false;
				}
			}

			// Adding the key to the set
			// System.out.println("KEY : " + key);
			duplicateSet.add(key);
			return true;
		}
		System.out.println(
				"Row " + (row.getRowNum() + 1) + " skipped due to missing mandatory fields : " + missingFields);
		return false;
	}

	private static Boolean isValid(Cell cell) {
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return false;
		}
		return true;
	}

	// Required format Wed Dec 16 2015 11:40:40 GMT+0530 (IST)
	public static Date getDateFromTimestamp(Cell cell) {
		if (!isValid(cell)) {
			return null;
		}

		Date date = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM d yyyy hh:mm:ss 'GMT+0530' (z)");
			Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTime(dateFormat.parse(getTextCellValue(cell)));
			// System.out.println(cal.getTime().getTime());
			date = cal.getTime();
		} catch (ParseException e) {
			System.out.println("Invalid date format in row : " + (cell.getRowIndex() + 1) + " column : "
					+ cell.getColumnIndex()
					+ ". Inserting null. Timestamp should be in format like Wed Dec 16 2015 11:40:40 GMT+0530 (IST)");
			return null;
		}
		return date;
	}

}
