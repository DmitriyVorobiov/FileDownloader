package com.example.filedownloader;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
	private boolean downloaded = false;
	private URL url;
	private int progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		url = getURL();
		if (url == null)
			return;

		if (getLoaderManager().getLoader(LOADER_ID) != null)
			getLoaderManager().initLoader(LOADER_ID, null, this);

		updateUIIdle();

	}

	private void updateUIIdle() {
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		button.setText(R.string.download);
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		statLabel.setText(R.string.idle);
	}

	private void updateUIDownloading() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(ProgressBar.VISIBLE);
				progressBar.setProgress(progress);
				button.setEnabled(false);
				statLabel.setText(R.string.downloading);
			}
		});

	}

	private void updateUIDownloaded() {
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		button.setEnabled(true);
		button.setText(R.string.open);
		statLabel.setText(R.string.downloaded);
		downloaded = true;
	}

	private URL getURL() {
		URL newURL = null;
		try {
			newURL = new URL(getString(R.string.adress));
		} catch (MalformedURLException e) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.malformedURLException),
					Toast.LENGTH_LONG).show();
		}
		return newURL;
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
		return new ImageLoader(this, url);
	}

	public void onClick(View v) {
		if (!downloaded) {
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		} else
			openFile(Environment.getExternalStorageDirectory()
					+ "/"
					+ Uri.parse(getString(R.string.adress))
							.getLastPathSegment());
	}

	@Override
	public void onLoaderReset(Loader<Bundle> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Bundle> arg0, Bundle message) {
		String response = message.getString(ImageLoader.RESULT_TYPE);
		switch (response) {
		case ImageLoader.PROGRESS: {

			progress = message.getInt(ImageLoader.PROGRESS);
			if (progress == ImageLoader.STATUS_FINISHED) {

				updateUIDownloaded();
				return;
			}
			updateUIDownloading();
			return;
		}

		case ImageLoader.ERROR: {
			Toast.makeText(getApplicationContext(),
					message.getString(ImageLoader.ERROR), Toast.LENGTH_LONG)
					.show();
			updateUIIdle();
			return;
		}

		}
	}
}
