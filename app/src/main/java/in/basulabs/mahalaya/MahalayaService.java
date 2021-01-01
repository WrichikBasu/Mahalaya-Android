package in.basulabs.mahalaya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MahalayaService extends Service {

	/**
	 * The notification ID for this service.
	 */
	private static final int MAIN_NOTIFICATION_ID = 6356;

	/**
	 * The notification ID for the final notification that leads to {@link ThankYouActivity}.
	 */
	private static final int FINAL_NOTIFICATION_ID = MAIN_NOTIFICATION_ID + 5;

	/**
	 * The {@link LocalDateTime} object indicating when the media will be played.
	 */
	public static LocalDateTime playbackDateTime;

	/**
	 * The {@link Uri} of the media file.
	 */
	private Uri media_uri;

	/**
	 * The {@link MediaPlayer} object used to play the media file.
	 */
	public static MediaPlayer mediaPlayer;

	/**
	 * Indicates which mode the service is currently in. Default is {@link #MODE_INACTIVE}.
	 * <p>
	 * Can have three values: {@link #MODE_INACTIVE}, {@link #MODE_COUNTDOWN} or {@link #MODE_MEDIA}.
	 * </p>
	 *
	 * @see #MODE_INACTIVE
	 * @see #MODE_COUNTDOWN
	 * @see #MODE_MEDIA
	 */
	public static byte mode = 0;

	/**
	 * Indicates that the service is currently inactive.
	 */
	public static final byte MODE_INACTIVE = 0;

	/**
	 * Indicates that countdown is going on.
	 */
	public static final byte MODE_COUNTDOWN = 1;

	/**
	 * Indicates that countdown is over, and the media is being played.
	 * <p>
	 * This will be the mode even if the {@link #mediaPlayer} is currently paused.
	 * </p>
	 */
	public static final byte MODE_MEDIA = 2;

	/**
	 * Indicates that the play button should be displayed in the notification.
	 * <p>
	 * This constant comes into play when the media is being played by the app ({@link #mode} = {@link #MODE_MEDIA}. It
	 * indicates that the playback is currently paused, and the "Play" symbol should be displayed in the notification. In
	 * addition, clicking on the action button should start the media player. This is used by {@link
	 * #updateNotificationForMediaPlayer(int)}.
	 * </p>
	 *
	 * @see #NOTIF_TYPE_PAUSE
	 * @see #updateNotificationForMediaPlayer(int)
	 */
	private static final int NOTIF_TYPE_PLAY = 1;

	/**
	 * Indicates that the pause button should be displayed in the notification.
	 * <p>
	 * This constant comes into play when the media is being played by the app ({@link #mode} = {@link #MODE_MEDIA}. It
	 * indicates that the playback is currently on, and the "Pause" symbol should be displayed in the notification. In
	 * addition, clicking on the action button should pause the media player. This is used by {@link
	 * #updateNotificationForMediaPlayer(int)}.
	 * </p>
	 *
	 * @see #NOTIF_TYPE_PLAY
	 * @see #updateNotificationForMediaPlayer(int)
	 */
	private static final int NOTIF_TYPE_PAUSE = 2;

	/**
	 * Indicates whether media player has been started at least once after it was prepared.
	 */
	private boolean hasMediaPlayerStarted;

	/**
	 * The time (in milliseconds) that has passed after the notification was last update during countdown.
	 * <p>
	 * This variable is used to determine how much time has passed since the last time the notification was updated, and is
	 * used during countdown when the screen is off. Once the screen is turned off, the delay between two consecutive
	 * notification updates is increased to save battery. When the screen is turned on, this variable is initialised to
	 * {@code 0L}.
	 * </p>
	 */
	private long millisAfterLastNotifUpdate;

	/**
	 * Indicates whether screen is currently off.
	 * <p>
	 * This variable is managed by {@link #broadcastReceiver}. It is {@code true} when the screen is off, and {@code false}
	 * otherwise.
	 * <p>
	 * This is used for setting the delay between two notification updates during countdown.
	 * </p>
	 */
	private boolean isScreenOff;

	private AudioFocusManager audioFocusManager;

	/**
	 * The large image that will be used in the notification when the media is being played.
	 */
	private Bitmap durgaBitmap;

	private Notification notif;
	private NotificationManager mNotificationManager;

	/**
	 * The content intent for the notification posted while playback is going on.
	 * <p>
	 * This {@link PendingIntent} points to {@link MediaPlayerActivity}.
	 * </p>
	 */
	private PendingIntent pendingIntent_activity;

	/**
	 * The intent for the play button in the notification.
	 * <p>
	 * This {@link PendingIntent} sends the {@link Constants#ACTION_START_PLAYER} broadcast.
	 * </p>
	 */
	private PendingIntent pendingIntent_play;

	/**
	 * The intent for the pause button in the notification.
	 * <p>
	 * This {@link PendingIntent} sends the {@link Constants#ACTION_PAUSE_PLAYER} broadcast.
	 * </p>
	 */
	private PendingIntent pendingIntent_pause;

	/**
	 * The volume of {@link AudioManager#STREAM_MUSIC} before playback started.
	 * <p>
	 * If this value is -1, it means that the app has not changed the media stream volume.
	 * </p>
	 */
	private int oldVolume;

	//--------------------------------------------------------------------------------------------

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			switch (Objects.requireNonNull(intent.getAction())) {

				case Constants.ACTION_START_PLAYER:
					restartMediaPlayer();
					break;

				case Constants.ACTION_PAUSE_PLAYER:
					pauseMediaPlayer(
							Objects.requireNonNull(intent.getExtras()).getBoolean(Constants.EXTRA_ABANDON_FOCUS));
					break;

				case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
					if (mediaPlayer != null && mediaPlayer.isPlaying() && mode == MODE_MEDIA) {
						pauseMediaPlayer(true);
					}
					break;

				case Constants.ACTION_DECREASE_VOLUME:
					mediaPlayer.setVolume(0.2f, 0.2f);
					break;

				case Constants.ACTION_INCREASE_VOLUME:
					mediaPlayer.setVolume(1.0f, 1.0f);
					break;

				case Intent.ACTION_SCREEN_OFF:
					isScreenOff = true;
					millisAfterLastNotifUpdate = 0L;
					break;

				case Intent.ACTION_SCREEN_ON:
					isScreenOff = false;
					millisAfterLastNotifUpdate = 0L;
					break;
			}
		}
	};

	//--------------------------------------------------------------------------------------------

	@Override
	public void onCreate() {

		durgaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.durga1);
		audioFocusManager = new AudioFocusManager(this);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		createNotificationChannel(MAIN_NOTIFICATION_ID);
		buildNotificationForCountdown(getString(R.string.default_time_left_notif));
		startForeground(MAIN_NOTIFICATION_ID, notif);

		hasMediaPlayerStarted = false;

		playbackDateTime = (LocalDateTime) Objects.requireNonNull(intent.getExtras())
				.getSerializable(Constants.EXTRA_PLAYBACK_DATE_TIME);
		playbackDateTime = Objects.requireNonNull(playbackDateTime, "Playback datetime was null!").withSecond(0).withNano(0);
		media_uri = intent.getData();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.ACTION_START_PLAYER);
		intentFilter.addAction(Constants.ACTION_PAUSE_PLAYER);
		intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		intentFilter.addAction(Constants.ACTION_DECREASE_VOLUME);
		intentFilter.addAction(Constants.ACTION_INCREASE_VOLUME);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(broadcastReceiver, intentFilter);

		startCountdown();

		isScreenOff = false;

		Intent intent1 = new Intent(this, CountdownActivity.class);
		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(intent1);

		return START_NOT_STICKY;
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.release();
		unregisterReceiver(broadcastReceiver);
		mode = MODE_INACTIVE;
	}

	//--------------------------------------------------------------------------------------------

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Builds the notification channel for Android O+.
	 *
	 * @param NOTIF_ID The notification ID with which the channel is to be built.
	 */
	private void createNotificationChannel(int NOTIF_ID) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(Integer.toString(NOTIF_ID),
					"Mahalaya_notifications", importance);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			channel.setSound(null, null);
			notificationManager.createNotificationChannel(channel);
		}
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * This function does the countdown. It waits for the time to come and starts the media player. In addition, it updates
	 * the notification with the time left and also sets the time in the CountdownActivity when the latter is alive.
	 */
	private void startCountdown() {

		LocalDateTime currentDateTime = LocalDateTime.now();

		Duration duration = Duration.between(currentDateTime, playbackDateTime);

		Context context = this;

		mode = MODE_COUNTDOWN;

		millisAfterLastNotifUpdate = 0L;

		//////////////////////////////////////////////////////////////////////////////
		// When the screen is on, the time left for playback to start is 1s,
		// but if screen is off, the delay is increased to 2 minutes.
		//////////////////////////////////////////////////////////////////////////////
		CountDownTimer countDownTimer = new CountDownTimer(duration.toMillis(), 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (isScreenOff) {
					millisAfterLastNotifUpdate += 1000;
					if (millisAfterLastNotifUpdate >= 120000) {
						updateNotificationDuringCountdown(DurationFinder.getDuration(millisUntilFinished,
								DurationFinder.TYPE_NOTIFICATION, context));
						millisAfterLastNotifUpdate = 0L;
					}
				} else {
					updateNotificationDuringCountdown(DurationFinder.getDuration(millisUntilFinished,
							DurationFinder.TYPE_NOTIFICATION, context));
				}
			}

			@Override
			public void onFinish() {
				buildNotificationForCountdown(getString(R.string.request_focus_notif));
				mNotificationManager.notify(MAIN_NOTIFICATION_ID, notif);
				setUpMediaPlayer();
			}
		};
		countDownTimer.start();
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Builds the notification while countdown is active.
	 *
	 * @param msg The message to be displayed in the notification (set as notification content text).
	 */
	private void buildNotificationForCountdown(String msg) {

		Intent intent = new Intent(this, CountdownActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		PendingIntent notifyPendingIntent = PendingIntent.getActivity(
				this, 701, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
				Integer.toString(MAIN_NOTIFICATION_ID))
				.setContentTitle(getString(R.string.app_name))
				.setContentText(msg)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(notifyPendingIntent)
				.setOnlyAlertOnce(true)
				.setSound(null)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_notification);

		notif = builder.build();
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Updates the notification during countdown with time left.
	 */
	private void updateNotificationDuringCountdown(String str) {
		buildNotificationForCountdown(getString(R.string.time_left_notif) + " " + str);
		mNotificationManager.notify(MAIN_NOTIFICATION_ID, notif);
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Builds the notification the first time the playback is started.
	 */
	private void buildNotificationForMediaPlayer() {

		Intent intent_act = new Intent(this, MediaPlayerActivity.class);
		Intent intent_play = new Intent();
		Intent intent_pause = new Intent();

		intent_play.setAction(Constants.ACTION_START_PLAYER);

		intent_pause.setAction(Constants.ACTION_PAUSE_PLAYER);
		intent_pause.putExtra(Constants.EXTRA_ABANDON_FOCUS, true);

		intent_act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent_act.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent_act.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		pendingIntent_activity = PendingIntent.getActivity(this, 5221, intent_act,
				PendingIntent.FLAG_UPDATE_CURRENT);
		pendingIntent_play = PendingIntent.getBroadcast(this, 5223, intent_play,
				PendingIntent.FLAG_CANCEL_CURRENT);
		pendingIntent_pause = PendingIntent.getBroadcast(this, 5223, intent_pause,
				PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(MAIN_NOTIFICATION_ID))
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.media_playing))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(durgaBitmap)
				.addAction(R.drawable.ic_pause, "Pause", pendingIntent_pause)
				.setContentIntent(pendingIntent_activity)
				.setSound(null)
				.setOngoing(true)
				.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
						.setShowActionsInCompactView(0)
						.setShowCancelButton(true));

		mNotificationManager.notify(MAIN_NOTIFICATION_ID, builder.build());
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Prepares the media player, and starts it if audio focus is achieved.
	 * <p>
	 * This function also handles the completion listener of the media player.
	 * <p>
	 * To request audio focus, the function calls {@link AudioFocusManager#requestAudioFocus()}. If {@link
	 * AudioManager#AUDIOFOCUS_REQUEST_GRANTED} is returned, the player is started immediately by calling {@link
	 * #startMediaPlayer()}. If {@link AudioManager#AUDIOFOCUS_REQUEST_DELAYED} is returned, the media player is started
	 * later by {@link AudioFocusManager#onAudioFocusChange(int)}. If {@link AudioManager#AUDIOFOCUS_REQUEST_FAILED} is
	 * returned, the {@link #repeatedlyAskForFocus()} function is invoked.
	 * </p>
	 */
	private void setUpMediaPlayer() {

		mode = MODE_MEDIA;

		mediaPlayer = new MediaPlayer();

		AudioAttributes attributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				.build();

		try {
			mediaPlayer.setDataSource(this, media_uri);
		} catch (IOException ignored) {
		}

		mediaPlayer.setAudioAttributes(attributes);
		mediaPlayer.setLooping(false);
		mediaPlayer.setVolume(1.0f, 1.0f);

		mediaPlayer.setOnPreparedListener(mp -> {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(1.0f));
			}

			int focusRequest = audioFocusManager.requestAudioFocus();

			if (focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

				startMediaPlayer();
				audioFocusManager.focusAbandoned = false;

			} else if (focusRequest == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {

				audioFocusManager.focusDelayed = true;
				audioFocusManager.focusAbandoned = false;

			} else {

				repeatedlyAskForFocus();
			}

		});
		mediaPlayer.prepareAsync();

		mediaPlayer.setOnCompletionListener(mp -> {

			mode = MODE_INACTIVE;

			AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (oldVolume != - 1) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, 0);
			}

			boolean isMediaPlayerActAlive = MediaPlayerActivity.IamAlive;

			Intent intent = new Intent()
					.setAction(Constants.ACTION_KILL_MEDIA_ACT);
			sendBroadcast(intent);

			stopForeground(true);

			if (isMediaPlayerActAlive) {
				////////////////////////////////////////////////////////////////////////
				// If MediaPlayerActivity is alive, it means that the user is looking
				// at the activity. In this state, the final activity can be
				// created directly instead of posting a notification.
				///////////////////////////////////////////////////////////////////////
				startFinalActivity();
			} else {
				///////////////////////////////////////////////////////////////////////
				// Android Q+ places restrictions on apps starting
				// an activity from a service. In addition, for lower versions,
				// if the user doesn't see the created activity soon enough, the
				// system destroys it. That is why a notification will be
				// posted that will stay up for the user.
				// On tapping the notification, the final activity will be created.
				////////////////////////////////////////////////////////////////////////
				buildFinalNotification();
			}
			audioFocusManager.abandonAudioFocus();
			stopSelf();
		});


	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Starts the media player for the first time.
	 *
	 * <p>
	 * This method should be used for starting the media player only the first time, i.e. just after the media player has
	 * been prepared and ready for starting. For restarting after a pause, use {@link MediaPlayer#start()}.
	 * <p>
	 * This function reads the {@link SharedPreferences} of {@link CountdownActivity} and makes necessary changes in the
	 * {@link SharedPreferences} of {@link MediaPlayerActivity}. If {@link CountdownActivity} is visible on the screen when
	 * the {@link #mediaPlayer} is being started, this function directly starts {@link MediaPlayerActivity}.
	 * </p>
	 */
	private void startMediaPlayer() {

		buildNotificationForMediaPlayer();

		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE);

		if (sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_VOL_CTRL_ENABLED, true)) {

			oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					sharedPreferences.getInt(Constants.SHARED_PREF_KEY_VOLUME,
							audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
		} else {
			oldVolume = - 1;
		}

		mediaPlayer.setVolume(1.0f, 1.0f);
		mediaPlayer.start();

		hasMediaPlayerStarted = true;

		// If CountdownActivity is alive, start MediaPlayerActivity
		if (CountdownActivity.IamAlive) {

			Intent intent = new Intent();
			intent.setAction(Constants.ACTION_KILL_COUNTDOWN_ACT);
			sendBroadcast(intent);

			Intent intent_act = new Intent(this, MediaPlayerActivity.class);
			intent_act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent_act.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent_act.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent_act);
		}
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Pauses the media player.
	 * <p>
	 * This function pauses the media player, and abandons focus only if asked to do so. Situations when focus should be
	 * abandoned is when the user themselves pause the player, or when focus is lost for an undefined period ({@link
	 * AudioManager#AUDIOFOCUS_LOSS}. Otherwise, focus is not abandoned.
	 * </p>
	 *
	 * @param abandonFocus Whether focus should be abandoned.
	 */
	private void pauseMediaPlayer(boolean abandonFocus) {

		try {
			mediaPlayer.pause();
		} catch (IllegalStateException ignored) {
		}

		updateNotificationForMediaPlayer(NOTIF_TYPE_PLAY);

		if (abandonFocus) {
			audioFocusManager.abandonAudioFocus();
		}

	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Re-starts the media player from a paused state.
	 */
	private void restartMediaPlayer() {

		if (audioFocusManager.focusDelayed) {

			audioFocusManager.focusDelayed = false;

			if (! hasMediaPlayerStarted) {
				startMediaPlayer();
			} else {
				try {
					mediaPlayer.start();
					mediaPlayer.setVolume(1.0f, 1.0f);
				} catch (IllegalStateException ignored) {
				}
				updateNotificationForMediaPlayer(NOTIF_TYPE_PAUSE);
			}

		} else {

			boolean hasFocusBeenReceived;

			if (audioFocusManager.focusAbandoned) {

				switch (audioFocusManager.requestAudioFocus()) {

					case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
						hasFocusBeenReceived = true;
						audioFocusManager.focusAbandoned = false;
						audioFocusManager.focusDelayed = false;
						break;

					case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
						audioFocusManager.focusDelayed = true;
						audioFocusManager.focusAbandoned = false;
						hasFocusBeenReceived = false;
						break;

					default:
						audioFocusManager.focusDelayed = false;
						audioFocusManager.focusAbandoned = true;
						hasFocusBeenReceived = false;
						break;
				}
			} else {
				hasFocusBeenReceived = true;
			}

			if (hasFocusBeenReceived) {
				try {
					mediaPlayer.start();
					mediaPlayer.setVolume(1.0f, 1.0f);
				} catch (IllegalStateException ignored) {
				}
				updateNotificationForMediaPlayer(NOTIF_TYPE_PAUSE);
			}
		}

	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Changes the play button to the pause button in the Notification and vice-versa.
	 * <p>
	 * Updates the pause/play button in the notification while media is being played. In addition to updating the buttons,
	 * the associated pending intents are changed as well so as to trigger the correct action.
	 * </p>
	 *
	 * @param code Can have two values: {@link #NOTIF_TYPE_PAUSE} and {@link #NOTIF_TYPE_PLAY}. When it is {@link
	 *        #NOTIF_TYPE_PLAY}, the play button is set, and when it is {@link #NOTIF_TYPE_PAUSE}, the pause button is set.
	 */
	private void updateNotificationForMediaPlayer(int code) {

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				getApplicationContext(), Integer.toString(MAIN_NOTIFICATION_ID))
				.setContentTitle(getString(R.string.app_name))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(durgaBitmap)
				.setSound(null)
				.setOngoing(true)
				.setContentIntent(pendingIntent_activity);

		if (code == NOTIF_TYPE_PLAY) {
			builder.addAction(R.drawable.ic_play, "Play", pendingIntent_play);
			builder.setContentText(getString(R.string.media_paused));
		} else if (code == NOTIF_TYPE_PAUSE) {
			builder.setContentText(getString(R.string.media_playing));
			builder.addAction(R.drawable.ic_pause, "Pause", pendingIntent_pause);
		}
		builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
				.setShowActionsInCompactView(0)
				.setShowCancelButton(true));

		mNotificationManager.notify(MAIN_NOTIFICATION_ID, builder.build());
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Calculates the time left for the playback to end.
	 *
	 * @return The time left (in milliseconds) for the playback to end.
	 */
	public static long getDurationLeft() {
		return (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition());
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Periodically asks for audio focus from the system until it is granted.
	 * <p>
	 * <b>Should be called iff the media player has never been started in the past, i.e. when
	 * {@link #hasMediaPlayerStarted} is {@code false}.</b>
	 * <p>
	 * This method is called if audio focus is not granted upon requesting ({@link AudioManager#AUDIOFOCUS_REQUEST_FAILED}.
	 * The system is requested for a focus change periodically. If {@link AudioManager#AUDIOFOCUS_REQUEST_GRANTED} or {@link
	 * AudioManager#AUDIOFOCUS_REQUEST_DELAYED} is received, the function stops executing.
	 * </p>
	 */
	private void repeatedlyAskForFocus() {

		if (! hasMediaPlayerStarted) {

			final String MESSAGE_KEY_REQUEST_FOCUS = "in.basulabs.mahalaya.MSG_REQUEST_FOCUS";

			AtomicBoolean hasFocusBeenReceived = new AtomicBoolean(false);

			Handler handler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(@NonNull Message msg) {
					super.handleMessage(msg);
					if (msg.getData().getBoolean(MESSAGE_KEY_REQUEST_FOCUS, false)) {

						int focusRequest = audioFocusManager.requestAudioFocus();

						if (focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
							startMediaPlayer();
							audioFocusManager.focusAbandoned = false;
							hasFocusBeenReceived.set(true);
						} else if (focusRequest == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
							audioFocusManager.focusDelayed = true;
							audioFocusManager.focusAbandoned = false;
							hasFocusBeenReceived.set(true);
						}
					}
				}
			};

			Thread thread = new Thread(() -> {
				Looper.prepare();

				while (! hasFocusBeenReceived.get()) {

					try {
						Thread.sleep(2000);
					} catch (InterruptedException ignored) {
					}

					Message message = Message.obtain();
					Bundle data = new Bundle();
					data.putBoolean(MESSAGE_KEY_REQUEST_FOCUS, true);
					message.setData(data);
					handler.sendMessage(message);

					try {
						Thread.sleep(3000);
					} catch (InterruptedException ignored) {
					}

				}
			});

			thread.start();
		}
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Builds the notification that, when clicked, will open {@link ThankYouActivity}.
	 * <p>
	 * When this function is called, the playback is complete and the media player has been released. Previously, we stopped
	 * the service and created this notification using a different ID. However, it was seen that if quite some time passes
	 * from the time of posting this notification, the notification failed to start {@link ThankYouActivity}. Hence, this
	 * notification will be created with the service still running in the foreground. Once the user taps on the notification,
	 * {@link ThankYouActivity} will be created, which will, in turn, kill this service via an intent.</p>
	 */
	private void buildFinalNotification() {
		createNotificationChannel(FINAL_NOTIFICATION_ID);

		Intent intent = new Intent(this, ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1226,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(FINAL_NOTIFICATION_ID))
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.playback_complete))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentIntent(pendingIntent)
				.setSound(null)
				.setOngoing(true)
				.setAutoCancel(true);

		mNotificationManager.notify(FINAL_NOTIFICATION_ID, builder.build());
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * Starts {@link ThankYouActivity}.
	 */
	private void startFinalActivity() {
		Intent intent = new Intent(this, ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

}