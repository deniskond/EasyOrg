package nosfie.easyorg.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import nosfie.easyorg.DataStructures.Task;
import nosfie.easyorg.DataStructures.Timespan;
import nosfie.easyorg.R;
import nosfie.easyorg.TaskList.TaskList;
import nosfie.easyorg.TaskList.TaskView;

import static nosfie.easyorg.Database.Queries.getNotesFromDB;

public class Notes extends AppCompatActivity {

    LinearLayout buttonAdd, buttonClose, notesContainer, noNotes;
    ImageView buttonAddImage;
    ArrayList<Task> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Setting up view elements
        buttonAdd = (LinearLayout)findViewById(R.id.buttonAdd);
        buttonClose = (LinearLayout)findViewById(R.id.buttonClose);
        buttonAddImage = (ImageView)findViewById(R.id.buttonAddImage);
        notesContainer = (LinearLayout)findViewById(R.id.notesContainer);
        noNotes = (LinearLayout)findViewById(R.id.noNotes);

        // Setting navigation buttons onClickListeners
        buttonAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        buttonAddImage.setImageResource(R.drawable.plus_dark);
                        break;
                    case MotionEvent.ACTION_UP:
                        buttonAddImage.setImageResource(R.drawable.plus);
                        Intent intent = new Intent(Notes.this, AddNote.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Drawing notes
        drawNotes();
    }

    private void drawNotes() {
        notes.clear();
        notesContainer.removeAllViews();
        notes = getNotesFromDB(this);
        int num = 1;
        for (Task note: notes) {
            LinearLayout taskRow = TaskView.getTaskRow(
                    this, num, note, true, true, Timespan.MONTH, new Callable() {
                        @Override
                        public Object call() throws Exception {
                            drawNotes();
                            return null;
                        }
                    }, null);
            notesContainer.addView(taskRow);
            num++;
        }
        if (notes.size() == 0)
            noNotes.setVisibility(View.VISIBLE);
        else
            noNotes.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawNotes();
    }

}
