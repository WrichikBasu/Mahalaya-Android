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
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class ManualDateActivity extends AppCompatActivity implements View.OnClickListener {

	private static int day, month;

	private int chosenTheme;

	private static boolean hasDateBeenPicked = false;

	private DatePicker datePicker;

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
		setContentView(R.layout.activity_manual_date);

		Toolbar myToolbar = findViewById(R.id.toolbar2);
		setSupportActionBar(myToolbar);

		Button datePicked = findViewById(R.id.datePicked);
		datePicker = findViewById(R.id.datePicker);

		datePicker.setMinDate(System.currentTimeMillis() - 1000);

		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.getInstance().get(Calendar.YEAR), 11, 31);
		datePicker.setMaxDate(cal.getTimeInMillis());

		datePicked.setOnClickListener(this);

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
		if (hasDateBeenPicked && (month != 0) && (day != 0)) {
			datePicker.updateDate(Calendar.getInstance().get(Calendar.YEAR), month - 1, day);
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
		if (v.getId() == R.id.datePicked) {

			day = datePicker.getDayOfMonth();
			month = datePicker.getMonth() + 1;

			hasDateBeenPicked = true;

			Toast.makeText(getApplicationContext(), getString(R.string.date_success),
					Toast.LENGTH_SHORT).show();

			startTimeManual();
		}
	}

	private void startTimeManual() {
		Intent intent = new Intent(this, ManualTimeActivity.class);
		intent.putExtra("month", month);
		intent.putExtra("day", day);
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
