package com.reflectmobile.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkManager {

	public static final String hostName = "http://rewyndr.truefitdemo.com";
	public static final String SOUND_HOST_NAME = "http://ec2-54-210-20-196.compute-1.amazonaws.com";

	private static HashMap<String, String> cookie = new HashMap<String, String>();

	public static interface HttpTaskHandler {
		void taskSuccessful(String result);

		void taskFailed(String reason);
	}

	public static interface HttpImageTaskHandler {
		void taskSuccessful(Drawable drawable);

		void taskFailed(String reason);
	}

	public static void clearCookies() {
		cookie = new HashMap<String, String>();
	}

	private static void setCookies(HttpURLConnection httpURLConnection) {
		StringBuilder cookiesToSet = new StringBuilder();
		for (String key : cookie.keySet()) {
			cookiesToSet.append(key + "=" + cookie.get(key) + "; ");
		}
		httpURLConnection.setRequestProperty("Cookie", cookiesToSet.toString());
		if (cookie.containsKey("XSRF-TOKEN")) {
			try {
				httpURLConnection.setRequestProperty("X-XSRF-TOKEN",
						URLDecoder.decode(cookie.get("XSRF-TOKEN"), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e("SetCookies", "Error with decoding XSRF token");
			}
		}
	}

	private static void getCookies(HttpURLConnection httpURLConnection) {
		String headerName = null;
		for (int i = 1; (headerName = httpURLConnection.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equals("Set-Cookie")) {
				String cookies = httpURLConnection.getHeaderField(i);
				for (String keyValue : cookies.split(";")) {
					String[] splittedKeyValue = keyValue.trim().split("=", 2);
					if (splittedKeyValue.length >= 2) {
						String key = splittedKeyValue[0];
						String value = splittedKeyValue[1];

						if (key != "path") {
							cookie.put(key, value);
						}
					}
				}
			}
		}

	}

	private static String readStream(String TAG, InputStream in) {
		BufferedReader reader = null;
		StringBuffer data = new StringBuffer("");
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data.toString();
	}

	public static class HttpGetTask extends AsyncTask<String, Void, Void> {

		private HttpTaskHandler handler;

		public HttpGetTask(HttpTaskHandler handler) {
			this.handler = handler;
		}

		private String TAG = "HttpGetTask";

		private String executeRequest(String url) throws IOException {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {
				httpUrlConnection = (HttpURLConnection) new URL(url)
						.openConnection();
				httpUrlConnection.setUseCaches(true);

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				data = readStream(TAG, in);

				// getCookies(httpUrlConnection);
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpPostTask extends AsyncTask<String, Void, Void> {

		private HttpTaskHandler handler;
		private String payload;

		public HttpPostTask(HttpTaskHandler handler, String payload) {
			this.handler = handler;
			this.payload = payload;
		}

		private String TAG = "HttpPostTask";

		private String executeRequest(String url) throws IOException {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {

				httpUrlConnection = (HttpURLConnection) new URL(url)
						.openConnection();

				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type",
						"application/json;charset=UTF-8");

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(outputStream, "UTF-8"));

				writer.write(payload);
				writer.flush();
				writer.close();
				outputStream.close();

				httpUrlConnection.connect();

				getCookies(httpUrlConnection);

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				data = readStream(TAG, in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}

			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpPutTask extends AsyncTask<String, Void, Void> {

		private HttpTaskHandler handler;
		private String payload;

		public HttpPutTask(HttpTaskHandler handler, String payload) {
			this.handler = handler;
			this.payload = payload;
		}

		private String TAG = "HttpPutTask";

		private String executeRequest(String url) throws IOException {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {

				httpUrlConnection = (HttpURLConnection) new URL(url)
						.openConnection();

				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setRequestMethod("PUT");
				httpUrlConnection.setRequestProperty("Content-Type",
						"application/json;charset=UTF-8");

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(outputStream, "UTF-8"));

				writer.write(payload);
				writer.flush();
				writer.close();
				outputStream.close();

				httpUrlConnection.connect();

				getCookies(httpUrlConnection);

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				data = readStream(TAG, in);
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}

			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpDeleteTask extends AsyncTask<String, Void, Void> {

		private HttpTaskHandler handler;

		public HttpDeleteTask(HttpTaskHandler handler) {
			this.handler = handler;
		}

		private String executeRequest(String url) throws IOException {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {
				httpUrlConnection = (HttpURLConnection) new URL(url)
						.openConnection();
				httpUrlConnection.setRequestMethod("DELETE");

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				getCookies(httpUrlConnection);
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpGetImageTask extends AsyncTask<String, Void, Void> {

		private HttpImageTaskHandler handler;

		public HttpGetImageTask(HttpImageTaskHandler handler) {
			this.handler = handler;
		}

		private Drawable executeRequest(String url) throws IOException {
			Drawable drawable = null;
			HttpURLConnection httpUrlConnection = null;

			try {
				httpUrlConnection = (HttpURLConnection) new URL(hostName + url)
						.openConnection();
				httpUrlConnection.setUseCaches(true);

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				drawable = Drawable.createFromStream(in, null);

				// getCookies(httpUrlConnection);
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return drawable;
		}

		private Drawable drawable;
		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				drawable = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					drawable = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (success) {
				handler.taskSuccessful(drawable);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpPostImageTask extends
			AsyncTask<String, Integer, Void> {

		private HttpTaskHandler handler;
		private String attachmentName = "image";
		private String postUrl;
		private Context context;
		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary = "----WebKitFormBoundaryCPjnvgQ8yShMhtso";

		public HttpPostImageTask(HttpTaskHandler handler, String postUrl,
				Context context) {
			this.handler = handler;
			this.postUrl = postUrl;
			this.context = context;
		}

		private String TAG = "HttpPostImageTask";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setMessage("Uploading photos...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}

		protected static long copy(InputStream input, OutputStream output)
				throws IOException {
			byte[] buffer = new byte[12288]; // 12K
			long count = 0L;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			dialog.setProgress(progress[0]);
		}

		private String executeRequest(String... params) throws IOException {
			String data = "";
			dialog.setMax(params.length);
			HttpURLConnection httpUrlConnection = null;

			for (int i = 0; i < params.length; i++) {
				String attachmentFileName = params[i];
				try {
					httpUrlConnection = (HttpURLConnection) new URL(
							this.postUrl).openConnection();
					httpUrlConnection.setUseCaches(false);
					httpUrlConnection.setDoOutput(true);

					httpUrlConnection.setRequestMethod("POST");
					httpUrlConnection.setRequestProperty("Connection",
							"Keep-Alive");
					httpUrlConnection.setRequestProperty("Cache-Control",
							"no-cache");
					httpUrlConnection.setRequestProperty("Content-Type",
							"multipart/form-data;boundary=" + this.boundary);

					setCookies(httpUrlConnection);

					DataOutputStream request = new DataOutputStream(
							httpUrlConnection.getOutputStream());

					request.writeBytes(this.twoHyphens + this.boundary
							+ this.crlf);
					request.writeBytes("Content-Disposition: form-data; name=\""
							+ this.attachmentName
							+ "\"; filename=\""
							+ attachmentFileName + "\"" + this.crlf);
					request.writeBytes("Content-Type: image/jpeg" + this.crlf);
					request.writeBytes(this.crlf);
					// Write data

					InputStream fs = new FileInputStream(attachmentFileName);

					Bitmap original = BitmapFactory.decodeStream(fs);
					int width = original.getWidth();
					int height = original.getHeight();
					int newWidth = 1000, newHeight = 1000;
					if (width > height) {
						newHeight = 1000 * height / width;
					} else {
						newWidth = 1000 * width / height;
					}

					Bitmap resized = Bitmap.createScaledBitmap(original,
							newWidth, newHeight, true);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					resized.compress(Bitmap.CompressFormat.JPEG, 100, out);
					InputStream dfs = new ByteArrayInputStream(
							out.toByteArray());

					Log.d(TAG, this.postUrl);
					Log.d(TAG, out.toByteArray().length + "");
					copy(dfs, request);

					request.writeBytes(this.crlf);
					request.writeBytes(this.twoHyphens + this.boundary
							+ this.twoHyphens + this.crlf);
					request.flush();
					request.close();

					httpUrlConnection.connect();

					getCookies(httpUrlConnection);

					InputStream in = new BufferedInputStream(
							httpUrlConnection.getInputStream());

					data = readStream(TAG, in);
					publishProgress(i + 1);
				} finally {
					if (null != httpUrlConnection)
						httpUrlConnection.disconnect();
				}
			}
			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				dialog.dismiss();
			} catch (Exception e) {
				Log.e(TAG, "Error disposing dialog");
			}
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	public static class HttpPostSoundTask extends
			AsyncTask<String, Integer, Void> {

		private HttpTaskHandler handler;
		private String attachmentName = "image";
		private String postUrl;
		private Context context;
		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary = "----WebKitFormBoundaryCPjnvgQ8yShMhtso";

		public HttpPostSoundTask(HttpTaskHandler handler, String postUrl,
				Context context) {
			this.handler = handler;
			this.postUrl = postUrl;
			this.context = context;
		}

		private String TAG = "HttpPostSoundTask";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setMessage("Uploading sound...");
			dialog.setIndeterminate(false);
			dialog.setCancelable(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setProgress(0);
			dialog.show();
		}

		protected long copy(InputStream input, OutputStream output)
				throws IOException {
			byte[] buffer = new byte[12288]; // 12K
			long count = 0L;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
				int progress = (int) (count / 1024);
				publishProgress(progress);
			}

			return count;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			dialog.setProgress(progress[0]);
		}

		private String executeRequest(String... params) throws IOException {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			for (int i = 0; i < params.length; i++) {
				String attachmentFileName = params[i];
				try {
					httpUrlConnection = (HttpURLConnection) new URL(
							this.postUrl).openConnection();
					httpUrlConnection.setUseCaches(false);
					httpUrlConnection.setDoOutput(true);

					httpUrlConnection.setRequestMethod("POST");
					httpUrlConnection.setRequestProperty("Connection",
							"Keep-Alive");
					httpUrlConnection.setRequestProperty("Cache-Control",
							"no-cache");
					httpUrlConnection.setRequestProperty("Content-Type",
							"multipart/form-data;boundary=" + this.boundary);

					DataOutputStream request = new DataOutputStream(
							httpUrlConnection.getOutputStream());

					request.writeBytes(this.twoHyphens + this.boundary
							+ this.crlf);
					request.writeBytes("Content-Disposition: form-data; name=\""
							+ this.attachmentName
							+ "\"; filename=\""
							+ attachmentFileName + "\"" + this.crlf);
					request.writeBytes("Content-Type: audio/3gpp2" + this.crlf);
					request.writeBytes(this.crlf);

					// Write data
					File file = new File(attachmentFileName);
					int maxProgress = (int) (file.length() / 1024);
					dialog.setMax(maxProgress);
					InputStream fs = new FileInputStream(attachmentFileName);

					copy(fs, request);

					request.writeBytes(this.crlf);
					request.writeBytes(this.twoHyphens + this.boundary
							+ this.twoHyphens + this.crlf);
					request.flush();
					request.close();

					httpUrlConnection.connect();

					InputStream in = new BufferedInputStream(
							httpUrlConnection.getInputStream());

					data = readStream(TAG, in);
				} finally {
					if (null != httpUrlConnection)
						httpUrlConnection.disconnect();
				}
			}
			return data;
		}

		private String data;
		private boolean success = false;

		@Override
		protected Void doInBackground(final String... params) {
			try {
				data = executeRequest(params[0]);
				success = true;
			} catch (IOException exception) {
				try {
					loginViaReflectSync();
					data = executeRequest(params[0]);
					success = true;
				} catch (IOException e) {
					data = "IOException";
					success = false;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				dialog.dismiss();
			} catch (Exception e) {
				Log.e(TAG, "Error disposing dialog");
			}
			if (success) {
				handler.taskSuccessful(data);
			} else {
				handler.taskFailed(data);
			}
		}
	}

	private static String userId;

	public static void setUsedId(String id) {
		userId = id;
	}

	public static void loginViaReflect(HttpTaskHandler handler) {
		clearCookies();

		// create json object for truefit
		// possible bugs
		JSONObject truefitLoginData = new JSONObject();
		JSONObject truefitUserData = new JSONObject();
		try {
			truefitUserData.put("uid", userId);
			// truefitUserData.put("token", accessToken);
			truefitUserData.put("expires_in", 6340);
			truefitUserData.put("provider", "google");
			truefitLoginData.put("user_data", truefitUserData);
		} catch (JSONException e) {
			Log.e("Login via Reflect", "Error parsing JSON");
		}
		String payload = truefitLoginData.toString();
		new HttpPostTask(handler, payload).execute(NetworkManager.hostName
				+ "/api/authentication/login");
	}

	// We are going to make synchronous calls to login api in order to get new
	// cookies from within the network thread
	private static void loginViaReflectSync() throws IOException {
		Log.e("loginViaReflectSync", "Retry log-in via Reflect");
		clearCookies();

		// create json object for truefit
		// possible bugs
		JSONObject truefitLoginData = new JSONObject();
		JSONObject truefitUserData = new JSONObject();
		try {
			truefitUserData.put("uid", userId);
			// truefitUserData.put("token", accessToken);
			truefitUserData.put("expires_in", 6340);
			truefitUserData.put("provider", "google");
			truefitLoginData.put("user_data", truefitUserData);
		} catch (JSONException e) {
			Log.e("Login via Reflect", "Error parsing JSON");
		}
		String payload = truefitLoginData.toString();
		String url = NetworkManager.hostName + "/api/authentication/login";
		HttpPostTask syncPostTask = new HttpPostTask(null, payload);

		syncPostTask.executeRequest(url);
	}
}
