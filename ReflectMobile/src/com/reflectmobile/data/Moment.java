package com.reflectmobile.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Moment {
	private static String TAG = "Moment";

	private int id;
	private String name;
	private String date;
	private ArrayList<Photo> photoList;
	private ArrayList<String> peopleList = null;
	private String jsonString;

	public Moment(int id, String name, String jsonString) {
		this.setId(id);
		this.setName(name);
		this.photoList = new ArrayList<Photo>();
		this.setJsonString(jsonString);
	}

	public ArrayList<Photo> getPhotoList() {
		return photoList;
	}

	public void setPhotoList(ArrayList<Photo> photoList) {
		this.photoList = photoList;
	}

	public Photo getPhoto(int index) {
		if (index <= photoList.size() - 1) {
			return photoList.get(index);
		} else {
			return null;
		}
	}

	public void addPhoto(Photo photo) {
		this.photoList.add(photo);
	}

	public int getNumOfPhotos() {
		return photoList.size();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public static Moment getMomentInfo(String jsonString) {
		try {
			JSONObject momentJSONObject = new JSONObject(jsonString);
			int momentID = momentJSONObject.getInt("id");
			String momentName = momentJSONObject.getString("name");
			Moment moment = new Moment(momentID, momentName, jsonString);

			JSONArray photoJSONArray = new JSONArray(
					momentJSONObject.getString("photos"));
			for (int count = 0; count < photoJSONArray.length(); count++) {
				Photo photo = Photo.getPhotoInfo(photoJSONArray
						.getString(photoJSONArray.length() - 1 - count));
				moment.addPhoto(photo);
			}
			return moment;

		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(ArrayList<String> peopleList) {
		this.peopleList = peopleList;
	}
	
	public void addPerson(String person) {
		this.peopleList.add(person);
	}
	
}
