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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

@SuppressWarnings("ALL")
public class MediaPlayerService extends Service {

	private PendingIntent pi_act, pi_play, pi_pause;
	private final int NOTIFICATION_ID = 122376;
	private static MediaPlayer mediaPlayer;
	private static MediaPlayerService myInstance;
	private Bitmap bmp;

	private BroadcastReceiver br;
	//TODO: Make the Broadcast Receiver a context receiver

	private static boolean countdownActivityIsAlive = false;

	private static boolean mediaPlayerActivityIsAlive = false;

	@Override
	public void onCreate() {
		super.onCreate();
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.durga1);
		Log.e(this.getClass().toString(), "Inside onCreate of service");
		MahalayaService.whichServiceIsRunning = 2;
		//registerReceiver();
		myInstance = this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(this.getClass().toString(), "Starting in thread " + Thread.currentThread().getName());

		Uri media_uri = intent.getData();
		assert media_uri != null;
		Log.e(this.getClass().toString(), "Received Uri: " + media_uri.toString());

		startForeground(NOTIFICATION_ID, buildForegroundNotification());

		mediaPlayer = MediaPlayer.create(this, media_uri);
		mediaPlayer.setLooping(false);
		mediaPlayer.setVolume(50, 50);
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.e(this.getClass().toString(), "Playback complete.");

				if (MediaPlayerActivity.IamAlive) {
					////////////////////////////////////////////////////////////////////////
					// If MediaPlayerActivity is alive, it means that the user is looking
					// at the activity. In this state, the final activity can be
					// created directly instead of posting a notification.
					///////////////////////////////////////////////////////////////////////
					mediaPlayerActivityIsAlive = true;
					startFinalActivity();
				} else {
					///////////////////////////////////////////////////////////////////////
					// Android Q and up places restrictions on apps starting
					// an activity from a service. In addition, for lower versions,
					// if the user doesn't see the created activity soon enough,
					// system destroys it. That is why a notification will be
					// posted that will (or maybe should) stay up for the user.
					// On tapping the notification, the final activity will be created.
					////////////////////////////////////////////////////////////////////////
					buildFinalNotification();
				}

				mp.stop();
				MahalayaService.whichServiceIsRunning = 0;
				stopForeground(true);
				stopSelf();
			}
		});
		mediaPlayer.start();
		Log.e(this.getClass().toString(), "Media player started.");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(this.getClass().toString(), "onDestroy() called.");
		mediaPlayer.release();
		//getApplicationContext().unregisterReceiver(br);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void createNotificationChannel(int NOTIFICATION_ID) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Log.e(this.getClass().toString(), "Creating notification channel.");
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(Integer.toString(NOTIFICATION_ID),
					"Mahalaya notifications", importance);
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			assert notificationManager != null;
			notificationManager.createNotificationChannel(channel);
		}
	}

	/**
	 * Builds the notification that is sent with the {@code startForeground(...)} method of the
	 * service.
	 *
	 * @return The notification to be sent.
	 */
	private Notification buildForegroundNotification() {

		Log.e(this.getClass().toString(), "Building notification.");

		Intent intent_act = new Intent(getApplicationContext(), MediaPlayerActivity.class);
		Intent intent_play = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);
		Intent intent_pause = new Intent(getApplicationContext(), MahalayaBroadcastReceiver.class);

		intent_play.putExtra("ActionCode", 1);
		intent_play.setAction("MAHALAYA_PLAYER_PLAY");

		intent_pause.putExtra("ActionCode", 2);
		intent_pause.setAction("MAHALAYA_PLAYER_PAUSE");

		intent_act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent_act.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

		pi_act = PendingIntent.getActivity(getApplicationContext(), 5223, intent_act,
				PendingIntent.FLAG_UPDATE_CURRENT);
		pi_play = PendingIntent.getBroadcast(getApplicationContext(), 5223, intent_play,
				PendingIntent.FLAG_CANCEL_CURRENT);
		pi_pause = PendingIntent.getBroadcast(getApplicationContext(), 5223, intent_pause,
				PendingIntent.FLAG_CANCEL_CURRENT);

		createNotificationChannel(this.NOTIFICATION_ID);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(NOTIFICATION_ID))
				.setContentTitle("Mahalaya")
				.setContentText("Media is playing.")
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(bmp)
				.addAction(R.drawable.ic_pause, "Pause", pi_pause)
				.setContentIntent(pi_act)
				.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
						.setShowActionsInCompactView(0)
						.setShowCancelButton(true));

		if (CountdownActivity.IamAlive) {
			// If CountdownActivity is alive, start MediaPlayerActivity
			countdownActivityIsAlive = true;
			startActivity(intent_act);
		}

		return builder.build();
	}

	/**
	 * Updates the pause/play button in the notification while media is being played. In addition to
	 * updating the buttons, the associated pending intents are changed as well so as to trigger the
	 * correct action.
	 *
	 * @param code When {@code code = 1}, the play button is set, and when {@code code = 2}, the
	 * 		pause button is set.
	 */
	public static void updateNotification(int code) {
		NotificationManager mNotificationManager = (NotificationManager) myInstance
				.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				myInstance.getApplicationContext(), Integer.toString(myInstance.NOTIFICATION_ID))
				.setContentTitle("Mahalaya")
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(myInstance.bmp)
				.setContentIntent(myInstance.pi_act);

		if (code == 1) {
			builder.addAction(R.drawable.ic_play, "Play", myInstance.pi_play);
			builder.setContentText("Media is paused.");
		} else if (code == 2) {
			builder.setContentText("Media is playing.");
			builder.addAction(R.drawable.ic_pause, "Pause", myInstance.pi_pause);
		}
		builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
				.setShowActionsInCompactView(0)
				.setShowCancelButton(true));

		mNotificationManager.notify(myInstance.NOTIFICATION_ID, builder.build());
	}

	/**
	 * Registers the broadcast receiver with the system as a context receiver.
	 */
	private void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("MAHALAYA_PLAYER_PLAY");
		intentFilter.addAction("MAHALAYA_PLAYER_PAUSE");
		getApplicationContext().registerReceiver(br, intentFilter);
	}

	/**
	 * Calculates the time left for the playback to end.
	 *
	 * @return The time left (in milliseconds) for the playback to end.
	 */
	public static long getDurationLeft() {
		Log.e("MediaPlayerService",
				"Millis: " + (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition()));
		return (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition());
	}

	private void buildFinalNotification() {
		final int NOTIFICATION_ID = 102236;

		createNotificationChannel(NOTIFICATION_ID);

		Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent
				.getActivity(getApplicationContext(), 1226, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
				Integer.toString(102236))
				.setContentTitle("Mahalaya App")
				.setContentText("Playback complete. View details.")
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setLargeIcon(bmp)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(
				Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());

		Log.e(this.getClass().toString(), "Final notification created.");
	}

	private void startFinalActivity() {
		Log.e(this.getClass().toString(), "Creating next activity...");
		Intent intent = new Intent(this, ThankYouActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
