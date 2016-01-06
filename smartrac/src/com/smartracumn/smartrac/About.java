package com.smartracumn.smartrac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class About extends Activity {

	private TextView about;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("About: ");
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.about);
		about = (TextView)findViewById(R.id.tv_about);
		about.setMovementMethod(new ScrollingMovementMethod());
		readFile();

	}
	
	private void readFile(){
		
		StringBuilder sb = new StringBuilder();
		AssetManager am = getAssets();
		try {
			InputStream inputStream = am.open("About.txt");

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line+"\n");
				}

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		about.setText(sb.toString());
	}
}
