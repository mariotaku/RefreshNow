package org.mariotaku.refreshnow.widget.internal;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class MotionEventProcessor {

	private float mDownFocusX, mDownFocusY, mLastFocusX, mLastFocusY;
	private final OnGestureEventListener mListener;
	private final int mTouchSlopSquare;

	public MotionEventProcessor(final Context context, final OnGestureEventListener listener) {
		mListener = listener;
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		final int touchSlop = configuration.getScaledTouchSlop();
		mTouchSlopSquare = touchSlop * touchSlop;
	}

	/**
	 * 
	 * @param ev The current {@link MotionEvent}
	 * @return <code>true</code> if event consumed, <code>false</code> otherwise
	 */
	public MotionEvent onTouchEvent(final MotionEvent ev) {
		final float focusX = ev.getX(), focusY = ev.getY();
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mDownFocusX = mLastFocusX = focusX;
				mDownFocusY = mLastFocusY = focusY;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final int deltaX = (int) (focusX - mDownFocusX);
				final int deltaY = (int) (focusY - mDownFocusY);
				final int distance = deltaX * deltaX + deltaY * deltaY;
				if (distance > mTouchSlopSquare) {
					final MotionEvent result = mListener.onScroll(ev, mLastFocusX - focusX, mLastFocusY - focusY);
					mLastFocusX = focusX;
					mLastFocusY = focusY;
					return result;
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				return mListener.onUp(ev);
			}
		}
		return ev;
	}

	public static interface OnGestureEventListener {

		public MotionEvent onScroll(MotionEvent ev, float distanceX, float distanceY);

		public MotionEvent onUp(MotionEvent ev);

	}

}
