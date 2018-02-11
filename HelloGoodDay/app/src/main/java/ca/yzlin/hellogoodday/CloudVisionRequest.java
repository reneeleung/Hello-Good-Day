package ca.yzlin.hellogoodday;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CloudVisionRequest extends AppCompatActivity {
    private static final String TAG = "CloudVisionRequest";
    private static final int CAMERA_REQUEST_CODE = 10001;
    private static final int RECORD_REQUEST_CODE = 10002;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyA5XvgRCsC4ZmTwZuUp_4QDSQqwwHccDII";
    public static final String BITMAP = "ca.yzlin.HelloGoodDay.bitmap";

    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";

    ProgressBar imageUploadProgress;
    TextView visionAPIData;
    ImageView imageView;

    private AssetManager assetManager;

    private Feature feature;
    private Bitmap bitmap;
    private final String visionAPI = "LABEL_DETECTION";

    private DatabaseReference mDatabase;
    public static String pun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_vision_request);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageUploadProgress = (ProgressBar) findViewById(R.id.imageUploadProgress);
        imageUploadProgress.setVisibility(View.GONE);
        visionAPIData = (TextView) findViewById(R.id.visionAPIData);

        assetManager = getAssets();

        feature = new Feature();
        feature.setType(visionAPI);
        feature.setMaxResults(5);

        bitmap = (Bitmap) getIntent().getParcelableExtra(MainActivity.BITMAP);
        imageView.setImageBitmap(bitmap);
        //callCloudVision(bitmap, feature);

        mDatabase = FirebaseDatabase.getInstance().getReference("data");

        try {
            callCloudVision(bitmap);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent objEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, objEvent);
    }

    @Override
    public void onBackPressed() {
        //finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        return;
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

    private void makeRequest(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, RECORD_REQUEST_CODE);
    }

    private int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //takePicture.setVisibility(View.VISIBLE);
        } else {
            //takePicture.setVisibility(View.INVISIBLE);
            makeRequest(android.Manifest.permission.CAMERA);
        }
    }

    private void writePun(String label, String quote) {
        mDatabase.child("puns").child(label).setValue(quote);
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        visionAPIData.setText("loading...");
        imageUploadProgress.setVisibility(View.VISIBLE);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                // find pun for each label / one with highest score
                String [] objects = result.split("\n");
                ArrayList<String> labels = new ArrayList<String>();
                for (int i = 0; i < objects.length; ++i) {
                    String label = objects[i].substring(0, objects[i].lastIndexOf(' '));
                    label = label.replaceAll("\\s+","");
                    labels.add(label);
                }

                //HashMap<String, String> database = new HashMap<String, String>();

                try {
                    InputStream is = assetManager.open("puns.txt");
                    Log.d(TAG, "found puns.txt in assets");
                    try {
                        String line = "";
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        if (is != null) {
                            while ((line = reader.readLine()) != null) {
                                String[] pair = line.split("\\|");
                                //database.put(pair[0],pair[1]);
                                writePun(pair[0],pair[1]);
                            }
                        }
                        Log.d(TAG, "input stream is null");
                    } finally {
                        try {is.close(); } catch (Throwable ignore) {}
                    }
                } catch (IOException e) {
                    Log.d(TAG, "File reading failed because " + e.getMessage());
                }

                pun = "";
                for (int i = 0; i < labels.size(); ++i) {
                    final String label_to_query = labels.get(i);
                    Log.d(TAG,"looking for label: " + label_to_query);
                    mDatabase.child("puns").orderByKey().equalTo(label_to_query).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                if (dataSnapshot.getValue() != null) {
                                    try {
                                        Log.d(TAG, ""+dataSnapshot.getValue());
                                        String str = dataSnapshot.getValue().toString();
                                        String[] result = str.split("=");
                                        Log.d(TAG, label_to_query +": " + result[1].substring(0, result[1].length()-1));
                                        pun = pun + label_to_query +": " + result[1].substring(0, result[1].length()-1) + "\n";
                                        Log.d(TAG, "pun is now updated: " + pun);

                                        if (pun == "") pun = "Sorry, no puns for your image. Please try again.";
                                        visionAPIData.setText(pun);
                                        imageUploadProgress.setVisibility(View.GONE);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.d(TAG, "Firebase query result is null");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d("onCancelled", " cancelled");
                        }
                    });
                    /*String var = database.get(labels.get(i));
                    if (var != null) {
                        pun += labels.get(i) +": " + var + "\n";
                    }*/
                }
                /*if (pun == "") pun = "Sorry, no puns for your image. Please try again.";
                visionAPIData.setText(pun);
                imageUploadProgress.setVisibility(View.GONE);*/
            }
        }.execute();
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotations;

        String message = "";
        entityAnnotations = imageResponses.getLabelAnnotations();
        message = formatAnnotation(entityAnnotations);
        return message;
    }

    private String formatAnnotation(List<EntityAnnotation> entityAnnotation) {
        String message = "";

        if (entityAnnotation != null) {
            for (EntityAnnotation entity : entityAnnotation) {
                message = message + "    " + entity.getDescription() + " " + entity.getScore();
                message += "\n";
            }
        } else {
            message = "Nothing Found";
        }
        return message;
    }
}
