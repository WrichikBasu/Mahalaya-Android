package in.basulabs.mahalaya;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

/**
 * A class to manage audio focus for the app.
 */
final class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {

	private final AudioManager audioManager;
	private AudioFocusRequest audioFocusRequest;

	/**
	 * When focus is ducked, this variable is changed to {@code true}. When the listener gets a
	 * notice of full AudioFocus GAIN, this variable determines whether the volume of media player
	 * should be increased or not.
	 */
	private static boolean focusDucked = false;

	/**
	 * If the {@code abandonAudioFocus()} method is called, this variable is set to {@code true}.
	 * When the system grants full GAIN to the app, this variable determines whether or not playback
	 * should be started. If {@code true}, it means that focus was deliberately abandoned by the
	 * app, so media player will not be started. Otherwise it means that the focus was lost
	 * transiently and hence, player should be started.
	 */
	static boolean focusAbandoned = false;

	AudioFocusHelper(Context context) {
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Requests AudioFocus from the system. As of now, delayed AudioFocus is not handled.
	 *
	 * @return {@code true} if system grants request, otherwise {@code false}.
	 */
	boolean requestAudioFocus() {
		int focusRequest;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			AudioAttributes audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
					.build();

			audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
					.setAudioAttributes(audioAttributes)
					.setAcceptsDelayedFocusGain(false)
					.setOnAudioFocusChangeListener(this)
					.build();

			focusRequest = audioManager.requestAudioFocus(audioFocusRequest);
		} else {
			focusRequest = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN);
		}

		switch (focusRequest) {
			case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
			default:
				//Log.e(this.getClass().toString(), "Focus request failure.");
				return false;
			case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
				//Log.e(this.getClass().toString(), "Focus request success.");
				return true;
		}
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		//Log.e(this.getClass().toString(), "Audio Focus changed.");
		if (MahalayaService.whichServiceIsRunning == 2) {
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_GAIN:
					//Log.e(this.getClass().toString(), "Focus gained. Starting player...");
					focusAbandoned = false;
					MahalayaService.restartMediaPlayer(false);
					if (focusDucked) {
						focusDucked = false;
						MahalayaService.changeVolume(1.0f);
					}
					break;
				case AudioManager.AUDIOFOCUS_LOSS:
					//Log.e(this.getClass().toString(), "Focus lost. Player paused.");
					MahalayaService.pauseMediaPlayer(true);
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					//Log.e(this.getClass().toString(), "Focus loss transient. Player paused.");
					MahalayaService.pauseMediaPlayer(false);
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					//Log.e(this.getClass().toString(), "Focus loss transient. Player ducked.");
					// Lower volume
					MahalayaService.changeVolume(0.1f);
					focusDucked = true;
					break;
			}
		}
	}

	/**
	 * Abandons AudioFocus from the app.
	 */
	void abandonAudioFocus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			audioManager.abandonAudioFocusRequest(audioFocusRequest);
		} else {
			audioManager.abandonAudioFocus(this);
		}
		focusAbandoned = true;
		//Log.e(this.getClass().toString(), "Focus abandoned.");
	}
}
