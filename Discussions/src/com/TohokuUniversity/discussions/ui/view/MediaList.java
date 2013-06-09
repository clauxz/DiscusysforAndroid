package com.TohokuUniversity.discussions.ui.view;

import com.TohokuUniversity.discussions.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.TohokuUniversity.discussions.ApplicationConstants;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Attachments;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Attachments.AttachmentType;
import com.TohokuUniversity.discussions.service.FileDownloader;
import com.TohokuUniversity.discussions.ui.IntentHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

@Deprecated
public class MediaList extends ListView {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = MediaList.class.getSimpleName();
	private final ImageLoader imageLoader;
	private SimpleCursorAdapter mAttachmentsAdapter;
	private final Context mContext;
	private int postionOffset = 0;

	public MediaList(final Context context) {

		this(context, null);
	}

	public MediaList(final Context context, final AttributeSet attrs) {

		this(context, attrs, android.R.attr.listViewStyle);
	}

	public MediaList(final Context context, final AttributeSet attrs, final int defStyle) {

		super(context, attrs, defStyle);
		mContext = context;
		imageLoader = ImageLoader.getInstance();
		setOnItemClickListener(new AttachmentsItemClickListener());
	}

	@Override
	public SimpleCursorAdapter getAdapter() {

		return mAttachmentsAdapter;
	}

	public void setAttachmentsAdapter() {

		mAttachmentsAdapter = new SimpleCursorAdapter(mContext, R.layout.list_item_media, null, new String[] {
				Attachments.Columns.TITLE, Attachments.Columns.ID, Attachments.Columns.FORMAT }, new int[] {
				R.id.text_attachment_name, R.id.image_attachment_preview, R.id.image_attachment_filetype }, 0);
		mAttachmentsAdapter.setViewBinder(new AttachmentsViewBinder());
		setAdapter(mAttachmentsAdapter);
	}

	public void setPositionOffset(final int positionOffset) {

		postionOffset = positionOffset;
	}

	private class AttachmentsItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(final AdapterView<?> arg0, final View v, final int position, final long id) {

			Cursor cursor = mAttachmentsAdapter.getCursor();
			if ((cursor != null) && cursor.moveToPosition(position - postionOffset)) {
				int formatColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
				int attachmentFormat = cursor.getInt(formatColumnIndex);
				switch (attachmentFormat) {
					case AttachmentType.JPG:
					case AttachmentType.PNG:
					case AttachmentType.BMP:
					case AttachmentType.PNG_SCREENSHOT:
						fireFullImageIntent(cursor);
						break;
					case AttachmentType.YOUTUBE:
						fireYoutubeIntent(cursor);
						break;
					case AttachmentType.PDF:
						firePdfDownloadIntent(cursor);
						break;
					case AttachmentType.GENERAL_WEB_LINK:
					case AttachmentType.NONE:
						Log.d(TAG, "[onItemClick] clicked on format: " + attachmentFormat);
						break;
					default:
						Log.e(TAG, "[onItemClick] unknown attachment format: " + attachmentFormat);
						break;
				}
			}
		}

