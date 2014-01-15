package org.mariotaku.refreshnow.widget;

import android.content.Context;
import android.util.DisplayMetrics;

public final class RefreshNowConfig {

	public static final int MAX_OVERSCROLL_DISTANCE = 48;

	private int maxOverScrollDistance;
	private int minPullDivisor;
	private int extraPullDivisor;

	private RefreshNowConfig() {

	}

	public int getExtraPullDivisor() {
		return extraPullDivisor;
	}

	public int getMaxOverScrollDistance() {
		return maxOverScrollDistance;
	}

	public int getMinPullDivisor() {
		return minPullDivisor;
	}

	public static final class Builder {

		private final float density;
		private boolean configBuilt;
		private final RefreshNowConfig config;

		public Builder(final Context context) {
			final DisplayMetrics dm = context.getResources().getDisplayMetrics();
			config = new RefreshNowConfig();
			density = dm.density;
			maxOverScrollDistance(MAX_OVERSCROLL_DISTANCE);
		}

		public RefreshNowConfig build() {
			configBuilt = true;
			return config;
		}

		public RefreshNowConfig.Builder extraPullDivisor(final int extraPullDivisor) {
			checkNotBuilt();
			config.extraPullDivisor = extraPullDivisor;
			return this;
		}

		public RefreshNowConfig.Builder maxOverScrollDistance(final int distanceDp) {
			checkNotBuilt();
			config.maxOverScrollDistance = Math.round(distanceDp * density);
			return this;
		}

		public RefreshNowConfig.Builder minPullDivisor(final int minPullDivisor) {
			checkNotBuilt();
			config.minPullDivisor = minPullDivisor;
			return this;
		}

		private void checkNotBuilt() {
			if (configBuilt) throw new IllegalStateException("build() already called!");
		}

	}
}