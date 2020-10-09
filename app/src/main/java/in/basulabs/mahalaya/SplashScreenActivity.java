package in.basulabs.mahalaya;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;

public class SplashScreenActivity extends AppCompatActivity {

	private static boolean hasNextActivityBeenStarted = false;

	private Handler handler;

	private static boolean IAmAlive = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.e(this.getClass().toString(), "Inside onCreate()");
		super.onCreate(savedInstanceState);

		//////////////////////////////////////////////////////////////////
		// For Android Q+, set default theme to "Set by System".
		// For lower versions, set default theme according to time.
		//////////////////////////////////////////////////////////////////
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		} else {
			if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || Calendar.getInstance()
					.get(Calendar.HOUR_OF_DAY) >= 22) {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
			}
		}

		setContentView(R.layout.activity_splash);
		if (! hasNextActivityBeenStarted) {
			hasNextActivityBeenStarted = true;
				/*Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						startNextActivity();
					}
				}, 7000);*/
			startNextActivity();
		}


	}

	@Override
	protected void onResume() {
		super.onResume();
		IAmAlive = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		IAmAlive = false;
	}

	private void startNextActivity() {

		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Bundle bundle = msg.getData();
				if (bundle.getBoolean("Start Next Activity?")) {
					Intent intent = new Intent(getApplicationContext(), DateTimeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					//Log.e(this.getClass().toString(), "Next activity started.");
					finish();
				}
			}
		};

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				float count = 0;
				while (count < 7) {
					if (IAmAlive) {
						count += 0.5;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException ignored) {
					}
				}
				Message msg = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putBoolean("Start Next Activity?", true);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		});
		thread.start();
	}

}


