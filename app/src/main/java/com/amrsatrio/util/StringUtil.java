package com.amrsatrio.util;

import android.content.Context;
import android.icu.text.MeasureFormat;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TtsSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// from: https://android.googlesource.com/platform/frameworks/base/+/master/packages/SettingsLib/src/com/android/settingslib/utils/StringUtil.java
public class StringUtil {
	public static final int SECONDS_PER_MINUTE = 60;
	public static final int SECONDS_PER_HOUR = 60 * 60;
	public static final int SECONDS_PER_DAY = 24 * 60 * 60;
	public static final int SECONDS_PER_WEEK = 7 * 24 * 60 * 60;

	/**
	 * Returns elapsed time for the given millis, in the following format:
	 * 2d 5h 40m 29s
	 *
	 * @param context     the application context
	 * @param millis      the elapsed time in milli seconds
	 * @param withSeconds include seconds?
	 * @return the formatted elapsed time
	 */
	// Android updated this method to use MeasureFormat.FormatWidth.SHORT instead of NARROW, but we're keeping it NARROW.
	public static CharSequence formatElapsedTime(Context context, double millis, boolean withSeconds) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		int seconds = (int) Math.floor(millis / 1000);

		/*if (!withSeconds) {
			// Round up.
			seconds += 30;
		}*/

		int days = 0, hours = 0, minutes = 0;

		if (seconds >= SECONDS_PER_DAY) {
			days = seconds / SECONDS_PER_DAY;
			seconds -= days * SECONDS_PER_DAY;
		}

		if (seconds >= SECONDS_PER_HOUR) {
			hours = seconds / SECONDS_PER_HOUR;
			seconds -= hours * SECONDS_PER_HOUR;
		}

		if (seconds >= SECONDS_PER_MINUTE) {
			minutes = seconds / SECONDS_PER_MINUTE;
			seconds -= minutes * SECONDS_PER_MINUTE;
		}

		final List<Measure> measureList = new ArrayList<>(4);

		if (days > 0) {
			measureList.add(new Measure(days, MeasureUnit.DAY));
		}

		if (hours > 0) {
			measureList.add(new Measure(hours, MeasureUnit.HOUR));
		}

		if (minutes > 0) {
			measureList.add(new Measure(minutes, MeasureUnit.MINUTE));
		}

		if (withSeconds && seconds > 0) {
			measureList.add(new Measure(seconds, MeasureUnit.SECOND));
		}

		if (measureList.size() == 0) {
			// Everything addable was zero, so nothing was added. We add a zero.
			measureList.add(new Measure(0, withSeconds ? MeasureUnit.SECOND : MeasureUnit.MINUTE));
		}

