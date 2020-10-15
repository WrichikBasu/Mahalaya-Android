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
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class ManualDateActivity extends AppCompatActivity implements View.OnClickListener {

	/**
	 * The date chosen by the user. Note that the time has not been chosen yet.
	 */
	private LocalDate playbackDate;

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

	private DatePicker datePicker;

	/**
	 * Save Instance State key for {@link #playbackDate}.
	 */
	private static final String SAVE_INSTANCE_KEY_PLAYBACK_DATE =
			"in.basulabs.mahalaya.ManualDateActivity.PLAYBACK_DATE";

	//-----------------------------------------------------------------------------------------------------------

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Objects.equals(intent.getAction(), Constants.ACTION_KILL_ALL_UI_ACTIVITIES)) {
				finish();
			}
		}
	};

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_date);

		Toolbar myToolbar = findViewById(R.id.toolbar2);
		setSupportActionBar(myToolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		Button datePickBtn = findViewById(R.id.datePicked);
		datePicker = findViewById(R.id.datePicker);

		datePicker.setMinDate(ZonedDateTime.now().toEpochSecond() * 1000);

		datePickBtn.setOnClickListener(this);

		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		registerReceiver(broadcastReceiver, intentFilter);

		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM :
				Constants.THEME_AUTO_TIME;

		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME,
				defaultTheme);

		if (savedInstanceState == null) {
			playbackDate = LocalDate.now();
		} else {
			playbackDate = (LocalDate) savedInstanceState.getSerializable(SAVE_INSTANCE_KEY_PLAYBACK_DATE);
		}

	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		datePicker.updateDate(playbackDate.getYear(), playbackDate.getMonthValue() - 1, playbackDate.getDayOfMonth());
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_INSTANCE_KEY_PLAYBACK_DATE, playbackDate);
	}


	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_nightDark:
				createThemeDialog();
				return true;

			case R.id.action_help:
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				startActivity(intent);
				return true;

			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.datePicked) {

			playbackDate = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());

			if (playbackDate.isBefore(LocalDate.now())) {
				Toast.makeText(getApplicationContext(), "Date is in the past!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.date_success), Toast.LENGTH_SHORT).show();
				startTimeActivity();
			}

		}
	}

	//-----------------------------------------------------------------------------------------------------------

	/**
	 * Starts {@link ManualTimeActivity}.
	 */
	private void startTimeActivity() {
		Intent intent = new Intent(this, ManualTimeActivity.class);
		intent.putExtra(Constants.EXTRA_PLAYBACK_DATE, playbackDate);
		startActivity(intent);
	}

	//-----------------------------------------------------------------------------------------------------------

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
