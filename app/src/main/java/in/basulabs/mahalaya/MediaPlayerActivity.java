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
import android.util.Log;
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

import java.util.Calendar;

public class MediaPlayerActivity extends AppCompatActivity implements View.OnClickListener {

	private Handler handler;
	private Button abortBtn;
	private TextView timeLeftView, currentStateView;
	@SuppressLint("StaticFieldLeak")
	private static MediaPlayerActivity myInstance;

	private static int chosenTheme;
	/////////////////////////////////////////////////////
	// 0 - Light theme
	// 1 - Dark Theme
	// 2 - Set by time
	// 3 - Set by System (Available on Android Q+)
	////////////////////////////////////////////////////

	private static boolean shouldThemeBeSet = true;
	//////////////////////////////////////////////////////////////////////////
	// Checks whether theme should be set by onCreate().
	// If the activity is created/recreated due to a theme change by the
	// theme change dialog box, then (except one case - set by time)
	// onCreate() should not change theme of the activity.
	/////////////////////////////////////////////////////////////////////////

	public static boolean IamAlive;
	////////////////////////////////////////////////////////////////////
	// Carries information whether the activity is visible or not.
	// This variable is changed by onCreate() and onPause()
	////////////////////////////////////////////////////////////////////

	private static boolean isThisFirstExecution = true;
	////////////////////////////////////////////////////////////////////////////////////
	// If the previous activity is alive when this activity has been started,
	// then the theme of this activity will be set according to the previous
	// activity. Once the user closes this activity after first creation,
	// onPause() will set the theme to "Set by time" (by setting
	// shouldThemeBeSet = true). However, if the user changes the theme during
	// first creation itself, then that is respected by changing this flag to
	// false in the dialog box listener, so that onPause() will no longer change
	// the theme automatically after first instance of this activity is paused.
	///////////////////////////////////////////////////////////////////////////////////

