package nosfie.easyorg.Settings;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import nosfie.easyorg.DataStructures.Daytime;
import nosfie.easyorg.R;

public class Settings extends AppCompatActivity  implements ColorPickerDialogListener {

    LinearLayout rectInProcess, rectDone, rectNotDone, rectPartlyDone, rectPostponed;
    LinearLayout buttonBack;
    RelativeLayout byDefaultButton;
    ImageView byDefaultImage;
    int colorLayoutId = 0;
    LinearLayout timeMidnight, timeCustom;
    ImageView timeMidnightRadio, timeCustomRadio;
    Daytime dayMargin = new Daytime();
    TextView customTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        rectInProcess = (LinearLayout)findViewById(R.id.rectInProcess);
        rectDone = (LinearLayout)findViewById(R.id.rectDone);
        rectNotDone = (LinearLayout)findViewById(R.id.rectNotDone);
        rectPartlyDone = (LinearLayout)findViewById(R.id.rectPartlyDone);
        rectPostponed = (LinearLayout)findViewById(R.id.rectPostponed);
        byDefaultButton = (RelativeLayout)findViewById(R.id.byDefaultButton);
        byDefaultImage = (ImageView)findViewById(R.id.byDefaultImage);
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        timeMidnight = (LinearLayout)findViewById(R.id.timeMidnight);
        timeCustom = (LinearLayout)findViewById(R.id.timeCustom);
        timeMidnightRadio = (ImageView)findViewById(R.id.timeMidnightRadio);
        timeCustomRadio = (ImageView)findViewById(R.id.timeCustomRadio);
        customTime = (TextView)findViewById(R.id.customTime);

        // Setting up colors from SharedPreferences values
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final int colorTaskActual = preferences.getInt("colorTaskActual", -1);
        final int colorTaskDone = preferences.getInt("colorTaskDone", -1);
        final int colorTaskFailed = preferences.getInt("colorTaskFailed", -1);
        final int colorTaskInProcess = preferences.getInt("colorTaskInProcess", -1);
        final int colorTaskPostponed = preferences.getInt("colorTaskPostponed", -1);
        rectInProcess.setBackgroundColor(colorTaskActual);
        rectDone.setBackgroundColor(colorTaskDone);
        rectNotDone.setBackgroundColor(colorTaskFailed);
        rectPartlyDone.setBackgroundColor(colorTaskInProcess);
        rectPostponed.setBackgroundColor(colorTaskPostponed);

