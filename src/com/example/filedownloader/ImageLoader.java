package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

public class ImageLoader extends ExtendedAsyncTaskLoader {

	protected final static String RESULT_TYPE = "result";
	protected final static int STATUS_FINISHED = 101;
	protected final static String ERROR = "error";
	protected final static String PROGRESS = "progress";
	private Context context;
	private URL url;
	private InputStream input;
	private OutputStream output;
	private URLConnection connection;
	private int currentProgress;

	public ImageLoader(Context context, URL url) {
		super(context);
		this.context = context;
		this.url = url;

	}

	private void sendProgress(int progress) {
		Bundle bndl = new Bundle();
		bndl.putString(RESULT_TYPE, PROGRESS);
		bndl.putInt(PROGRESS, progress);
		deliverResult(bndl);
	}

	@Override
	public Bundle loadInBackground() {
		input = null;
		output = null;
		Bundle result = new Bundle();
		try {
			connection = url.openConnection();
			connection.connect();
		} catch (IOException e) {
			result.putString(RESULT_TYPE, ERROR);
			result.putString(ERROR,
					context.getResources().getString(R.string.IOException));
			return result;
		}
		try {
			int lenghtOfFile = connection.getContentLength();
			input = new BufferedInputStream(url.openStream(), 1024);
			output = new FileOutputStream(
					Environment.getExternalStorageDirectory() + "/"
							+ Uri.parse(url.toString()).getLastPathSegment());
			long total = 0;
			int count;
			byte data[] = new byte[8192];
			while ((count = input.read(data)) != -1) {
				currentProgress = (int) (100d * total / lenghtOfFile);
				total += count;
				sendProgress(currentProgress);
				output.write(data, 0, count);
			}
			output.flush();
			output.close();
			input.close();
		} catch (IOException e) {
			result.putString(RESULT_TYPE, ERROR);
			result.putString(ERROR,
					context.getResources().getString(R.string.fileException));
			return result;
		}
		result.putString(RESULT_TYPE, PROGRESS);
		result.putInt(PROGRESS, STATUS_FINISHED);
		return result;
	}

}
