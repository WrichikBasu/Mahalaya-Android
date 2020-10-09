package in.basulabs.mahalaya;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class FilesActivity extends AppCompatActivity
		implements View.OnClickListener, AlertDialogBox.AlertDialogListener {

	private static Uri media_uri;

	private static int month, day, hour, minute;

	private final int GET_FILE = 10;

	private Button startProg_btn;

	private int chosenTheme;

	private TextView showSelectedFile;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		//Log.e(this.getClass().toString(), "Created.");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		Toolbar myToolbar = findViewById(R.id.toolbar4);
		setSupportActionBar(myToolbar);

		Bundle extras = getIntent().getExtras();
		assert extras != null;
		month = extras.getInt("month");
		day = extras.getInt("day");
		hour = extras.getInt("hour");
		minute = extras.getInt("minute");

		Button getFile_btn = findViewById(R.id.get_file_btn);
		startProg_btn = findViewById(R.id.startProg);
		showSelectedFile = findViewById(R.id.showSelectedFile);
		Button mp4 = findViewById(R.id.mp4_dwnld_button);
		Button mp3 = findViewById(R.id.mp3_dwnld_button);

		mp4.setOnClickListener(this);
		mp3.setOnClickListener(this);
		getFile_btn.setOnClickListener(this);
		startProg_btn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (media_uri == null) {
			//Log.e(this.getClass().toString(), "Null uri.");
			startProg_btn.setEnabled(false);
		} else {
			//Log.e(this.getClass().toString(), media_uri.toString());
			showSelectedFile.setText(media_uri.getPath());
			startProg_btn.setEnabled(true);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_nightDark) {
			createThemeDialog();
			return true;
		} else if (item.getItemId() == R.id.action_help) {
			Intent intent = new Intent(this, HelpActivity.class);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

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
		startActivityForResult(intent, GET_FILE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		super.onActivityResult(requestCode, resultCode, resultData);
		if (requestCode == GET_FILE && resultCode == AppCompatActivity.RESULT_OK) {
			// The resultData contains a URI for the document or directory that the user selected.
			media_uri = resultData.getData();
			//Log.e("DateTimeActivity", "Received Uri: " + media_uri.toString());
			assert media_uri != null;
			showSelectedFile.setText(media_uri.getPath());
			startProg_btn.setEnabled(true);
		}
	}

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

	/**
	 * Displays the alert dialog box.
	 */
	private void showAlertDialog() {
		DialogFragment dialog = new AlertDialogBox();
		dialog.show(getSupportFragmentManager(), "AlertDialogBox");
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialogFragment) {
		if (! isDateOK()) {
			Toast.makeText(getApplicationContext(), getString(R.string.past_time),
					Toast.LENGTH_LONG).show();
		} else {
			startProgram();
		}
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialogFragment) {
		Toast.makeText(getApplicationContext(), getString(R.string.execution_cancel),
				Toast.LENGTH_LONG).show();
	}

	private void startProgram() {
		Intent killActivities = new Intent();
		killActivities.setAction(Constants.ACTION_KILL_ALL_UI_ACTIVITIES);
		sendBroadcast(killActivities);

		Intent intent = new Intent(this, MahalayaService.class);
		intent.putExtra("month", month);
		intent.putExtra("day", day);
		intent.putExtra("hour", hour);
		intent.putExtra("minute", minute);
		intent.putExtra("fileUri", media_uri);
		intent.setData(media_uri);
		intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		startService(intent);

		this.finish();


	}

	/**
	 * Checks whether the date and time the user has entered is in the past, or in future.
	 *
	 * @return {@code true} if date and time is in future, otherwise {@code false}.
	 */
	private boolean isDateOK() {
		Calendar cal = Calendar.getInstance();
		if (month == cal.get(Calendar.MONTH) + 1 && day == cal.get(Calendar.DAY_OF_MONTH)) {
			int time_now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
			int time_inp = minute + hour * 60;
			return time_inp > time_now;
		}
		return true;
	}

	private void createThemeDialog() {
		AlertDialog.Builder alb = new AlertDialog.Builder(this);
		alb.setTitle(getString(R.string.choose_theme));
		alb.setCancelable(true);

		String[] items;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			items = new String[]{getString(R.string.light), getString(R.string.dark),
					getString(R.string.system)};
		} else {
			items = new String[]{getString(R.string.light), getString(R.string.dark)};
		}

		int checkedItem = 0;

		if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
			//Log.e(this.getClass().toString(), "App night mode active.");
			checkedItem = 1;
		} else if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
			//Log.e(this.getClass().toString(), "App night mode inactive.");
			checkedItem = 0;
		} else if (AppCompatDelegate
				.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
			checkedItem = 2;
		}

		alb.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				chosenTheme = which;
			}
		});

		alb.setPositiveButton(getString(R.string.set_theme), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (chosenTheme == 0) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				} else if (chosenTheme == 1) {
					AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				} else if (chosenTheme == 2) {
					AppCompatDelegate
							.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				}
			}
		});

		alb.show();
	}
}
