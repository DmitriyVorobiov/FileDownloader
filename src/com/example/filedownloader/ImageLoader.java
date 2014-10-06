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
	private Context context;
	private InputStream input;
	private OutputStream output;
	private URLConnection conection;

	public ImageLoader(Context context) {
		super(context);
		this.context = context;
		try {
			url = new URL(context.getString(R.string.adress));
		} catch (MalformedURLException e) {
			sendErrorBroadcast(MainActivity.TASK_ERROR,
					context.getString(R.string.malformedURLException));
		}
	}

	/**
	 * Returns path, if downloading was successful, else returns null
	 */
	@Override
	public String loadInBackground() {
		if (url == null) {
			return (null);
		}
		String path = Environment.getExternalStorageDirectory()
				+ "/"
				+ Uri.parse(context.getString(R.string.adress))
						.getLastPathSegment();
		input = null;
		output = null;
		try {
			conection = url.openConnection();
			conection.connect();
		} catch (IOException e) {
			sendErrorBroadcast(MainActivity.TASK_ERROR,
					context.getString(R.string.IOException));
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

				Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
				intent.putExtra(MainActivity.EXTRA_TASK, MainActivity.TASK_TASK);
				intent.putExtra(
						MainActivity.EXTRA_PROGRESS,
						Integer.parseInt(""
								+ (int) ((total * 100) / lenghtOfFile)));
				LocalBroadcastManager.getInstance(context)
						.sendBroadcast(intent);

				output.write(data, 0, count);
			}

		} catch (Exception e) {
			sendErrorBroadcast(MainActivity.TASK_ERROR,
					context.getString(R.string.fileException));
			return (null);
		} finally {
			try {
				output.flush();
				output.close();
				input.close();
			} catch (IOException e) {
				sendErrorBroadcast(MainActivity.TASK_ERROR,
						context.getString(R.string.fileException));
				return null;
			}
		}
		return (path);
	}

	private void sendErrorBroadcast(int param, String message) {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
		intent.putExtra(MainActivity.EXTRA_TASK, param);
		intent.putExtra(MainActivity.EXTRA_ERROR, message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
