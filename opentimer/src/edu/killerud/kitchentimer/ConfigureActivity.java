package edu.killerud.kitchentimer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by william on 10.10.13.
 */
public class ConfigureActivity extends PreferenceActivity{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
