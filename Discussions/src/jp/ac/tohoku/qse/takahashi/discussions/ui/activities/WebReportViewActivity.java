package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
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

	//private static final String SCRIPT_PATH="scripts/webreport.js";
	
	private static final String URL_REPORT="/discsvc/report?";
	private static final String URL_PARAM_DISCUSSIONID="discussionId=";
	private static final String URL_PARAM_TOPICID="topicId=";
	private static final String URL_PARAM_SESSIONID="sessionId=";
	
	private WebView mWebView;
	private EditText mEditText;
	private MenuItem mMenuItem;
	
	//private boolean IsScreeptAdded=false;
	private boolean IsShowBlock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initFormExtra();
		IsShowBlock=false;
		setContentView(R.layout.activity_webview);
		mWebView = (WebView) findViewById(R.id.webview);
		//mWebView.addJavascriptInterface(new JavaScriptReportInterface(), "JSIR");
		mWebView.getSettings().setJavaScriptEnabled(true);
		
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if(mMenuItem!=null){
					//Log.i("Disc","show progressbar URL:   "+String.valueOf(url));
					mMenuItem.setVisible(true);
				}
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				
				
				if(mMenuItem!=null){
					//Log.i("Disc","hide progressbar URL:   "+String.valueOf(url));
					mMenuItem.setVisible(false);//hide progress bar 
					
					
				}
				
				IsShowBlock=isWebViewShowLinkBlock(url);
				
				super.onPageFinished(view, url);
			}
		});
		/*
		mWebView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			
				WebView.HitTestResult hr=((WebView)v).getHitTestResult();
				Log.i("Disc", "getExtra = "+ hr.getExtra() + "\t\t Type=" + hr.getType());
				if(hr.getType()==WebView.HitTestResult.ANCHOR_TYPE){
					Log.i("Disc WEB","EXTRA:"+String.valueOf(hr.getExtra()));
				}
					
				return false;
			}
		});
		//*/
		mWebView.getSettings().setBuiltInZoomControls(true);
		
		if (isTablet(this)) {
			mWebView.getSettings().setUserAgentString(
					"Mozilla/5.0 AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		}
		
		
		mEditText = (EditText) findViewById(R.id.edittext_url);
		mWebView.requestFocus();
		
		InputMethodManager imm = (InputMethodManager) getSystemService(
			    INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		
	}
	
	@Override
	public void onBackPressed() {
		if(IsShowBlock==true){
			IsShowBlock=false;
			if(mWebView!=null){
				mWebView.scrollTo(0, 0);
			}
			return;
		}		
		super.onBackPressed();
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
		
		Log.v("Disc report","Report URl:"+url);
		mEditText.setText(url);
		
		/*
		//Can be added if need to add button in to the <div class='toc'...
		mWebView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				WebView.HitTestResult hr=((WebView)v).getHitTestResult();
				if(hr!=null)
					Log.i("Disc WEB TOUCH","TYPE:"+String.valueOf(hr.getType())+" EXTRA:"+String.valueOf(hr.getExtra()));
				
				
				if(event.getAction()==MotionEvent.ACTION_MOVE)
				{
					Log.i("Disc","ACTION MOVE");
				}
				return false;
			}
		});
		//*/
		
		
		mWebView.setFocusable(true);
		mWebView.loadUrl(url);
		
		
		if(mMenuItem!=null)
		{
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
			}
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Check url. If Anchor was pressed from the first DIV block with class='toc'. 
	 * 
	 * @param url
	 * @return TRUE if anchor was pressed otherwice return FALSE
	 */
	private boolean isWebViewShowLinkBlock(String url){
		
		String[] blocks={"#basicInfo","#participants","#bg",
				"#media","#sources","#finalBoard",
				"#summary","#argPoints","#clustInfo",
				"#linkInfo"};
		
	
		for(int i=0;i<blocks.length;i++){
			if(url.contains(blocks[i])){
					return true;
				}
		}
		return false;
	}
	
	
}
