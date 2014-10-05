package com.example.filedownloader;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	final String LOG_TAG = "MainActivity";
	static final int LOADER_ID = 1;
	public final static String BROADCAST_ACTION = "Task2";
	public final static String EXTRA_TASK = "task";
	public final static String EXTRA_PROGRESS = "progress"; // not public
	public final static String EXTRA_ERROR = "error";
	public final static int TASK_TASK = 1;
	public final static int TASK_ERROR = 2; // make static and delete in loader
	private int visibility;
	private boolean buttonIsEnabled;
	private ProgressBar progressBar;
	private TextView statLabel;
	private BroadcastReceiver broadcastReceiver;
	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		statLabel = (TextView) findViewById(R.id.statusLabel);
		button = (Button) findViewById(R.id.button);

		File extStore = Environment.getExternalStorageDirectory();
		File file = new File(extStore.getAbsolutePath(), "myimage.png");
		if (file.exists()) {
			button.setText(getResources().getString(R.string.open));
			statLabel.setText(R.string.downloaded);
			button.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					openFile(Environment.getExternalStorageDirectory()
							+ "/myimage.png");

				}
			});
			return;
		}

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

	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("vis", visibility);
		outState.putBoolean("en", buttonIsEnabled);
		outState.putString("status", statLabel.getText().toString());
	}

}
