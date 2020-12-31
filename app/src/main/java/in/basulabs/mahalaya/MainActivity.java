package in.basulabs.mahalaya;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/////////////////////////////////////////////////////////////////////////
		// If the user clicks on the launcher icon while any of the
		// two foreground services are running, the activity of that
		// service will be created.
		/////////////////////////////////////////////////////////////////////////
		if (MahalayaService.mode == MahalayaService.MODE_COUNTDOWN) {
			startCountdownActivity();
		} else if (MahalayaService.mode == MahalayaService.MODE_MEDIA) {
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
		Intent intent = new Intent(this, CountdownActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
		this.finish();
		overridePendingTransition(0, 0);
	}

	private void startMediaPlayerActivity() {
		Intent intent = new Intent(this, MediaPlayerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent);
		this.finish();
		overridePendingTransition(0, 0);
	}
}
