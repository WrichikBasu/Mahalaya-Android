package in.basulabs.mahalaya;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AlertDialogBox extends DialogFragment {

	public interface AlertDialogListener {
		void onDialogPositiveClick(DialogFragment dialogFragment);

		void onDialogNegativeClick(DialogFragment dialogFragment);
	}

	private AlertDialogListener listener;

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getString(R.string.message_alert))
				.setPositiveButton(getString(R.string.alert_cont),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								listener.onDialogPositiveClick(AlertDialogBox.this);

							}
						})
				.setNegativeButton(getString(R.string.alert_cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								listener.onDialogNegativeClick(AlertDialogBox.this);
							}
						});

		return builder.create();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			listener = (AlertDialogListener) context;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException("Must implement AlertDialogListener");
		}
	}

}
