package com.konkawise.dtv.dialog;

import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class TeletextDialog extends CommCheckItemDialog {

	@Override
	public void onStart() {
		super.onStart();
		Window window = getDialog().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		lp.x = 20;
		lp.y = 20;
		window.setAttributes(lp);
		window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.2), ViewGroup.LayoutParams.WRAP_CONTENT);
	}
}
