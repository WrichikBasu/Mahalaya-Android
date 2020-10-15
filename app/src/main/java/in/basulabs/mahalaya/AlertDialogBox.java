package in.basulabs.mahalaya;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AlertDialogBox extends DialogFragment {

	private AlertDialogListener listener;

	public interface AlertDialogListener {
		void onDialogPositiveClick(DialogFragment dialogFragment);

		void onDialogNegativeClick(DialogFragment dialogFragment);
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		if (context instanceof AlertDialogListener) {
			listener = (AlertDialogListener) context;
		} else {
			throw new ClassCastException(context.getClass() + " must implement AlertDialogBox.AlertDialogListener.");
		}
	}

	//--------------------------------------------------------------------------------------------

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getString(R.string.message_alert))
				.setPositiveButton(getString(R.string.alert_cont),
						(dialog, which) -> listener.onDialogPositiveClick(AlertDialogBox.this))
				.setNegativeButton(getString(R.string.alert_cancel),
						(dialog, which) -> listener.onDialogNegativeClick(AlertDialogBox.this));

		return builder.create();
	}


}
