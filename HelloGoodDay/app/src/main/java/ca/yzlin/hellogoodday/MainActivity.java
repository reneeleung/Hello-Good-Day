package ca.yzlin.hellogoodday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String SAVED_PROPS_FILE = "ca.yzlin.HelloGoodDay.SavedDataProperties";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addText(View view){
        Intent intent = new Intent(this, AddText.class);
        startActivity(intent);
    }

    public void displayHistory(View view){
        Intent intent = new Intent(this, HistoryText.class);
        startActivity(intent);
    }
}
