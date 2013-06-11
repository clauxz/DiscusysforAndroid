package jp.ac.tohoku.qse.takahashi.discussions.ui.adapter;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments.AttachmentType;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MediaAdapter extends CursorAdapter {

	private final ImageLoader imageLoader;
	private final byte[] buffer = new byte[16 * 1024];

	public MediaAdapter(final Context context, final Cursor c) {

		super(context, c, 0);
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {

		LayoutParams layoutParams = parent.getLayoutParams();
		MyLog.tempv("[newView] patent width: " + layoutParams.width + ", height: " + layoutParams.height);
		LayoutInflater inflater = LayoutInflater.from(context);
		View containerView = inflater.inflate(R.layout.grid_item_media, parent, false);
		ViewHolder viewHolder = new ViewHolder(containerView);
		containerView.setTag(viewHolder);
		// make item square proportion
		// LayoutParams layoutParams = containerView.getLayoutParams();
		// layoutParams.height = layoutParams.width;
		// containerView.setLayoutParams(layoutParams);
		return containerView;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {

		ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.populateData(cursor);
	}

	private class ViewHolder {

		private final ImageView mediaImageView;
		private final ImageView formatImageView;
		private final TextView titleTextView;

		public ViewHolder(final View container) {

			titleTextView = (TextView) container.findViewById(R.id.text_attachment_name);
			mediaImageView = (ImageView) container.findViewById(R.id.image_attachment_preview);
			formatImageView = (ImageView) container.findViewById(R.id.image_attachment_filetype);
		}

		private void populateData(final Cursor cursor) {

			populateTitle(cursor);
			populateFormat(cursor);
			populateMedia(cursor);
		}

		private void populateTitle(final Cursor cursor) {

			int titleIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.TITLE);
			String title = cursor.getString(titleIndex);
			titleTextView.setText(title);
		}

		private void populateFormat(final Cursor cursor) {

			int formatIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
			int format = cursor.getInt(formatIndex);
			switch (format) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
					formatImageView.setImageResource(R.drawable.ic_filetype_image_white);
					break;
				case AttachmentType.PNG_SCREENSHOT:
					formatImageView.setImageResource(R.drawable.ic_filetype_screenshot_white);
					break;
				case AttachmentType.YOUTUBE:
					formatImageView.setImageResource(R.drawable.ic_filetype_youtube_white);
					break;
				case AttachmentType.PDF:
					formatImageView.setImageResource(R.drawable.ic_filetype_pdf_white);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					formatImageView.setImageResource(R.drawable.stub);
					break;
				default:
					formatImageView.setImageResource(R.drawable.stub);
					break;
			}
		}

		private void populateMedia(final Cursor cursor) {

			int formatIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.FORMAT);
			int format = cursor.getInt(formatIndex);
			switch (format) {
				case AttachmentType.JPG:
				case AttachmentType.PNG:
				case AttachmentType.BMP:
				case AttachmentType.PNG_SCREENSHOT:
					populateMediaImage(cursor);
					break;
				case AttachmentType.YOUTUBE:
					populateMediaYoutube(cursor);
					break;
				case AttachmentType.PDF:
					populateMediaPdf(cursor);
					break;
				case AttachmentType.GENERAL_WEB_LINK:
				case AttachmentType.NONE:
					mediaImageView.setImageResource(R.drawable.stub);
					break;
				default:
					mediaImageView.setImageResource(R.drawable.stub);
					break;
			}
		}

		private void populateMediaYoutube(final Cursor cursor) {

			int thumbUrlIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.VIDEO_THUMB_URL);
			String thumbUrl = cursor.getString(thumbUrlIndex);
			imageLoader.displayImage(thumbUrl, mediaImageView);
		}

		private void populateMediaImage(final Cursor cursor) {

			int idIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.ID);
			int id = cursor.getInt(idIndex);
			String imageUrl = Attachments.getAttachmentDownloadLink(mContext, id);
			imageLoader.displayImage(imageUrl, mediaImageView);
		}

		private void populateMediaPdf(final Cursor cursor) {

			int thumbIndex = cursor.getColumnIndexOrThrow(Attachments.Columns.THUMB);
			byte[] thumb = cursor.getBlob(thumbIndex);
			if (thumb == null) {
				mediaImageView.setImageResource(R.drawable.ic_attachment_pdf);
				return;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = buffer;
			options.inDither = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inSampleSize = 2;
			Bitmap bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.length, options);
			mediaImageView.setImageBitmap(bitmap);
		}
	}
}
