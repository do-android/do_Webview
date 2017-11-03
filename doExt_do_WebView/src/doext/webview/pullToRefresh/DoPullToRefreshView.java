package doext.webview.pullToRefresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DoPullToRefreshView extends LinearLayout {
	// refresh states
	protected static final int PULL_TO_REFRESH = 0;
	protected static final int RELEASE_TO_REFRESH = 1;
	protected static final int REFRESHING = 2;
	// pull state
	protected static final int PULL_UP_STATE = 100;
	protected static final int PULL_DOWN_STATE = 101;

	protected static final String PULL = "pull";
	/**
	 * last y
	 */
	private int mLastMotionX, mLastMotionY;
	/**
	 * header view
	 */
	protected View mHeaderView;

	private WebView mWebView;
	/**
	 * header view height
	 */
	private int mHeaderViewHeight;
	/**
	 * header view image
	 */
	private ImageView mHeaderImageView;
	/**
	 * header tip text
	 */
	private TextView mHeaderTextView;
	/**
	 * header refresh time
	 */
	private TextView mHeaderUpdateTextView;
	/**
	 * header progress bar
	 */
	private ProgressBar mHeaderProgressBar;
	/**
	 * header view current state
	 */
	private int mHeaderState;
	/**
	 * footer view current state
	 */
	private int mFooterState;
	/**
	 * pull state,pull up or pull down;PULL_UP_STATE or PULL_DOWN_STATE
	 */
	protected int mPullState;
	/**
	 * 变为向下的箭头,改变箭头方向
	 */
	private RotateAnimation mFlipAnimation;
	/**
	 * 变为逆向的箭头,旋转
	 */
	private RotateAnimation mReverseFlipAnimation;

	protected boolean isShowDefaultHeader = true;

	public static String TYPEID = "do_WebView";
	/**
	 * 设置是否支持下拉刷新
	 * */
	private boolean supportHeaderRefresh;

	public void setSupportHeaderRefresh(boolean supportHeaderRefresh) {
		this.supportHeaderRefresh = supportHeaderRefresh;
	}

	private DoPullToRefreshTools mPullToRefreshTools;

	public DoPullToRefreshView(Context context) {
		super(context);
		mPullToRefreshTools = new DoPullToRefreshTools(context);
		init();
	}

	/**
	 * init
	 * 
	 * @param supportHeaderRefresh
	 *            设置是否支持下拉刷新
	 * @param supportFootFooterRefresh
	 *            是否支持上拉加载
	 */
	private void init() {
		// 需要设置成vertical
		setOrientation(LinearLayout.VERTICAL);
		// Load all of the animations we need in code rather than through XML
		mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);
		mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);
		// header view 在此添加,保证是第一个添加到linearlayout的最上端