	private final BroadcastReceiver activityKiller = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(this.getClass().toString(), "Broadcast received to kill activity.");
			finish();
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_palying_activity);
		Log.e(this.getClass().toString(), "Inside onCreate()");

		Toolbar myToolbar = findViewById(R.id.toolbar6);
		setSupportActionBar(myToolbar);

		// Attempt to change theme iff CountdownActivity is dead when this activity is created.
		if (! MahalayaService.countdownActivityIsAlive) {
			//////////////////////////////////////////////////////////////////
			// If activity has been (re)created by a theme change from
			// the theme change dialog box, then do not change theme.
			// In such a case, shouldThemeBeSet will be false as set by
			// the dialog box listener.
			/////////////////////////////////////////////////////////////////
			if (shouldThemeBeSet) {
				chosenTheme = 2;
				isThisFirstExecution = false;
				if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || Calendar.getInstance()
						.get(Calendar.HOUR_OF_DAY) >= 22) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
			}
		} else {
			////////////////////////////////////////////////////////////////////////////////////
			// If this block is reached, it means that CountdownActivity is alive and the
			// user is looking at it. So, theme will  be set according to that of
			// CountdownActivity and later changed to default "Set by time".
			//
			// Note:
			//``````````````````````````````````````````````````````````````````````````
			// 1. CountdownActivity is dead when this activity is created,
			// so change the MahalayaService.countdownActivityIsAlive flag to false.
			//
			// 2. Do not change theme, but determine current theme.
			///////////////////////////////////////////////////////////////////////////////////
			MahalayaService.countdownActivityIsAlive = false;
			isThisFirstExecution = true;
			shouldThemeBeSet = false;
			if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
				chosenTheme = 1;
			} else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
				chosenTheme = 0;
			} else if (AppCompatDelegate
					.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
				chosenTheme = 3;
			}
		}

		abortBtn = findViewById(R.id.abort_btn2);
		timeLeftView = findViewById(R.id.timeLeftView2);
		currentStateView = findViewById(R.id.currentStateView);

		abortBtn.setOnClickListener(this);

		myInstance = this;

		/////////////////////////////////////////////////////////////////////////////////////
		// Consider the case where the playback is paused. The currentStateView TextView
		// will show "Paused". Now if the user changes theme, then the activity will be
		// recreated. But currentStateView and timeLeftView will have default values
		// as setTimeLeft() works only when the media player is active. Therefore, we
		// set the views in onCreate() so that the user doesn't see default values.
		/////////////////////////////////////////////////////////////////////////////////////
		try {
			timeLeftView.setText(DurationFinder
					.getDuration(MahalayaService.getDurationLeft(), 1, getApplicationContext()));
			if (MahalayaService.mediaPlayer.isPlaying()) {
				setCurrentState(this.getString(R.string.media_playing));
			} else {
				setCurrentState(this.getString(R.string.media_paused));
			}
		} catch (Exception ex) {
			Log.e(this.getClass().toString(), ex.toString());
		}

		IntentFilter intentFilter = new IntentFilter(Constants.ACTION_KILL_MEDIA_ACT);
		registerReceiver(activityKiller, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		IamAlive = true;
		setTimeLeft();
		Log.e(this.getClass().toString(), "Inside onResume()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isThisFirstExecution && ! isChangingConfigurations()) {
			Log.e(this.getClass().toString(), "Resetting theme.");
			shouldThemeBeSet = true;
			isThisFirstExecution = false;
		}

		if (isChangingConfigurations()) {
			Log.e(this.getClass().toString(), "Changing configuration.");
		}
		Log.e(this.getClass().toString(), "Inside onPause()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		IamAlive = false;
		Log.e(this.getClass().toString(), "Inside onStop()");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(this.getClass().toString(), "Inside onDestroy()");
		unregisterReceiver(activityKiller);
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
		if (v.getId() == abortBtn.getId()) {
			Intent intent = new Intent(getApplicationContext(), MahalayaService.class);
			stopService(intent);
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	private void setTimeLeft() {
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				timeLeftView.setText(b.getString("TextView"));
			}
		};

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Log.e(this.getClass().toString(),
						"Inside thread: " + Thread.currentThread().getName());
				try {
					while (IamAlive) {
						Message message = Message.obtain();
						Bundle bundle = new Bundle();
						if (MahalayaService.mediaPlayer.isPlaying()) {
							long millis = MahalayaService.getDurationLeft();
							bundle.putString("TextView",
									DurationFinder.getDuration(millis, 1, getApplicationContext()));
							if (millis > 0) {
								bundle.putString("TextView", DurationFinder
										.getDuration(millis, 1, getApplicationContext()));
							} else {
								bundle.putString("TextView", "Playback complete");
								message.setData(bundle);
								handler.sendMessage(message);
								break;
							}
							message.setData(bundle);
							handler.sendMessage(message);
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Log.e(this.getClass().toString(), e.toString());
							}
						}
					}
				} catch (IllegalStateException e) {
					Log.e(this.getClass().toString(), e.toString());
				}
			}
		});
		thread.start();
	}

	public static void setCurrentState(String str) {
		myInstance.currentStateView.setText(str);
	}

	private void createThemeDialog() {
		AlertDialog.Builder alb = new AlertDialog.Builder(this);
		alb.setTitle(myInstance.getString(R.string.choose_theme));
		alb.setCancelable(true);

		String[] items;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			items = new String[]{myInstance.getString(R.string.light),
					myInstance.getString(R.string.dark),
					myInstance.getString(R.string.time), myInstance.getString(R.string.system)};
		} else {
			items = new String[]{myInstance.getString(R.string.light),
					myInstance.getString(R.string.dark),
					myInstance.getString(R.string.time)};
		}

		int checkedItem = chosenTheme;
		alb.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chosenTheme = which;
			}
		});

		alb.setPositiveButton(myInstance.getString(R.string.set_theme),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (isThisFirstExecution) {
							// User has set theme, so onPause() should no longer change theme.
							isThisFirstExecution = false;
						}
						if (chosenTheme == 0) {
							AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
							shouldThemeBeSet = false;
						} else if (chosenTheme == 1) {
							shouldThemeBeSet = false;
							AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
						} else if (chosenTheme == 2) {
							// Let onCreate() change the theme from now on unless changed by user.
							shouldThemeBeSet = true;
							recreate();
						} else if (chosenTheme == 3) {
							shouldThemeBeSet = false;
							AppCompatDelegate.setDefaultNightMode(
									AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
						}
					}
				});

		alb.show();
	}
}
