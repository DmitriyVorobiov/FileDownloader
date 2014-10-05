package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

public class ImageLoader extends AsyncTaskLoader<String> {

	private URL url;
	public final static int TASK_TASK = 1;
	public final static int TASK_ERROR = 2;
	Context context;
	boolean started = false;
	InputStream input;
	OutputStream output;
	URLConnection conection;

	public ImageLoader(Context context) {
		super(context);
		this.context = context;
		try {
			url = new URL(context.getString(R.string.adress));
		} catch (MalformedURLException e) {
			sendBroadcast(TASK_ERROR, "incorrect url specification"); // hardcoding
		}
	}

	@Override
	public String loadInBackground() {
		if (url == null) {
			return (null);
		}
		String path = Environment.getExternalStorageDirectory()
				+ Uri.parse(context.getString(R.string.adress))
						.getLastPathSegment();
		input = null;
		output = null;
		try {
			conection = url.openConnection();
			conection.connect();
		} catch (IOException e) {
			sendBroadcast(TASK_ERROR,
					"Unable to open connection to the resource"); // hardcoding
			return null;
		}

		try {
			int lenghtOfFile = conection.getContentLength();
			input = new BufferedInputStream(url.openStream(), 1024);
			output = new FileOutputStream(path);

			long total = 0;
			int count;
			byte data[] = new byte[1024];

			while ((count = input.read(data)) != -1) {
				total += count;
				sendBroadcast(TASK_TASK, ""
						+ (int) ((total * 100) / lenghtOfFile));
				output.write(data, 0, count);
			}

		} catch (IOException e) {
			sendBroadcast(TASK_ERROR, "Unable to write file");// hardcoding
			return (null);
		} finally {
			try {
			output.flush();
			output.close();
			input.close();
			}
			catch (IOException e) {
				sendBroadcast(TASK_ERROR, "An error occurs while working whis stream");// hardcoding
				return null;
			}
		}
		return (path);
	}

	private void sendBroadcast(int param, String message) {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
		intent.putExtra(MainActivity.EXTRA_TASK, param);
		intent.putExtra(MainActivity.EXTRA_ERROR, message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
