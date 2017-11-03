package doext.webview.pullToRefresh;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;

public class DoPullToRefreshTools {

	// 头部下拉刷新
	public TextView tv;
	public TextView tv_time;
	public ProgressBar progressBar;
	public ImageView iv;

	private SimpleDateFormat dateFormat;
	private SharedPreferences sp;

	private Context ctx;

	public DoPullToRefreshTools(Context context) {
		this.ctx = context;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		sp = context.getSharedPreferences(context.getPackageName() + "_refresh_time", Context.MODE_PRIVATE);
	}

	public RelativeLayout getPullToRefreshHeaderView(String typeID) {
		DoIModuleTypeID id = new GetTypeID(typeID);

		int header_id = DoResourcesHelper.getIdentifier("header", "layout", id);
		RelativeLayout headerView = (RelativeLayout) View.inflate(ctx, header_id, null);
		headerView.setLayoutParams(new LayoutParams(-1, -2));

		int tv_state_id = DoResourcesHelper.getIdentifier("tv_state", "id", id);
		this.tv = (TextView) headerView.findViewById(tv_state_id);
		this.tv.setText("下拉刷新");

		int tv_time_id = DoResourcesHelper.getIdentifier("tv_time", "id", id);
		this.tv_time = (TextView) headerView.findViewById(tv_time_id);

		int pb_id = DoResourcesHelper.getIdentifier("pb", "id", id);
		this.progressBar = (ProgressBar) headerView.findViewById(pb_id);

		int iv_id = DoResourcesHelper.getIdentifier("iv", "id", id);
		this.iv = (ImageView) headerView.findViewById(iv_id);
		int pulltorefresh_arrow_id = DoResourcesHelper.getIdentifier("pulltorefresh_arrow", "drawable", id);
		iv.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(), pulltorefresh_arrow_id));

		return headerView;
	}

	private class GetTypeID implements DoIModuleTypeID {
		private String typeID;

		GetTypeID(String _typeID) {
			this.typeID = _typeID;
		}

		@Override
		public String getTypeID() {
			return this.typeID;
		}
	}

	public String formatTime() {
		long currentTime = System.currentTimeMillis();
		long lastTime = sp.getLong(DoPullToRefreshView.TYPEID + "_refresh_time", currentTime);
		String timeStr = formatTime(currentTime - lastTime);
		// 几分钟前更新
		if (timeStr == null) {
			timeStr = "上次更新时间:" + dateFormat.format(new java.util.Date(lastTime));
		} else {
			timeStr = timeStr + "前更新";
		}
		return timeStr;
	}

	public void savaTime(long currentTime) {
		Editor editor = sp.edit();
		editor.putLong(DoPullToRefreshView.TYPEID + "_refresh_time", currentTime);
		editor.commit();
	}

	// 几分钟前更新
	private String formatTime(long time) {
		if (time == 0)
			return null;

		long m = time % 1000 > 0 ? time / 1000 + 1 : time / 1000;
		if (m < 60) {
			return m + "秒";
		}

		long s = m % 60 > 0 ? m / 60 + 1 : m / 60;
		if (s < 60) {
			return s + "分钟";
		}

		long h = s % 60 > 0 ? s / 60 + 1 : s / 60;
		if (h < 24) {
			return h + "小时";
		}

		long d = h % 24 > 0 ? h / 24 + 1 : h / 24;
		if (d < 31) {
			return d + "天";
		}

		return null;
	}
}
