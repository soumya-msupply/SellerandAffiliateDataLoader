package com.msupply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List ;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;


public class DBConnector {

	public static HashMap<String,Object> getMongoConnData(String env) {
		/*
		 * create a DB Connection.
		 */
		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		HashMap<String,Object> mongoConnData = new HashMap<String,Object>();

		if (env.equals("dev")) {
			seeds.add( new ServerAddress( "52.30.181.28", 27017));
			mongoConnData.put("DB", "local");
		} else if (env.equals("stg")) {
			//seeds.add( new ServerAddress( "52.30.255.50", 27017));
			//seeds.add( new ServerAddress( "54.72.130.17", 27017));
			//seeds.add( new ServerAddress( "52.19.222.192", 27017));

			//Host Names Mapped in C:\Windows\System32\drivers\etc file
			seeds.add( new ServerAddress( "mongo1.msupply", 27017));
			seeds.add( new ServerAddress( "mongo2.msupply", 27017));
			seeds.add( new ServerAddress( "mongo3.msupply", 27017));
			credentials.add(  
					MongoCredential.createCredential(
							"msupply",
							"msupplyDB",
							"supply123".toCharArray()
							)
					);
			mongoConnData.put("DB", "msupplyDB");
		} else if (env.equals("prdStandby")){
			seeds.add( new ServerAddress( "52.18.204.245", 27017));		 
			mongoConnData.put("DB", "msupplyDB");

		}	else if (env.equals("prd")) {
			//seeds.add( new ServerAddress( "52.18.27.216", 27017));
			//seeds.add( new ServerAddress( "52.16.203.249", 27017));
			//seeds.add( new ServerAddress( "54.72.140.247", 27017));

			//Host Names Mapped in C:\Windows\System32\drivers\etc file
			seeds.add( new ServerAddress( "mongo1.msupply.com", 27017));
			seeds.add( new ServerAddress( "mongo2.msupply.com", 27017));
			seeds.add( new ServerAddress( "mongo3.msupply.com", 27017));
			credentials.add(  
					MongoCredential.createCredential(
							"msupply",
							"msupplyDB",
							"supply123".toCharArray()
							)
					);
			mongoConnData.put("DB", "msupplyDB");



		} else {
			//local
			System.out.println("+++ Connecting to " +  env  + " +++");
			seeds.add( new ServerAddress( "localhost", 27017));
			mongoConnData.put("DB", "msupplyDB");
		}

		mongoConnData.put("seeds", seeds);
		mongoConnData.put("credentials", credentials);

		return mongoConnData;
	}

}
