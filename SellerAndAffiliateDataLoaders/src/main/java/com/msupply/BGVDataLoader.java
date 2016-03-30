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
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class BGVDataLoader {
	
	private static XSSFSheet dataSheet;
	
	public static void main(String[] args) {
		
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase("msupplyDB");
		

		// Getting timestamp for report file
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String timestamp = sdf.format(calendar.getTime());

		try {
			// Creating report file
			File file = new File("/home/soumya/work/loaders/bgv/demo/BGV_Data_Upload_Report - " + timestamp + ".txt");
			FileOutputStream fos = new FileOutputStream(file);
			TeeOutputStream myOut = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(myOut);
			System.setOut(ps);

			/*
			 * Data file of upload
			 */
			File inputFile = new File("/home/soumya/work/loaders/bgv/demo/BG Verification Data-DEV.xlsx");
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(inputFile));
			dataSheet = wb.getSheet("Sheet1"); /* wb.getSheetAt(2); */
			
			
			// Used for maintaining customer ids of all legitimate docs
			List<Integer> legitimateDocuments = new ArrayList<>();

			FindIterable<Document> legitimateDocs = db.getCollection("ServiceProviderBGV")
					.find()
					.projection(new Document("serviceProviderEntityBGDetails.affiliateInfo.affiliateId", 1)
							.append("_id", 0));

			for (Document document : legitimateDocs) {
				legitimateDocuments
						.add(
								((Document)((Document) document.get("serviceProviderEntityBGDetails")).get("affiliateInfo")).getInteger("affiliateId")

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

				if (isValid(row.getCell(0)) && getTextCellValue(row.getCell(8)).trim().equalsIgnoreCase("Clear")) {
					if (legitimateDocuments.contains(getNumericCellValue(row.getCell(0)))) {
						System.out.println("Skipped row : " + (row.getRowNum() + 1) + ". BGV data already exists in DB for Customer ID." + getNumericCellValue(row.getCell(0)));
						continue;
					}

					// Updating account info
					document = new Document("serviceProviderEntityBGDetails",	new Document("affiliateInfo", new Document()
							.append("affiliateId", getNumericCellValue(row.getCell(0)))
							.append("affiliateName", getTextCellValue(row.getCell(1)))
							.append("registeredDate", getDateFromText(row.getCell(2)))
							.append("categoryManager", getTextCellValue(row.getCell(3)))
							.append("category", getTextCellValue(row.getCell(4)))
							.append("bgvFeesReceived", getTextCellValue(row.getCell(5)))
							.append("documentSent",	getTextCellValue(row.getCell(6)))
							.append("documentResent", getTextCellValue(row.getCell(7)))
							.append("bgvStatus", getTextCellValue(row.getCell(8)))
							.append("remarks", getTextCellValue(row.getCell(9)))
							.append("DOB", getDateFromText(row.getCell(10)))
							.append("fathersName", getTextCellValue(row.getCell(11)))
							.append("address", getTextCellValue(row.getCell(12)))
							.append("amazonS3URL", getTextCellValue(row.getCell(19)))
							.append("customerInfo", getCustomerDetails(row))
					));

									

					 System.out.println(document.toJson());
					 db.getCollection("ServiceProviderBGV").insertOne(document);
						System.out.println("Successfully inserted row : " + (row.getRowNum() + 1) );
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
				} else {
					System.out.println("Skipped row : " + (row.getRowNum() + 1)
							+ ". Either blank or missing mandatory field Affiliate ID in MongoDB or BGV status not clear.");
				}

			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	private static List<Document> getCustomerDetails(Row row) {
		int CUST_FIELDS = 2;
		int START_INDEX = 13;
		List<Document> customerDetailsList = new ArrayList<Document>();

		for (int i = 0; i < 4; i++) {
			if (!isValid(row.getCell(START_INDEX + CUST_FIELDS * i))) {
				continue;
			}
			customerDetailsList.add(new Document()
					.append("customerName", getTextCellValue(row.getCell(START_INDEX + CUST_FIELDS * i)))
					.append("contactNumber", getTextCellValue(row.getCell(START_INDEX + 1 + CUST_FIELDS * i)))
					);
		}
		return customerDetailsList;
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
	
	public static String getHeader(Cell cell) {
		return dataSheet.getRow(3).getCell(cell.getColumnIndex()) + " / "
				+ dataSheet.getRow(2).getCell(cell.getColumnIndex())
				+ dataSheet.getRow(1).getCell(cell.getColumnIndex())
				+ dataSheet.getRow(0).getCell(cell.getColumnIndex());
	}

}