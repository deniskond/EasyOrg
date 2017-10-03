package nosfie.easyorg.Notes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.R;

public class AddNote extends AppCompatActivity {

    LinearLayout buttonBack, optionNo, optionYes, selectedBorder, buttonSave;
    ImageView optionNoRadio, optionYesRadio, selectedIcon;
    Task note = new Task();
    TextView buttonSaveText;
    EditText noteTitle, noteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        // Filling note with default values. I didn't want to refactor lots of code and create
        // another database table, so I'm putting notes into tasks table:
        // name => note title
        // count => note icon number (0 = no icon)
        // text => note text
        note.name = "";
        note.count = 0;
        note.text = "";

        // Setting up view elements
        buttonBack = (LinearLayout)findViewById(R.id.buttonBack);
        buttonSave = (LinearLayout)findViewById(R.id.buttonSave);
        buttonSaveText = (TextView)findViewById(R.id.buttonSaveText);
        optionNo = (LinearLayout)findViewById(R.id.optionNo);
        optionYes = (LinearLayout)findViewById(R.id.optionYes);
        optionNoRadio = (ImageView)findViewById(R.id.optionNoRadio);
        optionYesRadio = (ImageView)findViewById(R.id.optionYesRadio);
        selectedIcon = (ImageView)findViewById(R.id.selectedIcon);
        selectedBorder = (LinearLayout)findViewById(R.id.selectedBorder);
        noteTitle = (EditText)findViewById(R.id.noteTitle);
        noteText = (EditText)findViewById(R.id.noteText);

