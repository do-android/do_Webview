package doext.implement;

import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class DoCustomWebView extends WebView {

	private ProgressBar mProgressBar;

	public interface ToggledFullscreenCallback {
		public void toggledFullscreen(boolean fullscreen);
	}

	private OnDataCallback dataCallback;

	public interface OnDataCallback {
		public void onData(String value);
	}

	public void setOnDataCallback(OnDataCallback dataCallback) {
		this.dataCallback = dataCallback;
	}

	private boolean addedJavascriptInterface;

	public DoCustomWebView(Context context) {
		super(context);
		init(context);
	}

	public DoCustomWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DoCustomWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	@SuppressWarnings("deprecation")
	private void init(Context context) {
		mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 25, 0, -9));
		mProgressBar.setVisibility(View.GONE);
		addView(mProgressBar);
	}

	@SuppressWarnings("deprecation")
	public void setLoadingProgressBarColor(int backgroundColor, int progressColor) {
		mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 8, 0, 0));
		//Background
		ClipDrawable bgClipDrawable = new ClipDrawable(new ColorDrawable(backgroundColor), Gravity.LEFT, ClipDrawable.HORIZONTAL);
		bgClipDrawable.setLevel(10000);
		//Progress
		ClipDrawable progressClip = new ClipDrawable(new ColorDrawable(progressColor), Gravity.LEFT, ClipDrawable.HORIZONTAL);
		//Setup LayerDrawable and assign to progressBar
		Drawable[] progressDrawables = { bgClipDrawable, progressClip/* second */, progressClip };
		LayerDrawable progressLayerDrawable = new LayerDrawable(progressDrawables);
		progressLayerDrawable.setId(0, android.R.id.background);
		progressLayerDrawable.setId(1, android.R.id.secondaryProgress);
		progressLayerDrawable.setId(2, android.R.id.progress);
		mProgressBar.setProgressDrawable(progressLayerDrawable);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		mProgressBar.setLayoutParams(mProgressBar.getLayoutParams());
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public ProgressBar getProgressbar() {
		return this.mProgressBar;
	}

	/**
	 * Pass only a DoWebChromeClient instance.
	 */
	@Override
	@SuppressLint("SetJavaScriptEnabled")
	public void setWebChromeClient(WebChromeClient client) {
		super.setWebChromeClient(client);
	}

	@Override
	public void loadData(String data, String mimeType, String encoding) {
		addJavascriptInterface();
		super.loadData(data, mimeType, encoding);
	}

	@Override
	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
		addJavascriptInterface();
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
	}

	@Override
	public void loadUrl(String url) {
		addJavascriptInterface();
		super.loadUrl(url);
	}

	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		addJavascriptInterface();
		super.loadUrl(url, additionalHttpHeaders);
	}

	private void addJavascriptInterface() {
		if (!addedJavascriptInterface) {
			// Add javascript interface to be called when the video ends (mustbe
			// done before page load)
			addJavascriptInterface(this, "_DoCustomWebView"); // Must
			addedJavascriptInterface = true;
		}
	}

	@JavascriptInterface
	private void notifyVideoEnd() {

	}

	@JavascriptInterface
	public void onData(String value) {
		if (dataCallback != null) {
			dataCallback.onData(value);
		}
	}

	public int getContentWidth() {
		return this.computeHorizontalScrollRange();
	}
}