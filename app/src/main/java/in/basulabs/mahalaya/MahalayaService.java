package in.basulabs.mahalaya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.Calendar;

public class MahalayaService extends Service {

	private static final int NOTIFICATION_ID = 122356;

	private static int month;
	private static int day;
	private static int hour;
	private static int minute;
	private static Uri media_uri;
	public static MediaPlayer mediaPlayer;

	public static byte whichServiceIsRunning = 0;
	////////////////////////////////////////////////
	// Has the following values:
	// 0 - No service is running
	// 1 - Countdown is going on
	// 2 - Media player is active
	///////////////////////////////////////////////

	public static boolean countdownActivityIsAlive = false;
	public static boolean mediaPlayerActivityIsAlive = false;

	private static MahalayaService myInstance;
	private static AudioFocusHelper afh;
	private final String KEY_START_MEDIA_PLAYER = "Start Media Player?", KEY_SET_TEXT = "Set text in TextView?", KEY_GET_TEXT = "Text of TextView";
	private Bitmap bmp_durga;
	private Notification notif;
	private NotificationManager mNotificationManager;
	private Handler handler;
	private PendingIntent pi_act, pi_play, pi_pause;

	@Override
	public void onCreate() {
		//Log.e(this.getClass().toString(), "Inside onCreate");

		bmp_durga = BitmapFactory.decodeResource(getResources(), R.drawable.durga1);
		afh = new AudioFocusHelper(this);
		MahalayaService.whichServiceIsRunning = 1;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.e(this.getClass().toString(), "Inside onStartCommand()");

		Bundle extras = intent.getExtras();
		if (extras != null) {
			month = extras.getInt("month");
			day = extras.getInt("day");
			hour = extras.getInt("hour");
			minute = extras.getInt("minute");
		}
		media_uri = intent.getData();

		createNotificationChannel(NOTIFICATION_ID);
		buildForegroundNotificationForCountdown("Calculating time left...", true);
		startForeground(NOTIFICATION_ID, notif);
		doCountdown();

		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//Log.e(this.getClass().toString(), "onDestroy() called.");
		mediaPlayer.release();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Builds the notification channel for Android O+.
	 *
	 * @param NOTIF_ID The notification ID with which the channel is to be built.
	 */
	private void createNotificationChannel(int NOTIF_ID) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(Integer.toString(NOTIF_ID),
					"Mahalaya notifications", importance);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			channel.setSound(null, null);
			notificationManager.createNotificationChannel(channel);
		}
	}

	/////////////////////////////////////////////////////////////////
	// For the countdown:
	////////////////////////////////////////////////////////////////

	/**
	 * This function does the countdown. It waits for the time to come and starts the media player.
	 * In addition, it updates the notification with the time left and also sets the time in the
	 * CountdownActivity when the latter is alive.
	 */
	private void doCountdown() {
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				if (b.getBoolean(KEY_START_MEDIA_PLAYER)) {
					//Log.e(this.getClass().toString(), "Received message to start media player.");
					startMediaPlayer();
				}
				try {
					if (b.getBoolean(KEY_SET_TEXT)) {
						CountdownActivity.timeLeftView.setText(b.getString(KEY_GET_TEXT));
					}
				} catch (Exception ignored) {
				}
			}
		};

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				//Log.e(this.getClass().toString(), "Inside thread:" + Thread.currentThread().getName());

				int month_now, day_now, hrs_now, mins_now, secs_now;

				do {
					month_now = Calendar.getInstance().get(Calendar.MONTH) + 1;
					day_now = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
					hrs_now = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					mins_now = Calendar.getInstance().get(Calendar.MINUTE);
					secs_now = Calendar.getInstance().get(Calendar.SECOND);

					// Update the notification and set text in CountdownActivity iff it is alive
					DateTime dateTime1 = new DateTime(Calendar.getInstance().get(Calendar.YEAR),
							month_now, day_now, hrs_now, mins_now, secs_now);
					DateTime dateTime2 = new DateTime(Calendar.getInstance().get(Calendar.YEAR),
							month, day, hour, minute, 0);
					Duration duration = new Duration(dateTime1, dateTime2);

					updateNotificationDuringCountdown(DurationFinder
							.getDuration(duration.getMillis(), 2, getApplicationContext()));

					if (CountdownActivity.IamAlive) {
						Message message = Message.obtain();
						Bundle bundle = new Bundle();
						bundle.putBoolean(KEY_START_MEDIA_PLAYER, false);
						bundle.putBoolean(KEY_SET_TEXT, true);
						bundle.putString(KEY_GET_TEXT, DurationFinder
								.getDuration(duration.getMillis(), 1, getApplicationContext()));
						message.setData(bundle);
						handler.sendMessage(message);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Log.e(this.getClass().toString(), e.toString());
					}

				} while (! (month == month_now && day == day_now && hour == hrs_now && minute <= mins_now));

				//Log.e(this.getClass().toString(), "Time to start playback.");

				///////////////////////////////////////////////////////////////////////
				// Android has a rate limiting system in NotificationService.
				// This means that an app cannot post more than 10 notifications
				// per second in Android N+ (earlier this limit was 50). In order
				// to cope with this difficulty, a Thread.sleep is introduced
				// for 1000ms.
				///////////////////////////////////////////////////////////////////////
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Log.e(this.getClass().toString(), e.toString());
				}

				Message message = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putBoolean(KEY_START_MEDIA_PLAYER, true);
				bundle.putBoolean(KEY_SET_TEXT, false);
				bundle.putString(KEY_GET_TEXT, "");
				message.setData(bundle);
				handler.sendMessage(message);
			}
		});

		thread.start();
	}

	/**
	 * Builds the foreground notification.
	 *
	 * @param msg The message to be displayed in the notification (set as notification content
	 * 		text).
	 * @param shouldActivityBeStarted If {@code true}, activity CountdownActivity is created,
	 * 		otherwise not.
	 */
	private void buildForegroundNotificationForCountdown(String msg,
	                                                     boolean shouldActivityBeStarted) {

		Intent intent;

		if (shouldActivityBeStarted) {
			intent = new Intent(this, CountdownActivity.class);
			intent.putExtra("month", month);
			intent.putExtra("day", day);
			intent.putExtra("hour", hour);
			intent.putExtra("minute", minute);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}

		intent = new Intent(this, MahalayaBroadcastReceiver.class);
		intent.setAction(Constants.ACTION_START_COUNTDOWN_ACT);

		PendingIntent notifyPendingIntent = PendingIntent.getBroadcast(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(NOTIFICATION_ID))
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.time_left_notif) + " " + msg)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(notifyPendingIntent)
				.setOnlyAlertOnce(true)
				.setSound(null)
				.setSmallIcon(R.drawable.ic_notification);

		notif = builder.build();
	}

	/**
	 * Updates the notification with time left.
	 */
	private void updateNotificationDuringCountdown(String str) {
		buildForegroundNotificationForCountdown(str, false);
		mNotificationManager.notify(NOTIFICATION_ID, notif);
	}

	////////////////////////////////////////////////////////////////////
	// For the media player:
	///////////////////////////////////////////////////////////////////

	private void buildForegroundNotificationForMediaPlayer() {
		//Log.e(this.getClass().toString(), "Building notification.");

		Intent intent_act = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);
		Intent intent_play = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);
		Intent intent_pause = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);

		intent_play.setAction(Constants.ACTION_START_PLAYER);
		intent_pause.setAction(Constants.ACTION_PAUSE_PLAYER);
		intent_act.setAction(Constants.ACTION_START_MEDIA_PLAYER_ACT);

		pi_act = PendingIntent.getBroadcast(getApplicationContext(), 5223, intent_act,
				PendingIntent.FLAG_UPDATE_CURRENT);
		pi_play = PendingIntent.getBroadcast(getApplicationContext(), 5223, intent_play,
				PendingIntent.FLAG_CANCEL_CURRENT);
		pi_pause = PendingIntent.getBroadcast(getApplicationContext(), 5223, intent_pause,
				PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(NOTIFICATION_ID))
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.media_playing))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(bmp_durga)
				.addAction(R.drawable.ic_pause, "Pause", pi_pause)
				.setContentIntent(pi_act)
				.setSound(null)
				.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
						.setShowActionsInCompactView(0)
						.setShowCancelButton(true));

		if (CountdownActivity.IamAlive) {
			// If CountdownActivity is alive, start MediaPlayerActivity
			countdownActivityIsAlive = true;
			sendBroadcast(intent_act);
		}

		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	/**
	 * Starts the media playback.
	 * <p>
	 * In addition to starting the media player, this function also handles the completion listener
	 * of the media player. The media player is started iff AudioFocus is received by the app, as
	 * returned by {@code requestAudioFocus()}.
	 * </p>
	 */
	private void startMediaPlayer() {
		myInstance = this;

		MahalayaService.whichServiceIsRunning = 2;

		buildForegroundNotificationForMediaPlayer();

		mediaPlayer = MediaPlayer.create(this, media_uri);
		mediaPlayer.setLooping(false);
		mediaPlayer.setVolume(1.0f, 1.0f);
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				//Log.e(this.getClass().toString(), "Playback complete.");

				if (MediaPlayerActivity.IamAlive) {
					mediaPlayerActivityIsAlive = true;
				}
				Intent intent = new Intent();
				intent.setAction(Constants.ACTION_KILL_MEDIA_ACT);
				sendBroadcast(intent);

				stopForeground(true);

				if (mediaPlayerActivityIsAlive) {
					////////////////////////////////////////////////////////////////////////
					// If MediaPlayerActivity is alive, it means that the user is looking
					// at the activity. In this state, the final activity can be
					// created directly instead of posting a notification.
					///////////////////////////////////////////////////////////////////////
					startFinalActivity();
				} else {
					///////////////////////////////////////////////////////////////////////
					// Android Q and up places restrictions on apps starting
					// an activity from a service. In addition, for lower versions,
					// if the user doesn't see the created activity soon enough, the
					// system destroys it. That is why a notification will be
					// posted that will stay up for the user.
					// On tapping the notification, the final activity will be created.
					////////////////////////////////////////////////////////////////////////
					buildFinalNotification();
				}
				mp.stop();
				mp.release();
				afh.abandonAudioFocus();
				MahalayaService.whichServiceIsRunning = 0;
				stopSelf();
			}
		});

		if (afh.requestAudioFocus()) {
			//Log.e(this.getClass().toString(), "Media player started for first time.");

			Intent intent = new Intent();
			intent.setAction(Constants.ACTION_KILL_COUNTDOWN_ACT);
			sendBroadcast(intent);

			mediaPlayer.start();
		}
	}

	/**
	 * Pauses the media player, optionally abandoning AudioFocus if required.
	 *
	 * @param shouldFocusBeAbandoned If {@code true}, AudioFocus is abandoned, otherwise not.
	 */
	public static void pauseMediaPlayer(boolean shouldFocusBeAbandoned) {
		//Log.e(myInstance.getClass().toString(), "Player paused.");

		mediaPlayer.pause();
		if (MediaPlayerActivity.IamAlive) {
			MediaPlayerActivity.setCurrentState(myInstance.getString(R.string.media_paused));
		}

		updateNotificationForMediaPlayer(1);

		if (shouldFocusBeAbandoned) {
			afh.abandonAudioFocus();
		}
	}

	/**
	 * Re-starts the media player from a paused state, requesting AudioFocus when required.
	 *
	 * @param shouldFocusBeRequested If {@code true}, AudioFocus is requested, otherwise it is
	 * 		assumed that focus has been gained.
	 */
	public static void restartMediaPlayer(boolean shouldFocusBeRequested) {
		boolean hasFocusBeenReceived = ! shouldFocusBeRequested;
		if (shouldFocusBeRequested) {
			hasFocusBeenReceived = afh.requestAudioFocus();
		}

		if (hasFocusBeenReceived) {
			//Log.e(myInstance.getClass().toString(), "Player re-started.");
			mediaPlayer.start();
			if (MediaPlayerActivity.IamAlive) {
				// Activity is currently active on screen
				MediaPlayerActivity.setCurrentState(myInstance.getString(R.string.media_playing));
			}
			updateNotificationForMediaPlayer(2);
		}
	}

	/**
	 * Changes both the left and right volumes of the media player.
	 *
	 * @param newVolume The new volume to be set.
	 */
	public static void changeVolume(float newVolume) {
		mediaPlayer.setVolume(newVolume, newVolume);
		//Log.e(myInstance.getClass().toString(), "Media player volume changed.");
	}

	/**
	 * Updates the pause/play button in the notification while media is being played. In addition to
	 * updating the buttons, the associated pending intents are changed as well so as to trigger the
	 * correct action.
	 *
	 * @param code When {@code code = 1}, the play button is set, and when {@code code = 2}, the
	 * 		pause button is set.
	 */
	private static void updateNotificationForMediaPlayer(int code) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				myInstance.getApplicationContext(), Integer.toString(NOTIFICATION_ID))
				.setContentTitle(myInstance.getString(R.string.app_name))
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(myInstance.bmp_durga)
				.setSound(null)
				.setContentIntent(myInstance.pi_act);

		if (code == 1) {
			builder.addAction(R.drawable.ic_play, "Play", myInstance.pi_play);
			builder.setContentText(myInstance.getString(R.string.media_paused));
		} else if (code == 2) {
			builder.setContentText(myInstance.getString(R.string.media_playing));
			builder.addAction(R.drawable.ic_pause, "Pause", myInstance.pi_pause);
		}
		builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
				.setShowActionsInCompactView(0)
				.setShowCancelButton(true));

		myInstance.mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	/**
	 * Calculates the time left for the playback to end.
	 *
	 * @return The time left (in milliseconds) for the playback to end.
	 */
	public static long getDurationLeft() {
		return (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition());
	}

	/////////////////////////////////////////////////////////////////////////////
	// Once playback ends...
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Builds the notification for the ThankYouActivity.
	 * <p>
	 * When this function is called, the playback is complete and the media player has been
	 * released. Previously, we stopped the service and created this notification using a different
	 * ID. However, it was seen that if quite some time passes from the time of posting this
	 * notification, the notification failed to start ThankYouActivity. Hence, this notification
	 * will be created with the service still running in the foreground. Once the user taps on the
	 * notification, ThankYouActivity will be created, which will, in turn, kill this service via an
	 * intent.</p>
	 */
	private void buildFinalNotification() {
		int NOTIFICATION_ID = MahalayaService.NOTIFICATION_ID + 5;
		createNotificationChannel(NOTIFICATION_ID);

		/*Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1226, intent, PendingIntent.FLAG_CANCEL_CURRENT);*/

		Intent intent = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);
		intent.setAction(Constants.ACTION_START_THANK_YOU_ACT);
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(getApplicationContext(), 1226, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(NOTIFICATION_ID))
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText(getApplicationContext().getString(R.string.playback_complete))
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentIntent(pendingIntent)
				.setSound(null)
				.setAutoCancel(true);

		mNotificationManager.notify(NOTIFICATION_ID, builder.build());

		//Log.e(this.getClass().toString(), "Final notification created.");
	}

	/**
	 * Starts ThankYouActivity via an intent.
	 */
	private void startFinalActivity() {
		//Log.e(myInstance.getClass().toString(), "Creating next activity...");
		Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

}


////////////////////////////////////////////////////////////////////////////////////
// Preserved for reference: How to fire implicit intent for playing mp4 file
// ______________________________________________________________________________
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(media_uri, "video/*");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);*/
///////////////////////////////////////////////////////////////////////////////////



/*
	  /**
	    * Starts the inbuilt media player.
	   */
	/*private void startMediaPlayerService() {
		Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
		intent.setData(media_uri);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		startService(intent);
		Log.e(this.getClass().toString(), "Service started.");
	}*/