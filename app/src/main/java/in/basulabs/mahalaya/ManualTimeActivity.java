package in.basulabs.mahalaya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class ManualTimeActivity extends AppCompatActivity implements View.OnClickListener {

	private static boolean hasTimeBeenPicked = false;

	private static int day, month, hour, minute;

	private int chosenTheme;

	private TimePicker timePicker;

	private final BroadcastReceiver activityKiller = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.e(this.getClass().toString(), "Broadcast received to kill activity.");
			finish();
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.e(this.getClass().toString(), "Inside onCreate()");
		setContentView(R.layout.activity_manual_time);

		Toolbar myToolbar = findViewById(R.id.toolbar3);
		setSupportActionBar(myToolbar);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			month = extras.getInt("month");
			day = extras.getInt("day");
		}

		Button timePicked = findViewById(R.id.timePicked);
		timePicker = findViewById(R.id.timePicker1);
		timePicked.setOnClickListener(this);

		///////////////////////////////////////////////////////////
		// Register the broadcast receiver
		//////////////////////////////////////////////////////////
		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		registerReceiver(activityKiller, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.e(this.getClass().toString(), "Inside onResume()");
		if (hasTimeBeenPicked) {
			timePicker.setCurrentHour(hour);
			timePicker.setCurrentMinute(minute);

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.e(this.getClass().toString(), "Inside onDestroy()");
		unregisterReceiver(activityKiller);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		} else if (item.getItemId() == R.id.action_help) {
			Intent intent = new Intent(this, HelpActivity.class);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.timePicked) {
			hour = timePicker.getCurrentHour();
			minute = timePicker.getCurrentMinute();
			if (! isDateOK()) {
				Toast.makeText(getApplicationContext(), getString(R.string.past_time),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.time_success),
						Toast.LENGTH_SHORT).show();
				hasTimeBeenPicked = true;
				startFileActivity();
			}
		}

	}

	private boolean isDateOK() {
		Calendar cal = Calendar.getInstance();
		if (month == cal.get(Calendar.MONTH) + 1 && day == cal.get(Calendar.DAY_OF_MONTH)) {
			int time_now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
			int time_inp = minute + hour * 60;
			return time_inp > time_now;
		}
		return true;
	}

	private void startFileActivity() {
		Intent intent = new Intent(this, FilesActivity.class);
		intent.putExtra("month", month);
		intent.putExtra("day", day);
		intent.putExtra("hour", hour);
		intent.putExtra("minute", minute);
		startActivity(intent);
	}

	private void createThemeDialog() {
		AlertDialog.Builder alb = new AlertDialog.Builder(this);
		alb.setTitle(getString(R.string.choose_theme));
		alb.setCancelable(true);

		String[] items;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.system)};
		} else {
			items = new String[]{getString(R.string.light), getString(R.string.dark)};
		}

		int checkedItem = 0;

		if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
			//Log.e(this.getClass().toString(), "App night mode active.");
			checkedItem = 1;
		} else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
			//Log.e(this.getClass().toString(), "App night mode inactive.");
			checkedItem = 0;
		} else if (AppCompatDelegate
				.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
			checkedItem = 2;
		}

		alb.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chosenTheme = which;
			}
		});

		alb.setPositiveButton(getString(R.string.set_theme), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (chosenTheme == 0) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				} else if (chosenTheme == 1) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else if (chosenTheme == 2) {
					AppCompatDelegate
							.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				}
			}
		});

		alb.show();
	}
}
