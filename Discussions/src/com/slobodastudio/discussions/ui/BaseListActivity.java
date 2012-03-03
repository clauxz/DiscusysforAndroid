package com.slobodastudio.discussions.ui;

import com.slobodastudio.ui.actionbar.ActionBarActivity;
import com.slobodastudio.ui.actionbar.R;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseListActivity extends ActionBarActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list_activity);
		setTitle(R.string.activity_name_points);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.list_actionbar, menu);
		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				Toast.makeText(this, "Tapped home", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu_refresh:
				Toast.makeText(this, "Fake refreshing...", Toast.LENGTH_SHORT).show();
				getActionBarHelper().setRefreshActionItemState(true);
				getWindow().getDecorView().postDelayed(new Runnable() {

					@Override
					public void run() {

						getActionBarHelper().setRefreshActionItemState(false);
					}
				}, 1000);
				break;
			case R.id.menu_new:
				Toast.makeText(this, "Tapped new", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}