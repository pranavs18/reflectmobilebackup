package com.reflectmobile.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reflectmobile.R;
import android.util.Log;

public class Memory {
	private static String TAG = "Memory";

	private int id;
	private String type;
	private String content;
	private String info;

	public Memory(int id, String type, String content, String info) {
		this.setId(id);
		this.setType(type);
		this.setContent(content);
		this.setInfo(info);
	}

	public int getResourceId() {
		if (type.equals("detail")) {
			return R.drawable.detail_small;
		} else if (type.equals("story")) {
			return R.drawable.story_small;
		} else {
			return R.drawable.sound_small;
		}
	}

	public static Memory[] getMemoriesInfo(String jsonString) {
		try {
			JSONArray memoryJSONArray = new JSONArray(jsonString);
			Memory[] memories = new Memory[memoryJSONArray.length()];
			for (int count = 0; count < memoryJSONArray.length(); count++) {
				JSONObject memoryJSONObject = memoryJSONArray
						.getJSONObject(memoryJSONArray.length() - 1 - count);
				int id = memoryJSONObject.getInt("id");
				String type = memoryJSONObject.getString("memory_type");
				String content = memoryJSONObject.getString("memory_content");

				String date = memoryJSONObject.getString("updated_at");
				SimpleDateFormat formatFrom = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
				SimpleDateFormat formatTo = new SimpleDateFormat(
						"MM/dd/yy HH:mm aa", Locale.US);
				try {
					date = formatTo.format(formatFrom.parse(date));
				} catch (ParseException e) {
					e.printStackTrace();
					Log.e(TAG, "Error parsing date");
				}

				JSONObject owner = memoryJSONObject.getJSONObject("owner");
				String firstName = owner.getString("first_name");
				String lastName = owner.getString("last_name");

				String info = firstName + " " + lastName + " – " + date;
				memories[count] = new Memory(id, type, content, info);
			}
			return memories;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
