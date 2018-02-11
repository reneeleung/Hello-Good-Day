package ca.yzlin.hellogoodday;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

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

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HistoryText extends FragmentActivity implements OnMapReadyCallback{
    private SharedPreferences props;
    private SharedPreferences descrip;
    private ArrayList<HGDEntry> entries = new ArrayList<HGDEntry>();
    private GoogleMap mMap;

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
                String displayText = ent.getText() + "\n\tDate: " + ent.getDate() + "\n\n";

                TextView tv = new TextView(this);
                tv.setLayoutParams(lparams);
                tv.setText(displayText);
                layout.addView(tv);
            }
        }

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void changeView(View view){
        ToggleButton tb = (ToggleButton) findViewById(R.id.view_btn);
        ScrollView sv = (ScrollView) findViewById(R.id.scroll_view);
        View map = findViewById(R.id.map);
        if(tb.isChecked()) {
            sv.setVisibility(View.VISIBLE);
            map.setVisibility(View.INVISIBLE);
        }
        else{

            sv.setVisibility(View.INVISIBLE);
            map.setVisibility(View.VISIBLE);
        }

    }

    public void refreshSets(){
        entries.clear();
        props = getSharedPreferences(MainActivity.SAVED_PROPS_FILE, 0);
        descrip = getSharedPreferences(MainActivity.SAVED_DESCRIP_FILE, 0);
        int size = props.getInt("indexAt", -1);
        if(size>0){
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
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int size = entries.size();
        for(int i=0; i<size; i++){
            LatLng latLng;
            if(!entries.get(i).getHasLocation()) latLng = new LatLng(43.09078, -77.67489);
            else latLng = entries.get(i).getLocation();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(entries.get(i).getText());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.addMarker(markerOptions);
        }

    }
}
