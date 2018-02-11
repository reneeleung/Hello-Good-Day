package ca.yzlin.hellogoodday;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String SAVED_PROPS_FILE = "ca.yzlin.HelloGoodDay.SavedDataProperties";
    public static final String BITMAP = "ca.yzlin.HelloGoodDay.bitmap";

    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST_CODE = 10001;

    private Bitmap bitmap;

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

    public void takePicture(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            Intent intent = new Intent(this, CloudVisionRequest.class);
            intent.putExtra(BITMAP, bitmap);
            startActivity(intent);
        }
    }
}
