package ca.yzlin.hellogoodday;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CloudVisionRequest extends AppCompatActivity {
    private static final String TAG = "CloudVisionRequest";
    private static final int RECORD_REQUEST_CODE = 10002;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyA5XvgRCsC4ZmTwZuUp_4QDSQqwwHccDII";

    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";

    Button takePicture;
    ProgressBar imageUploadProgress;
    //Spinner spinnerVisionAPI;
    TextView visionAPIData;
    ImageView imageView;

    private Feature feature;
    private Bitmap bitmap;
    private final String visionAPI = "LABEL_DETECTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_vision_request);
        imageView = (ImageView) findViewById(R.id.imageView);
        takePicture = (Button) findViewById(R.id.takePicture);
        imageUploadProgress = (ProgressBar) findViewById(R.id.imageUploadProgress);
        imageUploadProgress.setVisibility(View.GONE);
        visionAPIData = (TextView) findViewById(R.id.visionAPIData);

        feature = new Feature();
        feature.setType(visionAPI);
        feature.setMaxResults(5);

        bitmap = (Bitmap) getIntent().getParcelableExtra(MainActivity.BITMAP);
        imageView.setImageBitmap(bitmap);
        //callCloudVision(bitmap, feature);
        try {
            callCloudVision(bitmap);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "Image picking failed because " + e.getMessage());
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
            takePicture.setVisibility(View.VISIBLE);
        } else {
            takePicture.setVisibility(View.INVISIBLE);
            makeRequest(android.Manifest.permission.CAMERA);
        }
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
                    labels.add(objects[i].substring(0, objects[i].lastIndexOf(' ')));
                }

                String pun = "";
                for (int i = 0; i < labels.size(); ++i) {
                    pun = pun + labels.get(i) + "; ";
                }
                visionAPIData.setText(pun);
                imageUploadProgress.setVisibility(View.GONE);
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
