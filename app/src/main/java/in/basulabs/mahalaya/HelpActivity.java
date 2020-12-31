package in.basulabs.mahalaya;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);

		((TextView) findViewById(R.id.how_to_textContent)).setMovementMethod(LinkMovementMethod.getInstance());

		Toolbar myToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		Button mp4 = findViewById(R.id.mp4_dwnld_button);
		Button mp3 = findViewById(R.id.mp3_dwnld_button);
		mp4.setOnClickListener(this);
		mp3.setOnClickListener(this);

		ScrollView scrollView = findViewById(R.id.how_to_scrollView);
		scrollView.smoothScrollTo(0, 0);
	}

	//--------------------------------------------------------------------------------------------------------------

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
			case R.id.mp4_dwnld_button:
				//Log.e(this.getClass().toString(), "mp4 download requested.");
				intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("https://drive.google.com/file/d/1f4RmIt_mErCRMoVGS1ArszBZAHUCcWoN/view?usp=sharing"));
				startActivity(intent);
				break;
			case R.id.mp3_dwnld_button:
				//Log.e(this.getClass().toString(), "mp3 download requested.");
				intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("https://drive.google.com/file/d/1xGuKpBqPWgjJUkdFUVCgKn3L58ozJbey/view?usp=sharing"));
				startActivity(intent);
				break;
		}
	}

	//-------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		if (item.getItemId() == android.R.id.home){
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
