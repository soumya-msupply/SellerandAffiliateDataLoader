package com.msupply;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class Test2 {
	
	private static double format2DigitDecimal(Object obj) {
		return (new Double(new DecimalFormat("###.##").format(Double.parseDouble(obj.toString()))));
	} 
	
	private static String formatSpecKey(String specKey){
		
		try {
		String character = "~";
		String specKey_temp = specKey.trim().replaceAll("\\s+",character);   
		
		String[] splits = specKey_temp.split(character);
		
		
		String finalStr = null;
		if (splits.length == 1) {
			return  splits[0].toLowerCase();
		} else {
			finalStr = splits[0].toLowerCase();
			for (int i = 1; i < splits.length; i++) {
				finalStr = finalStr + splits[i].substring(0,1).toUpperCase() + splits[i].substring(1).toLowerCase();
			}
		}
		
		
		
//		int charCount = specKey_temp.length() - specKey_temp.replace(character, "").length();
//		
//		if (charCount == 0) {
//			return  specKey_temp.substring(0,1).toLowerCase() + specKey_temp.substring(1);
//		}
//		
//		String finalStr = null;
//		for (int i=0; i<=charCount; i++ ) {
//			 if (i==0 && specKey_temp.indexOf(character) != -1) {
//				 finalStr =  specKey_temp.substring(0,1).toLowerCase() + specKey_temp.substring(1,specKey_temp.indexOf(character)).toLowerCase();
//				 specKey_temp = specKey_temp.substring(specKey_temp.indexOf(character)+1);
//			 } else{
//				 if (specKey_temp.indexOf(character) != -1) {
//					 finalStr = finalStr + specKey_temp.substring(0,1).toUpperCase() + specKey_temp.substring(1,specKey_temp.indexOf(character)).toLowerCase();
//					 specKey_temp = specKey_temp.substring(specKey_temp.indexOf(character)+1);
//				 }else{
//					 finalStr = finalStr + specKey_temp.substring(0,1).toUpperCase() + specKey_temp.substring(1).toLowerCase();
//				 }
//			 }
//		}
		return finalStr;
		}catch (Exception e) {
			e.printStackTrace();
			return specKey;
		}
	}
	
	public static void main (String args[]){
		
		System.out.println(formatSpecKey("Weight       Attribute          Measure  For"));
		 
		  
		  
	}
	
	 	
}