package in.basulabs.mahalaya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.util.Objects;

public class MahalayaBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.e(this.getClass().toString(), "Broadcast received.");
		switch (Objects.requireNonNull(intent.getAction())) {
			case Constants.ACTION_START_PLAYER:
				MahalayaService.restartMediaPlayer(AudioFocusHelper.focusAbandoned);
				break;
			case Constants.ACTION_PAUSE_PLAYER:
				MahalayaService.pauseMediaPlayer(true);
				break;
			case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
				if (MahalayaService.mediaPlayer != null && MahalayaService.mediaPlayer.isPlaying()
						&& MahalayaService.whichServiceIsRunning == 2) {
					//Log.e(this.getClass().toString(), "Audio becoming noisy...");
					MahalayaService.pauseMediaPlayer(true);
				}
				break;
			case Constants.ACTION_START_THANK_YOU_ACT:
				Intent intent1 = new Intent(context, ThankYouActivity.class);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				context.startActivity(intent1);
				break;
			case Constants.ACTION_START_COUNTDOWN_ACT:
				Intent intent2 = new Intent(context, CountdownActivity.class);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent2.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				if (! CountdownActivity.IamAlive) {
					context.startActivity(intent2);
				}
				break;
			case Constants.ACTION_START_MEDIA_PLAYER_ACT:
				Intent intent3 = new Intent(context, MediaPlayerActivity.class);
				intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent3.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				if (! MediaPlayerActivity.IamAlive) {
					context.startActivity(intent3);
				}
				break;
		}
	}
}
