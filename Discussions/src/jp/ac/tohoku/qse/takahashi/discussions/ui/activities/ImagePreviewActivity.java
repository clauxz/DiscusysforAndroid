package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import jp.ac.tohoku.qse.takahashi.discussions.ui.view.TouchImageView;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ImagePreviewActivity extends BaseActivity {

	private static final String EXTRA_URI = "EXTRA_URI";
	private ImageLoader imageLoader;
	private TouchImageView imageView;
	private SelectedPoint mSelectedPoint;
	private int mAttachmentId;

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
			MenuInflater menuInflater = getSupportMenuInflater();
			menuInflater.inflate(R.menu.actionbar_image_preview, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_delete:
				getServiceHelper().deleteAttachment(mAttachmentId, mSelectedPoint);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_preview);
		imageView = (TouchImageView) findViewById(R.id.iv_full_image);
		imageLoader = ImageLoader.getInstance();
		if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
			mSelectedPoint = getIntent().getParcelableExtra(ExtraKey.SELECTED_POINT);
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		startAttachmentImageLoader();
		String attachmentId = Attachments.getValueId(getIntent().getData());
		mAttachmentId = Integer.parseInt(attachmentId);
		String uriString = Attachments.getAttachmentDownloadLink(this, attachmentId);
		imageLoader.displayImage(uriString, imageView, new ImageLoadingListener() {

			@Override
			public void onLoadingStarted() {

				// TODO Auto-generated method stub
			}

			@Override
			public void onLoadingFailed(final FailReason failReason) {

				Toast.makeText(ImagePreviewActivity.this, "Failed to load image: " + failReason.name(),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onLoadingComplete(final Bitmap loadedImage) {

				// TODO Auto-generated method stub
			}

			@Override
			public void onLoadingCancelled() {

				// TODO Auto-generated method stub
			}
		});
	}

	private void startAttachmentImageLoader() {

		AttachmentImageCursorLoader loader = new AttachmentImageCursorLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(AttachmentImageCursorLoader.ATTACHMENT_ID, args, loader);
	}

	private class AttachmentImageCursorLoader implements LoaderCallbacks<Cursor> {

		private static final int ATTACHMENT_ID = 0x00;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case ATTACHMENT_ID:
					return getAttachmentCursorLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case ATTACHMENT_ID:
					getSupportActionBar().setTitle("");
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case ATTACHMENT_ID:
					swapAttachmentTitle(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getAttachmentCursorLoader(final Bundle arguments) {

			Uri attachmentUri = getUriFromArguments(arguments);
			return new CursorLoader(ImagePreviewActivity.this, attachmentUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void swapAttachmentTitle(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int titleColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.TITLE);
				String title = cursor.getString(titleColumn);
				getSupportActionBar().setTitle(title);
			}
		}
	}
}