package org.mariotaku.refreshnow.widget.iface;

import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.refreshnow.widget.RefreshNowConfig;
import org.mariotaku.refreshnow.widget.internal.MotionEventProcessor;
import org.mariotaku.refreshnow.widget.internal.MotionEventProcessor.OnGestureEventListener;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

public interface IRefreshNowView {

	public static final String REFRESHNOW_LOGTAG = "RefreshNow";

	public RefreshMode getRefreshMode();

	public boolean isRefreshing();

	public void setConfig(RefreshNowConfig config);

	public void setOnRefreshListener(OnRefreshListener listener);

	public void setRefreshComplete();

	public void setRefreshIndicatorView(View view);

	public void setRefreshing(boolean refreshing);

	public void setRefreshMode(RefreshMode mode);

	public static final class Helper implements IRefreshNowView, OnGestureEventListener {

		private final View mView;
		private View mIndicatorView;

		private final MotionEventProcessor mEventProcessor;
		private final OverScroller mScroller;

		private OnRefreshListener mRefreshListener;

		private RefreshMode mRefreshMode;
		private RefreshNowConfig mConfig;
		private boolean mIsRefreshing;

		private int mPreviousScrollY;

		public Helper(final View view, final Context context, final AttributeSet attrs, final int defStyle) {
			if (!(view instanceof IRefreshNowView))
				throw new IllegalArgumentException("this view instance must implement IRefreshNowView");
			mConfig = new RefreshNowConfig.Builder(context).build();
			mView = view;
			mEventProcessor = new MotionEventProcessor(context, this);
			mScroller = new OverScroller(context);
			setRefreshMode(RefreshMode.BOTH);
		}

		public void dispatchOnScrollChanged(final int l, final int t, final int oldl, final int oldt) {
			dispatchPulled(mView.getScrollY());
		}

		public void dispatchPulled(final int scrollY) {
			if (mRefreshMode == RefreshMode.NONE) {
				((IRefreshNowIndicatorView) mIndicatorView).onPulled(0);
				return;
			}
			final float pullPercent = Math.abs((float) scrollY) / mConfig.getMaxOverScrollDistance();
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
		public MotionEvent onDown(final MotionEvent ev) {
			return ev;
		}

		@Override
		public MotionEvent onScroll(final MotionEvent ev, final float distanceX, final float distanceY) {
			final int deltaY = Math.round(distanceY), scrollY = mView.getScrollY();
			final boolean canScrollVertically = mView.canScrollVertically(deltaY);
			final boolean error = scrollY != 0 && mView.canScrollVertically(scrollY);
			final MotionEvent result;
			if (scrollY == 0 && mPreviousScrollY != 0 && canScrollVertically) {
				result = MotionEvent.obtain(ev);
				result.setAction(MotionEvent.ACTION_DOWN);
			} else if (scrollY != 0 && mPreviousScrollY == 0) {
				result = MotionEvent.obtain(ev);
				result.setAction(MotionEvent.ACTION_UP);
			} else {
				result = ev;
			}
			mPreviousScrollY = scrollY;
			if (canScrollVertically && scrollY == 0) return result;
			if (Math.abs(scrollY) >= mConfig.getMaxOverScrollDistance()) {
				cancelTouchEvent();
				dispatchRefreshStart(scrollY);
			} else if (error) {
				mView.scrollTo(0, 0);
			} else {
				mView.scrollBy(0, computeDeltaY(deltaY, scrollY, true));
			}
			return result;
		}

		@Override
		public MotionEvent onUp(final MotionEvent ev) {
			mPreviousScrollY = 0;
			return ev;
		}

		public MotionEvent processOnTouchEvent(final MotionEvent ev) {
			final int action = ev.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					mScroller.abortAnimation();
					break;
				}
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL: {
					cancelPullToRefresh();
					break;
				}
			}
			return mEventProcessor.onTouchEvent(ev);
		}

		@Override
		public void setConfig(final RefreshNowConfig config) {
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

		private void cancelPullToRefresh() {
			final int scrollY = mView.getScrollY();
			if (scrollY != 0) {
				mScroller.springBack(0, scrollY, 0, 0, 0, 0);
				mView.postDelayed(new SpringBackRunnable(this), 16);
			}
		}

		private void cancelTouchEvent() {
			final long time = SystemClock.uptimeMillis();
			mView.dispatchTouchEvent(MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0));
		}

		private int computeDeltaY(final int deltaY, final int scrollY, final boolean isTouchEvent) {
			if (isTouchEvent && mIsRefreshing) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY < 0 && !mRefreshMode.hasStart()) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY > 0 && !mRefreshMode.hasEnd()) return 0;
			final float pullPercent = Math.abs((float) scrollY) / mConfig.getMaxOverScrollDistance();
			final int divisorMin = Math.max(1, mConfig.getMinPullDivisor());
			final int divisorExtra = Math.max(0, mConfig.getExtraPullDivisor());
			final int divisorTotal = divisorMin + Math.round(pullPercent * divisorExtra);
			return isTouchEvent ? deltaY / divisorTotal : deltaY;
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
