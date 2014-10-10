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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class ImageLoader extends AsyncTaskLoader<Void> {

	private URL url;
	protected static String URL = "url";
	protected final static int STATUS_ERROR = -1;
	protected final static int STATUS_IDLE = 0;
	protected final static int STATUS_DOWNLOADING = 1;
	protected final static int STATUS_DOWNLOADED = 2;
	protected final static String ERROR = "error";
	protected final static String PROGRESS = "progress";
	private Context context;
	private InputStream input;
	private OutputStream output;
	private URLConnection conection;
	private Handler handler;
	private int currentProgress;
	private int status;
	private String path;
	private String errorMessage;

	public ImageLoader(Context context, Bundle args, Handler headHandler) {
		super(context);
		this.context = context;
		this.handler = headHandler;
		try {
			url = new URL(args.getString(URL));
			path = Environment.getExternalStorageDirectory() + "/"
					+ Uri.parse(args.getString(URL)).getLastPathSegment();
		} catch (MalformedURLException e) {
			errorMessage = context.getString(R.string.malformedURLException);
			sendMessage(STATUS_ERROR, errorMessage, ERROR);
		}
	}

	@Override
	public Void loadInBackground() {
		status = STATUS_DOWNLOADING;
		if (url == null) {
			return (null);
		}
		input = null;
		output = null;
		try {
			conection = url.openConnection();
			conection.connect();
		} catch (IOException e) {
			errorMessage = context.getString(R.string.IOException);
			sendMessage(STATUS_ERROR, errorMessage, ERROR);
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
				currentProgress = (int) (100d * total / lenghtOfFile);
				total += count;
				sendMessage(STATUS_DOWNLOADING,
						String.valueOf(currentProgress), PROGRESS);
				output.write(data, 0, count);
			}

		} catch (Exception e) {
			errorMessage = context.getString(R.string.fileException);
			sendMessage(STATUS_ERROR, errorMessage, ERROR);
			return (null);
		} finally {
			try {
				output.flush();
				output.close();
				input.close();
			} catch (IOException e) {
				errorMessage = context.getString(R.string.fileException);
				sendMessage(STATUS_ERROR, errorMessage, ERROR);
				return null;
			}
		}
		handler.sendEmptyMessage(STATUS_DOWNLOADED);
		status = STATUS_DOWNLOADED;
		return null;
	}

	private void sendMessage(int what, String data, String key) {
		String sendingData = data;
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(key, sendingData);
		message.setData(bundle);
		message.what = what;
		handler.sendMessage(message);
	}

	protected int getStatus() {
		return status;
	}

	protected void setHandler(Handler handler) {
		this.handler = handler;
	}
}
