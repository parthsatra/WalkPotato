package csc495.potato.walk.walkpotato.UI.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

import csc495.potato.walk.walkpotato.R;

/**
 * Created by Kenan on 5/4/2015.
 */
public class SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {
    final public static int DEFAULT_GOAL = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences prefs =
                getActivity().getSharedPreferences("walkpotato", Context.MODE_MULTI_PROCESS);

        Preference goal = findPreference("goal");
        goal.setOnPreferenceClickListener(this);
        goal.setSummary(getString(R.string.goal_summary, prefs.getInt("goal", DEFAULT_GOAL)));

    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        AlertDialog.Builder builder;
        View v;
        final SharedPreferences prefs =
                getActivity().getSharedPreferences("walkpotato", Context.MODE_MULTI_PROCESS);
        switch (preference.getTitleRes()) {
            case R.string.goal:
                builder = new AlertDialog.Builder(getActivity());
                final NumberPicker np = new NumberPicker(getActivity());
                np.setMinValue(1);
                np.setMaxValue(100000);
                np.setValue(prefs.getInt("goal", 10000));
                builder.setView(np);
                builder.setTitle(R.string.set_goal);
                builder.setPositiveButton(android.R.string.ok,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                np.clearFocus();
                                prefs.edit().putInt("goal", np.getValue()).commit();
                                preference.setSummary(
                                        getString(R.string.goal_summary, np.getValue()));
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                Dialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
                break;
        }
        return false;
    }
}
