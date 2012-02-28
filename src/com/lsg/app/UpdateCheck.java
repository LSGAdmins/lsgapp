package com.lsg.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class UpdateCheck extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Functions.setTheme(true, false, this);
		getWindow().setBackgroundDrawableResource(R.layout.background);

		setContentView(R.layout.updatecheck);
		
		/*TextView yourVersion = (TextView) findViewById(R.id.updatecheck_yourversion);
		yourVersion.setText(getString(R.string.your_version) + ": " + getString(R.string.versioncode));
		
		TextView actVersion = (TextView) findViewById(R.id.updatecheck_actversion);
		String actVers = Functions.getActVersion(this);
		Log.d("'" + getString(R.string.versioncode) + "'", "'" + actVers + "'");
		if(actVers.contains(getString(R.string.versioncode)))
			actVersion.setText(getString(R.string.act_version) + ": " + actVers);
		else
			actVersion.setText(getString(R.string.your_version_is_act));*/
	}
}