package com.example.ex1lecture2button;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void increment(View view) {

		int temp = MySingleton.getInstance().getI();
		temp++;
		MySingleton.getInstance().setI(temp);
		TextView tv = (TextView) findViewById(R.id.testText2);
		
		tv.setText(" " + temp);
	}

}
