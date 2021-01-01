package in.basulabs.mahalaya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class MediaPlayerActivity extends AppCompatActivity implements View.OnClickListener {

	private Button abortBtn;
	private TextView timeLeftView, currentStateView;

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

	private CountDownTimer playbackCountDownTimer;

	/**
	 * Indicates whether this activity is alive or dead.
	 */
	public static boolean IamAlive;

	//--------------------------------------------------------------------------------------------

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			switch (Objects.requireNonNull(intent.getAction())) {

				case Constants.ACTION_KILL_MEDIA_ACT:
					finish();
					break;

				case Constants.ACTION_PAUSE_PLAYER:
					currentStateView.setText(getString(R.string.media_paused));
					if (playbackCountDownTimer != null) {
						playbackCountDownTimer.cancel();
					}
					break;

				case Constants.ACTION_START_PLAYER:
					currentStateView.setText(getString(R.string.media_playing));
					initialiseTimer();
					playbackCountDownTimer.start();
					break;
			}
		}
	};

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_palying_activity);

		Toolbar myToolbar = findViewById(R.id.toolbar6);
		setSupportActionBar(myToolbar);

		// Get the theme:
		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM : Constants.THEME_AUTO_TIME;
		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME, defaultTheme);

		// Set the theme:
		if (savedInstanceState == null) {
			changeTheme();
		}

		abortBtn = findViewById(R.id.abort_btn2);
		timeLeftView = findViewById(R.id.timeLeftView2);
		currentStateView = findViewById(R.id.currentStateView);

		abortBtn.setOnClickListener(this);

		/////////////////////////////////////////////////////////////////////////////////////
		// Consider the case where the playback is paused. The currentStateView TextView
		// will show "Paused". Now if the config changes, the activity will be
		// recreated. But currentStateView and timeLeftView will have default values
		// as setTimeLeft() works only when the media player is active. Therefore, we
		// set the views in onCreate() so that the user doesn't see default values.
		/////////////////////////////////////////////////////////////////////////////////////
		try {
			timeLeftView.setText(DurationFinder.getDuration(MahalayaService.getDurationLeft(),
					DurationFinder.TYPE_ACTIVITY, this));
			if (MahalayaService.mediaPlayer.isPlaying()) {
				currentStateView.setText(this.getString(R.string.media_playing));
			} else {
				currentStateView.setText(this.getString(R.string.media_paused));
			}
		} catch (Exception ignored) {
		}

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_KILL_MEDIA_ACT);
		intentFilter.addAction(Constants.ACTION_PAUSE_PLAYER);
		intentFilter.addAction(Constants.ACTION_START_PLAYER);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	//--------------------------------------------------------------------------------------------

	@Override
	protected void onStart() {
		super.onStart();

		IamAlive = true;

		if (MahalayaService.mediaPlayer.isPlaying()) {
			initialiseTimer();
			playbackCountDownTimer.start();
		}
	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onStop() {
		super.onStop();

		if (playbackCountDownTimer != null) {
			playbackCountDownTimer.cancel();
		}
		IamAlive = false;
	}

	//-------------------------------------------------------------------------------------------

	private void initialiseTimer() {

		Context context = this;

		playbackCountDownTimer = new CountDownTimer(MahalayaService.getDurationLeft(), 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (MahalayaService.mediaPlayer.isPlaying()) {
					timeLeftView.setText(DurationFinder.getDuration(millisUntilFinished, DurationFinder.TYPE_ACTIVITY, context));
				}
			}

			@Override
			public void onFinish() {
				timeLeftView.setText(getString(R.string.playback_complete));
			}
		};

	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	//-------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu2, menu);
		return true;
	}

	//-------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//-------------------------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		if (v.getId() == abortBtn.getId()) {
			Intent intent = new Intent(getApplicationContext(), MahalayaService.class);
			stopService(intent);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	//-------------------------------------------------------------------------------------------

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

		getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
				.edit()
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
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
