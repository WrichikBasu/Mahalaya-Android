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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class DateTimeActivity extends AppCompatActivity implements View.OnClickListener {

	private static int month, day, hours, minutes;

	private static boolean hasUserPickedValues = false;

	private Button setToday_btn, proceed_btn;

	private int chosenTheme;

	private final BroadcastReceiver activityKiller = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Log.e(this.getClass().toString(), "Broadcast received to kill activity.");
			finish();
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		//Log.e(this.getClass().toString(), "Created.");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date_time);

		Toolbar myToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);

		setToday_btn = findViewById(R.id.btn_today);
		Button setTmrw_btn = findViewById(R.id.btn_tmrw);
		Button setManual_btn = findViewById(R.id.btn_manual);
		proceed_btn = findViewById(R.id.btn_proceed);


		setToday_btn.setOnClickListener(this);
		setTmrw_btn.setOnClickListener(this);
		setManual_btn.setOnClickListener(this);
		proceed_btn.setOnClickListener(this);

		proceed_btn.setEnabled(false);
		setTmrw_btn.setEnabled(true);
		setManual_btn.setEnabled(true);

		///////////////////////////////////////////////////////////
		// Register the broadcast receiver
		//////////////////////////////////////////////////////////
		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		registerReceiver(activityKiller, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (hasUserPickedValues) {
			proceed_btn.setEnabled(true);
		}
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 4) {
			setToday_btn.setEnabled(false);
		} else {
			setToday_btn.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		proceed_btn.setEnabled(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(activityKiller);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		} else if (item.getItemId() == R.id.action_help) {
			Intent intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startManualDate() {
		Intent intent = new Intent(this, ManualDateActivity.class);
		startActivity(intent);
	}

	private void startFileActivity() {
		Intent intent = new Intent(this, FilesActivity.class);
		intent.putExtra("month", month);
		intent.putExtra("day", day);
		intent.putExtra("hour", hours);
		intent.putExtra("minute", minutes);
		startActivity(intent);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btn_today:
				Calendar cal = Calendar.getInstance();

				month = cal.get(Calendar.MONTH) + 1;
				day = cal.get(Calendar.DAY_OF_MONTH);
				hours = 4;
				minutes = 0;

				Toast.makeText(getApplicationContext(), getString(R.string.toast_today),
						Toast.LENGTH_SHORT).show();

				proceed_btn.setEnabled(true);
				hasUserPickedValues = true;
				break;

			case R.id.btn_tmrw:
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);

				month = cal.get(Calendar.MONTH) + 1;
				day = cal.get(Calendar.DAY_OF_MONTH);
				hours = 4;
				minutes = 0;

				Toast.makeText(getApplicationContext(), getString(R.string.toast_tomorrow),
						Toast.LENGTH_SHORT).show();

				proceed_btn.setEnabled(true);
				hasUserPickedValues = true;
				break;

			case R.id.btn_manual:
				hasUserPickedValues = false;
				startManualDate();
				break;

			case R.id.btn_proceed:
				startFileActivity();
				break;
		}
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