		private void firePdfDownloadIntent(final Cursor cursor) {

			logd("[firePdfDownloadIntent]");
			Intent testPdfIntent = IntentHelper.getViewPdfIntent("test.pdf");
			if (isIntentAvailable(testPdfIntent)) {
				int idColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.ID);
				final int valueId = cursor.getInt(idColumn);
				String pdfUrl = Attachments.getAttachmentDownloadLink(mContext, valueId);
				logd("[firePdfDownloadIntent] pdfUrl: " + pdfUrl);
				Uri uri = Uri.parse(pdfUrl);
				String fileName = Attachments.getPdfAttachmentFileName(uri);
				logd("[firePdfDownloadIntent] fileName: " + fileName);
				if (FileDownloader.hasFileDownloaded(fileName)) {
					Intent intent = IntentHelper.getViewPdfIntent(fileName);
					mContext.startActivity(intent);
				} else {
					new DownloadPdfTask().execute(pdfUrl);
				}
			} else {
				showPdfViewerNeedToBeInstalledDialog();
			}
		}

		private boolean isIntentAvailable(final Intent intent) {

			final PackageManager packageManager = mContext.getPackageManager();
			List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		}

		private void fireFullImageIntent(final Cursor cursor) {

			int idColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.ID);
			final int valueId = cursor.getInt(idColumn);
			Intent intent = new Intent(Intent.ACTION_VIEW, Attachments.buildTableUri(valueId));
			mContext.startActivity(intent);
		}

		private void fireYoutubeIntent(final Cursor cursor) {

			int youtubeVideoColumn = cursor.getColumnIndexOrThrow(Attachments.Columns.VIDEO_LINK_URL);
			final String youtubeLink = cursor.getString(youtubeVideoColumn);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink));
			mContext.startActivity(intent);
		}

		private void showPdfViewerNeedToBeInstalledDialog() {

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage(R.string.dialog_text_pdf_viewer_need_install_first).setCancelable(true)
					.setPositiveButton(R.string.button_title_go_to_market,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(final DialogInterface dialog, final int id) {

									String marketUrl = "market://search?q=pdf+reader&c=apps";
									Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl));
									market.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									mContext.startActivity(market);
								}
							}).setNegativeButton(R.string.button_title_cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(final DialogInterface dialog, final int id) {

									dialog.cancel();
								}
							});
			builder.create().show();
		}
	}

	private class AttachmentsViewBinder implements ViewBinder {

		private final byte[] buffer = new byte[16 * 1024];

		@Override
		public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

			switch (view.getId()) {
				case R.id.image_attachment_preview:
					setPreviewImage((ImageView) view, cursor, columnIndex);
					return true;
				case R.id.text_attachment_name:
					((TextView) view).setText(cursor.getString(columnIndex));
					return true;
				case R.id.image_attachment_filetype:
					setFiletypeImage((ImageView) view, cursor, columnIndex);
					return true;
				default:
					Log.e(TAG, "[setViewValue] unknown view: " + view.getId());
					return false;
			}
		}

		private void setFiletypeImage(final ImageView imageView, final Cursor cursor, final int columnIndex) {

			int attachmentFormat = cursor.getInt(columnIndex);
			switch (attachmentFormat) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
					imageView.setImageResource(R.drawable.ic_filetype_image);
					break;
				case AttachmentType.PNG_SCREENSHOT:
					imageView.setImageResource(R.drawable.ic_filetype_screenshot);
					break;
				case AttachmentType.YOUTUBE:
					imageView.setImageResource(R.drawable.ic_filetype_youtube);
					break;
				case AttachmentType.PDF:
					imageView.setImageResource(R.drawable.ic_filetype_pdf);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					imageView.setImageResource(R.drawable.stub);
					break;
				default:
					Log.e(TAG, "[setPreviewImage] unknown attachment format: " + attachmentFormat);
					imageView.setImageResource(R.drawable.stub);
					break;
			}
		}

		private void setPreviewImage(final ImageView imageView, final Cursor cursor, final int columnIndex) {

			int formatColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
			int attachmentFormat = cursor.getInt(formatColumnIndex);
			switch (attachmentFormat) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
				case AttachmentType.PNG_SCREENSHOT:
					final int valueId = cursor.getInt(columnIndex);
					String urlString = Attachments.getAttachmentDownloadLink(mContext, valueId);
					imageLoader.displayImage(urlString, imageView);
					break;
				case AttachmentType.YOUTUBE:
					int youtubeThumbColumn = cursor
							.getColumnIndexOrThrow(Attachments.Columns.VIDEO_THUMB_URL);
					final String youtubeThumbString = cursor.getString(youtubeThumbColumn);
					imageLoader.displayImage(youtubeThumbString, imageView);
					break;
				case AttachmentType.PDF:
					int dataColumnIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.THUMB);
					byte[] thumbData = cursor.getBlob(dataColumnIndex);
					if (thumbData == null) {
						imageView.setImageResource(R.drawable.ic_attachment_pdf);
						break;
					}
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inTempStorage = buffer;
					options.inDither = false;
					options.inPurgeable = true;
					options.inInputShareable = true;
					options.inSampleSize = 2;
					Bitmap bitmap = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
					imageView.setImageBitmap(bitmap);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					imageView.setImageResource(R.drawable.stub);
					break;
				default:
					Log.e(TAG, "[setPreviewImage] unknown attachment format: " + attachmentFormat);
					imageView.setImageResource(R.drawable.stub);
					break;
			}
		}
	}

	private class DownloadPdfTask extends AsyncTask<String, Void, Intent> {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			dialog = ProgressDialog.show(mContext, "", "Downloading file...", true);
			dialog.show();
		}

		@Override
		protected Intent doInBackground(final String... params) {

			String pdfUrl = params[0];
			Uri uri = Uri.parse(pdfUrl);
			logd("[downloadPdfFile] url: " + uri.toString());
			String fileName = Attachments.getPdfAttachmentFileName(uri);
			logd("[dowloadPdfFile] fileName: " + fileName);
			FileDownloader.downloadFromUrl(uri.toString(), fileName);
			Intent pdfViewIntent = IntentHelper.getViewPdfIntent(fileName);
			return pdfViewIntent;
		}

		@Override
		protected void onPostExecute(final Intent result) {

			super.onPostExecute(result);
			dialog.dismiss();
			logd(result.getAction());
			mContext.startActivity(result);
		}
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}
}
