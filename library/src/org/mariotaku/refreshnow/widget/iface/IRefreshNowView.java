package org.mariotaku.refreshnow.widget.iface;

import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

public interface IRefreshNowView {

	public static final String REFRESHNOW_LOGTAG = "RefreshNow";

	public RefreshMode getRefreshMode();

	public boolean isRefreshing();

	public void setConfig(Config config);

	public void setOnRefreshListener(OnRefreshListener listener);

	public void setRefreshComplete();

	public void setRefreshIndicatorView(View view);

	public void setRefreshing(boolean refreshing);

	public void setRefreshMode(RefreshMode mode);

	public static final class Config {

		public static final int MAX_OVERSCROLL_DISTANCE = 48;

		private int maxOverScrollDistance;

		private Config() {

		}

		public static final class Builder {

			private final float density;
			private boolean configBuilt;
			private final Config config;

			public Builder(final Context context) {
				final DisplayMetrics dm = context.getResources().getDisplayMetrics();
				config = new Config();
				density = dm.density;
				maxOverScrollDistance(MAX_OVERSCROLL_DISTANCE);
			}

			public Config build() {
				configBuilt = true;
				return config;
			}

			public Builder maxOverScrollDistance(final int distanceDp) {
				checkNotBuilt();
				config.maxOverScrollDistance = Math.round(distanceDp * density);
				return this;
			}

			private void checkNotBuilt() {
				if (configBuilt) throw new IllegalStateException("build() already called!");
			}

		}
	}

	public static final class Helper implements IRefreshNowView, OnGestureListener {

		private final View mView;
		private View mIndicatorView;

		private OnRefreshListener mRefreshListener;
		private final GestureDetector mGestureDetector;
		private final OverScroller mScroller;

		private RefreshMode mRefreshMode;

		private boolean mIsRefreshing;
		private Config mConfig;

		public Helper(final View view, final Context context, final AttributeSet attrs, final int defStyle) {
			if (!(view instanceof IRefreshNowView))
				throw new IllegalArgumentException("this view instance must implement IRefreshNowView");
			mConfig = new Config.Builder(context).build();
			mView = view;
			mGestureDetector = new GestureDetector(context, this);
			mScroller = new OverScroller(context);
			setRefreshMode(RefreshMode.BOTH);
		}

		public void beforeOnTouchEvent(final MotionEvent ev) {
			final int action = ev.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					mScroller.forceFinished(true);
					break;
				}
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					final int scrollY = mView.getScrollY();
					if (scrollY != 0) {
						mScroller.springBack(0, scrollY, 0, 0, 0, 0);
						mView.postDelayed(new SpringBackRunnable(this), 16);
					}
					break;
				}
			}
			mGestureDetector.onTouchEvent(ev);
		}

		public void dispatchOnScrollChanged(final int l, final int t, final int oldl, final int oldt) {
			dispatchPulled(mView.getScrollY());
		}

		public void dispatchPulled(final int scrollY) {
			if (mRefreshMode == RefreshMode.NONE) {
				((IRefreshNowIndicatorView) mIndicatorView).onPulled(0);
				return;
			}
			final float pullPercent = Math.abs((float) scrollY) / mConfig.maxOverScrollDistance;
			if (mIndicatorView != null) {
				((IRefreshNowIndicatorView) mIndicatorView).onPulled(pullPercent);
			}
		}

		public void dispatchRefreshStart(final int scrollY) {
			final RefreshMode refreshMode = scrollY > 0 ? RefreshMode.END : RefreshMode.START;
			if (mRefreshListener != null) {
				mRefreshListener.onRefreshStart(refreshMode);
			}
			((IRefreshNowView) mView).setRefreshing(true);
		}

		@Override
		public RefreshMode getRefreshMode() {
			return mRefreshMode;
		}

		@Override
		public boolean isRefreshing() {
			return mIsRefreshing;
		}

		@Override
		public boolean onDown(final MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
			return true;
		}

		@Override
		public void onLongPress(final MotionEvent e) {

		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
			final int deltaY = Math.round(distanceY), scrollY = mView.getScrollY();
			if (mView.canScrollVertically(deltaY) && scrollY == 0) return true;
			if (Math.abs(scrollY) >= mConfig.maxOverScrollDistance) {
				cancelTouchEvent();
				dispatchRefreshStart(scrollY);
			} else {
				mView.scrollBy(0, computeDeltaY(deltaY, scrollY, true));
			}
			return true;
		}

		@Override
		public void onShowPress(final MotionEvent e) {

		}

		@Override
		public boolean onSingleTapUp(final MotionEvent e) {
			return true;
		}

		@Override
		public void setConfig(final Config config) {
			if (config == null) throw new NullPointerException();
			mConfig = config;
		}

		public void setFriction(final float friction) {
			mScroller.setFriction(friction);
		}

		@Override
		public void setOnRefreshListener(final OnRefreshListener listener) {
			mRefreshListener = listener;
		}

		@Override
		public void setRefreshComplete() {
			setRefreshing(false);
			if (mRefreshListener != null && mRefreshMode != RefreshMode.NONE) {
				mRefreshListener.onRefreshComplete();
			}
		}

		@Override
		public void setRefreshIndicatorView(final View view) {
			if (!(view instanceof IRefreshNowIndicatorView))
				throw new IllegalArgumentException("this view must implement IRefreshNowIndicatorView");
			mIndicatorView = view;
		}

		@Override
		public void setRefreshing(final boolean refreshing) {
			mIsRefreshing = refreshing;
			if (mIndicatorView != null) {
				if (refreshing) {
					((IRefreshNowIndicatorView) mIndicatorView).onRefreshStart();
				} else {
					((IRefreshNowIndicatorView) mIndicatorView).onRefreshComplete();
				}
			}
		}

		@Override
		public void setRefreshMode(final RefreshMode mode) {
			mRefreshMode = mode;
		}

		private void cancelTouchEvent() {
			final long time = SystemClock.uptimeMillis();
			mView.dispatchTouchEvent(MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0));
		}

		private int computeDeltaY(final int deltaY, final int scrollY, final boolean isTouchEvent) {
			if (isTouchEvent && mIsRefreshing) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY < 0 && !mRefreshMode.hasStart()) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY > 0 && !mRefreshMode.hasEnd()) return 0;
			final float pullPercent = Math.abs((float) scrollY) / mConfig.maxOverScrollDistance;
			final int factor = 2 + Math.round(pullPercent * 3);
			return isTouchEvent ? deltaY / factor : deltaY;
		}

		private static class SpringBackRunnable implements Runnable {

			private final View mView;
			private final OverScroller mScroller;

			SpringBackRunnable(final Helper helper) {
				mView = helper.mView;
				mScroller = helper.mScroller;
			}

			@Override
			public void run() {
				if (mScroller.computeScrollOffset()) {
					final int scrollY = mScroller.getCurrY();
					mView.scrollTo(mView.getScrollX(), scrollY);
					mView.postDelayed(this, 16);
				} else {
					mView.scrollTo(mView.getScrollX(), 0);
				}
			}
		}
	}
}
