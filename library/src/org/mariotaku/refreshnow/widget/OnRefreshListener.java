package org.mariotaku.refreshnow.widget;

public interface OnRefreshListener {

	public void onRefreshComplete();

	public void onRefreshStart(RefreshMode mode);

}
