package com.reflectmobile.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;
import android.util.Log;

public class Tag {
	private static String TAG = "Tag";
	
	private int id;
	private int photoID;
	private String type;
	private String name;
	private int upLeftX;
	private int upLeftY;
	private int boxLength;
	private int boxWidth;
	private String jsonString;
	private List<Point> pointList;
	private boolean isSquareTag;
	
	public Tag(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPhotoID() {
		return photoID;
	}
	public void setPhotoID(int photoID) {
		this.photoID = photoID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getUpLeftX() {
		return upLeftX;
	}
	public void setUpLeftX(int upLeftX) {
		this.upLeftX = upLeftX;
	}
	public int getUpLeftY() {
		return upLeftY;
	}
	public void setUpLeftY(int upLeftY) {
		this.upLeftY = upLeftY;
	}
	public int getBoxLength() {
		return boxLength;
	}
	public void setBoxLength(int boxLength) {
		this.boxLength = boxLength;
	}
	public int getBoxWidth() {
		return boxWidth;
	}
	public void setBoxWidth(int boxWidth) {
		this.boxWidth = boxWidth;
	}
	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
	public List<Point> getPointList() {
		return pointList;
	}

	public void setPointList(List<Point> pointList) {
		this.pointList = pointList;
	}

	public boolean isSquareTag() {
		return isSquareTag;
	}

	public void setSquareTag(boolean isSquareTag) {
		this.isSquareTag = isSquareTag;
	}

	public void addBoundary(List<Point> pointList) {
		this.isSquareTag = false;
		this.pointList = pointList;
	}
	
	
	public static Tag getTagInfo(String jsonString) {
		try {
			JSONObject tagJSONObject = new JSONObject(jsonString);
			int tagID = tagJSONObject.getInt("id");
			
			// Get tag information from json object and set it to tag object
			Tag tag = new Tag(tagID);
			tag.setPhotoID(tagJSONObject.getInt("photo_id"));
			tag.setType(tagJSONObject.getString("tag_type"));
			tag.setUpLeftX(tagJSONObject.getInt("x_coordinate"));
			tag.setUpLeftY(tagJSONObject.getInt("y_coordinate"));
			tag.setBoxLength(tagJSONObject.getInt("box_length"));
			tag.setBoxWidth(tagJSONObject.getInt("box_width"));
			tag.setName(tagJSONObject.getString("object_name"));
			tag.setSquareTag(true);
			return tag;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
			e.printStackTrace();
		}
		return null;
	}
}
