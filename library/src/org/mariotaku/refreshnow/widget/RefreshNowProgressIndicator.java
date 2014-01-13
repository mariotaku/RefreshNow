package org.mariotaku.refreshnow.widget;

import org.mariotaku.refreshnow.R;
import org.mariotaku.refreshnow.widget.iface.IRefreshNowIndicatorView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class RefreshNowProgressIndicator extends SmoothProgressBar implements IRefreshNowIndicatorView {

	private boolean mRefreshStart;
	private boolean mIsPulling;

	public RefreshNowProgressIndicator(final Context context) {
		this(context, null);
	}

	public RefreshNowProgressIndicator(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.spbStyle);
	}

	public RefreshNowProgressIndicator(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = context.getResources();
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SmoothProgressBar, defStyle, 0);
		final int color = a.getColor(R.styleable.SmoothProgressBar_spb_color, res.getColor(R.color.spb_default_color));
		a.recycle();
		final ShapeDrawable shape = new ShapeDrawable();
		shape.setShape(new RectShape());
		shape.getPaint().setColor(color);
		final ClipDrawable clipDrawable = new ClipDrawable(shape, Gravity.CENTER, ClipDrawable.HORIZONTAL);
		setProgressDrawable(clipDrawable);
		setMax(100);
		setVisibility(GONE);
	}

	@Override
	public void onPulled(final float percent) {
		setProgress(Math.round(percent * 100));
		if (mRefreshStart) {
			setVisibility(VISIBLE);
			setIndeterminate(true);
		} else if (mIsPulling) {
			setIndeterminate(false);
			setVisibility(percent > 0 ? VISIBLE : GONE);
		} else {
			setIndeterminate(true);
			setVisibility(GONE);
		}
	}

	@Override
	public void onRefreshComplete() {
		setIndeterminate(false);
		setVisibility(GONE);
		mRefreshStart = false;
	}

	@Override
	public void onRefreshStart() {
		mRefreshStart = true;
		setVisibility(VISIBLE);
		setIndeterminate(true);
	}

	@Override
	public void setIsPulling(final boolean isPulling) {
		mIsPulling = isPulling;
	}

}
