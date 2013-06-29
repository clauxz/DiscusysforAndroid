package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import jp.ac.tohoku.qse.takahashi.discussions.R;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * WebReportViewActivity show HTML report of the discusstion point for specified cession.
 * Session  ID, discussion id and topic id ware set using Intent extra {@link Bundle}.
 * 
 * @author Alexander Bigel 
 * alexander.bigel@gmail.com
 */
public class WebReportViewActivity extends BaseActivity {

	private static final String URL_REPORT="/discsvc/report?";
	private static final String URL_PARAM_DISCUSSIONID="discussionId=";
	private static final String URL_PARAM_TOPICID="topicId=";
	private static final String URL_PARAM_SESSIONID="sessionId=";
	
	private WebView mWebView;
	private EditText mEditText;
	private MenuItem mMenuItem;
	
	private Bundle savedState;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initFormExtra();
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webview);
		
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if(mMenuItem!=null){
					Log.i("Disc","show progressbar URL:   "+String.valueOf(url));
					mMenuItem.setVisible(true);
				}
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				if(mMenuItem!=null){
					Log.i("Disc","hide progressbar URL:   "+String.valueOf(url));
					mMenuItem.setVisible(false);//hide progress bar 
				}
				super.onPageFinished(view, url);
			}
		});
		//mWebView.getSettings().setLoadWithOverviewMode(true);
		//mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		
		if (isTablet(this)) {
			mWebView.getSettings().setUserAgentString(
					"Mozilla/5.0 AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		}
		
		if(savedInstanceState!=null)
		{
			Log.i("Disc restore",String.valueOf(savedInstanceState));
			this.savedState=savedInstanceState;
			mWebView.restoreState(savedInstanceState);
		}
		else
		{
			this.savedState=null;
			
		}
		
		mEditText = (EditText) findViewById(R.id.edittext_url);
		mWebView.requestFocus();
		
		InputMethodManager imm = (InputMethodManager) getSystemService(
			    INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mWebView.saveState(outState);
		super.onSaveInstanceState(outState);
	}
	@Override
	protected void onResume() {

		super.onResume();
		if(this.savedState==null)
			buildUrl();
	}
	/**
	 * Detect Discussion and topic IDs.
	 */
	private void initFormExtra(){
		if (!getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getIntent().hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
	}
	
	private void buildUrl(){
		
		int discussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID, 0);
		int topicId = getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		int sessionId=getIntent().getExtras().getInt(ExtraKey.SESSION_ID);
		
		//url sample
		//http://192.170.1.222/discsvc/report?discussionId=1&topicId=1&sessionId=1
		String server=PreferenceHelper.getServerAddress(this);
		
		String url="http://";
		url+=server;
		url+=URL_REPORT;
		//discussion ID
		url+=URL_PARAM_DISCUSSIONID+String.valueOf(discussionId);
		//topic ID
		url+="&"+URL_PARAM_TOPICID+String.valueOf(topicId);
		//session ID
		url+="&"+URL_PARAM_SESSIONID+String.valueOf(sessionId);
		
		Log.v("Discussion","Report URl:"+url);
		mEditText.setText(url);
		mWebView.setFocusable(true);
		mWebView.loadUrl(url);
		
		if(mMenuItem!=null)
		{
			Log.i("Disc","mMenuItem visible=true; buildUrl");
			mMenuItem.setVisible(true);
		}
	}


	public boolean isTablet(final Context context) {

		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}
	
	@Override
	protected void onControlServiceConnected() {
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu  menu) {
		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_refresh, menu);
		mMenuItem=menu.findItem(R.id.menu_refresh_progressbar);
		if(mMenuItem!=null)
			mMenuItem.setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{
		case R.id.menu_refresh:
			{
				String url=mEditText.getText().toString();
				mWebView.loadUrl(url);
				Log.i("Dsc","URL: "+String.valueOf(url));				
			}
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
