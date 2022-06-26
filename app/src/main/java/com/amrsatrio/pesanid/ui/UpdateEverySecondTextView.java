package com.amrsatrio.pesanid.ui;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import com.google.android.material.textview.MaterialTextView;

import java.util.function.Supplier;

public class UpdateEverySecondTextView extends MaterialTextView {
	private Handler handler = new Handler();
	private Supplier<CharSequence> textSupplier;
	private boolean mShouldRunTicker;
	private long interval = 1000L;
	private final Runnable mTicker = new Runnable() {
		public void run() {
			if (textSupplier != null) {
				setText(textSupplier.get());
			}

			long now = SystemClock.uptimeMillis();
//			long next = now + (1000 - now % 1000);
			long next = now - now % interval + interval;

			handler.postAtTime(mTicker, next);
		}
	};

	public UpdateEverySecondTextView(Context context) {
		super(context);
	}

	public UpdateEverySecondTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UpdateEverySecondTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setTextSupplier(Supplier<CharSequence> textSupplier) {
		this.textSupplier = textSupplier;
		setText(textSupplier != null ? textSupplier.get() : null);
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	@Override
	public void onVisibilityAggregated(boolean isVisible) {
		super.onVisibilityAggregated(isVisible);

		if (!mShouldRunTicker && textSupplier != null && isVisible) {
			mShouldRunTicker = true;
			mTicker.run();
		} else if (mShouldRunTicker && !isVisible) {
			mShouldRunTicker = false;
			handler.removeCallbacks(mTicker);
		}
	}
}