package in.basulabs.mahalaya;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.time.LocalTime;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class SplashScreenActivity extends AppCompatActivity {

	/**
	 * The currently active theme.
	 * <p>
	 * Can have four values: {@link Constants#THEME_LIGHT}, {@link Constants#THEME_DARK}, {@link Constants#THEME_SYSTEM}, {@link Constants#THEME_AUTO_TIME}.
	 * </p>
	 *
	 * @see Constants#THEME_LIGHT
	 * @see Constants#THEME_DARK
	 * @see Constants#THEME_SYSTEM
	 * @see Constants#THEME_AUTO_TIME
	 */
	int currentTheme;

	private CountDownTimer countDownTimer;

	/**
	 * The time (in milliseconds) after which the next activity should be launched.
	 * <p>
	 * This variable has to be used with {@link #countDownTimer}. On each tick, the {@code millisUntilFinished} is stored in this variable. If the activity is
	 * recreated  or paused, then the countdown will start from the left over state and not again from the beginning.
	 * </p>
	 */
	private long millisInFuture;

	/**
	 * Save instance state key for storing {@link #millisInFuture}.
	 */
	private static final String SAVE_INSTANCE_KEY_MILLIS_IN_FUTURE = "in.basulabs.mahalaya.SplashScreenActivity.MILLIS_IN_FUTURE";

	//--------------------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		// Get the theme:
		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM :	Constants.THEME_AUTO_TIME;
		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME,	defaultTheme);

		// Set the theme:
		if (savedInstanceState == null) {
			changeTheme();
			millisInFuture = 5000;
		} else {
			millisInFuture = savedInstanceState.getLong(SAVE_INSTANCE_KEY_MILLIS_IN_FUTURE);
		}

	}

	//--------------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();

		Context context = this;

		countDownTimer = new CountDownTimer(millisInFuture, 100) {

			@Override
			public void onTick(long millisUntilFinished) {
				millisInFuture = millisUntilFinished;
			}

			@Override
			public void onFinish() {
				Intent intent = new Intent(context, DateTimeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
			}
		};
		countDownTimer.start();
	}

	//--------------------------------------------------------------------------------------------------------------

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SAVE_INSTANCE_KEY_MILLIS_IN_FUTURE, millisInFuture);
	}

	//---------------------------------------------------------------------------------------------------------------

	@Override
	protected void onPause() {
		super.onPause();
		countDownTimer.cancel();
	}


	//---------------------------------------------------------------------------------------------------------------

	private void changeTheme() {

		switch (currentTheme) {
			case Constants.THEME_LIGHT:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;

			case Constants.THEME_DARK:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;

			case Constants.THEME_SYSTEM:
				AppCompatDelegate
						.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				break;

			case Constants.THEME_AUTO_TIME:
				if (LocalTime.now().isBefore(LocalTime.of(6, 0))
						|| LocalTime.now().isAfter(LocalTime.of(21, 59))) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
				break;
		}
	}

}


