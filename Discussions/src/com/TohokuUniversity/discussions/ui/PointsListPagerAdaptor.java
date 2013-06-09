package com.TohokuUniversity.discussions.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/** The <code>PagerAdapter</code> serves the fragments when paging.
 * 
 * @author mwho */
public class PointsListPagerAdaptor extends FragmentPagerAdapter {

	private final List<Fragment> fragments;

	/** @param fm
	 * @param fragments */
	public PointsListPagerAdaptor(final FragmentManager fm, final List<Fragment> fragments) {

		super(fm);
		this.fragments = fragments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {

		return fragments.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(final int position) {

		return fragments.get(position);
	}

	@Override
	public CharSequence getPageTitle(final int position) {

		return getItem(position).getArguments().getString(ExtraKey.LIST_HEADER);
	}
}
