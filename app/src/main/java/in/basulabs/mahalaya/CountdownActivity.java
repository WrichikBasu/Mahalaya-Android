package in.basulabs.mahalaya;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import java.text.NumberFormat;
import java.util.Calendar;

public class CountdownActivity extends AppCompatActivity implements View.OnClickListener {

	private static int month, day, hour, minute;
	/////////////////////////////////////////////////////
	// These values are made static so that if, on
	// activity re-creation, the intent delivers null
	// values, the duration can still be shown.
	////////////////////////////////////////////////////

	@SuppressLint("StaticFieldLeak")
	public static TextView timeLeftView;

	private static int chosenTheme;
	/////////////////////////////////////////////////////
	// 0 - Light theme
	// 1 - Dark Theme
	// 2 - Set by time
	// 3 - Set by System (Available on Android Q+)
	////////////////////////////////////////////////////

	public static boolean IamAlive;
	////////////////////////////////////////////////////////////////////
	// Carries information whether the activity is visible or not.
	// This variable is changed by onCreate() and onPause()
	////////////////////////////////////////////////////////////////////

	private static boolean shouldThemeBeSet = false;
	//////////////////////////////////////////////////////////////////////////
	// Checks whether theme should be set by onCreate().
	// If the activity is created/recreated due to a theme change by the
	// theme change dialog box, then (except one case - set by time)
	// onCreate() should not change theme of the activity. Do not change
	// theme first time as this activity will be created when the previous
	// activity has just died.
	/////////////////////////////////////////////////////////////////////////

	private static boolean isThisFirstExecution = true;
	////////////////////////////////////////////////////////////////////////////////////
	// When this activity is started for the first time, the user would be looking
	// at it. So the theme of this activity will be set according to the previous
	// activity. Once the user closes this activity after first creation,
	// onPause() will set the theme to "Set by time" (by setting
	// shouldThemeBeSet = true), and set this flag to false.
	//
	// However, if the user changes the theme during
	// first creation itself, then that is respected by changing this flag to
	// false in the dialog box listener, so that onPause() will no longer change
	// the theme automatically after first instance of this activity is paused.
	///////////////////////////////////////////////////////////////////////////////////

	private Handler handler;
	private TextView reminder;
	private boolean colorIsBlack = true;

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
		setContentView(R.layout.countdown_activity);
		//Log.e(this.getClass().toString(), "Inside onCreate()");

		//////////////////////////////////////////////////////////////
		// Set the action bar:
		/////////////////////////////////////////////////////////////
		Toolbar myToolbar = findViewById(R.id.toolbar5);
		setSupportActionBar(myToolbar);

