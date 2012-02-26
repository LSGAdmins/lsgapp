package com.lsg.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateCheck extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Functions.setTheme(true, false, this);
		
		setContentView(R.layout.updatecheck);
		
		TextView updatecheck = (TextView) findViewById(R.id.updatecheck);
		//yourVersion.setText(getString(R.string.your_version) + ": " + getString(R.string.versioncode));
		String actVers = Functions.getActVersion(this);
		if(actVers.equals(getString(R.string.versioncode)))
			updatecheck.setText(getString(R.string.your_version) + ": " + getString(R.string.versioncode)
					+ '\n' + getString(R.string.act_version) + ": " + actVers);
		else
			updatecheck.setText(getString(R.string.your_version) + ": " + getString(R.string.versioncode)
					+ '\n' + getString(R.string.your_version_is_act));
		//actVersion.setText("dsaf");
		//Toast.makeText(this, actVers, Toast.LENGTH_LONG).show();
	}
}