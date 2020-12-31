package in.basulabs.mahalaya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class DateTimeActivity extends AppCompatActivity implements View.OnClickListener {

	/**
	 * The date and time when the playback should be started. If no date and time is selected, this will be {@code
	 * null}.
	 */
	private LocalDateTime playbackDateTime;

	private Button setTodayBtn, proceedBtn;

	/**
	 * The currently active theme.
	 * <p>
	 * Can have four values: {@link Constants#THEME_LIGHT}, {@link Constants#THEME_DARK}, {@link
	 * Constants#THEME_SYSTEM}, {@link Constants#THEME_AUTO_TIME}.
	 * </p>
	 *
	 * @see Constants#THEME_LIGHT
	 * @see Constants#THEME_DARK
	 * @see Constants#THEME_SYSTEM
	 * @see Constants#THEME_AUTO_TIME
	 */
	private int currentTheme;

	/**
	 * Save Instance State key for {@link #playbackDateTime}.
	 */
	private static final String SAVE_INSTANCE_KEY_PLAYBACK_DATE_TIME =	"in.basulabs.mahalaya.DateTimeActivity.PLAYBACK_DATE_TIME";

	//-------------------------------------------------------------------------------------------

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Objects.equals(intent.getAction(), Constants.ACTION_KILL_ALL_UI_ACTIVITIES)) {
				finish();
			}
		}
	};

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date_time);

		Toolbar myToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);

		setTodayBtn = findViewById(R.id.btn_today);
		Button setTmrw_btn = findViewById(R.id.btn_tmrw);
		Button setManual_btn = findViewById(R.id.btn_manual);
		proceedBtn = findViewById(R.id.btn_proceed);

		setTodayBtn.setOnClickListener(this);
		setTmrw_btn.setOnClickListener(this);
		setManual_btn.setOnClickListener(this);
		proceedBtn.setOnClickListener(this);

		proceedBtn.setEnabled(false);
		setTmrw_btn.setEnabled(true);
		setManual_btn.setEnabled(true);

		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		registerReceiver(broadcastReceiver, intentFilter);

		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM : Constants.THEME_AUTO_TIME;

		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME,	defaultTheme);

		if (savedInstanceState == null){
			playbackDateTime = null;
		} else {
			playbackDateTime = (LocalDateTime) savedInstanceState.getSerializable(SAVE_INSTANCE_KEY_PLAYBACK_DATE_TIME);
		}

	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();

		if (playbackDateTime == null) {
			proceedBtn.setEnabled(false);
		} else {
			proceedBtn.setEnabled(true);
		}

		if (! LocalTime.now().isBefore(LocalTime.of(4, 0))) {
			setTodayBtn.setEnabled(false);
		} else {
			setTodayBtn.setEnabled(true);
		}
	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_INSTANCE_KEY_PLAYBACK_DATE_TIME, playbackDateTime);
	}


	//-------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onPause() {
		super.onPause();
		proceedBtn.setEnabled(false);
	}

	//-------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	//-------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_nightDark:
				createThemeDialog();
				return true;
			case R.id.action_help:
				Intent intent = new Intent(this, HelpActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//-------------------------------------------------------------------------------------------

	/**
	 * Starts {@link ManualDateActivity}.
	 */
	private void startManualDateActivity() {
		Intent intent = new Intent(this, ManualDateActivity.class);
		startActivity(intent);
	}

	//-------------------------------------------------------------------------------------------

	/**
	 * Starts {@link FilesActivity}.
	 */
	private void startFileActivity() {
		Intent intent = new Intent(this, FilesActivity.class);
		intent.putExtra(Constants.EXTRA_PLAYBACK_DATE_TIME, playbackDateTime);
		startActivity(intent);
	}

	//-------------------------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btn_today:
				playbackDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0));

				Toast.makeText(getApplicationContext(), getString(R.string.toast_today), Toast.LENGTH_SHORT).show();

				proceedBtn.setEnabled(true);
				break;

			case R.id.btn_tmrw:
				playbackDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(4, 0)).plusDays(1);

				Toast.makeText(getApplicationContext(), getString(R.string.toast_tomorrow),	Toast.LENGTH_SHORT).show();

				proceedBtn.setEnabled(true);
				break;

			case R.id.btn_manual:
				playbackDateTime = null;
				startManualDateActivity();
				break;

			case R.id.btn_proceed:
				startFileActivity();
				break;
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
