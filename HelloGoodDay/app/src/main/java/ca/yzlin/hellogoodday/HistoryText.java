package ca.yzlin.hellogoodday;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class HistoryText extends AppCompatActivity {
    private SharedPreferences props;
    private SharedPreferences descrip;
    private ArrayList<HGDEntry> entries = new ArrayList<HGDEntry>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_text);
        props = getSharedPreferences(MainActivity.SAVED_PROPS_FILE, 0);
        descrip = getSharedPreferences(MainActivity.SAVED_DESCRIP_FILE, 0);

        LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout);
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int size = props.getInt("indexAt", -1);
        if(size==-1 || size==0){
            TextView tv = new TextView(this);
            tv.setLayoutParams(lparams);
            tv.setText("Nothing's been saved yet!");
            layout.addView(tv);
        }
        else{
            for(int i=0; i<(size+1); i++){
                String fields = props.getString(String.valueOf(i), "");
                if(fields=="") continue;
                String[] arg = fields.split(" ");
                if(arg.length < 3) continue;
                String longi = "0";
                String lati = "0";
                if(arg.length>=4) longi = arg[3];
                if(arg.length>=5) lati = arg[4];
                String descripId = arg[1];
                String text = descrip.getString(descripId,"");
                HGDEntry ent = new HGDEntry(arg[0], text, arg[2], longi, lati);
                entries.add(ent);
                TextView tv = new TextView(this);
                tv.setLayoutParams(lparams);
                tv.setText(ent.getText());
                layout.addView(tv);
            }
        }
    }
}
