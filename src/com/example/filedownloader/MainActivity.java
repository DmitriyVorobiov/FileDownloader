package com.example.filedownloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LoaderCallbacks<Bundle>,
		OnClickListener {

	private static final int LOADER_ID = 1;
	private ProgressBar progressBar;
	private TextView statLabel;
	private Button button;
	private String path;
	private Loader<Bundle> loader;
	private boolean downloaded = false;
	private Handler handler;
	private URL url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		try {
			url = new URL(getString(R.string.adress));
		} catch (MalformedURLException e) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.malformedURLException),
					Toast.LENGTH_LONG).show();
			return;
		}

		loader = getLoaderManager().initLoader(LOADER_ID, null, this);

		path = Environment.getExternalStorageDirectory() + "/"
				+ Uri.parse(getString(R.string.adress)).getLastPathSegment();

		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath(), Uri.parse(getString(R.string.adress))
				.getLastPathSegment());
		if (file.exists()) {
			 progressBar.setVisibility(ProgressBar.INVISIBLE);
			 button.setEnabled(true);
			 button.setText(R.string.open);
			 statLabel.setText(R.string.downloaded);
			 downloaded = true;
			 return;
		}

		progressBar.setVisibility(ProgressBar.INVISIBLE);
		button.setText(R.string.download);
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		statLabel.setText(R.string.idle);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int progress = msg.what;
				progressBar.setVisibility(ProgressBar.VISIBLE);
				progressBar.setProgress(progress);
				button.setEnabled(false);
				statLabel.setText(R.string.downloading);
			}
		};

	}

	public void openFile(String path) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + path);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}
	
	@Override
	public Loader<Bundle> onCreateLoader(int id, Bundle args) {
		loader = new ImageLoader(this, url);
		return loader;
	}

	public void onClick(View v) {
		if (!downloaded) {
			loader.forceLoad();
		} else
			openFile(path);
	}

	@Override
	public void onLoaderReset(Loader<Bundle> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Bundle> arg0, Bundle message) {
		String response = message.getString(ImageLoader.RESULT_TYPE);
		switch (response) {
		case ImageLoader.PROGRESS: {

			int progress = message.getInt(ImageLoader.PROGRESS);
			if (progress == ImageLoader.STATUS_FINISHED) {
				progressBar.setVisibility(ProgressBar.INVISIBLE);
				button.setEnabled(true);
				button.setText(R.string.open);
				statLabel.setText(R.string.downloaded);
				downloaded = true;
				return;
			}
			handler.sendEmptyMessage(progress);
			return;
		}

		case ImageLoader.ERROR: {

			File file = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath(), Uri.parse(getString(R.string.adress))
					.getLastPathSegment());
			if (file.exists()) {
				file.delete();
			}
			Toast.makeText(getApplicationContext(),
					message.getString(ImageLoader.ERROR), Toast.LENGTH_LONG)
					.show();
			button.setEnabled(true);
			button.setText(R.string.download);
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			statLabel.setText(R.string.idle);
			return;
		}

		}
	}
}
