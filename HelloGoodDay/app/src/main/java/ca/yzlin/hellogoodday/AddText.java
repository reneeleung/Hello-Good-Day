package ca.yzlin.hellogoodday;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddText extends AppCompatActivity {
    private boolean hasLocation;
    private float longitude;
    private float latitude;
    private Date dateTime;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        hasLocation = false;
        longitude = 0;
        latitude = 0;
        dateTime = Calendar.getInstance().getTime();
    }

    public void saveText(View view){
        // getShared preferences
        SharedPreferences props = getSharedPreferences(MainActivity.SAVED_PROPS_FILE, 0);
        SharedPreferences descrip = getSharedPreferences(MainActivity.SAVED_DESCRIP_FILE, 0);
        SharedPreferences.Editor propsEditor = props.edit();
        SharedPreferences.Editor descripEditor = descrip.edit();

        String text = ((EditText) findViewById(R.id.text_field)).getText().toString();
        String loca = "false";
        String longi = "0";
        String lati = "0";
        if(hasLocation){
            loca = "true";
            longi = String.valueOf(longitude);
            lati = String.valueOf(latitude);
        }
        String date = dateFormat.format(dateTime);

        int propId = props.getInt("indexAt", -1);
        propId++;
        propsEditor.putInt("indexAt", propId);
        int descripId = descrip.getInt("indexAt", -1);
        descripId++;
        descripEditor.putInt("indexAt", descripId);

        String fields = date + " " + String.valueOf(descripId) + " " + loca + " " + longi + " " + lati;
        propsEditor.putString(String.valueOf(propId), fields);
        descripEditor.putString(String.valueOf(descripId), text);

        // Commit edits
        propsEditor.commit();
        descripEditor.commit();
        finish();
    }
}
