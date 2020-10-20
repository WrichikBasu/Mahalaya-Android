package in.basulabs.mahalaya;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

/**
 * A class to manage audio focus for the app.
 */
final class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

	private final AudioManager audioManager;
	private AudioFocusRequest audioFocusRequest;
	private final Context context;

	/**
	 * Indicates whether volume was ducked.
	 * <p>
	 * When the focus loss is {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}, this variable is changed to
	 * {@code true}. When the listener gets a notice of {@link AudioManager#AUDIOFOCUS_GAIN}, this variable determines
	 * whether the volume of media player should be increased or not.
	 * </p>
	 */
	private boolean volumeDucked = false;

	/**
	 * Indicates whether focus was abandoned.
	 */
	boolean focusAbandoned = false;

	/**
	 * Indicates whether a delayed focus gain was received.
	 */
	boolean focusDelayed;

	//------------------------------------------------------------------------------------------

	/**
	 * Main constructor.
	 *
	 * @param context The Context which wants to me to manage audio focus.
	 */
	AudioFocusManager(Context context) {
		this.context = context;
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	//------------------------------------------------------------------------------------------

	/**
	 * Requests audio focus from the system.
	 *
	 * @return The result of {@link AudioManager#requestAudioFocus(AudioFocusRequest)} or {@link
	 *        AudioManager#requestAudioFocus(AudioManager.OnAudioFocusChangeListener, int, int)}, whichever applicable.
	 */
	int requestAudioFocus() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			AudioAttributes audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.build();

			audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
					.setAudioAttributes(audioAttributes)
					.setAcceptsDelayedFocusGain(true)
					.setOnAudioFocusChangeListener(this)
					.build();

			return audioManager.requestAudioFocus(audioFocusRequest);
		} else {
			return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		}

	}

	//------------------------------------------------------------------------------------------

	@Override
	public void onAudioFocusChange(int focusChange) {

		if (MahalayaService.mode == MahalayaService.MODE_MEDIA) {

			switch (focusChange) {

				case AudioManager.AUDIOFOCUS_GAIN:

					focusAbandoned = false;
					restartPlayer();

					if (volumeDucked) {
						volumeDucked = false;
						Intent intent2 = new Intent(Constants.ACTION_INCREASE_VOLUME);
						context.sendBroadcast(intent2);
					}
					break;

				case AudioManager.AUDIOFOCUS_LOSS:
					pausePlayer(true);
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					pausePlayer(false);
					break;

				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

					// Decrease volume:
					Intent intent3 = new Intent(Constants.ACTION_DECREASE_VOLUME);
					context.sendBroadcast(intent3);
					volumeDucked = true;
					break;
			}
		}
	}

	//------------------------------------------------------------------------------------------

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
	}

	//------------------------------------------------------------------------------------------

	/**
	 * Pause the media playback by sending the {@link Constants#ACTION_PAUSE_PLAYER} broadcast.
	 *
	 * @param abandonFocus Whether focus should be abandoned. Sent as {@link Constants#EXTRA_ABANDON_FOCUS}.
	 */
	private void pausePlayer(boolean abandonFocus) {
		Intent intent = new Intent(Constants.ACTION_PAUSE_PLAYER);
		intent.putExtra(Constants.EXTRA_ABANDON_FOCUS, abandonFocus);
		context.sendBroadcast(intent);
	}

	//------------------------------------------------------------------------------------------

	/**
	 * (Re)starts the media playback by sending the {@link Constants#ACTION_START_PLAYER} broadcast.
	 */
	private void restartPlayer() {
		Intent intent = new Intent(Constants.ACTION_START_PLAYER);
		context.sendBroadcast(intent);
	}

}
