package ca.yzlin.hellogoodday;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HistoryText extends AppCompatActivity {
    private SharedPreferences props;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_text);
        props = getSharedPreferences(MainActivity.SAVED_PROPS_FILE, 0);

        LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int size = props.getInt("indexAt", -1);
        if(size==0){
            TextView tv = new TextView(this);
            tv.setLayoutParams(lparams);
            tv.setText("Nothing's been saved yet!");
            layout.addView(tv);
        }
        else{
            for(int i=0; i<(size+1); i++){

            }
        }
    }
}