        // Setting up selected day margin option from SharedPreferences values
        String[] timeSplit = preferences.getString("dayMargin", "").split(":");
        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);
        dayMargin = new Daytime(hours, minutes);
        if (hours == 0 && minutes == 0)
            timeMidnightRadio.setImageResource(R.drawable.radio_checked_medium);
        else {
            timeCustomRadio.setImageResource(R.drawable.radio_checked_medium);
            customTime.setText(dayMargin.toString().replace('-', ':'));
        }

        // Setting up color selectors onClickListeners
        rectInProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorLayoutId = R.id.rectInProcess;
                ColorPickerDialog.newBuilder().setColor(colorTaskActual).show(Settings.this);
            }
        });
        rectDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorLayoutId = R.id.rectDone;
                ColorPickerDialog.newBuilder().setColor(colorTaskDone).show(Settings.this);
            }
        });
        rectNotDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorLayoutId = R.id.rectNotDone;
                ColorPickerDialog.newBuilder().setColor(colorTaskFailed).show(Settings.this);
            }
        });
        rectPartlyDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorLayoutId = R.id.rectPartlyDone;
                ColorPickerDialog.newBuilder().setColor(colorTaskInProcess).show(Settings.this);
            }
        });
        rectPostponed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorLayoutId = R.id.rectPostponed;
                ColorPickerDialog.newBuilder().setColor(colorTaskPostponed).show(Settings.this);
            }
        });

        // Setting "By default" button OnClickListener
        byDefaultButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        byDefaultImage.setImageResource(R.drawable.by_default_button_medium_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        byDefaultImage.setImageResource(R.drawable.by_default_button_medium);
                        showByDefaultPromptDialog();
                        break;
                }
                return true;
            }
        });

        // Setting improvised radio buttons onTouchListeners for day margin settings
        timeMidnight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (dayMargin.hours == 0 && dayMargin.minutes == 0)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeMidnight.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        timeMidnight.setBackgroundColor(0x00000000);
                        dayMargin.hours = 0;
                        dayMargin.minutes = 0;
                        timeMidnightRadio.setImageResource(R.drawable.radio_checked_medium);
                        timeCustomRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        customTime.setText(getResources().getString(R.string.not_set_n));
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        String dayMarginStr = dayMargin.toString();
                        dayMarginStr = dayMarginStr.replace('-', ':');
                        editor.putString("dayMargin", dayMarginStr);
                        editor.commit();
                        break;
                }
                return true;
            }
        });
        timeCustom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeCustom.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        timeCustom.setBackgroundColor(0x00000000);
                        timeMidnightRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        timeCustomRadio.setImageResource(R.drawable.radio_checked_medium);
                        showTimePickerDialog();
                        break;
                }
                return true;
            }
        });

        // Setting navigation button onClickListener
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onColorSelected(int dialogId, @ColorInt int color) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        switch (colorLayoutId) {
            case (R.id.rectInProcess):
                rectInProcess.setBackgroundColor(color);
                editor.putInt("colorTaskActual", color);
                break;
            case (R.id.rectDone):
                rectDone.setBackgroundColor(color);
                editor.putInt("colorTaskDone", color);
                break;
            case (R.id.rectNotDone):
                rectNotDone.setBackgroundColor(color);
                editor.putInt("colorTaskFailed", color);
                break;
            case (R.id.rectPartlyDone):
                rectPartlyDone.setBackgroundColor(color);
                editor.putInt("colorTaskInProcess", color);
                break;
            case (R.id.rectPostponed):
                rectPostponed.setBackgroundColor(color);
                editor.putInt("colorTaskPostponed", color);
                break;
        }
        editor.commit();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // Do nothing
    }

    private void showByDefaultPromptDialog() {
        AlertDialog.Builder ad = new AlertDialog.Builder(Settings.this);
        ad.setTitle(getResources().getString(R.string.confirm_action));
        ad.setMessage(getResources().getString(R.string.set_default_colors_prompt));
        ad.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                setDefaultTaskColors();
                Toast.makeText(Settings.this, getResources().getString(R.string.success_default_colors_set), Toast.LENGTH_SHORT).show();
            }
        });
        ad.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                ///
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    private void setDefaultTaskColors() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("colorTaskActual", ResourcesCompat.getColor(getResources(), R.color.colorTaskActual, null));
        editor.putInt("colorTaskDone", ResourcesCompat.getColor(getResources(), R.color.colorTaskDone, null));
        editor.putInt("colorTaskPostponed", ResourcesCompat.getColor(getResources(), R.color.colorTaskPostponed, null));
        editor.putInt("colorTaskFailed", ResourcesCompat.getColor(getResources(), R.color.colorTaskFailed, null));
        editor.putInt("colorTaskInProcess", ResourcesCompat.getColor(getResources(), R.color.colorTaskInProcess, null));
        editor.commit();
        rectInProcess.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTaskActual, null));
        rectDone.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTaskDone, null));
        rectNotDone.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTaskFailed, null));
        rectPartlyDone.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTaskInProcess, null));
        rectPostponed.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTaskPostponed, null));
    }

    private void showTimePickerDialog() {
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hours, int minutes) {
                dayMargin.hours = hours;
                dayMargin.minutes = minutes;
                customTime.setText(dayMargin.toString().replace('-', ':'));
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                String dayMarginStr = dayMargin.toString();
                dayMarginStr = dayMarginStr.replace('-', ':');
                editor.putString("dayMargin", dayMarginStr);
                editor.commit();
            }
        }, dayMargin.hours, dayMargin.minutes, true);
        tpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dayMargin.hours == 0 && dayMargin.minutes == 0) {
                    timeMidnightRadio.setImageResource(R.drawable.radio_checked_medium);
                    timeCustomRadio.setImageResource(R.drawable.radio_unchecked_medium);
                    customTime.setText(getResources().getString(R.string.not_set_n));
                }
            }
        });
        tpd.show();
    }
}