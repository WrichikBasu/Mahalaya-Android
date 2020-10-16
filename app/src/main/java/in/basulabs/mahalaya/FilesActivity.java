package in.basulabs.mahalaya;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static in.basulabs.mahalaya.Constants.SHARED_PREF_KEY_THEME;

public class FilesActivity extends AppCompatActivity
		implements View.OnClickListener, AlertDialogBox.AlertDialogListener {

	private Button startProgramBtn;
	private TextView selectedFileTextView;

	/**
	 * The {@link Uri} of the media file chosen by the user.
	 */
	private Uri media_uri;

	/**
	 * The date and time at which the media will be played.
	 */
	private LocalDateTime playbackDateTime;

	/**
	 * Request code for letting the user choose a document.
	 */
	private final int FILE_REQUEST_CODE = 480;

	/**
	 * The currently active theme.
	 * <p>
	 * Can have four values: {@link Constants#THEME_LIGHT}, {@link Constants#THEME_DARK}, {@link
	 * Constants#THEME_SYSTEM}, {@link Constants#THEME_AUTO_TIME}.
	 * </p>
	 *
	 * @see Constants#THEME_LIGHT
	 * @see Constants#THEME_DARK
	 * @see Constants#THEME_SYSTEM
	 * @see Constants#THEME_AUTO_TIME
	 */
	private int currentTheme;

	/**
	 * Save Instance State key for {@link #playbackDateTime}.
	 */
	private static final String SAVE_INSTANCE_KEY_DATE_TIME = "in.basulabs.mahalaya.FilesActivity.DATE_TIME";

	/**
	 * Save instance state key for {@link #media_uri}.
	 */
	private static final String SAVE_INSTANCE_KEY_MEDIA_URI = "in.basulabs.mahalaya.FilesActivity.MEDIA_URI";

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		//Log.e(this.getClass().toString(), "Created.");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		Toolbar myToolbar = findViewById(R.id.toolbar4);
		setSupportActionBar(myToolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		Button getFile_btn = findViewById(R.id.get_file_btn);
		startProgramBtn = findViewById(R.id.startProg);
		selectedFileTextView = findViewById(R.id.showSelectedFile);
		Button mp4 = findViewById(R.id.mp4_dwnld_button);
		Button mp3 = findViewById(R.id.mp3_dwnld_button);

		mp4.setOnClickListener(this);
		mp3.setOnClickListener(this);
		getFile_btn.setOnClickListener(this);
		startProgramBtn.setOnClickListener(this);

		if (savedInstanceState == null) {
			playbackDateTime = (LocalDateTime) Objects.requireNonNull(getIntent().getExtras())
					.getSerializable(Constants.EXTRA_PLAYBACK_DATE_TIME);
		} else {
			playbackDateTime = (LocalDateTime) savedInstanceState.getSerializable(SAVE_INSTANCE_KEY_DATE_TIME);
			media_uri = savedInstanceState.getParcelable(SAVE_INSTANCE_KEY_MEDIA_URI);
		}

		int defaultTheme = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? Constants.THEME_SYSTEM :
				Constants.THEME_AUTO_TIME;
		currentTheme = getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE).getInt(SHARED_PREF_KEY_THEME,
				defaultTheme);

	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();

		startProgramBtn.setEnabled(media_uri != null);
		showSelectedFileName();
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_INSTANCE_KEY_DATE_TIME, playbackDateTime);
		outState.putParcelable(SAVE_INSTANCE_KEY_MEDIA_URI, media_uri);
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		switch (item.getItemId()) {

			case R.id.action_nightDark:
				createThemeDialog();
				return true;

			case R.id.action_help:
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				startActivity(intent);
				return true;

			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	/**
	 * Opens the file browser and lets the user select an mp4 file.
	 */
	private void openFile() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		intent.setType("*/*");
		String[] mimeTypes = new String[]{"audio/mpeg", "video/mp4"};
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		startActivityForResult(intent, FILE_REQUEST_CODE);
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		super.onActivityResult(requestCode, resultCode, resultData);
		if (requestCode == FILE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
			// The resultData contains a URI for the document or directory that the user selected.
			media_uri = resultData.getData();
			//Log.e("DateTimeActivity", "Received Uri: " + media_uri.toString());
			assert media_uri != null;
			showSelectedFileName();
			startProgramBtn.setEnabled(true);
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	/**
	 * Retrieves the file name from {@link #media_uri} and siaplays it.
	 */
	private void showSelectedFileName() {
		if (media_uri != null) {
			try (Cursor cursor = getContentResolver().query(media_uri, null, null, null, null)) {
				if (cursor != null) {
					int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					cursor.moveToFirst();
					selectedFileTextView.setText(cursor.getString(nameIndex));
				}
			}
		} else {
			selectedFileTextView.setText(getString(R.string.default_file));
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.get_file_btn:
				openFile();
				break;
			case R.id.startProg:
				showAlertDialog();
				break;
			case R.id.mp4_dwnld_button: {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse(
						"https://drive.google.com/file/d/1f4RmIt_mErCRMoVGS1ArszBZAHUCcWoN/view?usp=sharing"));
				startActivity(intent);
				break;
			}
			case R.id.mp3_dwnld_button: {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse(
						"https://drive.google.com/file/d/1xGuKpBqPWgjJUkdFUVCgKn3L58ozJbey/view?usp=sharing"));
				startActivity(intent);
				break;
			}
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	/**
	 * Displays the alert dialog box.
	 */
	private void showAlertDialog() {
		DialogFragment dialog = new AlertDialogBox();
		dialog.show(getSupportFragmentManager(), "AlertDialogBox");
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onDialogPositiveClick(DialogFragment dialogFragment) {
		if (! playbackDateTime.isAfter(LocalDateTime.now())) {
			Toast.makeText(getApplicationContext(), getString(R.string.past_time),
					Toast.LENGTH_LONG).show();
		} else {
			startProgram();
		}
	}

	//-----------------------------------------------------------------------------------------------------------

	@Override
	public void onDialogNegativeClick(DialogFragment dialogFragment) {
		Toast.makeText(getApplicationContext(), getString(R.string.execution_cancel),
				Toast.LENGTH_LONG).show();
	}

	//-----------------------------------------------------------------------------------------------------------

	private void startProgram() {
		Intent killActivities = new Intent();
		killActivities.setAction(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		sendBroadcast(killActivities);

		Intent intent = new Intent(this, MahalayaService.class);
		intent.putExtra(Constants.EXTRA_PLAYBACK_DATE_TIME, playbackDateTime);
		intent.setData(media_uri);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		ContextCompat.startForegroundService(this, intent);

		finish();
	}

	//-----------------------------------------------------------------------------------------------------------

	private void createThemeDialog() {
		AlertDialog.Builder alb = new AlertDialog.Builder(this);
		alb.setTitle(getString(R.string.choose_theme));
		alb.setCancelable(true);

		String[] items;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.time), getString(R.string.system)};
		} else {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.time)};
		}

		AtomicInteger newTheme = new AtomicInteger(currentTheme);

		alb.setSingleChoiceItems(items, currentTheme, (dialog, which) -> newTheme.set(which));

		alb.setPositiveButton(getString(R.string.set_theme), (dialog, which) -> {
			currentTheme = newTheme.get();
			changeTheme();
		});

		alb.show();
	}

	//---------------------------------------------------------------------------------------------------------------

	private void changeTheme() {

		getSharedPreferences(Constants.SHARED_PREF_FILE, MODE_PRIVATE)
				.edit()
				.remove(SHARED_PREF_KEY_THEME)
				.putInt(SHARED_PREF_KEY_THEME, currentTheme)
				.commit();

		switch (currentTheme) {
			case Constants.THEME_LIGHT:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;

			case Constants.THEME_DARK:
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;

			case Constants.THEME_SYSTEM:
				AppCompatDelegate
						.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
