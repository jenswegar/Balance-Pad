package fi.wegar.balancepad;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	public static final String KEY_DEFAULT_CURRENCY_PREFERENCE = "default_currency";
	
	private ListPreference mDefaultCurrency;
	
	/**
	 * creates the view and populates the fields with default values
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    // Get a reference to the preferences
	    mDefaultCurrency = (ListPreference) getPreferenceScreen().findPreference( KEY_DEFAULT_CURRENCY_PREFERENCE );
	    
    }
    
    /**
     * Set initial values for summaries and register a listener to the preferences so the values will be updated.
     */
    @Override
    protected void onResume() {
    	super.onResume();

    	// Setup the initial values
    	//mCheckBoxPreference.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting");
    	mDefaultCurrency.setSummary( getString(R.string.current_value_is)+" " + mDefaultCurrency.getEntry().toString() ); 

    	// Set up a listener whenever a key changes
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Remove preference key listeners when the screen closes so it doesn't pull resources.
     */
    @Override
    protected void onPause() {
    	super.onPause();
    	//Unregister the listener whenever a key changes Ê Ê Ê Ê Ê Ê
    	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    }
    
    /**
     * Called when one of the preferences changes value. Used to update the view to reflect the correct value
     */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		// Let's do something a preference value changes
		if (key.equals("hello")) {
			//mCheckBoxPreference.setSummary(sharedPreferences.getBoolean(key, false) ? "Disable this setting" : "Enable this setting");
		}
		else if (key.equals(KEY_DEFAULT_CURRENCY_PREFERENCE)) {
			mDefaultCurrency.setSummary( getString(R.string.current_value_is)+" " + sharedPreferences.getString(key, "") ); 
		}
		
	}
    
}


