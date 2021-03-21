package com.lubenard.oring_reminder;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;


import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import java.util.Locale;

/**
 * Settings page.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String TAG = "SettingsActivity";
    private static Activity activity;
    private Fragment thisFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);
        activity = getActivity();

        thisFragment = this;
        // Language change listener
        final Preference language = findPreference("ui_language");
        language.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Language value has changed for " + newValue);
                switch (newValue.toString()) {
                    case "en":
                        Utils.setAppLocale(activity, "en-us");
                        break;
                    case "fr":
                        Utils.setAppLocale(activity, "fr");
                        break;
                    case "system":
                        break;
                }
                getActivity().getSupportFragmentManager().popBackStackImmediate();
                return true;
            }
        });

        // Theme change listener
        final Preference theme = findPreference("ui_theme");
        theme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "Theme value has changed for " + newValue);
                switch (newValue.toString()) {
                    case "dark":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case "white":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case "battery_saver":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                        break;
                    case "system":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
                restartActivity();
                return true;
            }
        });

        // feedback preference click listener
        Preference wearing_time = findPreference("myring_wearing_time");
        wearing_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int newTimeWeared = Integer.parseInt(newValue.toString());
                if (newTimeWeared < 13 || newTimeWeared > 18) {
                    new AlertDialog.Builder(getContext()).setTitle(R.string.alertdialog_dangerous_wearing_time)
                            .setMessage(R.string.alertdialog_dangerous_wearing_body)
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                }
                return true;
            }
        });

        Preference exportXML = findPreference("datas_export_data_xml");
        exportXML.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.custom_backup_title_alertdialog);
                final View customLayout = getLayoutInflater().inflate(R.layout.custom_view_backup_dialog, null);
                builder.setView(customLayout);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Utils.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                            return;
                        Intent intent = new Intent(getContext(), BackupRestore.class);
                        intent.putExtra("mode", 1);

                        boolean isDatasChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                        boolean isSettingsChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                        if (!isDatasChecked && !isSettingsChecked)
                            return;

                        intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                        intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        Preference importXML = findPreference("datas_import_data_xml");
        importXML.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Utils.checkOrRequestPerm(getActivity(), getContext(), Manifest.permission.READ_EXTERNAL_STORAGE))
                    return false;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.custom_restore_title_alertdialog);
                final View customLayout = getLayoutInflater().inflate(R.layout.custom_view_backup_dialog, null);

                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).setText(R.string.custom_restore_alertdialog_save_datas);
                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).setText(R.string.custom_restore_alertdialog_save_settings);

                builder.setView(customLayout);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), BackupRestore.class);
                        intent.putExtra("mode", 2);

                        boolean isDatasChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_datas)).isChecked();
                        boolean isSettingsChecked =
                                ((CheckBox)customLayout.findViewById(R.id.custom_backup_restore_alertdialog_settings)).isChecked();

                        if (!isDatasChecked && !isSettingsChecked)
                            return;

                        intent.putExtra("shouldBackupRestoreDatas", isDatasChecked);
                        intent.putExtra("shouldBackupRestoreSettings", isSettingsChecked);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        // reset preference click listener
        Preference reset = findPreference("datas_erase_data");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.settings_alertdialog_erase_title)
                        .setMessage("Are you sure you want to delete all your data ? All losses are definitive. Once completed, the app will shut down.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete DB
                                getContext().deleteDatabase(DbManager.getDBName());
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
            }
        });

        // feedback preference click listener
        Preference debugMenu = findPreference("other_debug_menu");
        debugMenu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new DebugFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            }
        });

        // feedback preference click listener
        Preference feedback = findPreference("other_feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","escatrag@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "oRing - Reminder");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                return true;
            }
        });

        // feedback preference click listener
        Preference aboutLicenses = findPreference("other_about_licenses");
        aboutLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new AboutFragment(), null)
                        .addToBackStack(null).commit();
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);


        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                }
            });
        }
    }

    public static void restartActivity() {
        activity.recreate();
    }
}

