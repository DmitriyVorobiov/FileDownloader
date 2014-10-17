package com.example.filedownloader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;

public class ExtendedAsyncTaskLoader extends AsyncTaskLoader<Bundle> {

	private Bundle mData;

	public ExtendedAsyncTaskLoader(Context ctx) {
		super(ctx);
	}

	@Override
	public Bundle loadInBackground() {
		return null;
	}

	@Override
	public void deliverResult(Bundle data) {
		if (isReset()) {
			releaseResources(data);
			return;
		}
		Bundle oldData = mData;
		mData = data;
		if (isStarted()) {
			super.deliverResult(data);
		}

		if (oldData != null && oldData != data) {
			releaseResources(oldData);
		}
	}

	@Override
	protected void onStartLoading() {
		if (mData != null) {
			deliverResult(mData);
		}

		if (takeContentChanged() || mData == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		onStopLoading();

		if (mData != null) {
			releaseResources(mData);
		}
	}

	@Override
	public void onCanceled(Bundle data) {
		super.onCanceled(data);
		releaseResources(data);
	}

	private void releaseResources(Bundle data) {
		data = null;
	}

}
