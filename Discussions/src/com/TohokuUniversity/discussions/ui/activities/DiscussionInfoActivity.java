package com.TohokuUniversity.discussions.ui.activities;

import com.TohokuUniversity.discussions.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Discussions;
import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.TohokuUniversity.discussions.ui.fragments.DiscussionInfoFragment;
import com.TohokuUniversity.discussions.ui.fragments.DiscussionMediaTabFragment;
import com.TohokuUniversity.discussions.ui.fragments.DiscussionSourcesTabFragment;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DiscussionInfoActivity extends BaseActivity {

	private static final int DESCRIPTION_TAB_POSITION = 1;
	private static final String EXTRA_KEY_TAB_INDEX = "extra_key_tab_index";
	private static final int MEDIA_TAB_POSITION = 0;
	private static final int SOURCE_TAB_POSITION = 2;
	private static final String TAG = PointDetailsActivity.class.getSimpleName();
	private int discussionId;
	private TabsAdapter mTabsAdapter;
	private ViewPager mViewPager;

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
		setContentView(R.layout.activity_discussion_description);
		initFromIntentExtra(getIntent());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(final int position) {

				Log.d(TAG, "[onPageSelected] position: " + position);
				// When swiping between pages, select the
				// corresponding tab.
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
		setupActionBarTabs();
		if (savedInstanceState != null) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(EXTRA_KEY_TAB_INDEX, DESCRIPTION_TAB_POSITION));
		} else {
			getSupportActionBar().setSelectedNavigationItem(DESCRIPTION_TAB_POSITION);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt(EXTRA_KEY_TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
	}

	private void addDescriptionTab() {

		Tab desctiptionTab = getSupportActionBar().newTab();
		// desctiptionTab.setText(R.string.tab_title_description);
		Bundle descriptionArguments = DiscussionInfoFragment.intentToFragmentArguments(getIntent());
		descriptionArguments.putParcelable(ExtraKey.URI, getIntent().getData());
		desctiptionTab.setIcon(R.drawable.ic_tab_description);
		mTabsAdapter.addTab(desctiptionTab, FragmentTag.DISCUSSION_DESCRIPTION, DiscussionInfoFragment.class,
				descriptionArguments);
	}

	private void addMediaTab() {

		Tab mediaTab = getSupportActionBar().newTab();
		// mediaTab.setText(R.string.tab_title_media);
		Bundle mediaArguments = DiscussionMediaTabFragment.intentToFragmentArguments(getIntent());
		mediaArguments.putParcelable(ExtraKey.URI, getIntent().getData());
		mediaTab.setIcon(R.drawable.ic_tab_attachments);
		mTabsAdapter.addTab(mediaTab, FragmentTag.DISCUSSION_MEDIA, DiscussionMediaTabFragment.class,
				mediaArguments);
	}

	private void addSourceTab() {

		Tab sourceTab = getSupportActionBar().newTab();
		// sourceTab.setText(R.string.tab_title_source);
		Bundle sourceArguments = DiscussionSourcesTabFragment.intentToFragmentArguments(getIntent());
		sourceArguments.putParcelable(ExtraKey.URI, getIntent().getData());
		sourceTab.setIcon(R.drawable.ic_tab_sources);
		mTabsAdapter.addTab(sourceTab, FragmentTag.DISCUSSION_SOURCE, DiscussionSourcesTabFragment.class,
				sourceArguments);
	}

	private void initFromIntentExtra(final Intent intent) {

		discussionId = Integer.parseInt(Discussions.getValueId(intent.getData()));
	}

	private boolean isLandscape() {

		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	private boolean isScreenSizeNormal() {

		int screenLayout = getResources().getConfiguration().screenLayout;
		return (screenLayout & Configuration.SCREENLAYOUT_SIZE_NORMAL) == Configuration.SCREENLAYOUT_SIZE_NORMAL;
	}

	private boolean isScreenSizeSmall() {

		int screenLayout = getResources().getConfiguration().screenLayout;
		return (screenLayout & Configuration.SCREENLAYOUT_SIZE_SMALL) == Configuration.SCREENLAYOUT_SIZE_SMALL;
	}

	private void setupActionBarTabs() {

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		if (isLandscape()) {
			if (isScreenSizeNormal() || isScreenSizeSmall()) {
				bar.setDisplayShowHomeEnabled(false);
				bar.setDisplayShowTitleEnabled(false);
			}
		}
		mTabsAdapter = new TabsAdapter(this, bar, mViewPager);
		addMediaTab();
		addDescriptionTab();
		addSourceTab();
	}

	/** This is a helper class that implements the management of tabs and all details of connecting a ViewPager
	 * with associated TabHost. It relies on a trick. Normally a tab host has a simple API for supplying a
	 * View or Intent that each tab will show. This is not sufficient for switching between pages. So instead
	 * we make the content part of the tab host 0dp high (it is not shown) and the TabsAdapter supplies its
	 * own dummy view to show as the tab content. It listens to changes in tabs, and takes care of switch to
	 * the correct paged in the ViewPager whenever the selected tab changes. */
	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			OnPageChangeListener {

		private final Map<String, Fragment> fragments = new HashMap<String, Fragment>();
		private final ActionBar mActionBar;
		private final Context mContext;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		private final ViewPager mViewPager;

		public TabsAdapter(final FragmentActivity activity, final ActionBar actionBar, final ViewPager pager) {

			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(final Tab tab, final String tag, final Class<?> clss, final Bundle args) {

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mActionBar.addTab(tab.setTabListener(this));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {

			return mTabs.size();
		}

		@Override
		public Fragment getItem(final int position) {

			TabInfo info = mTabs.get(position);
			Fragment fragment = fragments.get(info.tag);
			if (fragment == null) {
				fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
				fragments.put(info.tag, fragment);
			}
			return fragment;
		}

		@Override
		public void onPageScrolled(final int position, final float positionOffset,
				final int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(final int state) {

		}

		@Override
		public void onPageSelected(final int position) {

			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

		}

		@Override
		public void onTabSelected(final Tab tab, final FragmentTransaction ft) {

			mViewPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

		}

		static final class TabInfo {

			private final Bundle args;
			private final Class<?> clss;
			private final String tag;

			TabInfo(final String _tag, final Class<?> _class, final Bundle _args) {

				tag = _tag;
				clss = _class;
				args = _args;
			}
		}
	}

	private final class FragmentTag {

		private static final String DISCUSSION_DESCRIPTION = "discussion_description_tag";
		private static final String DISCUSSION_MEDIA = "discussion_media_tag";
		private static final String DISCUSSION_SOURCE = "discussion_source_tag";
	}
}