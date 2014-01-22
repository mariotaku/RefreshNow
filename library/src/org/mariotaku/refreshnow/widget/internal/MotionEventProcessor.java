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
		final int action = ev.getAction();

		final boolean pointerUp = (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
		final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

		// Determine focal point
		float sumX = 0, sumY = 0;
		final int count = ev.getPointerCount();
		for (int i = 0; i < count; i++) {
			if (skipIndex == i) {
				continue;
			}
			sumX += ev.getX(i);
			sumY += ev.getY(i);
		}
		final int div = pointerUp ? count - 1 : count;
		final float focusX = sumX / div;
		final float focusY = sumY / div;

		switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_DOWN: {
				mDownFocusX = mLastFocusX = focusX;
				mDownFocusY = mLastFocusY = focusY;
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				mDownFocusX = mLastFocusX = focusX;
				mDownFocusY = mLastFocusY = focusY;
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				mDownFocusX = mLastFocusX = focusX;
				mDownFocusY = mLastFocusY = focusY;
				return mListener.onDown(ev);
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

		public MotionEvent onDown(MotionEvent ev);

		public MotionEvent onScroll(MotionEvent ev, float distanceX, float distanceY);

		public MotionEvent onUp(MotionEvent ev);

	}

}
