package seniordesign.lucknell.com.seniordesign;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsPrefActivity extends AppCompatPreferenceActivity
        implements OnSharedPreferenceChangeListener {
    private static final String TAG = "SETTINGS Activity";
    static boolean change = false;
    static Activity thisActivity;
    static int id = 0;static int hex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // addPreferencesFromResource(R.xml.pref_main);
        setTitle("Senior Design Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        thisActivity = this;
        id = 0;
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();

    }


    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_name)));
            bindPreferenceSummaryToValue(findPreference("1235"));
            ListPreference btDevicesList = (ListPreference) findPreference("1235");
            CheckBoxPreference fake = (CheckBoxPreference) findPreference("key_fake");
            Preference version = getPreferenceManager().findPreference(getString(R.string.key_version));
            Preference feedback = getPreferenceManager().findPreference(getString(R.string.key_send_feedback));
            Preference about = getPreferenceScreen().findPreference("key_about");
            btDevicesList.setSummary(btDevicesList.getValue());
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

            ArrayList<String> s = new ArrayList<String>();
            for (BluetoothDevice bt : pairedDevices)
                s.add(bt.getName());
            btDevicesList.setEntries(listToArray(s));
            ArrayList<String> v = new ArrayList<String>();
            for (BluetoothDevice bt : pairedDevices)
                v.add(bt.getAddress());
            btDevicesList.setEntryValues(listToArray(v));
            //bindPreferenceSummaryToValue(findPreference("1235"));
            // load settings fragment
            feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    sendFeedback(thisActivity);
                    return false;
                }
            });
            version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (id++ > 8)
                        displayMessage();
                    return false;
                }
            });
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                        if(hex++>8)
                            credits(thisActivity);
                    return false;
                }
            });

        }

        public CharSequence[] listToArray(ArrayList<String> list) {
            CharSequence[] sequence = new CharSequence[list.size()];
            for (int i = 0; i < list.size(); i++) {
                sequence[i] = list.get(i);
            }
            return sequence;
        }


    }

    private static Handler messageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                String message = (String) msg.obj;
                Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (change) {
                Intent home = new Intent(SettingsPrefActivity.this, MainActivity.class);
                startActivity(home);
            }
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         }


    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_name")) {
                    change = true;
                    //if(validateMac(stringValue))
                    preference.setSummary(stringValue);
                }
            }
            else if (preference instanceof  ListPreference)
            {if(preference.getKey().equals("1235"))
            {
                change = true;
                preference.setSummary(stringValue);
            }

            }else {
                Toast.makeText(null, stringValue, Toast.LENGTH_SHORT).show();
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"lucknell3@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Your Senior Design App");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

    private static void credits(Context context)
    {
        Intent intent = new Intent(context,credits.class);
        context.startActivity(intent);
    }

    private static void displayMessage() {
        Message msg = new Message();
        msg.what = 1;
        //msg.obj = "test.";
        msg.obj = ""+(char)77+(char)97+
                (char)100+(char)101+
                (char)32+(char)98+
                (char)121+(char)32+
                (char)76+(char)117+
                (char)99+(char)107+
                (char)110+(char)101+
                (char)108+(char)108+
                (char)32+(char)97+(char)110+
                (char)100+(char)32+
                (char)69+(char)110+
                (char)114+(char)105+
                (char)113+(char)117+
                (char)101+(char)46;


        messageHandler.sendMessage(msg);
    }

    private boolean validateMac(String mac) {
        Pattern p = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
        Matcher m = p.matcher(mac);
        Log.d(TAG, m.find() + " did it work?");
        return m.find();
    }
}