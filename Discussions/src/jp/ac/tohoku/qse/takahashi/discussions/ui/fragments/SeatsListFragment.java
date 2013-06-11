package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.OdataWriteClient;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.PersonsTopics;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Seats;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Topics;
import jp.ac.tohoku.qse.takahashi.discussions.photon.constants.DeviceType;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import org.odata4j.core.OEntity;

public class SeatsListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = SeatsListFragment.class.getSimpleName();
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int EMPTY_STRING_RES_ID = R.string.text_empty_seats_list;
	/** This is the Adapter being used to display the list's data. */
	private SimpleCursorAdapter mAdapter;

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void validateExtras(final Bundle extras) {

		if (extras == null) {
			throw new NullPointerException("Extras was null");
		}
		if (!extras.containsKey(ExtraKey.SESSION_ID)) {
			throw new IllegalArgumentException("Extras doesnt contain session id");
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		// Give some text to display if there is no data.
		setEmptyText(getResources().getString(EMPTY_STRING_RES_ID));
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_seat_linear, null, new String[] {
				Seats.Columns.NAME, Seats.Columns.COLOR, Persons.Columns.NAME }, new int[] {
				R.id.list_item_text, R.id.image_person_color, R.id.list_item_sub_text }, 0);
		mAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.list_item_sub_text: {
						TextView textView = (TextView) view;
						String personName = cursor.getString(columnIndex);
						if (TextUtils.isEmpty(personName)) {
							textView.setText(getString(R.string.text_empty_seat_person));
						} else {
							textView.setText(personName);
						}
						return true;
					}
					case R.id.image_person_color: {
						ImageView colorView = (ImageView) view;
						colorView.setBackgroundColor(cursor.getInt(columnIndex));
						return true;
					}
					case R.id.list_item_text: {
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					}
					default:
						return false;
				}
			}
		});
		setListAdapter(mAdapter);
		// Start out with a progress indicator.
		setListShown(false);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor != null) {
			int personNameColIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
			String personName = cursor.getString(personNameColIndex);
			if (TextUtils.isEmpty(personName)) {
				int seatId = getSeatId(position);
				int color = getItemColor(position);
				createExperientUser(seatId, color);
			} else {
				int color = getItemColor(position);
				int personIdColIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
				int personId = cursor.getInt(personIdColIndex);
				Intent intent = createDiscussionIntent(personName, color, personId);
				getActivity().startActivity(intent);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		Uri uri = Seats.buildSeatsPersonsUri(getSessionId());
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.
		mAdapter.swapCursor(data);
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	protected int getItemColor(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			throw new NullPointerException("Cant read seat color from null cursor");
		}
		int columnIndex = cursor.getColumnIndexOrThrow(Seats.Columns.COLOR);
		return cursor.getInt(columnIndex);
	}

	protected int getSeatId(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return -1;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
		long id = cursor.getLong(columnIndex);
		String where = BaseColumns._ID + "=" + String.valueOf(id);
		Cursor seat = getActivity().getContentResolver().query(Seats.CONTENT_URI, null, where, null, null);
		int seatId = Integer.MIN_VALUE;
		if (seat.moveToFirst()) {
			int idColumnIndex = seat.getColumnIndexOrThrow(Seats.Columns.ID);
			seatId = seat.getInt(idColumnIndex);
		}
		seat.close();
		return seatId;
	}

	private int getSessionId() {

		Bundle extras = getActivity().getIntent().getExtras();
		return extras.getInt(ExtraKey.SESSION_ID, Integer.MIN_VALUE);
	}

	private Intent createDiscussionIntent(final String personName, final int personColor, final int personId) {

		Bundle extras = getActivity().getIntent().getExtras();
		validateExtras(extras);
		Uri uri = Persons.buildDiscussionUri(personId);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		int sessionId = extras.getInt(ExtraKey.SESSION_ID, Integer.MIN_VALUE);
		intent.putExtra(ExtraKey.SESSION_ID, sessionId);
		intent.putExtra(ExtraKey.PERSON_ID, personId);
		intent.putExtra(ExtraKey.PERSON_NAME, personName);
		intent.putExtra(ExtraKey.PERSON_COLOR, personColor);
		return intent;
	}

	private void createExperientUser(final int seatId, final int color) {

		final EditText userNameEdit = new EditText(getActivity());
		final String dialorTitle = getActivity().getString(R.string.dialog_title_user_name);
		final String saveButtonTitle = getActivity().getString(R.string.menu_action_save);
		final String cancelButtonTitle = getActivity().getString(R.string.menu_action_cancel);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(dialorTitle).setCancelable(false).setPositiveButton(saveButtonTitle,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int id) {

						String userNameStr = userNameEdit.getText().toString();
						ParamStructure params = new ParamStructure();
						params.personColor = color;
						params.personName = userNameStr;
						params.seatId = seatId;
						new CreatePersonTask().execute(params);
					}
				}).setNegativeButton(cancelButtonTitle, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(final DialogInterface dialog, final int id) {

				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.setView(userNameEdit);
		alert.show();
	}

	private Intent createExperientUser2(final int seatId, final String userName, final int color) {

		Bundle extras = getActivity().getIntent().getExtras();
		int sessionId = extras.getInt(ExtraKey.SESSION_ID, Integer.MIN_VALUE);
		String where = Persons.Columns.SEAT_ID + "=" + seatId + " AND " + Persons.Columns.SESSION_ID + "="
				+ sessionId;
		Cursor cur = getActivity().getContentResolver().query(Persons.CONTENT_URI, null, where, null, null);
		int personId;
		if (cur.moveToFirst()) {
			int indexId = cur.getColumnIndexOrThrow(Persons.Columns.ID);
			personId = cur.getInt(indexId);
			OdataWriteClient odataWrite = new OdataWriteClient(getActivity());
			odataWrite.updatePerson(personId, userName);
			ContentValues cv = new ContentValues();
			cv.put(Persons.Columns.ID, personId);
			cv.put(Persons.Columns.NAME, userName);
			Uri personUri = Persons.buildTableUri(personId);
			getActivity().getContentResolver().update(personUri, cv, null, null);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(Persons.Columns.SESSION_ID, sessionId);
			cv.put(Persons.Columns.SEAT_ID, seatId);
			cv.put(Persons.Columns.COLOR, color);
			cv.put(Persons.Columns.EMAIL, "no-email");
			cv.put(Persons.Columns.NAME, userName);
			cv.put(Persons.Columns.ONLINE, false);
			cv.put(Persons.Columns.ONLINE_DEVICE_TYPE, DeviceType.ANDROID);
			personId = insertPerson(cv);
		}
		cur.close();
		cur = getActivity().getContentResolver().query(Topics.CONTENT_URI,
				new String[] { Topics.Columns.ID }, null, null, null);
		int indexId = cur.getColumnIndexOrThrow(Topics.Columns.ID);
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			ContentValues cv = new ContentValues();
			cv.put(PersonsTopics.Columns.PERSON_ID, personId);
			int topicId = cur.getInt(indexId);
			cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
			OdataWriteClient.insertPersonTopicLink(getActivity(), personId, topicId);
			getActivity().getContentResolver().insert(Persons.buildTopicUri(1231231), cv);
		}
		cur.close();
		return createDiscussionIntent(userName, color, personId);
	}

	private int insertPerson(final ContentValues personValues) {

		logd("[insertPerson] " + personValues.toString());
		OdataWriteClient odataWrite = new OdataWriteClient(getActivity());
		OEntity entity = odataWrite.insertPerson(personValues);
		int newId = (Integer) entity.getProperty(Persons.Columns.ID).getValue();
		logd("[insertPerson] new person id: " + newId);
		personValues.remove(Persons.Columns.ID);
		personValues.put(Persons.Columns.ID, newId);
		getActivity().getContentResolver().insert(Persons.CONTENT_URI, personValues);
		return newId;
	}

	private class CreatePersonTask extends AsyncTask<ParamStructure, Void, Intent> {

		ProgressDialog dialog;

		@Override
		protected Intent doInBackground(final ParamStructure... params) {

			ParamStructure parameters = params[0];
			return createExperientUser2(parameters.seatId, parameters.personName, parameters.personColor);
		}

		@Override
		protected void onPostExecute(final Intent result) {

			super.onPostExecute(result);
			dialog.dismiss();
			getActivity().startActivity(result);
		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
			dialog = ProgressDialog.show(getActivity(), "", getActivity().getString(
					R.string.dialog_title_creating_user), true);
			dialog.show();
		}
	}

	private class ParamStructure {

		private int personColor;
		private String personName;
		private int seatId;
	}
}
