package doext.implement;

import doext.define.do_WebView_MAbstract;

/**
 * 自定义扩展组件Model实现，继承do_WebView_MAbstract抽象类；
 *
 */
public class do_WebView_Model extends do_WebView_MAbstract {

	public do_WebView_Model() throws Exception {
		super();
	}

	@Override
	public void callJavaScript(String _script) {
		final StringBuffer sb = new StringBuffer();
		sb.append("javascript:");
		sb.append(_script);
//		Activity ctx = (Activity) this.getCurrentPage().getPageView();
//		ctx.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
		do_WebView_View doWebViewView = (do_WebView_View) getCurrentUIModuleView();
		doWebViewView.webView.loadUrl(sb.toString());
//			}
//		});
	}
	@Override
	public void didLoadView() throws Exception {
		super.didLoadView();
		((do_WebView_View)this.getCurrentUIModuleView()).loadDefalutScriptFile();
	}
}
