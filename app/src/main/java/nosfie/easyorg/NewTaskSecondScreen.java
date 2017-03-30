package nosfie.easyorg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NewTaskSecondScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_second_screen);

        Button button_back = (Button)findViewById(R.id.buttonBack);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskSecondScreen.this, NewTaskFirstScreen.class);
                startActivity(intent);
            }
        });

        Button button_next = (Button)findViewById(R.id.buttonNext);
        button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewTaskSecondScreen.this, NewTaskThirdScreen.class);
                startActivity(intent);
            }
        });

    }

}