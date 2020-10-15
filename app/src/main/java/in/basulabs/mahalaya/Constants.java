package in.basulabs.mahalaya;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contains all the constants required by the app, as well as some static methods that can be referenced from all other
 * classes.
 */
final class Constants {

	/**
	 * Broadcast action: The receiving activity should kill itself.
	 * <p>
	 * Broadcast action: Should be received by context-registered receivers in activities only. When this broadcast is
	 * received, the activity should kill itself by calling {@link AppCompatActivity#finish()}.
	 * </p>
	 */
	static final String ACTION_KILL_ALL_UI_ACTIVITIES = "in.basulabs.mahalaya.KILL_ALL_USER_INTERACTION_ACTIVITIES";

	/**
	 * Broadcast action: Start (play from a paused state) the media player.
	 */
	static final String ACTION_START_PLAYER = "in.basulabs.mahalaya.PLAYER_PLAY";

	/**
	 * Broadcast action: Pause the media player.
	 */
	static final String ACTION_PAUSE_PLAYER = "in.basulabs.mahalaya.PLAYER_PAUSE";

	/**
	 * Broadcast action: Kill {@link CountdownActivity}.
	 * <p>
	 * {@link CountdownActivity} will receive this broadcast through a context-registered receiver, and upon receiving,
	 * will call {@code finish()}.
	 * </p>
	 */
	static final String ACTION_KILL_COUNTDOWN_ACT = "in.basulabs.mahalaya.KILL_COUNTDOWN_ACTIVITY";

	/**
	 * Broadcast action: Kill {@link MediaPlayerActivity}.
	 * <p>
	 * {@link MediaPlayerActivity} will receive this broadcast through a context-registered receiver, and upon
	 * receiving, will call {@code finish()}.
	 * </p>
	 */
	static final String ACTION_KILL_MEDIA_ACT = "in.basulabs.mahalaya.KILL_MEDIA_PLAYER_ACTIVITY";

	/**
	 * Broadcast action: Decrease media volume.
	 */
	static final String ACTION_DECREASE_VOLUME = "in.basulabs.mahalaya.DECREASE_MEDIA_VOLUME";

	/**
	 * Broadcast action: Increase media volume.
	 */
	static final String ACTION_INCREASE_VOLUME = "in.basulabs.mahalaya.INCREASE_MEDIA_VOLUME";

	/**
	 * Intent extra for passing the {@link LocalDateTime} object that guides when the playback should start.
	 * <p>
	 * The object is passed using {@link android.content.Intent#putExtra(String, Serializable)}.
	 * </p>
	 */
	static final String EXTRA_PLAYBACK_DATE_TIME = "in.basulabs.mahalaya.PLAYBACK_DATE_TIME";

	/**
	 * Intent extra for passing the {@link LocalDate} object that guides when the playback should start.
	 * <p>
	 * The object is passed using {@link android.content.Intent#putExtra(String, Serializable)}. Note that the time has
	 * <b>not</b> yet been specified.
	 * </p>
	 */
	static final String EXTRA_PLAYBACK_DATE = "in.basulabs.mahalaya.PLAYBACK_DATE_TIME";

	/**
	 * Intent extra to be sent with {@link #ACTION_PAUSE_PLAYER}, indicating whether audio focus should be abandoned.
	 * The value is boolean.
	 */
	static final String EXTRA_ABANDON_FOCUS = "in.basulabs.mahalaya.ABANDON_FOCUS";

	/**
	 * Indicates that the theme of the app should be light. Corresponds to {@link androidx.appcompat.app.AppCompatDelegate#MODE_NIGHT_NO}.
	 */
	static final int THEME_LIGHT = 0;

	/**
	 * Indicates that the theme of the app should be light. Corresponds to {@link androidx.appcompat.app.AppCompatDelegate#MODE_NIGHT_YES}.
	 */
	static final int THEME_DARK = 1;

	/**
	 * The app will set its theme according to time. From 10:00 PM to 6:00 AM, the theme will be dark, and light
	 * otherwise.
	 */
	static final int THEME_AUTO_TIME = 2;

	/**
	 * Indicates that the theme of the app should be light. Corresponds to {@link androidx.appcompat.app.AppCompatDelegate#MODE_NIGHT_FOLLOW_SYSTEM}.
	 * Available only on Android Q+.
	 */
	static final int THEME_SYSTEM = 3;

	/**
	 * The {@link android.content.SharedPreferences} file name for this app.
	 */
	static final String SHARED_PREF_FILE = "in.basulabs.mahalaya.SHARED_PREFERENCES";

	/**
	 * {@link android.content.SharedPreferences} key for the app theme.
	 */
	static final String SHARED_PREF_KEY_THEME = "in.basulabs.mahalaya.SHARED_PREF_THEME";


}
