package com.wanmei.dospy.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.example.android_year_month_wheel.R;

public class CustomDialog extends Dialog {

	private Context mContext;
	
	public CustomDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	public CustomDialog(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public CustomDialog createPopupWindow() {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomDialog dialog = new CustomDialog(mContext, R.style.DialogPopupWindow);
			View layout = inflater.inflate(R.layout.layout_page_popup_view, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			return dialog;
	}
}