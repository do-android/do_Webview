package doext.implement;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.MissingResourceException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import core.DoServiceContainer;
import core.helper.DoResourcesHelper;
import core.interfaces.DoActivityResultListener;
import core.interfaces.DoIPageView;

public class DoWebChromeClient extends WebChromeClient implements DoActivityResultListener {

	private LinearLayout loadingView;
	private DoCustomWebView webView;
	private boolean isVideoFullscreen;
	private FrameLayout videoViewContainer;
	private CustomViewCallback videoViewCallback;
	private do_WebView_Model model;
	private boolean isShowLoadingProgress;

	protected static final String LANGUAGE_DEFAULT_ISO3 = "eng";
	protected static final String CHARSET_DEFAULT = "UTF-8";
	protected static final int REQUEST_CODE_FILE_PICKER = 51426;
	private Activity mActivity;
	private String mUploadableFileTypes = "*/*";
	private String mLanguageIso3;
	private int mRequestCodeFilePicker = REQUEST_CODE_FILE_PICKER;
	protected ValueCallback<Uri> mFileUploadCallbackFirst;
	protected ValueCallback<Uri[]> mFileUploadCallbackSecond;

	public DoWebChromeClient() {
	}

	public DoWebChromeClient(DoCustomWebView webView, do_WebView_Model model, boolean isShowLoadingProgress) {
		this.webView = webView;
		this.model = model;
		this.isShowLoadingProgress = isShowLoadingProgress;
		mActivity = DoServiceContainer.getPageViewFactory().getAppContext();
		mLanguageIso3 = getLanguageIso3();
	}

