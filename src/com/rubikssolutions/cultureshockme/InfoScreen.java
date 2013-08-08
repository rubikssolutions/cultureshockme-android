package com.rubikssolutions.cultureshockme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class InfoScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);		
		ImageButton mainButton = (ImageButton) findViewById(R.id.button_logo);
		mainButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mainScreeIntent = new Intent(InfoScreen.this, MainActivity.class);
				InfoScreen.this.startActivity(mainScreeIntent);
			}
		});
	}

}
