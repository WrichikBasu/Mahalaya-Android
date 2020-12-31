package in.basulabs.mahalaya;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.time.LocalTime;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class ThankYouActivity extends AppCompatActivity {

	/**
	 * The currently active theme.
	 * <p>
	 * Can have four values: {@link Constants#THEME_LIGHT}, {@link Constants#THEME_DARK}, {@link Constants#THEME_SYSTEM}, {@link Constants#THEME_AUTO_TIME}.
	 * </p>
	 *
	 * @see Constants#THEME_LIGHT
	 * @see Constants#THEME_DARK
	 * @see Constants#THEME_SYSTEM
	 * @see Constants#THEME_AUTO_TIME
	 */
	private int currentTheme;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_end);
		setSupportActionBar(findViewById(R.id.toolbar7));

		// Get the theme:
		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM : Constants.THEME_AUTO_TIME;
		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME, defaultTheme);

		// Set the theme:
		if (savedInstanceState == null) {
			changeTheme();
		}

	}

	private void changeTheme() {

		switch (currentTheme) {
			case Constants.THEME_LIGHT:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;

			case Constants.THEME_DARK:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;

			case Constants.THEME_SYSTEM:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
