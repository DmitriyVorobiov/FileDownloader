package com.example.filedownloader;

import java.io.File;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LoaderCallbacks<String> {

	private static final int LOADER_ID = 1;
	protected final static String BROADCAST_ACTION = "FileDownloader";
	protected final static String EXTRA_TASK = "task";
	protected final static String EXTRA_PROGRESS = "progress";
	protected final static String EXTRA_ERROR = "error";
	protected final static int TASK_TASK = 1;
	protected final static int TASK_ERROR = 2;
	private int visibility;
	private boolean buttonIsEnabled;
	private ProgressBar progressBar;
	private TextView statLabel;
	private BroadcastReceiver broadcastReceiver;
	private Button button;
	private String fileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fileName = Uri.parse(getString(R.string.adress)).getLastPathSegment();
		getLoaderManager().initLoader(LOADER_ID, null, this);
		setContentView(R.layout.activity_main);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);

		if (savedInstanceState == null) {
			buttonIsEnabled = true;
			visibility = ProgressBar.INVISIBLE;
			statLabel.setText(R.string.idle);
		} else {
			visibility = savedInstanceState.getInt("vis");
			buttonIsEnabled = savedInstanceState.getBoolean("en");
			progressBar.setVisibility(visibility);
			button.setEnabled(buttonIsEnabled);
			String status = savedInstanceState.getString("status");
			statLabel.setText(status);
		}

		File extStore = Environment.getExternalStorageDirectory();
		File file = new File(extStore.getAbsolutePath(), fileName);
		if (file.exists()) {
			button.setText(getResources().getString(R.string.open));
			statLabel.setText(R.string.downloaded);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					openFile(Environment.getExternalStorageDirectory() + "/"
							+ fileName);

				}
			});
			return;
		}

		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startLoading();
			}
		});

		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				int task = intent.getIntExtra(EXTRA_TASK, 0);
				int progress = intent.getIntExtra(EXTRA_PROGRESS, 0);

				if (task == TASK_TASK) {
					progressBar.setProgress(progress);
				}

				if (task == TASK_ERROR) {
					statLabel.setText(R.string.idle);
					visibility = ProgressBar.INVISIBLE;
					progressBar.setVisibility(visibility);
					buttonIsEnabled = true;
					button.setEnabled(buttonIsEnabled);
					String error = intent.getStringExtra(EXTRA_ERROR);
					Toast.makeText(getApplicationContext(), error,
							Toast.LENGTH_LONG).show();
				}
			}

		};
		LocalBroadcastManager.getInstance(this).registerReceiver(
				broadcastReceiver, new IntentFilter(BROADCAST_ACTION));

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("vis", visibility);
		outState.putBoolean("en", buttonIsEnabled);
		outState.putString("status", statLabel.getText().toString());
	}

	public void openFile(String path) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + path);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}

	public Loader<String> onCreateLoader(int id, Bundle args) {
		ImageLoader loader = null;
		if (id == LOADER_ID) {
			loader = new ImageLoader(this);
		}
		return loader;
	}

	public void startLoading() {
		visibility = ProgressBar.VISIBLE;
		progressBar.setVisibility(visibility);
		buttonIsEnabled = false;
		button.setEnabled(buttonIsEnabled);
		statLabel.setText(R.string.downloading);
		Loader<String> loader;
		loader = getLoaderManager().getLoader(LOADER_ID);
		loader = getLoaderManager().restartLoader(LOADER_ID, null, this);
		loader.forceLoad();
	}

	public void onLoadFinished(Loader<String> loader, final String result) {
		if (result == null) {
			return;
		}
		visibility = ProgressBar.INVISIBLE;
		progressBar.setVisibility(visibility);
		buttonIsEnabled = true;
		button.setEnabled(true);
		button.setText(getResources().getString(R.string.open));
		statLabel.setText(R.string.downloaded);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				openFile(result);
			}
		});
	}

	public void onLoaderReset(Loader<String> arg0) {
		// TODO Auto-generated method stub

	}

}
