package com.example.filedownloader;

import java.io.File;

import android.annotation.SuppressLint;
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

public class MainActivity extends Activity implements LoaderCallbacks<Void>,
		OnClickListener {

	private static final int LOADER_ID = 1;
	private int STATUS;
	private ProgressBar progressBar;
	private TextView statLabel;
	private Button button;
	private Handler handler;
	private String path;
	private Loader<Void> loader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);
		button.setOnClickListener(this);
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		path = Environment.getExternalStorageDirectory() + "/"
				+ Uri.parse(getString(R.string.adress)).getLastPathSegment();

		// checked to avoid leaks
		if (handler == null)
			initHandler();

		File extStore = Environment.getExternalStorageDirectory();
		File file = new File(extStore.getAbsolutePath(), Uri.parse(
				getString(R.string.adress)).getLastPathSegment());
		if (file.exists()) {
			handler.sendEmptyMessage(ImageLoader.STATUS_DOWNLOADED);
			return;
		}

		getLoaderManager().initLoader(LOADER_ID, null, this);
		if (loader == null) {
			loader = getLoaderManager().getLoader(LOADER_ID);
			((ImageLoader) loader).setHandler(handler);
		}
		STATUS = ((ImageLoader) loader).getStatus();
		handler.sendEmptyMessage(STATUS);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		// to avoid memory leaks i create handler only once
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				STATUS = msg.what;
				switch (STATUS) {
				case ImageLoader.STATUS_IDLE: {
					button.setEnabled(true);
					button.setText(R.string.download);
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					statLabel.setText(R.string.idle);
				}
					break;
				case ImageLoader.STATUS_DOWNLOADING: {

					int progress = Integer.parseInt(msg.getData().getString(
							ImageLoader.PROGRESS));
					progressBar.setVisibility(ProgressBar.VISIBLE);
					progressBar.setProgress(progress);
					button.setEnabled(false);
					statLabel.setText(R.string.downloading);
				}
					break;

				case ImageLoader.STATUS_DOWNLOADED: {
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					button.setEnabled(true);
					button.setText(R.string.open);
					statLabel.setText(R.string.downloaded);

				}
					break;

				case ImageLoader.STATUS_ERROR: {
					String error = msg.getData().getString(ImageLoader.ERROR);
					Toast.makeText(getApplicationContext(), error,
							Toast.LENGTH_LONG).show();

					loader = getLoaderManager().restartLoader(LOADER_ID, null,
							MainActivity.this);

					this.sendEmptyMessage(ImageLoader.STATUS_IDLE);
				}
					break;
				}
			}

		};
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void openFile(String path) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + path);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}

	public Loader<Void> onCreateLoader(int id, Bundle args) {
		Bundle bundle = new Bundle();
		bundle.putString(ImageLoader.URL, getString(R.string.adress));
		loader = new ImageLoader(this, bundle, handler);
		return loader;
	}

	public void onClick(View v) {
		switch (STATUS) {
		case (ImageLoader.STATUS_IDLE): {
			loader.forceLoad();
		}
			break;
		case (ImageLoader.STATUS_DOWNLOADED): {
			openFile(path);
		}
			break;
		}
	}

	public void onLoaderReset(Loader<Void> arg0) {
	}

	public void onLoadFinished(Loader<Void> arg0, Void arg1) {
	}
}