	// file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
		openFileChooser(uploadMsg, acceptType, null);
	}

	// file upload callback (Android 5.0 (API level 21) -- current) (public method)
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
		openFileInput(null, filePathCallback);
		return true;
	}

	// file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
	public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
		openFileInput(uploadMsg, null);
	}

	@SuppressLint("NewApi")
	protected void openFileInput(final ValueCallback<Uri> fileUploadCallbackFirst, final ValueCallback<Uri[]> fileUploadCallbackSecond) {
		if (mFileUploadCallbackFirst != null) {
			mFileUploadCallbackFirst.onReceiveValue(null);
		}
		mFileUploadCallbackFirst = fileUploadCallbackFirst;

		if (mFileUploadCallbackSecond != null) {
			mFileUploadCallbackSecond.onReceiveValue(null);
		}
		mFileUploadCallbackSecond = fileUploadCallbackSecond;

		((DoIPageView) mActivity).registActivityResultListener(this);
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.addCategory(Intent.CATEGORY_OPENABLE);
		i.setType(mUploadableFileTypes);

		mActivity.startActivityForResult(Intent.createChooser(i, getFileUploadPromptLabel()), mRequestCodeFilePicker);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		((DoIPageView) mActivity).unregistActivityResultListener(this);
		if (requestCode == mRequestCodeFilePicker) {
			if (resultCode == Activity.RESULT_OK) {
				if (intent != null) {
					if (mFileUploadCallbackFirst != null) {
						mFileUploadCallbackFirst.onReceiveValue(intent.getData());
						mFileUploadCallbackFirst = null;
					} else if (mFileUploadCallbackSecond != null) {
						Uri[] dataUris;
						try {
							dataUris = new Uri[] { Uri.parse(intent.getDataString()) };
						} catch (Exception e) {
							dataUris = null;
						}

						mFileUploadCallbackSecond.onReceiveValue(dataUris);
						mFileUploadCallbackSecond = null;
					}
				}
			} else {
				if (mFileUploadCallbackFirst != null) {
					mFileUploadCallbackFirst.onReceiveValue(null);
					mFileUploadCallbackFirst = null;
				} else if (mFileUploadCallbackSecond != null) {
					mFileUploadCallbackSecond.onReceiveValue(null);
					mFileUploadCallbackSecond = null;
				}
			}
		}
	}

	private String getLanguageIso3() {
		try {
			return Locale.getDefault().getISO3Language().toLowerCase(Locale.US);
		} catch (MissingResourceException e) {
			return LANGUAGE_DEFAULT_ISO3;
		}
	}

	private String getFileUploadPromptLabel() {
		try {
			if (mLanguageIso3.equals("zho"))
				return decodeBase64("6YCJ5oup5LiA5Liq5paH5Lu2");
			else if (mLanguageIso3.equals("spa"))
				return decodeBase64("RWxpamEgdW4gYXJjaGl2bw==");
			else if (mLanguageIso3.equals("hin"))
				return decodeBase64("4KSP4KSVIOCkq+CkvOCkvuCkh+CksiDgpJrgpYHgpKjgpYfgpII=");
			else if (mLanguageIso3.equals("ben"))
				return decodeBase64("4KaP4KaV4Kaf4Ka/IOCmq+CmvuCmh+CmsiDgpqjgpr/gprDgp43gpqzgpr7gpprgpqg=");
			else if (mLanguageIso3.equals("ara"))
				return decodeBase64("2KfYrtiq2YrYp9ixINmF2YTZgSDZiNin2K3Yrw==");
			else if (mLanguageIso3.equals("por"))
				return decodeBase64("RXNjb2xoYSB1bSBhcnF1aXZv");
			else if (mLanguageIso3.equals("rus"))
				return decodeBase64("0JLRi9Cx0LXRgNC40YLQtSDQvtC00LjQvSDRhNCw0LnQuw==");
			else if (mLanguageIso3.equals("jpn"))
				return decodeBase64("MeODleOCoeOCpOODq+OCkumBuOaKnuOBl+OBpuOBj+OBoOOBleOBhA==");
			else if (mLanguageIso3.equals("pan"))
				return decodeBase64("4KiH4Kmx4KiVIOCoq+CovuCoh+CosiDgqJrgqYHgqKPgqYs=");
			else if (mLanguageIso3.equals("deu"))
				return decodeBase64("V8OkaGxlIGVpbmUgRGF0ZWk=");
			else if (mLanguageIso3.equals("jav"))
				return decodeBase64("UGlsaWggc2lqaSBiZXJrYXM=");
			else if (mLanguageIso3.equals("msa"))
				return decodeBase64("UGlsaWggc2F0dSBmYWls");
			else if (mLanguageIso3.equals("tel"))
				return decodeBase64("4LCS4LCVIOCwq+CxhuCxluCwsuCxjeCwqOCxgSDgsI7gsILgsJrgsYHgsJXgsYvgsILgsKHgsL8=");
			else if (mLanguageIso3.equals("vie"))
				return decodeBase64("Q2jhu41uIG3hu5l0IHThuq1wIHRpbg==");
			else if (mLanguageIso3.equals("kor"))
				return decodeBase64("7ZWY64KY7J2YIO2MjOydvOydhCDshKDtg50=");
			else if (mLanguageIso3.equals("fra"))
				return decodeBase64("Q2hvaXNpc3NleiB1biBmaWNoaWVy");
			else if (mLanguageIso3.equals("mar"))
				return decodeBase64("4KSr4KS+4KSH4KSyIOCkqOCkv+CkteCkoeCkvg==");
			else if (mLanguageIso3.equals("tam"))
				return decodeBase64("4K6S4K6w4K+BIOCuleCvh+CuvuCuquCvjeCuquCviCDgrqTgr4fgrrDgr43grrXgr4E=");
			else if (mLanguageIso3.equals("urd"))
				return decodeBase64("2KfbjNqpINmB2KfYptmEINmF24zauiDYs9uSINin2YbYqtiu2KfYqCDaqdix24zaug==");
			else if (mLanguageIso3.equals("fas"))
				return decodeBase64("2LHYpyDYp9mG2KrYrtin2Kgg2qnZhtuM2K8g24zaqSDZgdin24zZhA==");
			else if (mLanguageIso3.equals("tur"))
				return decodeBase64("QmlyIGRvc3lhIHNlw6dpbg==");
			else if (mLanguageIso3.equals("ita"))
				return decodeBase64("U2NlZ2xpIHVuIGZpbGU=");
			else if (mLanguageIso3.equals("tha"))
				return decodeBase64("4LmA4Lil4Li34Lit4LiB4LmE4Lif4Lil4LmM4Lir4LiZ4Li24LmI4LiH");
			else if (mLanguageIso3.equals("guj"))
				return decodeBase64("4KqP4KqVIOCqq+CqvuCqh+CqsuCqqOCrhyDgqqrgqrjgqoLgqqY=");
		} catch (Exception e) {
		}

		// return English translation by default
		return "Choose a file";
	}

	private String decodeBase64(final String base64) throws IllegalArgumentException, UnsupportedEncodingException {
		final byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
		return new String(bytes, CHARSET_DEFAULT);
	}

	public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
		if (model == null)
			return true;
		AlertDialog.Builder builder = new AlertDialog.Builder((Activity) model.getCurrentPage().getPageView());
		builder.setMessage(message);
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
		return true;
	}

	public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
		if (model == null)
			return true;
		AlertDialog.Builder builder = new AlertDialog.Builder((Activity) model.getCurrentPage().getPageView());
		builder.setMessage(message);
		builder.setPositiveButton("OK", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				result.confirm();
			}
		});
		builder.setNeutralButton("Cancel", new AlertDialog.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				result.cancel();
			}
		});
		builder.setCancelable(false);
		builder.create();
		builder.show();
		return true;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		if (this.isShowLoadingProgress) {
			if (newProgress == 100) {
				webView.getProgressbar().setVisibility(View.GONE);
			} else {
				if (webView.getProgressbar().getVisibility() == View.GONE)
					webView.getProgressbar().setVisibility(View.VISIBLE);
				webView.getProgressbar().setProgress(newProgress);
			}
		}
		super.onProgressChanged(view, newProgress);
	}

	public void onConsoleMessage(String message, int lineNumber, String sourceID) {
		DoServiceContainer.getLogEngine().writeInfo(sourceID + "-line" + lineNumber + " :" + message, "WebView : onConsoleMessage\n");
	}

	public boolean isVideoFullscreen() {
		return isVideoFullscreen;
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		if (model == null)
			return;
		if (view instanceof FrameLayout) {

			// A video wants to be shown
			this.videoViewContainer = (FrameLayout) view;
			// Save video related variables
			this.videoViewCallback = callback;
			if (webView != null && webView.getSettings().getJavaScriptEnabled()) {
				// Run javascript code that detects the video end and notifies
				// the interface
				String js = "javascript:";
				js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];";
				js += "if (_ytrp_html5_video !== undefined) {";
				{
					js += "function _ytrp_html5_video_ended() {";
					{
						// js +=
						// "_ytrp_html5_video.removeEventListener('pause', _ytrp_html5_video_ended);";
						js += "_DoCustomWebView.notifyVideoEnd();";
					}
					js += "}";
					js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
				}
				js += "}";
				webView.loadUrl(js);
			}
		}
		if (!isVideoFullscreen) {
			Activity _activity = (Activity) model.getCurrentPage().getPageView();
			ImageView iv = new ImageView(_activity);
			FrameLayout.LayoutParams btnLayoutParams = new FrameLayout.LayoutParams(-2, -2);
			int exit_id = DoResourcesHelper.getIdentifier("deviceone_videoview_fullscreen", "drawable", null);
			iv.setImageBitmap(BitmapFactory.decodeResource(_activity.getResources(), exit_id));
			btnLayoutParams.gravity = Gravity.RIGHT;
			btnLayoutParams.topMargin = (int) (model.getYZoom() * 30);
			btnLayoutParams.rightMargin = (int) (model.getXZoom() * 20);
			this.videoViewContainer.addView(iv, btnLayoutParams);
			this.videoViewContainer.setBackgroundColor(Color.BLACK);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onHideCustomView();
				}
			});
			_activity.addContentView(this.videoViewContainer, new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
			this.isVideoFullscreen = true;
		}
	}

	@Override
	public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
		onShowCustomView(view, callback);
	}

	@Override
	public void onHideCustomView() {
		if (isVideoFullscreen) {
			this.videoViewContainer.setVisibility(View.GONE);
			if (videoViewCallback != null)
				videoViewCallback.onCustomViewHidden();

			// Reset video related variables
			isVideoFullscreen = false;
			videoViewContainer = null;
			videoViewCallback = null;
		}
	}

	/**
	 * 设置缓存视频时的进度条
	 */
	@Override
	public View getVideoLoadingProgressView() {
		if (model == null)
			return null;
		if (loadingView == null) {
			Activity _activity = (Activity) model.getCurrentPage().getPageView();
			loadingView = new LinearLayout(_activity);
			loadingView.setOrientation(LinearLayout.VERTICAL);

			ProgressBar _progressBar = new ProgressBar(_activity);
			TextView _tv = new TextView(_activity);
			_tv.setText("加载中...");
			_tv.setTextColor(Color.WHITE);

			LinearLayout.LayoutParams _tvParams = new LinearLayout.LayoutParams(-1, -2);
			_tvParams.gravity = Gravity.CENTER_HORIZONTAL;
			_tv.setLayoutParams(_tvParams);
			loadingView.addView(_progressBar);
			loadingView.addView(_tv);

		}
		loadingView.setVisibility(View.VISIBLE);
		return loadingView;
	}

	/**
	 * Notifies the class that the back key has been pressed by the user. This
	 * must be called from the Activity's onBackPressed(), and if it returns
	 * false, the activity itself should handle it. Otherwise don't do anything.
	 * 
	 * @return Returns true if the event was handled, and false if it is not
	 *         (video view is not visible)
	 */
	public boolean onBackPressed() {
		if (isVideoFullscreen) {
			onHideCustomView();
			return true;
		} else {
			return false;
		}
	}

	public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
		String _url = webView.getUrl();
		int _index = _url.lastIndexOf("/");
		if (_index != -1) {
			_url = _url.substring(0, _index);
		}
		builder.setMessage("“" + _url + "”" + "想使用您当前的位置");
		DialogInterface.OnClickListener dialogButtonOnClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int clickedButton) {
				if (DialogInterface.BUTTON_POSITIVE == clickedButton) {
					callback.invoke(origin, true, true);
				} else if (DialogInterface.BUTTON_NEGATIVE == clickedButton) {
					callback.invoke(origin, false, false);
				}
				DoWebChromeClient.super.onGeolocationPermissionsShowPrompt(origin, callback);
			}
		};
		builder.setPositiveButton("好", dialogButtonOnClickListener);
		builder.setNegativeButton("不允许", dialogButtonOnClickListener);
		builder.show();

	}

	@Override
	public void onCloseWindow(WebView w) {
		super.onCloseWindow(w);
		if (w.canGoBack()) {
			w.goBack();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
		quotaUpdater.updateQuota(estimatedSize * 2);
	}
}