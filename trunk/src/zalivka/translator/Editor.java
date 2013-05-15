package zalivka.translator;

import java.util.HashMap;
import java.util.Map;

import zalivka.translator.Translator.TransUnit;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Editor {

	public static void editTranslation(final Activity ctx, final TransUnit unit) {
		AlertDialog.Builder b = new Builder(ctx);
		b.setTitle("Suggest a translation");
		
		View root = LayoutInflater.from(ctx).inflate(R.layout.edit, null);
		((TextView)root.findViewById(R.id.en_str_edit)).setText(unit.en);
		final EditText ed = (EditText)root.findViewById(R.id.nat_str_edit);
		ed.setText(unit.nat);
		
		final Translator host = (Translator) ctx;
		
		b.setView(root);
		b.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				String nat = ed.getText().toString();
				if (TextUtils.isEmpty(nat)) {
					return;
				}
				unit.nat = nat;
				host.save(unit);
			}

		});
		b.setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		b.create().show();
	}

	
	
}
