package com.reflectmobile.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Network {
	private static String TAG = "Network";
	private int id;
	private String name;
	private String description;

	public Network(int id, String name, String description) {
		this.setId(id);
		this.setName(name);
		this.setDescription(description);
	}

	public static Network[] getNetworksInfo(String jsonString) {
		try {
			JSONArray networksJSONArray = new JSONArray(jsonString);
			Network[] networks = new Network[networksJSONArray.length()];
			for (int count = 0; count < networksJSONArray.length(); count++) {
				JSONObject networkJSONObject = networksJSONArray
						.getJSONObject(count);
				int id = networkJSONObject.getInt("id");
				String name = networkJSONObject.getString("name");
				String description = networkJSONObject.getString("description");
				networks[count] = new Network(id, name, description);
			}
			return networks;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON");
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
