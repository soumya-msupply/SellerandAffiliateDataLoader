package com.msupply;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

public class ProjectImageDetails {
	
	private Integer customerID;
	private String profileImage;
	private List<String> projects;
	
	public ProjectImageDetails(Document doc) {
		super();
		Document project = (Document) doc.get("value");
		this.customerID = ((Double)project.get("customerID")).intValue();
		this.profileImage = project.getString("profileURL");;
		this.projects = (ArrayList<String>)project.get("projects");
	}

	public Integer getCustomerID() {
		return customerID;
	}

	public void setCustomerID(Integer customerID) {
		this.customerID = customerID;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public List<String> getProjects() {
		return projects;
	}

	public void setProjects(List<String> projects) {
		this.projects = projects;
	}
	
	
	
	

}