//		addHeaderView();
	}

	private void addHeaderView() {
		// header view
		if (isShowDefaultHeader) {
			mHeaderImageView = mPullToRefreshTools.iv;
			mHeaderTextView = mPullToRefreshTools.tv;
			mHeaderUpdateTextView = mPullToRefreshTools.tv_time;
			mHeaderProgressBar = mPullToRefreshTools.progressBar;
		}
		// header layout
		measureView(mHeaderView);
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mHeaderViewHeight);
		// 设置topMargin的值为负的header View高度,即将其隐藏在最上方
		params.topMargin = -(mHeaderViewHeight);
		addView(mHeaderView, params);

	}

	protected void setHeaderView(View view) {
		if (view != null) {
			mHeaderView = view;
			isShowDefaultHeader = false;
		} else {
			mHeaderView = mPullToRefreshTools.getPullToRefreshHeaderView(TYPEID);
			isShowDefaultHeader = true;
		}
		addHeaderView();
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		initContentAdapterView();
	}

	/**
	 * init AdapterView like ListView,GridView and so on;or init ScrollView
	 * 
	 */
	private void initContentAdapterView() {

		int count = getChildCount();
		int n = 0;
		if (supportHeaderRefresh) {
			if (count < 2)
				throw new IllegalArgumentException("This layout must contain 2 child views,and AdapterView or ScrollView must in the second position!");
		}
		View view = null;
		for (int i = 0; i < count - n; ++i) {
			view = getChildAt(i);
			if (view instanceof WebView) {
				mWebView = (WebView) view;
			}
		}
		if (mWebView == null) {
			throw new IllegalArgumentException("must contain a AdapterView or ScrollView or WebView in this layout!");
		}

	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		int y = (int) e.getRawY();
		int x = (int) e.getRawX();
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onHeaderRefreshComplete(mPullToRefreshTools.formatTime());
			// 首先拦截down事件,记录y坐标
			mLastMotionY = y;
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			// deltaY > 0 是向下运动,< 0是向上运动
			int deltaX = x - mLastMotionX;
			int deltaY = y - mLastMotionY;
			boolean isRefresh = isRefreshViewScroll(deltaX, deltaY);
			// 一旦底层View收到touch的action后调用这个方法那么父层View就不会再调用onInterceptTouchEvent了，也无法截获以后的action
			getParent().requestDisallowInterceptTouchEvent(isRefresh);
			if (isRefresh) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return false;
	}

	/*
	 * 如果在onInterceptTouchEvent()方法中没有拦截(即onInterceptTouchEvent()方法中 return
	 * false)则由PullToRefreshView 的子View来处理;否则由下面的方法来处理(即由PullToRefreshView自己来处理)
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// if (mLock) {
		// return true;
		// }
		int y = (int) event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// onInterceptTouchEvent已经记录
			// mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaY = y - mLastMotionY;
			if (mPullState == PULL_DOWN_STATE) {// 执行下拉
				if (supportHeaderRefresh)
					headerPrepareToRefresh(deltaY);
				// setHeaderPadding(-mHeaderViewHeight);
			}
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			int topMargin = getHeaderTopMargin();
			if (mPullState == PULL_DOWN_STATE) {
				if (topMargin >= 0) {
					// 开始刷新
					if (supportHeaderRefresh)
						headerRefreshing();
				} else {
					// 还没有执行刷新，重新隐藏
					if (supportHeaderRefresh)
						setHeaderTopMargin(-mHeaderViewHeight);
				}
			}
			break;
		}
		return false;
	}

	/**
	 * 是否应该到了父View,即PullToRefreshView滑动
	 * 
	 * @param deltaY
	 *            , deltaY > 0 是向下运动,< 0是向上运动
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private boolean isRefreshViewScroll(int deltaX, int deltaY) {
		if (!supportHeaderRefresh || mHeaderState == REFRESHING || mFooterState == REFRESHING) {
			return false;
		}
		// 对于WebView
		if (mWebView != null) {
			View child = mWebView.getChildAt(1);
			if (deltaY > 0 && mWebView.getScrollY() == 0) {
				mPullState = PULL_DOWN_STATE;
				return true;

			} else if (deltaY < 0 && child != null) {
				if ((mWebView.getContentHeight() * mWebView.getScale()) <= getHeight() + mWebView.getScrollY()) {
					mPullState = PULL_UP_STATE;
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * header 准备刷新,手指移动过程,还没有释放
	 * 
	 * @param deltaY
	 *            ,手指滑动的距离
	 */
	private void headerPrepareToRefresh(int deltaY) {
		int newTopMargin = changingHeaderViewTopMargin(deltaY);
		// 当header view的topMargin>=0时，说明已经完全显示出来了,修改header view 的提示状态
		if (newTopMargin >= 0 && mHeaderState != RELEASE_TO_REFRESH) {
			if (isShowDefaultHeader) {
				mHeaderTextView.setText("松开后刷新");
				mHeaderUpdateTextView.setVisibility(View.VISIBLE);
				mHeaderImageView.clearAnimation();
				mHeaderImageView.startAnimation(mFlipAnimation);
			}
			mHeaderState = RELEASE_TO_REFRESH;
			fireEvent(mHeaderState, newTopMargin, PULL);
		} else if (newTopMargin < 0 && newTopMargin > -mHeaderViewHeight) {// 拖动时没有释放
			if (isShowDefaultHeader) {
				if (mHeaderState != PULL_TO_REFRESH) {
					mHeaderImageView.clearAnimation();
					mHeaderImageView.startAnimation(mReverseFlipAnimation);
				}
				mHeaderTextView.setText("下拉刷新");
			}
			mHeaderState = PULL_TO_REFRESH;
			fireEvent(mHeaderState, newTopMargin, PULL);
		}
	}

	/**
	 * 修改Header view top margin的值
	 * 
	 * @param deltaY
	 */
	private int changingHeaderViewTopMargin(int deltaY) {
		LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
		float newTopMargin = params.topMargin + deltaY * 0.3f;
		// 这里对上拉做一下限制,因为当前上拉后然后不释放手指直接下拉,会把下拉刷新给触发了,感谢网友yufengzungzhe的指出
		// 表示如果是在上拉后一段距离,然后直接下拉
		if (deltaY > 0 && mPullState == PULL_UP_STATE && Math.abs(params.topMargin) <= mHeaderViewHeight) {
			return params.topMargin;
		}
		// 同样地,对下拉做一下限制,避免出现跟上拉操作时一样的bug
		if (deltaY < 0 && mPullState == PULL_DOWN_STATE && Math.abs(params.topMargin) >= mHeaderViewHeight) {
			return params.topMargin;
		}
		params.topMargin = (int) newTopMargin;
		mHeaderView.setLayoutParams(params);
		invalidate();
		return params.topMargin;
	}

	/**
	 * header refreshing
	 * 
	 */
	private void headerRefreshing() {
		mHeaderState = REFRESHING;
		setHeaderTopMargin(0);
		if (isShowDefaultHeader) {
			mHeaderImageView.setVisibility(View.GONE);
			mHeaderImageView.clearAnimation();
			mHeaderProgressBar.setVisibility(View.VISIBLE);
			mHeaderTextView.setText("加载中...");
		}
		fireEvent(mHeaderState, 0, PULL);
	}

	/**
	 * 设置header view 的topMargin的值
	 * 
	 * @param topMargin
	 *            ，为0时，说明header view 刚好完全显示出来； 为-mHeaderViewHeight时，说明完全隐藏了
	 */
	private void setHeaderTopMargin(int topMargin) {
		LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
		params.topMargin = topMargin;
		mHeaderView.setLayoutParams(params);
		invalidate();
	}

	/**
	 * header view 完成更新后恢复初始状态
	 * 
	 */
	public void onHeaderRefreshComplete() {
		setHeaderTopMargin(-mHeaderViewHeight);
		if (isShowDefaultHeader) {
			mHeaderImageView.setVisibility(View.VISIBLE);
			mHeaderTextView.setText("下拉刷新");
			mHeaderProgressBar.setVisibility(View.GONE);
		}
		mHeaderState = PULL_TO_REFRESH;
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void onHeaderRefreshComplete(CharSequence lastUpdated) {
		setLastUpdated(lastUpdated);
	}

	/**
	 * Set a text to represent when the list was last updated.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void setLastUpdated(CharSequence lastUpdated) {
		if (isShowDefaultHeader) {
			if (lastUpdated != null) {
				mHeaderUpdateTextView.setVisibility(View.VISIBLE);
				mHeaderUpdateTextView.setText(lastUpdated);
			} else {
				mHeaderUpdateTextView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 获取当前header view 的topMargin
	 * 
	 */
	private int getHeaderTopMargin() {
		LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
		return params.topMargin;
	}

	protected void fireEvent(int mHeaderState, int newTopMargin, String eventName) {
		// 由子类实现
	}

	public void savaTime(long currentTime) {
		mPullToRefreshTools.savaTime(currentTime);
	}
}
