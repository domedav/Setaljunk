package com.domedav.setaljunk.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QrCodeGenerator {
	/// This string is used to identify the scanned QR code, that it is in fact from the app (Base64)
	public static final String APPVALIDATION_QR = "qrVer1_U8OpdMOhbGp1bmtBcHBRUkNvZGU=";
	
	public static Bitmap generateQRCode(Context context, String from){
		QRGEncoder encoder = new QRGEncoder(from, null, QRGContents.Type.TEXT, dpToPx(context, 344));
		int colorPrimary = resolveColor(context, com.google.android.material.R.attr.colorPrimary);
		encoder.setColorBlack(android.graphics.Color.TRANSPARENT); // make qr code look material you
		encoder.setColorWhite(colorPrimary);
		
		return encoder.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
	}
	
	private static int resolveColor(@NonNull Context context, int attr) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}
	
	private static int dpToPx(@NonNull Context context, int dp) {
		float density = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * density);
	}
}
