package com.msupply;

import static com.msupply.LoaderUtil.getNumericCellValue;
import static com.msupply.LoaderUtil.getTextCellValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class ExcelLoadData {

	public static void main(String[] args) {

		try (FileInputStream fis = new FileInputStream(
				new File("/home/soumya/work/loaders/Affiliate/demo/Affiliate Data Collection Sheet - Testing.xlsx"));
				MongoClient mongoClient = new MongoClient();

		) {

			List<Cell> list = new ArrayList<Cell>();

			MongoDatabase db = mongoClient.getDatabase("msupplyDB");

			XSSFWorkbook workbook = new XSSFWorkbook(fis);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> ite = sheet.rowIterator();
			int countRow = 1;
			int countColumn = 0;
			while (ite.hasNext()) {
				countRow++;
				Row row = (Row) ite.next();
				if (countRow >= 6) {
					Iterator<Cell> cell = row.cellIterator();
					while (cell.hasNext()) {
						countColumn++;
						if (countColumn <= 8) {
							Cell c = cell.next();
							list.add(c);
						} else {
							countColumn = 0;
							break;
						}
					}
					System.out.println("Row : " + (row.getRowNum() + 1));
					System.out.println(getNumericCellValue(list.get(1)));
					if (getNumericCellValue(list.get(1)) != 0) {
						Document document = Document.parse(getJson(list));
						db.getCollection("ServiceProvider").insertOne(document);
						list.clear();
					}
					
//					System.exit(0);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getJson(List<Cell> list) {
		String s = "{ \n\t\t\t\t\"serviceProviderEntity\" : {\n\t\t\t\t\"profileInfo\" : {\n\t\t\t\t\t\t\"accountInfo\" : {\n\t\t\t\t\t\t\t\t\"customerId\" :"
				+ getNumericCellValue(list.get(1)) + " ,\n\t\t\t\t\t\t\t\t\"serviceProviderId\" : "
				+ getNumericCellValue(list.get(1)) + " ,\n\t\t\t\t\t\t\t\t\"firstName\" : \""
				+ getTextCellValue(list.get(2)) + "\",\n\t\t\t\t\t\t\t\t\"lastName\":\"" + getTextCellValue(list.get(3))
				+ "\",\n\t\t\t\t\t\t\t\t\"mobile\" : \"" + getTextCellValue(list.get(5))
				+ "\",\n\t\t\t\t\t\t\t\t\"email\" : \"" + getTextCellValue(list.get(4))
				+ "\",\n\t\t\t\t\t\t\t\t\"serviceTaxNumber\" : \"null\",\n\t\t\t\t\t\t\t\t\"PAN\" : " + null + ",\n\t\t\t\t\t\t\t\t\"TIN\" : null,\n\t\t\t\t\t\t\t\t\"AadharNumber\" : null,\n\t\t\t\t\t\t\t\t\"startDate\" :\"2015-12-23T06:39:22.679Z\",\n\t\t\t\t\t\t\t\t\"endDate\" : \"\",\n\t\t\t\t\t\t\t\t\"isActive\" : false,\n\t\t\t\t\t\t\t\t\"verificationStatus\" : \"pending\",\n\t\t\t\t\t\t\t\t\"paymentStatus\" : \"pending\"\n\t\t\t\t\t\t}\n\t\t\t\t},\n\t\t\t\t\"passwords\" : {\n\t\t\t\t\t\t\"lastGeneratedOTP\" : 0,\n\t\t\t\t\t\t\"previousPasswordHash\" : \"482c811da5d5b4bc6d497ffa98491e38\",\n\t\t\t\t\t\t\"passwordHash\" : \"password\",\n\t\t\t\t\t\t\"passwordChanged\": \"Y\"\n\t\t\t\t},\n\t\t\t\t\"projectsInfo\" : null\n\t\t\t}\n\t\t}";
		JSONObject jsonObj = new JSONObject(s);
		System.out.println(jsonObj.toString());
		return jsonObj.toString();
	}
}
