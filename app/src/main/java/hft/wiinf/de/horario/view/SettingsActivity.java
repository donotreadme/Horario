package hft.wiinf.de.horario.view;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hft.wiinf.de.horario.R;
import hft.wiinf.de.horario.Service.NotificationReceiver;
import hft.wiinf.de.horario.controller.PersonController;
import hft.wiinf.de.horario.model.Person;

public class SettingsActivity extends Fragment {

    private static final String TAG = "SettingFragmentActivity";
    Button button_settings, button_support, button_copyright, button_feedback;
    RelativeLayout rLayout_main, rLayout_settings, rLayout_support, rLayout_copyright, rLayout_feedback;
    EditText editTextUsername;
    Person person;

    private PendingIntent pendingIntent;

    public SettingsActivity() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.activity_settings, container, false);
        return view;
    }

    //Method will be called directly after View is created
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        try {
            person = PersonController.getPersonWhoIam();
        } catch (NullPointerException e) {
            Log.d(TAG, "SettingsActivity:" + e.getMessage());
        }

        Intent alarmIntent = new Intent(getActivity(), NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);

        //Initialize all Gui-Elements
        button_settings = (Button) view.findViewById(R.id.settings_button_settings);
        button_support = (Button) view.findViewById(R.id.settings_button_support);
        button_copyright = (Button) view.findViewById(R.id.settings_button_copyright);
        button_feedback = (Button) view.findViewById(R.id.settings_button_feedback);

        rLayout_main = (RelativeLayout) view.findViewById(R.id.settings_relativeLayout_main);
        rLayout_copyright = (RelativeLayout) view.findViewById(R.id.settings_relativeLayout_copyright);
        rLayout_feedback = (RelativeLayout) view.findViewById(R.id.settings_relativeLayout_feedback);
        rLayout_settings = (RelativeLayout) view.findViewById(R.id.settings_relativeLayout_settings);
        rLayout_support = (RelativeLayout) view.findViewById(R.id.settings_relativeLayout_support);

        editTextUsername = (EditText) view.findViewById(R.id.settings_settings_editText_username);

        //If username is already saved -> pull it from db an set Text equal to it
        if (person != null) {
            editTextUsername.setText(person.getName());
        }

        //Everything that needs to happen after click on "Settings" button
        //set Visibility of mainLayout to Gone and settingsLayout to Visible
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Person person = new Person(true, "023131", "Flolilo");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, 2018);
                cal.set(Calendar.MONTH, 4);
                cal.set(Calendar.DAY_OF_MONTH, 2);
                cal.set(Calendar.HOUR_OF_DAY, 19);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                long milli = cal.getTimeInMillis() - 15*60000;
                
                AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                manager.set(AlarmManager.RTC_WAKEUP, milli , pendingIntent);

                rLayout_main.setVisibility(View.GONE);
                rLayout_settings.setVisibility(View.VISIBLE);

                if (person == null) {
                    try {
                        person = PersonController.getPersonWhoIam();
                    } catch (NullPointerException e) {
                        Log.d(TAG, "SettingsActivity:" + e.getMessage());
                    }
                }
                if (person != null) {
                    editTextUsername.setText(person.getName());
                }
            }
        });

        //Everything that needs to happen after click on "Feedback" button
        //set Visibility of mainLayout to Gone and FeedbackLayout to Visible
        button_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rLayout_main.setVisibility(View.GONE);
                rLayout_feedback.setVisibility(View.VISIBLE);
            }
        });

        //Everything that needs to happen after click on "Copyright" button
        //set Visibility of mainLayout to Gone and copyrightLayout to Visible
        button_copyright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rLayout_main.setVisibility(View.GONE);
                rLayout_copyright.setVisibility(View.VISIBLE);
            }
        });

        //Everything that needs to happen after click on "Support" button
        //set Visibility of mainLayout to Gone and supportLayout to Visible
        button_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rLayout_main.setVisibility(View.GONE);
                rLayout_support.setVisibility(View.VISIBLE);
            }
        });

        //Make EditText-Field editable
        editTextUsername.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editTextUsername.setFocusable(true);
                editTextUsername.setFocusableInTouchMode(true);
                return false;
            }
        });

        //Everything that needs to happen after Username was written in the EditText-Field
        editTextUsername.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String inputText = v.getText().toString();

                //RegEx: no whitespace at the beginning
                Pattern pattern_username = Pattern.compile("^([\\S]).*");
                Matcher matcher_username = pattern_username.matcher(inputText);

                if (actionId == EditorInfo.IME_ACTION_DONE && matcher_username.matches()) {
                    if (person != null) {
                        person.setName(inputText);
                        PersonController.addPersonMe(person);
                    } else {
                        //ToDo: get correct phoneNumber
                        person = new Person(true, "007", inputText);
                        PersonController.addPersonMe(person);
                    }
                    Toast toast = Toast.makeText(view.getContext(), R.string.thanksForUsername, Toast.LENGTH_SHORT);
                    toast.show();
                    editTextUsername.setFocusable(false);
                    editTextUsername.setFocusableInTouchMode(false);
                } else {
                    Toast toast = Toast.makeText(view.getContext(), R.string.noValidUsername, Toast.LENGTH_SHORT);
                    toast.show();
                    if (person != null) {
                        editTextUsername.setText(person.getName());
                    }
                    return true;
                }
                return false;
            }
        });
    }
}
