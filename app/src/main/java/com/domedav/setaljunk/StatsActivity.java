package com.domedav.setaljunk;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.domedav.setaljunk.sharedpreferences.AppDataStore;
import com.google.android.material.textview.MaterialTextView;

public class StatsActivity extends AppCompatActivity {
	
	AppCompatImageButton _navigateBack;
	
	MaterialTextView _totalStepsText;
	MaterialTextView _totalCaloriesText;
	MaterialTextView _totalWalkedDistance;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		setContentView(R.layout.activity_stats);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		_navigateBack = findViewById(R.id.navigate_back_button);
		
		_navigateBack.setOnClickListener(l -> finish()); // if back is pressed, close this activity
		
		_totalStepsText = findViewById(R.id.total_steps_display);
		_totalCaloriesText = findViewById(R.id.total_calories_display);
		_totalWalkedDistance = findViewById(R.id.total_distance_display);
		
		var totalSteps = AppDataStore.getData(AppDataStore.StatsPrefsKeys.STOREKEY, AppDataStore.StatsPrefsKeys.DATAKEY_TOTAL_STEPS, 0);
		
		// unisex data, minimalistic
		var totalDistance = totalSteps * 0.0007; //km
		var totalCalories = totalSteps * 0.04; //kcal
		
		_totalStepsText.setText(getString(R.string.stats_steps_total_string, totalSteps + ""));
		_totalCaloriesText.setText(getString(R.string.stats_calories_total_string, totalCalories + ""));
		_totalWalkedDistance.setText(getString(R.string.stats_distance_total_string, totalDistance + ""));
	}
}