        // Setting navigation onClickListeners
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setting improvised radioButtons onClickListeners
        optionNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 0)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        optionNo.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        optionNo.setBackgroundColor(0x00000000);
                        optionNoRadio.setImageResource(R.drawable.radio_checked_medium);
                        optionYesRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        selectedBorder.setVisibility(View.GONE);
                        note.count = 0;
                        break;
                }
                return true;
            }
        });
        optionYes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //if (note.count != 0)
                //    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        optionYes.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        optionYes.setBackgroundColor(0x00000000);
                        optionNoRadio.setImageResource(R.drawable.radio_unchecked_medium);
                        optionYesRadio.setImageResource(R.drawable.radio_checked_medium);
                        showSelectIconDialog();
                        break;
                }
                return true;
            }
        });

        // Final "Save" button onTouchListener
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNextClicked, null));
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonSave.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorButtonNext, null));
                        note.name = noteTitle.getText().toString();
                        note.text = noteText.getText().toString();
                        note.type = Task.TYPE.NOTE;
                        if (note.text.equals("")) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_note_text),Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        note.insertIntoDatabase(AddNote.this);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.success_added_note),Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
                return true;
            }
        };
        buttonSave.setOnTouchListener(onTouchListener);
        buttonSaveText.setOnTouchListener(onTouchListener);
    }

    private void showSelectIconDialog() {
        // Setting basic dialog elements
        WindowManager windowManager = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        final Dialog selectIconDialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = ((Activity)this).getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = inflater.inflate(R.layout.note_icon_dialog,
                (ViewGroup)rootView.findViewById(R.id.dialog_root));
        selectIconDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectIconDialog.setContentView(layout);
        LinearLayout root = (LinearLayout)layout.findViewById(R.id.dialog_root);
        root.setMinimumWidth((int)(displaySize.x * 0.85f));

        // Setting up dialog elements
        final LinearLayout videoRow = (LinearLayout)layout.findViewById(R.id.videoRow);
        final LinearLayout audioRow = (LinearLayout)layout.findViewById(R.id.audioRow);
        final LinearLayout moneyRow = (LinearLayout)layout.findViewById(R.id.moneyRow);
        final LinearLayout carRow = (LinearLayout)layout.findViewById(R.id.carRow);
        final LinearLayout gamesRow = (LinearLayout)layout.findViewById(R.id.gamesRow);
        final LinearLayout booksRow = (LinearLayout)layout.findViewById(R.id.booksRow);
        final LinearLayout videoBorder = (LinearLayout)layout.findViewById(R.id.videoBorder);
        final LinearLayout audioBorder = (LinearLayout)layout.findViewById(R.id.audioBorder);
        final LinearLayout moneyBorder = (LinearLayout)layout.findViewById(R.id.moneyBorder);
        final LinearLayout carBorder = (LinearLayout)layout.findViewById(R.id.carBorder);
        final LinearLayout gamesBorder = (LinearLayout)layout.findViewById(R.id.gamesBorder);
        final LinearLayout booksBorder = (LinearLayout)layout.findViewById(R.id.booksBorder);

        // Setting OK and Cancel button listeners
        Button buttonOK = (Button)layout.findViewById(R.id.buttonOK);
        Button buttonCancel = (Button)layout.findViewById(R.id.buttonCancel);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (note.count) {
                    case 1:
                        selectedIcon.setImageResource(R.drawable.note_icon_1);
                        break;
                    case 2:
                        selectedIcon.setImageResource(R.drawable.note_icon_2);
                        break;
                    case 3:
                        selectedIcon.setImageResource(R.drawable.note_icon_3);
                        break;
                    case 4:
                        selectedIcon.setImageResource(R.drawable.note_icon_4);
                        break;
                    case 5:
                        selectedIcon.setImageResource(R.drawable.note_icon_5);
                        break;
                    case 6:
                        selectedIcon.setImageResource(R.drawable.note_icon_6);
                        break;
                }
                selectedBorder.setVisibility(View.VISIBLE);
                selectIconDialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionNoRadio.setImageResource(R.drawable.radio_checked_medium);
                optionYesRadio.setImageResource(R.drawable.radio_unchecked_medium);
                selectedBorder.setVisibility(View.GONE);
                note.count = 0;
                selectIconDialog.dismiss();
            }
        });

        // Setting icons select onClickListeners
        videoRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 1)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        videoRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        videoRow.setBackgroundColor(0x00000000);
                        note.count = 1;
                        videoBorder.setBackgroundResource(R.drawable.border_big_selected);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        audioRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 2)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        audioRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        audioRow.setBackgroundColor(0x00000000);
                        note.count = 2;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_big_selected);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        moneyRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 3)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        moneyRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        moneyRow.setBackgroundColor(0x00000000);
                        note.count = 3;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_big_selected);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        carRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 4)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        carRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        carRow.setBackgroundColor(0x00000000);
                        note.count = 4;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_big_selected);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        gamesRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 5)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        gamesRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        gamesRow.setBackgroundColor(0x00000000);
                        note.count = 5;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_big_selected);
                        booksBorder.setBackgroundResource(R.drawable.border_small);
                        break;
                }
                return true;
            }
        });
        booksRow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (note.count == 6)
                    return true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        booksRow.setBackgroundColor(0x88CCCCCC);
                        break;
                    case MotionEvent.ACTION_UP:
                        booksRow.setBackgroundColor(0x00000000);
                        note.count = 6;
                        videoBorder.setBackgroundResource(R.drawable.border_small);
                        audioBorder.setBackgroundResource(R.drawable.border_small);
                        moneyBorder.setBackgroundResource(R.drawable.border_small);
                        carBorder.setBackgroundResource(R.drawable.border_small);
                        gamesBorder.setBackgroundResource(R.drawable.border_small);
                        booksBorder.setBackgroundResource(R.drawable.border_big_selected);
                        break;
                }
                return true;
            }
        });

        // Setting selected element
        switch (note.count) {
            case 1:
                videoBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 2:
                audioBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 3:
                moneyBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 4:
                carBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 5:
                gamesBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 6:
                booksBorder.setBackgroundResource(R.drawable.border_big_selected);
                break;
            case 0:
                videoBorder.setBackgroundResource(R.drawable.border_big_selected);
                note.count = 1;
                break;
        }

        // Showing
        selectIconDialog.show();
    }

}
