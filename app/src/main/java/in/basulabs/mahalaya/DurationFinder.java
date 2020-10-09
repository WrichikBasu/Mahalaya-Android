package in.basulabs.mahalaya;

import android.content.Context;

import java.text.NumberFormat;

class DurationFinder {

	/**
	 * Takes a duration in milliseconds and converts it to days, hours, minutes and seconds.
	 *
	 * @param milli The duration
	 * @param type {@<code>type = 1</code>} indicates output for activity, while {@<code>type =
	 * 		2</code>} indicates output for notification
	 *
	 * @return
	 */
	@SuppressWarnings("JavaDoc")
	static String getDuration(long milli, int type, Context ctxt) {

		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);

		long seconds = milli / 1000;

		long minutes = seconds / 60;
		seconds = seconds % 60;

		long hours = minutes / 60;
		minutes = minutes % 60;

		long days = hours / 24;
		hours = hours % 24;

		String msg = "";

		if (type == 1) {

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
								.getString(R.string.and) + " " + numFormat
								.format(seconds) + " " + ctxt.getResources()
								.getQuantityString(R.plurals.secs, (int) seconds);
					}
				} else {
					msg = numFormat.format(hours) + " " + ctxt.getResources()
							.getQuantityString(R.plurals.hour, (int) hours) + ", " + numFormat
							.format(minutes) + " " + ctxt.getResources()
							.getQuantityString(R.plurals.mins, (int) minutes) + " " + ctxt
							.getString(R.string.and) + " " + numFormat.format(seconds) + " " + ctxt
							.getResources().getQuantityString(R.plurals.secs, (int) seconds);
				}
			} else {
				msg = numFormat.format(days) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.day, (int) days) + ", " + numFormat
						.format(hours) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.hour, (int) hours) + ", " + numFormat
						.format(minutes) + " " + ctxt.getResources()
						.getQuantityString(R.plurals.mins, (int) minutes) + " " + ctxt
						.getString(R.string.and) + " " + numFormat.format(seconds) + " " + ctxt
						.getResources().getQuantityString(R.plurals.secs, (int) seconds);
			}
		} else if (type == 2) {
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
						.getResources().getString(R.string.hour_short) + " " + numFormat
						.format(minutes) + " " + ctxt.getResources()
						.getString(R.string.minute_short) + " " + numFormat
						.format(seconds) + " " + ctxt.getResources()
						.getString(R.string.second_short);
			}
		}

		return msg;
	}
}
