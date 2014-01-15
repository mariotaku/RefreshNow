package org.mariotaku.refreshnow.sample;

import java.util.ArrayList;

import org.mariotaku.refreshnow.widget.OnRefreshListener;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.refreshnow.widget.RefreshNowListView;
import org.mariotaku.refreshnow.widget.RefreshNowProgressIndicator;
import org.mariotaku.refreshnow.widget.RefreshNowProgressIndicator.IndicatorConfig;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class StyledByCodeActivity extends Activity implements OnRefreshListener {

	private RefreshNowListView mListView;

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mListView = (RefreshNowListView) findViewById(android.R.id.list);
	}

	@Override
	public void onRefreshComplete() {
		Toast.makeText(this, String.format("onRefreshComplete"), Toast.LENGTH_SHORT).show();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onRefreshStart(final RefreshMode mode) {
		Toast.makeText(this, String.format("onRefreshStart: %s", mode), Toast.LENGTH_SHORT).show();
		setProgressBarIndeterminateVisibility(true);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mListView.setRefreshComplete();
			}
		}, 5000L);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample_basic);
		final ArrayList<String> objects = new ArrayList<String>();
		for (int i = 0; i < 50; i++) {
			objects.add(String.format("Item %d", i));
		}
		final RefreshNowProgressIndicator indicator = (RefreshNowProgressIndicator) findViewById(android.R.id.progress);
		final Resources res = getResources();
		final int[] colors = res.getIntArray(R.array.gplus_colors);
		final int width = (int) (res.getDisplayMetrics().density * 3);
		final IndicatorConfig.Builder cb = new IndicatorConfig.Builder(this);
		cb.reversed(true);
		cb.mirrorMode(true);
		cb.speed(1.7f);
		cb.separatorLength(0);
		cb.interpolator(new AccelerateDecelerateInterpolator());
		cb.progressColor(colors[0]);
		cb.indeterminateColors(colors);
		cb.sectionsCount(2);
		cb.indeterminateWidth(width);
		cb.progressWidth(width);
		mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, objects));
		mListView.setOnRefreshListener(this);
		mListView.setRefreshIndicatorView(indicator);
		indicator.setConfig(cb.build());
	}

}
