package in.basulabs.mahalaya;

import android.content.Context;

import java.text.NumberFormat;

/**
 * Converts a duration to human-readable form.
 */
final class DurationFinder {

	/**
	 * Indicates that the output is for an activity.
	 * <p>
	 * In this type of output, nothing is abbreviated. For example, seconds will be written as
	 * "seconds", minutes as "minutes", and so on.
	 * </p>
	 */
	static final int TYPE_ACTIVITY = 1;

	/**
	 * Indicates that the output is for use in a notification.
	 * <p>
	 * In this type of output, everything is abbrevaited. For example, seconds will be written as
	 * "s", minutes as "m" and so on.
	 * </p>
	 */
	static final int TYPE_NOTIFICATION = 2;

	/**
	 * Converts a duration (in milliseconds) to human-readable form.
	 *
	 * @param millis The duration in milliseconds.
	 * @param type The type of output. Can be either {@link #TYPE_ACTIVITY} or
	 * {@link #TYPE_NOTIFICATION}.
	 * @param ctxt The context of the caller. Needed to produce the output in the proper Locale.
	 *
	 * @return The duration in a human-readable form.
	 */
	static String getDuration(long millis, int type, Context ctxt) {

		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);

		long seconds = millis / 1000;

		long minutes = seconds / 60;
		seconds = seconds % 60;

		long hours = minutes / 60;
		minutes = minutes % 60;

		long days = hours / 24;
		hours = hours % 24;

		String msg = "";

		if (type == TYPE_ACTIVITY) {

			if (days == 0) {
				if (hours == 0) {
					if (minutes == 0) {
						if (seconds == 0) {
							msg = ctxt.getString(R.string.time_up);
						} else {
							msg = numFormat.format(seconds) + " " + ctxt.getResources()
									.getQuantityString(R.plurals.secs, (int) seconds);
						}
					} else {
						msg = numFormat.format(minutes) + " " + ctxt.getResources()
								.getQuantityString(R.plurals.mins, (int) minutes) + " " + ctxt
								.getString(R.string.and) + " "
								+ numFormat.format(seconds) + " " + ctxt.getResources()
								.getQuantityString(R.plurals.secs, (int) seconds);
					}
				} else {
					msg = numFormat.format(hours) + " " + ctxt.getResources()
							.getQuantityString(R.plurals.hour, (int) hours) + ", "
							+ numFormat.format(minutes) + " " + ctxt.getResources()
							.getQuantityString(R.plurals.mins, (int) minutes) + " " + ctxt
							.getString(R.string.and) + " "
							+ numFormat.format(seconds) + " " + ctxt
							.getResources().getQuantityString(R.plurals.secs, (int) seconds);
				}
			} else {
				msg = numFormat.format(days) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.day, (int) days) + ", " + numFormat
						.format(hours) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.hour, (int) hours) + ", " + numFormat
						.format(minutes) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.mins, (int) minutes) + " " + ctxt
						.getString(R.string.and) + " "
						+ numFormat.format(seconds) + " " + ctxt
						.getResources().getQuantityString(R.plurals.secs, (int) seconds);
			}
		} else if (type == TYPE_NOTIFICATION) {
			if (days == 0) {
				if (hours == 0) {
					if (minutes == 0) {
						if (seconds == 0) {
							msg = ctxt.getString(R.string.time_up);
						} else {
							msg = numFormat.format(seconds) + " " + ctxt.getResources()
									.getString(R.string.second_short);
						}
					} else {
						msg = numFormat.format(minutes) + " " + ctxt.getResources()
								.getString(R.string.minute_short) + " " + numFormat
								.format(seconds) + " " + ctxt.getResources()
								.getString(R.string.second_short);
					}
				} else {
					msg = numFormat.format(hours) + " " + ctxt.getResources()
							.getString(R.string.hour_short) + " " + numFormat
							.format(minutes) + " " + ctxt.getResources()
							.getString(R.string.minute_short) + " " + numFormat
							.format(seconds) + " " + ctxt.getResources()
							.getString(R.string.second_short);
				}
			} else {
				msg = numFormat.format(days) + " " + ctxt.getResources()
						.getString(R.string.day_short) + " " + numFormat.format(hours) + " " + ctxt
						.getResources().getString(R.string.hour_short)
						+ " " + numFormat.format(minutes) + " " + ctxt.getResources()
						.getString(R.string.minute_short) + " "
						+ numFormat.format(seconds) + " " + ctxt.getResources()
						.getString(R.string.second_short);
			}
		}

		return msg;
	}
}
