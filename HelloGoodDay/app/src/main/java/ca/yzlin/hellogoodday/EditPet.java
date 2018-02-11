package ca.yzlin.hellogoodday;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class EditPet extends AppCompatActivity {
    private static ImageView imgview;
    private static Button save;
    private static Button nextPet;
    private int current_image;
    int[]images = {R.drawable.blue, R.drawable.blue_green, R.drawable.blue_pink,
            R.drawable.green, R.drawable.multicolour, R.drawable.yellow_blue, R.drawable.yellow_pink,
            R.drawable.yellow_blue, R.drawable.pink_green, R.drawable.yellow, R.drawable.pink, R.drawable.yellow_green};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet);
        buttonclick();
    }

    public void buttonclick(){
        imgview = (ImageView) findViewById(R.id.imageView);
        nextPet = (Button)findViewById(R.id.changePet);
        nextPet.setOnClickListener(
            new View.OnClickListener(){
                @Override
                public void onClick(View view){
              //      while(current_image < 12) {
                        current_image++;
                        current_image = current_image % images.length;
                        imgview.setImageResource(images[current_image]);
                  //      current_image++;
                  //  }
                }
            }
        );
    }
}
