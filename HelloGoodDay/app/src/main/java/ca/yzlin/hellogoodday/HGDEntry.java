package ca.yzlin.hellogoodday;

/**
 * Created by Fabulous Unicorns on 2018-02-11.
 */

public class HGDEntry{
    private String date, hasLocation, longitude, latitude, text;
    HGDEntry(String date, String text, String hasLocation, String longitude, String latitude){
        this.date = date;
        this.hasLocation = hasLocation;
        this.longitude = longitude;
        this.latitude = latitude;
        this.text = text;
    }
    String getText(){
        return this.text;
    }
}
