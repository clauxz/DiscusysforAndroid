package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

public class CommentDetailsActivity extends BaseActivity {

	private static final String EXTRA_URI = "extras_uri";
	private TextView commentTextView;
	private ImageView personColorImageView;
	private TextView personNameTextView;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
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
		setContentView(R.layout.activity_comment);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		commentTextView = (TextView) findViewById(R.id.textViewComment);
		personColorImageView = (ImageView) findViewById(R.id.image_person_color);
		personNameTextView = (TextView) findViewById(R.id.text_comment_person_name);
		startCommentsLoader();
	}

	private void startCommentsLoader() {

		CommentLoader loader = new CommentLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(CommentLoader.COMMENT_ID, args, loader);
	}

	private class CommentLoader implements LoaderCallbacks<Cursor> {

		private static final int COMMENT_ID = 0x00;
		private static final int PERSON_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case COMMENT_ID:
					return getCommentLoader(arguments);
				case PERSON_ID:
					return getPersonLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case COMMENT_ID:
					commentTextView.setText("");
					break;
				case PERSON_ID:
					personNameTextView.setText("");
					personColorImageView.setBackgroundColor(0);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case COMMENT_ID:
					swapComment(data);
					break;
				case PERSON_ID:
					swapPerson(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getCommentLoader(final Bundle arguments) {

			Uri commentsUri = getUriFromArguments(arguments);
			return new CursorLoader(CommentDetailsActivity.this, commentsUri, null, null, null, null);
		}

		private CursorLoader getPersonLoader(final Bundle arguments) {

			Uri personUri = getUriFromArguments(arguments);
			return new CursorLoader(CommentDetailsActivity.this, personUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void startPersonLoader(final int personId) {

			CommentLoader loader = new CommentLoader();
			Bundle args = new Bundle();
			args.putParcelable(EXTRA_URI, Persons.buildTableUri(personId));
			getSupportLoaderManager().initLoader(CommentLoader.PERSON_ID, args, loader);
		}

		private void swapComment(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int commentColumn = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
				int personIdColumn = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
				String comment = cursor.getString(commentColumn);
				commentTextView.setText(comment);
				int personId = cursor.getInt(personIdColumn);
				startPersonLoader(personId);
			}
		}

		private void swapPerson(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int personNameColumn = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
				int personColorColumn = cursor.getColumnIndexOrThrow(Persons.Columns.COLOR);
				String name = cursor.getString(personNameColumn);
				personNameTextView.setText(name);
				int color = cursor.getInt(personColorColumn);
				personColorImageView.setBackgroundColor(color);
			}
		}
	}
}