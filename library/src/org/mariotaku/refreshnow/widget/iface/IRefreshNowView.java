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

	public boolean canOverScroll();

	public RefreshMode getRefreshMode();

	public boolean isOverScrolling();

	public boolean isRefreshing();

	public void setOnRefreshListener(OnRefreshListener listener);

	public void setRefreshComplete();

	public void setRefreshIndicatorView(View view);

	public void setRefreshing(boolean refreshing);

	public void setRefreshMode(RefreshMode mode);

	public class Helper implements IRefreshNowView, OnGestureListener {

		private static final int MAX_Y_OVERSCROLL_DISTANCE = 48;

		private final View mView;
		private View mIndicatorView;

		private OnRefreshListener mRefreshListener;
		private final GestureDetector mGestureDetector;
		private final OverScroller mScroller;

		private RefreshMode mRefreshMode;
		private final int mMaxYOverscrollDistance;

		private boolean mIsRefreshing;
		private boolean mIsDown;

		public Helper(final View view, final Context context, final AttributeSet attrs, final int defStyle) {
			if (!(view instanceof IRefreshNowView))
				throw new IllegalArgumentException("this view instance must implement IRefreshNowView");
			mView = view;
			mGestureDetector = new GestureDetector(context, this);
			final DisplayMetrics metrics = view.getResources().getDisplayMetrics();
			final float density = metrics.density;
			mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
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
				case MotionEvent.ACTION_UP: {
					mIsDown = false;
					final int scrollY = mView.getScrollY();
					if (scrollY != 0) {
						if (canOverScroll()) {
							cancelTouchEvent();
						} else {
							mScroller.springBack(0, scrollY, 0, 0, 0, 0);
							mView.postDelayed(new SpringBackRunnable(this), 16);
						}
					}
				}
				case MotionEvent.ACTION_CANCEL: {
					final int scrollY = mView.getScrollY();
					if (scrollY != 0 && !canOverScroll()) {
						mScroller.springBack(0, scrollY, 0, 0, 0, 0);
						mView.postDelayed(new SpringBackRunnable(this), 16);
					}
					break;
				}
			}
			mGestureDetector.onTouchEvent(ev);
		}

		public void beforeOverScrollBy(final int deltaX, final int deltaY, final int scrollX, final int scrollY,
				final int scrollRangeX, final int scrollRangeY, final int maxOverScrollX, final int maxOverScrollY,
				final boolean isTouchEvent) {
			if (Math.abs(scrollY) >= mMaxYOverscrollDistance && scrollY != 0 && isTouchEvent) {
				cancelTouchEvent();
				dispatchRefreshStart(scrollY);
			}
		}

		public void cancelTouchEvent() {
			final long time = SystemClock.uptimeMillis();
			mView.dispatchTouchEvent(MotionEvent.obtain(time, time, MotionEvent.ACTION_CANCEL, 0, 0, 0));
		}

		@Override
		public boolean canOverScroll() {
			return ((IRefreshNowView) mView).canOverScroll();
		}

		public int computeDeltaY(final int deltaY, final int scrollY, final boolean isTouchEvent) {
			if (isTouchEvent && mIsRefreshing) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY < 0 && !mRefreshMode.hasStart()) return 0;
			if (isTouchEvent && scrollY == 0 && deltaY > 0 && !mRefreshMode.hasEnd()) return 0;
			final float pullPercent = Math.abs((float) scrollY) / mMaxYOverscrollDistance;
			final int factor = 2 + Math.round(pullPercent * 3);
			return isTouchEvent ? deltaY / factor : deltaY;
		}

		public void dispatchOnOverScrolled(final int scrollX, final int scrollY, final boolean clampedX,
				final boolean clampedY) {
			if (!canOverScroll()) return;
			dispatchPulled(scrollY);
		}

		public void dispatchOnScrollChanged(final int l, final int t, final int oldl, final int oldt) {
			if (canOverScroll()) return;
			dispatchPulled(mView.getScrollY());
		}

		public void dispatchPulled(final int scrollY) {
			final float pullPercent = Math.abs((float) scrollY) / mMaxYOverscrollDistance;
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

		public int getMaxYOverscrollDistance() {
			return mMaxYOverscrollDistance;
		}

		@Override
		public RefreshMode getRefreshMode() {
			return mRefreshMode;
		}

		@Override
		public boolean isOverScrolling() {
			return ((IRefreshNowView) mView).isOverScrolling();
		}

		@Override
		public boolean isRefreshing() {
			return mIsRefreshing;
		}

		@Override
		public boolean onDown(final MotionEvent e) {
			mIsDown = true;
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
			final int deltaY = Math.round(distanceY);
			final boolean canOverScroll = canOverScroll();
			if (canOverScroll && !isOverScrolling()) {
				dispatchPulled(0);
			}
			if (canOverScroll || !mIsDown) return true;
			final int scrollY = mView.getScrollY();
			if (Math.abs(scrollY) >= getMaxYOverscrollDistance()) {
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
