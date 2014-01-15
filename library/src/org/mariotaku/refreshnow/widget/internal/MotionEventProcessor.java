package org.mariotaku.refreshnow.widget.internal;

import android.view.MotionEvent;

public class MotionEventProcessor {

	private float mLastFocusX, mLastFocusY;
	private final OnGestureEventListener mListener;

	public MotionEventProcessor(final OnGestureEventListener listener) {
		mListener = listener;
	}

	/**
	 * 
	 * @param ev The current {@link MotionEvent}
	 * @return <code>true</code> if event consumed, <code>false</code> otherwise
	 */
	public MotionEvent onTouchEvent(final MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mLastFocusX = ev.getX();
				mLastFocusY = ev.getY();
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final float x = ev.getX(), y = ev.getY();
				final MotionEvent result = mListener.onScroll(ev, mLastFocusX - x, mLastFocusY - y);
				mLastFocusX = x;
				mLastFocusY = y;
				return result;
			}
		}
		return ev;
	}

	public static interface OnGestureEventListener {

		public MotionEvent onScroll(MotionEvent ev, float distanceX, float distanceY);

	}

}
