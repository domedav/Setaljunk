package com.domedav.setaljunk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/// This is needed, because we need to change the way we crop in the view
public class SquareSurfaceView extends SurfaceView {
	private float cameraAspectRatio;
	
	public SquareSurfaceView(Context context) {
		super(context);
	}
	
	public SquareSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public SquareSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	
	
	public void setCameraAspectRatio(float aspectRatio) {
		cameraAspectRatio = aspectRatio;
		requestLayout();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		if (cameraAspectRatio > 0) {
			float viewAspectRatio = (float) width / height;
			float scale = (viewAspectRatio > cameraAspectRatio)
					? (float) height / (width / cameraAspectRatio)
					: (float) width / (height * cameraAspectRatio);
			
			setScaleX(scale);
			setScaleY(scale);
			setPivotX(width / 2f);
			setPivotY(height / 2f);
		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}

