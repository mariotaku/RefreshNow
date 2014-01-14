package org.mariotaku.refreshnow.widget.iface;

public interface IRefreshNowIndicatorView {

	public void onPulled(float percent);

	public void onRefreshComplete();

	public void onRefreshStart();

}
