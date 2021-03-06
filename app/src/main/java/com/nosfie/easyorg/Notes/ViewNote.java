package com.nosfie.easyorg.Notes;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nosfie.easyorg.DataStructures.Task;
import com.nosfie.easyorg.R;

import static com.nosfie.easyorg.Helpers.DateStringsHelper.getHumanMonthNameGenitive;

public class ViewNote extends AppCompatActivity {

    EditText noteText;
    Task note = new Task();
    TextView noteTitle, windowTitle;
    ImageView noteIcon;
    LinearLayout noteContainer, buttonClose, buttonSave;
    boolean firstTouch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Setting up view elements
        noteText = (EditText)findViewById(R.id.noteText);
        noteTitle = (TextView)findViewById(R.id.noteTitle);
        windowTitle = (TextView)findViewById(R.id.windowTitle);
        noteIcon = (ImageView)findViewById(R.id.noteIcon);
        noteContainer = (LinearLayout)findViewById(R.id.noteContainer);
        buttonClose = (LinearLayout)findViewById(R.id.buttonClose);
        buttonSave = (LinearLayout)findViewById(R.id.buttonSave);

        noteText.setFocusable(false);
        noteText.setEnabled(true);

        // Receiving note info
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            note.id = extras.getInt("id");
            note.name = extras.getString("name");
            note.text = extras.getString("text");
            noteTitle.setText(note.name);
            noteText.setText(note.text);
            String[] dateSplit = extras.getString("date").split("\\.");
            note.customEndDate.year = Integer.parseInt(dateSplit[0]);
            note.customEndDate.month = Integer.parseInt(dateSplit[1]);
            note.customEndDate.day = Integer.parseInt(dateSplit[2]);
            windowTitle.setText(getResources().getString(R.string.note_dated_by) + " " +
                    note.customEndDate.day + " " +
                    getHumanMonthNameGenitive(note.customEndDate.month) + " " +
                    note.customEndDate.year
            );
            note.count = extras.getInt("count");
            switch (note.count) {
                case 0:
                    noteIcon.setVisibility(View.GONE);
                    break;
                case 1:
                    noteIcon.setImageResource(R.drawable.note_icon_1);
                    break;
                case 2:
                    noteIcon.setImageResource(R.drawable.note_icon_2);
                    break;
                case 3:
                    noteIcon.setImageResource(R.drawable.note_icon_3);
                    break;
                case 4:
                    noteIcon.setImageResource(R.drawable.note_icon_4);
                    break;
            }
        }

        // Setting note container onClickListener to activate note text editing
        noteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstTouch) {
                    noteText.setFocusableInTouchMode(true);
                    noteText.setFocusable(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(noteText, InputMethodManager.SHOW_IMPLICIT);
                    noteText.setSelection(noteText.getText().length());
                    firstTouch = false;
                }
            }
        });

        // Setting navigation button listener
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setting "Save" button onClickListener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.text = noteText.getText().toString();
                note.type = Task.TYPE.NOTE;
                note.synchronize(ViewNote.this);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_saved_note),Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

}