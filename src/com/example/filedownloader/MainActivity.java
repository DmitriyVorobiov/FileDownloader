package com.example.filedownloader;

import java.io.File;

import android.R;
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
	private final String VISIBILITY = "visibility";
	private final String AVAILABILITY = "enable";
	private final String STATUS = "status";
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
		setContentView(R.layout.activity_main);

		getLoaderManager().initLoader(LOADER_ID, null, this);

		fileName = Uri.parse(getString(R.string.adress)).getLastPathSegment();
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);

		if (savedInstanceState == null) {
			buttonIsEnabled = true;
			visibility = ProgressBar.INVISIBLE;
			statLabel.setText(R.string.idle);
		} else {
			//Этого ничего не надо - андроид сам запоминает состояние контролов в savedInstanceState
			visibility = savedInstanceState.getInt(VISIBILITY);
			buttonIsEnabled = savedInstanceState.getBoolean(AVAILABILITY);
			progressBar.setVisibility(visibility);
			button.setEnabled(buttonIsEnabled);
			String status = savedInstanceState.getString(STATUS);
			statLabel.setText(status);
		}

		File extStore = Environment.getExternalStorageDirectory();
		File file = new File(extStore.getAbsolutePath(), fileName);
		if (file.exists()) {
			button.setText(getResources().getString(R.string.open)); //пойдет и просто button.setText(R.string.open);
			statLabel.setText(R.string.downloaded);

			//в целом плохо, что setOnClickListener сетится в нескольких местах 
			button.setOnClickListener(new OnClickListener() {

				@Override
                public void onClick(View v) {
					openFile(Environment.getExternalStorageDirectory() + "/"
							+ fileName);

				}
			});
			return;
		}

		button.setOnClickListener(new OnClickListener() {

			@Override
            public void onClick(View v) {
				startLoading();
			}
		});

		//можно обойтись и без броадкастов
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

	@Override
    protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Этого ничего не надо - андроид сам запоминает состояние контролов в savedInstanceState
		outState.putInt(VISIBILITY, visibility);
		outState.putBoolean(AVAILABILITY, buttonIsEnabled);
		outState.putString(STATUS, statLabel.getText().toString());
	}

	public void openFile(String path) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + path);
		intent.setDataAndType(uri, "image/*");
		startActivity(intent);
	}

	@Override
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

		//http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
		Loader<String> loader;
		loader = getLoaderManager().getLoader(LOADER_ID);
		loader = getLoaderManager().restartLoader(LOADER_ID, null, this);
		loader.forceLoad();
	}

	@Override
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

			@Override
            public void onClick(View v) {
				openFile(result);
			}
		});
	}

	@Override
    public void onLoaderReset(Loader<String> arg0) {
		// TODO Auto-generated method stub

	}

}
