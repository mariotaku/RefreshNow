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
import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;

public class RefreshNowProgressIndicator extends SmoothProgressBar implements IRefreshNowIndicatorView {

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
		setProgressColor(color);
		setMax(100);
		setIndeterminate(false);
		updateVisibility();
	}

	@Override
	public void onPulled(final float percent) {
		setProgress(Math.round(percent * 100));
		updateVisibility();
	}

	@Override
	public void onRefreshComplete() {
		setIndeterminate(false);
		setProgress(0);
		updateVisibility();
	}

	@Override
	public void onRefreshStart() {
		setIndeterminate(true);
		setProgress(0);
		updateVisibility();
	}

	public void setIndeterminateProgressBuilder(final SmoothProgressDrawable.Builder builder) {
		setIndeterminateDrawable(builder.build());
	}

	public void setProgressColor(final int color) {
		final ShapeDrawable shape = new ShapeDrawable();
		shape.setShape(new RectShape());
		shape.getPaint().setColor(color);
		final ClipDrawable clipDrawable = new ClipDrawable(shape, Gravity.CENTER, ClipDrawable.HORIZONTAL);
		setProgressDrawable(clipDrawable);
		final SmoothProgressDrawable.Builder builder = new SmoothProgressDrawable.Builder(getContext());
		builder.color(color);
		setIndeterminateProgressBuilder(builder);
	}

	private void updateVisibility() {
		if (isIndeterminate()) {
			setVisibility(VISIBLE);
		} else {
			setVisibility(getProgress() > 0 ? VISIBLE : GONE);
		}
	}
}
