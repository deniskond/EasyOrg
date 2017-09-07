package nosfie.easyorg.TaskList;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import nosfie.easyorg.R;

public class ShoppingList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting up view and hiding action bar
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

}