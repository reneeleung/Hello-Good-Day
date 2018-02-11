package ca.yzlin.hellogoodday;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fabulous Unicorns on 2018-02-11.
 */

public class HGDEntry{
    private String date, text;
    boolean hasLocation;
    double longitude, latitude;
    HGDEntry(String date, String text, String hasLocation, String longitude, String latitude){
        this.date = date;
        this.hasLocation=false;
        if(hasLocation.equals("true")) this.hasLocation=true;
    //    try{
            this.longitude = Double.parseDouble(longitude);
            this.latitude = Double.parseDouble(latitude);
     //   }catch(Exception e){
       //     this.hasLocation = false;
      //  }
        this.text = text;
    }
    String getText(){
        return this.text;
    }
    boolean getHasLocation(){return this.hasLocation;}
    LatLng getLocation() { return new LatLng(this.latitude, this.longitude);}
}
