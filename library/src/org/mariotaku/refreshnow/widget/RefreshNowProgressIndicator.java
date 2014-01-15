package org.mariotaku.refreshnow.widget;

import org.mariotaku.refreshnow.R;
import org.mariotaku.refreshnow.widget.iface.IRefreshNowIndicatorView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Interpolator;
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
		final Config.Builder builder = new Config.Builder(context);
		builder.progressColor(color);
		setConfig(builder.build());
		setMax(1000);
		setIndeterminate(false);
		updateVisibility();
	}

	@Override
	public void onPulled(final float percent) {
		setProgress(Math.round(percent * getMax()));
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

	public void setConfig(final Config config) {
		final Drawable progressDrawable = config.getProgressDrawable();
		if (progressDrawable != null) {
			setProgressDrawable(progressDrawable);
		}
		final Drawable indeterminateDrawable = config.getIndeterminateDrawable();
		if (indeterminateDrawable != null) {
			setIndeterminateDrawable(indeterminateDrawable);
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void updateVisibility() {
		if (isIndeterminate()) {
			setVisibility(VISIBLE);
		} else {
			setVisibility(getProgress() > 0 ? VISIBLE : GONE);
		}
	}

	public static final class Config {

		private final Drawable progressDrawable;
		private final Drawable indeterminateDrawable;

		private Config(final Drawable progressDrawable, final Drawable indeterminateDrawable) {
			this.progressDrawable = progressDrawable;
			this.indeterminateDrawable = indeterminateDrawable;
		}

		public Drawable getIndeterminateDrawable() {
			return indeterminateDrawable;
		}

		public Drawable getProgressDrawable() {
			return progressDrawable;
		}

		public static final class Builder {

			private final SmoothProgressDrawable.Builder indeterminateDrawableBuilder;
			private int progressColor;
			private boolean changeProgressDrawable, changeIndeterminateDrawable;

			public Builder(final Context context) {
				indeterminateDrawableBuilder = new SmoothProgressDrawable.Builder(context);
				progressColor(context.getResources().getColor(R.color.spb_default_color));
			}

			public Config build() {
				final ShapeDrawable shape = new ShapeDrawable();
				shape.setShape(new RectShape());
				shape.getPaint().setColor(progressColor);
				final ClipDrawable progressDrawable = new ClipDrawable(shape, Gravity.CENTER, ClipDrawable.HORIZONTAL);
				return new Config(changeProgressDrawable ? progressDrawable : null,
						changeIndeterminateDrawable ? indeterminateDrawableBuilder.build() : null);
			}

			public Builder changeIndeterminateDrawable(final boolean change) {
				changeIndeterminateDrawable = change;
				return this;
			}

			public Builder changeProgressDrawable(final boolean change) {
				changeProgressDrawable = change;
				return this;
			}

			public Builder indeterminateColor(final int color) {
				indeterminateDrawableBuilder.color(color);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder indeterminateColors(final int[] colors) {
				indeterminateDrawableBuilder.colors(colors);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder interpolator(final Interpolator interpolator) {
				indeterminateDrawableBuilder.interpolator(interpolator);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder mirrorMode(final boolean mirrorMode) {
				indeterminateDrawableBuilder.mirrorMode(mirrorMode);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder progressColor(final int color) {
				progressColor = color;
				changeProgressDrawable = true;
				return this;
			}

			public Builder reversed(final boolean reversed) {
				indeterminateDrawableBuilder.reversed(reversed);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder sectionsCount(final int sectionsCount) {
				indeterminateDrawableBuilder.sectionsCount(sectionsCount);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder separatorLength(final int separatorLength) {
				indeterminateDrawableBuilder.separatorLength(separatorLength);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder speed(final float speed) {
				indeterminateDrawableBuilder.speed(speed);
				changeIndeterminateDrawable = true;
				return this;
			}

			public Builder width(final int width) {
				indeterminateDrawableBuilder.width(width);
				changeIndeterminateDrawable = true;
				return this;
			}

		}
	}
}
