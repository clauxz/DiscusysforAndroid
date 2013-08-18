package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Description;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Source;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sources;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ActivityHelper;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import jp.ac.tohoku.qse.takahashi.discussions.ui.activities.BaseActivity;
import jp.ac.tohoku.qse.takahashi.discussions.utils.TextViewUtils;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PointSourcesTabFragment extends SherlockFragment implements 
	OnItemClickListener,
	OnItemLongClickListener,
	OnClickListener 
{

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final int PICK_URL_REQUEST = 0x01;
	private static final String TAG = PointSourcesTabFragment.class.getSimpleName();
	private boolean mFooterButtonsEnabled;
	private int mDescriptionId;
	private TextView mPointNameTextView;
	private SelectedPoint mSelectedPoint;
	private SimpleCursorAdapter mSourcesAdapter;
	private final SourcesCursorLoader mSourcesCursorLoader;
	private ListView mSourcesList;
	private String mDescriptionLink;

	private int mPositionClick;
	private boolean isDeleteDialogShow=false;
	public PointSourcesTabFragment() {

		mSourcesCursorLoader = new SourcesCursorLoader();
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (!intent.hasExtra(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("intent was without point id");
		}
		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("intent was without person id");
		}
		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("intent was without topic id");
		}
		if (intent.getAction() == null) {
			throw new IllegalStateException("intent was without action string");
		}
		int discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		int personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
		int topicId = intent.getIntExtra(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
		int pointId = intent.getIntExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		SelectedPoint point = new SelectedPoint();
		point.setDiscussionId(discussionId);
		point.setPersonId(personId);
		point.setTopicId(topicId);
		point.setPointId(pointId);
		arguments.putParcelable(ExtraKey.SELECTED_POINT, point);
		boolean viewEnabled = Intent.ACTION_EDIT.equals(intent.getAction());
		arguments.putBoolean(ExtraKey.VIEW_ENABLED, viewEnabled);
		return arguments;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		initFromArguments();
		View attachmentsView = inflater.inflate(R.layout.tab_fragment_point_sources, container, false);
		mSourcesList = (ListView) attachmentsView.findViewById(R.id.listview_sources);
		addSourcesHeader(inflater);
		if (mFooterButtonsEnabled) {
			addSourcesFooter(inflater);
		}
		return attachmentsView;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		setSourcesAdapter();
		initSourcesLoader();
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

		switch (requestCode) {
			case PICK_URL_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					mDescriptionLink = data.getDataString();
					logd("[onActivityresult] isBound: " + ((BaseActivity) getActivity()).isBound());
					if (((BaseActivity) getActivity()).isBound()) {
						onAttachSourceAdded();
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.btn_attach_url:
				ActivityHelper.startSearchWebActivityForResult(getActivity(), TextViewUtils
						.toString(mPointNameTextView), PICK_URL_REQUEST);
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onItemClick(final AdapterView<?> parent, 
			final View view, final int position, final long id) {
		//if(isDeleteDialogShow)//block onClick event if onLoncgEvent shows delete URL dialog
		{
			TextView linkTextView = (TextView) view.findViewById(R.id.text_source_link);
			CharSequence urlSequence = linkTextView.getText();
			if (!TextUtils.isEmpty(urlSequence)) {
				Uri uri = Uri.parse(urlSequence.toString());
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				
				startActivity(intent);
			}
		}
	}
	
	/**
	 * Delete URL reaction
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, 
			final View view, final int position,final long id) 
	{
		isDeleteDialogShow=true;
		
		TextView linkTextView = (TextView) view.findViewById(R.id.text_source_link);
		CharSequence urlSequence = linkTextView.getText();
		if (!TextUtils.isEmpty(urlSequence)) {
			Uri uri = Uri.parse(urlSequence.toString());
			mPositionClick=position-1;
			showDeleteUrlDialog(uri);
		}
		
		return true;
	}
	
	private void showDeleteUrlDialog(final Uri uri){
		
		AlertDialog.Builder builder=new Builder(getActivity());
		builder.setTitle(R.string.dialog_title_delete_url);
		builder.setPositiveButton(R.string.button_title_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isDeleteDialogShow=false;
				deleteUrl();//id);
				
			}
		});
		builder.setNegativeButton(R.string.button_title_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isDeleteDialogShow=false;
			}
		});
		builder.setMessage(uri.toString());
		
		Dialog dialog=builder.create();
		dialog.show();
		
	}
	
	
	private void deleteUrl(){
		
		if(mPositionClick<mSourcesAdapter.getCount())
		{
			Cursor cursor=mSourcesAdapter.getCursor();
			cursor.moveToPosition(mPositionClick);
			int posColId=cursor.getColumnIndexOrThrow(Sources.Columns.ID);
			int urlId = cursor.getInt(posColId);
			
			((BaseActivity)getActivity()).getServiceHelper().deleteAttachedURL(urlId, mSelectedPoint);
		}
	}

	
	public void onServiceConnected() {

		if (mDescriptionLink != null) {
			onAttachSourceAdded();
			mDescriptionLink = null;
		}
	}

	private void initFromArguments() {

		Bundle arguments = getArguments();
		if (arguments == null) {
			throw new NullPointerException("You are trying to instantiate fragment without arguments");
		}
		if (!arguments.containsKey(ExtraKey.SELECTED_POINT)) {
			throw new IllegalStateException("fragment was called without selected point extra");
		}
		if (!arguments.containsKey(ExtraKey.VIEW_ENABLED)) {
			throw new IllegalStateException("fragment was called without view enabled extra");
		}
		mSelectedPoint = arguments.getParcelable(ExtraKey.SELECTED_POINT);
		mFooterButtonsEnabled = arguments.getBoolean(ExtraKey.VIEW_ENABLED);
	}

	private void addSourcesFooter(final LayoutInflater inflater) {

		View footerView = inflater.inflate(R.layout.layout_source_footer, null, false);
		footerView.findViewById(R.id.btn_attach_url).setOnClickListener(this);
		mSourcesList.addFooterView(footerView);
	}

	private void addSourcesHeader(final LayoutInflater inflater) {

		View headerView = inflater.inflate(R.layout.list_header_point_name, null, false);
		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
		mSourcesList.addHeaderView(headerView, null, false);
	}

	private void setSourcesAdapter() {

		if (mSourcesList.getAdapter() == null) {
			mSourcesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_source_2, null,
					new String[] { Sources.Columns.LINK, Sources.Columns.ORDER_NUMBER }, new int[] {
							R.id.text_source_link, R.id.textSourceNumber }, 0);
			mSourcesAdapter.setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(final View view, final Cursor data, final int columnId) {

					switch (view.getId()) {
						case R.id.text_source_link:
							String link = data.getString(columnId);
							((TextView) view).setText(link);
							return true;
						case R.id.textSourceNumber:
							int currentNumber = data.getInt(columnId);
							if (currentNumber == 0) {
								currentNumber = data.getPosition() + 1;
							}
							((TextView) view).setText(String.valueOf(currentNumber));
							return true;
						default:
							return false;
					}
				}
			});
			mSourcesList.setAdapter(mSourcesAdapter);
		}
		mSourcesList.setOnItemClickListener(this);
		mSourcesList.setOnItemLongClickListener(this);
	}

	private void initSourcesLoader() {

		Bundle args = new Bundle();
		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
		getLoaderManager().initLoader(SourcesCursorLoader.POINT_NAME_ID, args, mSourcesCursorLoader);
		getLoaderManager().initLoader(SourcesCursorLoader.DESCRIPTION_ID, args, mSourcesCursorLoader);
	}

	private void onAttachSourceAdded() {

		Source source = new Source();
		source.setLink(mDescriptionLink);
		source.setDescriptionId(mDescriptionId);
		int orderNumber = mSourcesAdapter.getCount() + 1;
		source.setOrderNumber(orderNumber);
		((BaseActivity) getActivity()).getServiceHelper().insertSource(source, mSelectedPoint);
	}

	private class SourcesCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int DESCRIPTION_ID = 0x02;
		private static final int POINT_NAME_ID = 0x01;
		private static final int SOURCE_ID = 0x00;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case SOURCE_ID: {
					if (!arguments.containsKey(ExtraKey.DESCRIPTION_ID)) {
						throw new IllegalArgumentException("Loader was called without description id");
					}
					int descriptionId = arguments.getInt(ExtraKey.DESCRIPTION_ID, Integer.MIN_VALUE);
					String where = Sources.Columns.DESCRIPTION_ID + "=?";
					String[] args = new String[] { String.valueOf(descriptionId) };
					String sortOrder = Sources.Columns.ORDER_NUMBER + " ASC";
					return new CursorLoader(getActivity(), Sources.CONTENT_URI, null, where, args, sortOrder);
				}
				case POINT_NAME_ID: {
					if (!arguments.containsKey(ExtraKey.POINT_ID)) {
						throw new IllegalArgumentException("Loader was called without point id");
					}
					int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
					String where = Points.Columns.ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
				}
				case DESCRIPTION_ID: {
					if (!arguments.containsKey(ExtraKey.POINT_ID)) {
						throw new IllegalArgumentException("Loader was called without point id");
					}
					int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
					String where = Descriptions.Columns.POINT_ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case SOURCE_ID:
					mSourcesAdapter.swapCursor(null);
					break;
				case POINT_NAME_ID:
					mPointNameTextView.setText("");
					break;
				case DESCRIPTION_ID:
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			if (DEBUG) {
				Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
			}
			switch (loader.getId()) {
				case SOURCE_ID:
					mSourcesAdapter.swapCursor(data);
					break;
				case POINT_NAME_ID:
					if (data.moveToFirst()) {
						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
						String name = data.getString(nameColumnIndex);
						mPointNameTextView.setText(name);
					}
					break;
				case DESCRIPTION_ID:
					if (data.getCount() == 1) {
						Description description = new Description(data);
						mDescriptionId = description.getId();
						Bundle args = new Bundle();
						args.putInt(ExtraKey.DESCRIPTION_ID, mDescriptionId);
						getLoaderManager().initLoader(SourcesCursorLoader.SOURCE_ID, args,
								mSourcesCursorLoader);
					} else {
						Log.w(TAG, "[onLoadFinished] LOADER_DESCRIPTION_ID count was: " + data.getCount());
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	
}
