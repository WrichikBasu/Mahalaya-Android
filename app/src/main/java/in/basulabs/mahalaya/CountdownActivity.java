package in.basulabs.mahalaya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;
import static in.basulabs.mahalaya.MahalayaService.playbackDateTime;

public class CountdownActivity extends AppCompatActivity implements View.OnClickListener,
		CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

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
	private int currentTheme;

	/**
	 * Indicates whether this activity is visible on the screen or not.
	 * <p>
	 * This variable is changed by {@link #onResume()} and {@link #onPause()}.
	 * </p>
	 */
	public static boolean IamAlive;

	/**
	 * This variable is used with {@link #countDownTimer} to change the color of the warning periodically.
	 */
	private boolean colorIsNormal = true;

	/**
	 * This timer has two tasks: 1. Show the time left, and 2. Change the colour of the warning.
	 */
	private CountDownTimer countDownTimer;

	private SeekBar volumeSeekBar;
	private TextView volumeControlLabel;

	SharedPreferences sharedPreferences;

	//--------------------------------------------------------------------------------------------

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Objects.equals(intent.getAction(), Constants.ACTION_KILL_COUNTDOWN_ACT)) {
				finish();
			}
		}
	};

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.countdown_activity);

		sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);

		// Set the action bar:
		Toolbar myToolbar = findViewById(R.id.toolbar5);
		setSupportActionBar(myToolbar);

		// Set the theme:
		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM : Constants.THEME_AUTO_TIME;

		currentTheme = sharedPreferences.getInt(SHARED_PREF_KEY_THEME, defaultTheme);

		if (savedInstanceState == null) {
			changeTheme();
		}

		// Initialise volume control options:

		CheckBox volumeControlCheckBox = findViewById(R.id.volumeControlCheckBox);
		volumeSeekBar = findViewById(R.id.playbackVolumeSeekBar);
		volumeControlLabel = findViewById(R.id.playbackVolumeLabel);

		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

		volumeControlCheckBox.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_VOL_CTRL_ENABLED, true));
		volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumeSeekBar.setProgress(sharedPreferences.getInt(Constants.SHARED_PREF_KEY_VOLUME,
				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));

		volumeControlLabel.setEnabled(volumeControlCheckBox.isChecked());
		volumeSeekBar.setEnabled(volumeControlCheckBox.isChecked());

		volumeControlCheckBox.setOnCheckedChangeListener(this);
		volumeSeekBar.setOnSeekBarChangeListener(this);

		int day = playbackDateTime.getDayOfMonth(), month = playbackDateTime.getMonthValue(),
				year = playbackDateTime.getYear(), hour = playbackDateTime.getHour(),
				minute = playbackDateTime.getMinute();

		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);

		/////////////////////////////////////////////////////////
		// Display the date when the media will be played:
		////////////////////////////////////////////////////////
		TextView dateView = findViewById(R.id.dateView);
		String date = (Integer.toString(day).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(day)) : ("" + numFormat.format(day)))
				+ "." + (Integer.toString(month).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(month)) : ("" + numFormat.format(month))) + "."
				+ "" + numFormat.format(year);
		dateView.setText(date);

		//////////////////////////////////////////////////////
		// Display the time when the media will be played:
		/////////////////////////////////////////////////////
		TextView timeView = findViewById(R.id.timeView);
		String time = (Integer.toString(hour).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(hour)) : ("" + numFormat.format(hour)))
				+ ":" + (Integer.toString(minute).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(minute)) : "" + numFormat.format(minute));
		timeView.setText(time);

		TextView timeLeftTextView = findViewById(R.id.timeLeftView);
		TextView warningTextView = findViewById(R.id.reminder_vol);

		Button abort_btn = findViewById(R.id.abort_btn);
		abort_btn.setOnClickListener(this);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_KILL_COUNTDOWN_ACT);
		registerReceiver(broadcastReceiver, intentFilter);

		/////////////////////////////////////////////////////////////
		// Display the time left, and periodically change colour
		// of the warning.
		/////////////////////////////////////////////////////////////
		Duration duration = Duration.between(LocalDateTime.now(), playbackDateTime);
		Context context = this;

		countDownTimer = new CountDownTimer(duration.toMillis(), 500) {
			@Override
			public void onTick(long millisUntilFinished) {

				// Display time left:
				timeLeftTextView.setText(DurationFinder.getDuration(millisUntilFinished, DurationFinder.TYPE_ACTIVITY, context));

				// Change warning colour:
				if (colorIsNormal) {
					warningTextView.setTextColor(getResources().getColor(R.color.focus_color));
				} else {
					warningTextView.setTextColor(getResources().getColor(R.color.default_color));
				}
				colorIsNormal = ! colorIsNormal;
			}

			@Override
			public void onFinish() {
				timeLeftTextView.setText(R.string.time_up);
			}
		};
	}

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		IamAlive = true;
		countDownTimer.start();
	}

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onPause() {
		super.onPause();
		IamAlive = false;
		countDownTimer.cancel();
	}

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu2, menu);
		return true;
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.abort_btn) {
			Intent intent = new Intent(this, MahalayaService.class);
			stopService(intent);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Creates the theme chooser dialog.
	 */
	private void createThemeDialog() {

		AlertDialog.Builder alb = new AlertDialog.Builder(this);
		alb.setTitle(getString(R.string.choose_theme));
		alb.setCancelable(true);

		String[] items;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.time), getString(R.string.system)};
		} else {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.time)};
		}

		AtomicInteger newTheme = new AtomicInteger(currentTheme);

		alb.setSingleChoiceItems(items, currentTheme, (dialog, which) -> newTheme.set(which));

		alb.setPositiveButton(getString(R.string.set_theme), (dialog, which) -> {
			currentTheme = newTheme.get();
			changeTheme();
		});

		alb.show();
	}

	//---------------------------------------------------------------------------------------------------------------

	/**
	 * Changes the theme of the app, and also updates the theme in {@link android.content.SharedPreferences}.
	 */
	private void changeTheme() {

		sharedPreferences.edit()
				.remove(SHARED_PREF_KEY_THEME)
				.putInt(SHARED_PREF_KEY_THEME, currentTheme)
				.commit();

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

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

		volumeControlLabel.setEnabled(isChecked);
		volumeSeekBar.setEnabled(isChecked);

		sharedPreferences.edit()
				.remove(Constants.SHARED_PREF_KEY_VOL_CTRL_ENABLED)
				.putBoolean(Constants.SHARED_PREF_KEY_VOL_CTRL_ENABLED, isChecked)
				.commit();
	}

	//----------------------------------------------------------------------------------------------------------

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

	}

	//----------------------------------------------------------------------------------------------------------

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	//-----------------------------------------------------------------------------------------------------

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

		sharedPreferences.edit()
				.remove(Constants.SHARED_PREF_KEY_VOLUME)
				.putInt(Constants.SHARED_PREF_KEY_VOLUME, seekBar.getProgress())
				.commit();

	}

}

