package in.basulabs.mahalaya;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//////////////////////////////////////////////////////////////////////////////////////
		// If the user clicks on the launcher icon while any of the
		// two foreground services are running, the activity of that
		// service will be created.
		//``````````````````````````````````````````````````````````````
		// MahalayaService.whichServiceIsRunning = 0 => No service is running
		// MahalayaService.whichServiceIsRunning = 1 => Countdown is running
		// MahalayaService.whichServiceIsRunning = 2 => Media Player is running
		//////////////////////////////////////////////////////////////////////////////////////
		if (MahalayaService.whichServiceIsRunning == 1) {
			startCountdownActivity();
		} else if (MahalayaService.whichServiceIsRunning == 2) {
			startMediaPlayerActivity();
		} else {
			createSplashScreen();
		}
	}

	private void createSplashScreen() {
		Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(intent);
		this.finish();
		overridePendingTransition(0, 0);
	}

	private void startCountdownActivity() {
		Intent intent = new Intent(this, MahalayaBroadcastReceiver.class);
		intent.setAction(Constants.ACTION_START_COUNTDOWN_ACT);
		sendBroadcast(intent);
		//Log.e(this.getClass().toString(), "Activity opened while service is running; sent to Mahalaya Start activity.");
		this.finish();
		overridePendingTransition(0, 0);
	}

	private void startMediaPlayerActivity() {
		Intent intent = new Intent(this, MahalayaBroadcastReceiver.class);
		intent.setAction(Constants.ACTION_START_MEDIA_PLAYER_ACT);
		sendBroadcast(intent);
		//Log.e(this.getClass().toString(), "Activity opened while service is running; sent to Media manage activity.");
		this.finish();
		overridePendingTransition(0, 0);
	}
}
