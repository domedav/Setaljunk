package com.domedav.setaljunk.popupmenus;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.domedav.setaljunk.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

public class AppPopupMenu {
	private static final String TAG = "AppPopupMenu";
	private static Dialog dialog;
	public interface Callback{
		void onActionNo();
		void onActionYes();
		void onActionOk();
	}
	public static void showPopupMenuYesNo(Context context, String header, String description, String actionNo, String actionYes, Callback callback){
		if(dialog != null){
			Log.e(TAG, "showPopupMenu: there is already an active dialog, cant display a new one");
			return;
		}
		Log.i(TAG, "showPopupMenu: showingPopupMenu...");
		
		dialog = new Dialog(context);
		
		dialog.setContentView(R.layout.popup_menu);
		Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		((MaterialTextView)dialog.findViewById(R.id.popup_header)).setText(header);
		((MaterialTextView)dialog.findViewById(R.id.popup_description)).setText(description);
		((MaterialButton)dialog.findViewById(R.id.popup_action_no)).setText(actionNo);
		((MaterialButton)dialog.findViewById(R.id.popup_action_yes)).setText(actionYes);
		(dialog.findViewById(R.id.popup_action_ok)).setVisibility(View.GONE);
		
		(dialog.findViewById(R.id.popup_action_no)).setOnClickListener(l -> {
			callback.onActionNo();
		});
		(dialog.findViewById(R.id.popup_action_yes)).setOnClickListener(l -> {
			callback.onActionYes();
		});
		
		dialog.show();
	}
	
	public static void showPopupMenuOk(Context context, String header, String description, String actionOk, Callback callback){
		if(dialog != null){
			Log.e(TAG, "showPopupMenu: there is already an active dialog, cant display a new one");
			return;
		}
		Log.i(TAG, "showPopupMenu: showingPopupMenu...");
		
		dialog = new Dialog(context);
		
		dialog.setContentView(R.layout.popup_menu);
		Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		((MaterialTextView)dialog.findViewById(R.id.popup_header)).setText(header);
		((MaterialTextView)dialog.findViewById(R.id.popup_description)).setText(description);
		((MaterialButton)dialog.findViewById(R.id.popup_action_ok)).setText(actionOk);
		
		(dialog.findViewById(R.id.popup_action_yes)).setVisibility(View.GONE);
		(dialog.findViewById(R.id.popup_action_no)).setVisibility(View.GONE);
		(dialog.findViewById(R.id.popup_yesno_space)).setVisibility(View.GONE);
		
		(dialog.findViewById(R.id.popup_action_ok)).setOnClickListener(l -> {
			callback.onActionOk();
		});
		
		dialog.show();
	}
	
	public static void dismissPopupMenu(){
		if(dialog == null){
			return;
		}
		dialog.dismiss();
		dialog = null;
	}
}
