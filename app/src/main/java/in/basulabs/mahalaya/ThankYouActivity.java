package in.basulabs.mahalaya;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class ThankYouActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//////////////////////////////////////////////////////////////////////////
		// Following code segment will determine the theme of the activity.
		// If MediaPlayerActivity was alive at the time of creation of this
		// activity, it means that the user is looking at that activity,
		// so theme will not be changed. Otherwise, the theme will depend on
		// system theme for Android Q+, and on time for lower versions.
		//////////////////////////////////////////////////////////////////////////
		if (MahalayaService.mediaPlayerActivityIsAlive) {
			Log.e(getClass().toString(), "MediaPlayerActivity was alive.");
			MahalayaService.mediaPlayerActivityIsAlive = false;
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				/*int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
				switch (currentNightMode) {
					case Configuration.UI_MODE_NIGHT_NO:
						AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
						break;
					case Configuration.UI_MODE_NIGHT_YES:
						AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
						break;
				}*/
			} else {
				if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6 || Calendar.getInstance()
						.get(Calendar.HOUR_OF_DAY) >= 22) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				}
			}
		}
		setContentView(R.layout.activity_end);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar7));
	}
}