		/////////////////////////////////////////////////////////////////////
		// Do not change theme on first instance.
		//
		// Also, if activity has been (re)created by a theme change from
		// theme change dialog box, then do not change theme.
		/////////////////////////////////////////////////////////////////////
		if (shouldThemeBeSet) {
			// Set theme to "Choose by time" default
			chosenTheme = 2;
			if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || Calendar.getInstance()
					.get(Calendar.HOUR_OF_DAY) >= 22) {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
			} else {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
			}
		} else {
			//////////////////////////////////////////////////////////////////
			// We have to determine the theme only the first time the
			// activity is created, because from consequent creations,
			// either onPause() will set the theme, or the user will
			// select the theme.
			/////////////////////////////////////////////////////////////////
			if (isThisFirstExecution) {
				if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
					chosenTheme = 1;
				} else if (AppCompatDelegate
						.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
					chosenTheme = 0;
				} else if (AppCompatDelegate
						.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
					chosenTheme = 3;
				}
			}
		}

		//////////////////////////////////////////////////////////////////
		// Get the data from the intent:
		/////////////////////////////////////////////////////////////////
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			month = extras.getInt("month");
			day = extras.getInt("day");
			hour = extras.getInt("hour");
			minute = extras.getInt("minute");
			//Log.e(this.getClass().toString(), "Received " + month + " " + day + " " + hour + " " + minute);
		}

		////////////////////////////////////////////////////////////////
		// Set the default values for the following text views:
		// 1. Date that has been set
		// 2. Time that has been set
		// 3. Time left for playback to end
		///////////////////////////////////////////////////////////////
		TextView dateView = findViewById(R.id.dateView);
		TextView timeView = findViewById(R.id.timeView);
		timeLeftView = findViewById(R.id.timeLeftView);
		reminder = findViewById(R.id.reminder_vol);

		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setGroupingUsed(false);

		String date = (Integer.toString(day).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(day)) : ("" + numFormat.format(day)))
				+ "." + (Integer.toString(month).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(month)) : ("" + numFormat.format(month))) + "."
				+ "" + numFormat.format(Calendar.getInstance().get(Calendar.YEAR));

		dateView.setText(date);

		String time = (Integer.toString(hour).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(hour)) : ("" + numFormat.format(hour)))
				+ ":" + (Integer.toString(minute).length() == 1 ? (numFormat.format(0L)
				+ "" + numFormat.format(minute)) : "" + numFormat.format(minute));

		timeView.setText(time);

		Button abort_btn = findViewById(R.id.abort_btn);
		abort_btn.setOnClickListener(this);

		///////////////////////////////////////////////////////////
		// Register the broadcast receiver
		//////////////////////////////////////////////////////////
		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_COUNTDOWN_ACT);
		registerReceiver(activityKiller, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.e(this.getClass().toString(), "Inside onResume()");
		changeTVColor();
		IamAlive = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Log.e(this.getClass().toString(), "Inside onPause()");

		// Set default theme only the first time the activity is created.
		if (isThisFirstExecution && ! isChangingConfigurations()) {
			shouldThemeBeSet = true;
			isThisFirstExecution = false;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Activity is no longer visible.
		IamAlive = false;
		//Log.e(this.getClass().toString(), "Inside onStop()");
	}

	@Override
	protected void onDestroy() {
		//Log.e(this.getClass().toString(), "Inside onDestroy()");
		super.onDestroy();
		unregisterReceiver(activityKiller);
		timeLeftView = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu2, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.abort_btn) {
			Intent intent = new Intent(this, MahalayaService.class);
			stopService(intent);
			//Log.e(this.getClass().toString(), "Program aborted.");
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	/**
	 * Not only creates the theme dialog, but also sets the theme as per user choice.
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

		int checkedItem = chosenTheme;
		alb.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chosenTheme = which;
			}
		});

		alb.setPositiveButton(getString(R.string.set_theme), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isThisFirstExecution = false;
				if (chosenTheme == 0) {
					//Log.e(this.getClass().toString(), "Setting theme to light.");
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
					shouldThemeBeSet = false;
				} else if (chosenTheme == 1) {
					// Log.e(this.getClass().toString(), "Setting theme to dark.");
					shouldThemeBeSet = false;
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else if (chosenTheme == 2) {
					// Log.e(this.getClass().toString(), "Setting theme according to time.");
					// Let onCreate() change the theme from now on unless changed by user.
					shouldThemeBeSet = true;
					recreate();
				} else if (chosenTheme == 3) {
					// Log.e(this.getClass().toString(), "Setting theme to system default.");
					shouldThemeBeSet = false;
					AppCompatDelegate
							.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				}
			}
		});

		alb.show();
	}

	private void changeTVColor() {
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				if (b.getBoolean("CHANGE_COLOR")) {
					if (colorIsBlack) {
						reminder.setTextColor(getResources().getColor(R.color.focus_color));
					} else {
						reminder.setTextColor(getResources().getColor(R.color.default_color));
					}
					colorIsBlack = ! colorIsBlack;
				}
			}
		};

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				while (IamAlive) {
					Message msg = Message.obtain();
					Bundle data = new Bundle();
					data.putBoolean("CHANGE_COLOR", true);
					msg.setData(data);
					handler.sendMessage(msg);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}
}

