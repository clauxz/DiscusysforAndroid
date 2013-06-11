package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class YoutubeActivity extends BaseActivity {

	private WebView mWebView;
	private boolean loadingFinished = true;
	private boolean redirect = false;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youtube);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.requestFocus();
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String urlNewString) {

				if (!loadingFinished) {
					redirect = true;
				}
				loadingFinished = false;
				mWebView.loadUrl(urlNewString);
				return true;
			}

			@Override
			public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {

				super.onPageStarted(view, url, favicon);
				loadingFinished = false;
				// SHOW LOADING IF IT ISNT ALREADY VISIBLE
				setSupportProgressBarIndeterminateVisibility(true);
			}

			@Override
			public void onPageFinished(final WebView view, final String url) {

				if (!redirect) {
					loadingFinished = true;
				}
				if (loadingFinished && !redirect) {
					// HIDE LOADING IT HAS FINISHED
					setSupportProgressBarIndeterminateVisibility(false);
				} else {
					redirect = false;
				}
			}
		});
		mWebView.loadUrl("http://m.youtube.com");
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_webview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_cancel:
				setMyResult(RESULT_CANCELED, null);
				finish();
				break;
			case R.id.menu_new_attachment:
				onActionSave();
				break;
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onControlServiceConnected() {

		// TODO: this activity doesn't react with service
	}

	private void onActionSave() {

		String vid = Uri.parse(mWebView.getUrl()).getQueryParameter("v");
		if (vid == null) {
			showSelectVideoAlertDialog();
			return;
		}
		String youtubeLinkString = "http://www.youtube.com/watch?v=" + vid;
		Uri uri = Uri.parse(youtubeLinkString);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		setMyResult(RESULT_OK, intent);
		finish();
	}

	private void setMyResult(final int code, final Intent intent) {

		if (getParent() == null) {
			setResult(code, intent);
		} else {
			getParent().setResult(code, intent);
		}
	}

	private void showSelectVideoAlertDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.dialog_text_youtube_select_video_first).setCancelable(true)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int id) {

						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
