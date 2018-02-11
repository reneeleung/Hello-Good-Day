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


public class AddText extends FragmentActivity implements OnMapReadyCallback {
    private boolean hasLocation;
    private double longitude;
    private double latitude;
    private Date dateTime;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh:mm:ss");

    private GoogleMap mMap;
    private Button b;
    EditText vibes;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        vibes=(EditText)findViewById(R.id.text_field);
        vibes.setText("");
        hasLocation = false;
        longitude = 0;
        latitude = 0;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        b = (Button)findViewById(R.id.save_btn);
        //   b=(Button)findViewById(R.id.click);
        // text=(TextView)findViewById(R.id.textView);

     /*   b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibes=(EditText)findViewById(R.id.text_field);
                // etemail=(EditText)findViewById(R.id.email);
                //   etpassword=(EditText)findViewById(R.id.password);
                // text.setText("Your Input: " + vibes.getText().toString());
            }
        });*/


        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void saveText(View view){
      //  vibes=(EditText)findViewById(R.id.text_field);
        // getShared preferences
        SharedPreferences props = getSharedPreferences(MainActivity.SAVED_PROPS_FILE, 0);
        SharedPreferences descrip = getSharedPreferences(MainActivity.SAVED_DESCRIP_FILE, 0);
        SharedPreferences.Editor propsEditor = props.edit();
        SharedPreferences.Editor descripEditor = descrip.edit();

        dateTime = Calendar.getInstance().getTime();
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
       // finish();
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


        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new OnMapClickListener() {

            public void onSearch(View view) {
                EditText location_tf = (EditText) findViewById(R.id.text_field);
                String location = location_tf.getText().toString();


            }

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);
                // etemail=(EditText)findViewById(R.id.email);
                //   etpassword=(EditText)findViewById(R.id.password);
                //   text.setText("Your Input: " + vibes.getText().toString());
                // to get Lat: latLng.latitude
                // to get Long: latLng.longitude
                hasLocation = true;
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(vibes.getText().toString());

                // Clears the previously touched position
                //.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

            }
        });
        /*

        // Add a marker in Sydney and move the camera
        LatLng curLocation = new LatLng(43.09078, -77.67489);
        mMap.addMarker(new MarkerOptions().position(curLocation).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
        */

    }
}