		final Measure[] measureArray = measureList.toArray(new Measure[measureList.size()]);
		final Locale locale = context.getResources().getConfiguration().locale;
		final MeasureFormat measureFormat = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW);
		sb.append(measureFormat.formatMeasures(measureArray));

		if (measureArray.length == 1 && MeasureUnit.MINUTE.equals(measureArray[0].getUnit())) {
			// Add ttsSpan if it only have minute value, because it will be read as "meters"
			final TtsSpan ttsSpan = new TtsSpan.MeasureBuilder().setNumber(minutes).setUnit("minute").build();
			sb.setSpan(ttsSpan, 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return sb;
	}

	public static CharSequence formatElapsedTime_numeric(Context context, double millis) {
		SpannableStringBuilder sb = new SpannableStringBuilder();
		int seconds = (int) Math.floor(millis / 1000);
		int days = 0, hours = 0, minutes = 0;

		if (seconds >= SECONDS_PER_DAY) {
			days = seconds / SECONDS_PER_DAY;
			seconds -= days * SECONDS_PER_DAY;
		}

		if (seconds >= SECONDS_PER_HOUR) {
			hours = seconds / SECONDS_PER_HOUR;
			seconds -= hours * SECONDS_PER_HOUR;
		}

		if (seconds >= SECONDS_PER_MINUTE) {
			minutes = seconds / SECONDS_PER_MINUTE;
			seconds -= minutes * SECONDS_PER_MINUTE;
		}

		List<Measure> measureList = new ArrayList<>(4);

		if (days > 0) {
			measureList.add(new Measure(days, MeasureUnit.DAY));
		}

		if (hours > 0) {
			measureList.add(new Measure(hours, MeasureUnit.HOUR));
		}

		measureList.add(new Measure(minutes, MeasureUnit.MINUTE));
		measureList.add(new Measure(seconds, MeasureUnit.SECOND));
		sb.append(MeasureFormat.getInstance(context.getResources().getConfiguration().locale, MeasureFormat.FormatWidth.NUMERIC).formatMeasures(measureList.toArray(new Measure[0])));
		return sb;
	}

	/**
	 * Returns relative time for the given millis in the past with different format style.
	 * In a short format such as "2 days ago", "5 hr. ago", "40 min. ago", or "29 sec. ago".
	 * In a long format such as "2 days ago", "5 hours ago",  "40 minutes ago" or "29 seconds ago".
	 *
	 * <p>The unit is chosen to have good information value while only using one unit. So 27 hours
	 * and 50 minutes would be formatted as "28 hr. ago", while 50 hours would be formatted as
	 * "2 days ago".
	 *
	 * @param context     the application context
	 * @param millis      the elapsed time in milli seconds
	 * @param withSeconds include seconds?
	 * @param formatStyle format style
	 * @return the formatted elapsed time
	 */
	public static CharSequence formatRelativeTime(Context context, double millis, boolean withSeconds, RelativeDateTimeFormatter.Style formatStyle) {
		final int seconds = (int) Math.floor(millis / 1000);
		final RelativeUnit unit;
		final int value;

		if (withSeconds && seconds < 2 * SECONDS_PER_MINUTE) { // 0 - 119 secs
//			return context.getResources().getString(R.string.time_unit_just_now);
			return "Just now";
		} else if (seconds < 2 * SECONDS_PER_HOUR) { // 120 - 7199
			unit = RelativeUnit.MINUTES;
			value = (seconds + SECONDS_PER_MINUTE / 2) / SECONDS_PER_MINUTE;
		} else if (seconds < 2 * SECONDS_PER_DAY) { // 7199 -
			unit = RelativeUnit.HOURS;
			value = (seconds + SECONDS_PER_HOUR / 2) / SECONDS_PER_HOUR;
		} else {
			unit = RelativeUnit.DAYS;
			value = (seconds + SECONDS_PER_DAY / 2) / SECONDS_PER_DAY;
		}

		final Locale locale = context.getResources().getConfiguration().locale;
		final RelativeDateTimeFormatter formatter = RelativeDateTimeFormatter.getInstance(
				ULocale.forLocale(locale),
				null /* default NumberFormat */,
				formatStyle,
				android.icu.text.DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE);
		return formatter.format(value, RelativeDateTimeFormatter.Direction.LAST, unit);
	}

	/**
	 * Returns relative time for the given millis in the past, in a long format such as "2 days
	 * ago", "5 hours ago",  "40 minutes ago" or "29 seconds ago".
	 *
	 * <p>The unit is chosen to have good information value while only using one unit. So 27 hours
	 * and 50 minutes would be formatted as "28 hr. ago", while 50 hours would be formatted as
	 * "2 days ago".
	 *
	 * @param context     the application context
	 * @param millis      the elapsed time in milli seconds
	 * @param withSeconds include seconds?
	 * @return the formatted elapsed time
	 * @deprecated use {@link #formatRelativeTime(Context, double, boolean,
	 * RelativeDateTimeFormatter.Style)} instead.
	 */
	@Deprecated
	public static CharSequence formatRelativeTime(Context context, double millis, boolean withSeconds) {
		return formatRelativeTime(context, millis, withSeconds, RelativeDateTimeFormatter.Style.LONG);
	}

	public static CharSequence formatRelativeTime_MOD(Context context, double millis, boolean withSeconds) {
		final int seconds = (int) Math.floor(millis / 1000);
		final int value;

		final MeasureUnit unit;

		if (withSeconds && seconds < SECONDS_PER_MINUTE) {
			unit = MeasureUnit.SECOND;
			value = seconds;
		} else if (seconds < SECONDS_PER_HOUR) {
			unit = MeasureUnit.MINUTE;
			value = seconds / SECONDS_PER_MINUTE;
		} else if (seconds < SECONDS_PER_DAY) {
			unit = MeasureUnit.HOUR;
			value = seconds / SECONDS_PER_HOUR;
		} else if (seconds < SECONDS_PER_WEEK) {
			unit = MeasureUnit.DAY;
			value = seconds / SECONDS_PER_DAY;
		} else {
			unit = MeasureUnit.WEEK;
			value = seconds / SECONDS_PER_WEEK;
		}

		final Locale locale = context.getResources().getConfiguration().locale;
		final MeasureFormat measureFormat = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW);
		return measureFormat.formatMeasures(new Measure(value, unit));
	}
}