package android.bignerdranch.com.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by bruno on 11/30/15.
 */
public class TimePickerFragment extends DialogFragment {

    public static final String EXTRA_TIME = "com.bignerdranch.android.criminalactivity.time";

    private static final String ARG_TIME = "time";
    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date time) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Date time = (Date) getArguments().getSerializable(ARG_TIME);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_time_picker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        if(Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setMinute(minute);
            mTimePicker.setHour(hour);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }



        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour, minute;
                        if(Build.VERSION.SDK_INT >= 23) {
                            hour    = mTimePicker.getHour();
                            minute  = mTimePicker.getMinute();
                        } else {
                            hour    = mTimePicker.getCurrentHour();
                            minute  = mTimePicker.getCurrentMinute();
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        sendResult(Activity.RESULT_OK, calendar.getTime());
                    }
                })
                .create();
    }

    public void sendResult(int resultCode, Date time) {
        if (getTargetFragment() == null) return;
        Intent it = new Intent();
        it.putExtra(EXTRA_TIME, time);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, it);
    }
}
