package com.example.android.camera2basic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private FusedLocationProviderClient client;
    private Button UploadBn, ChooseBn, PictureBn;
    private EditText NAME;
    private ImageView imgView;
    private final int IMG_REQUEST = 1;
    Bitmap bitmap;
    //variable for server url
    private String UploadUrl = "http://192.168.137.1/upload.php";
    private float mheight, mwidth, focal_length, azimuth, pitch, roll;
    private double latitude1 , longitude1 ;
    String latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent myintent = getIntent();
        mheight = myintent.getFloatExtra("mheight", 0);
        mwidth = myintent.getFloatExtra("mwidth", 0);
        focal_length = myintent.getFloatExtra("focal_length", 0);
        azimuth = myintent.getFloatExtra("azimuth", 0);
        pitch = myintent.getFloatExtra("pitch", 0);
        roll = myintent.getFloatExtra("roll", 0);


        requestPermission();

        client = LocationServices.getFusedLocationProviderClient(this);


        UploadBn = (Button) findViewById(R.id.uploadBn);
        //PictureBn = (Button)findViewById(R.id.pictureBn);
        ChooseBn = (Button) findViewById(R.id.chooseBn);
        NAME = (EditText) findViewById(R.id.name);
        imgView = (ImageView) findViewById(R.id.imageView);
        ChooseBn.setOnClickListener(this);
        UploadBn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseBn: {
                selectImage();

                break;
            }

            case R.id.uploadBn: {
                uploadImage();
                break;
            }

        }
    }





    private void selectImage() {


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                    latitude1 = location.getLatitude();
                    latitude = Double.toString(latitude1);
                    longitude1 = location.getLongitude();
                    longitude = Double.toString(longitude1);
                }


            }

        });


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                imgView.setImageBitmap(bitmap);
                imgView.setVisibility(View.VISIBLE);
                NAME.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {


    StringRequest stringRequest=new StringRequest(Request.Method.POST, UploadUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject reader = new JSONObject(response);
                            String Response = reader.getString("message");
                            Toast.makeText(MainActivity.this, Response, Toast.LENGTH_LONG).show();
                            imgView.setImageResource(0);
                            imgView.setVisibility(View.GONE);
                            NAME.setText("");
                            NAME.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext(),"error:"+ error.toString(),Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image_name", NAME.getText().toString().trim());
                params.put("image", imageToString(bitmap));
                params.put("sensor_size_height", String.valueOf(mheight));
                params.put("sensor_size_width", String.valueOf(mwidth));
                params.put("focal_length", String.valueOf(focal_length));
                params.put("azimuth", String.valueOf(azimuth));
                params.put("pitch", String.valueOf(pitch));
                params.put("roll", String.valueOf(roll));
                params.put("latitude", latitude);
                params.put("longitude", longitude);

                return params;
            }
        };stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 240000;
            }

            @Override
            public int getCurrentRetryCount() {

                return 360000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }

    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);

    }

   /* private void mylocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {

                     latitude1 = location.getLatitude();
                    String lat = Double.toString(latitude1);
                     longitude1 = location.getLongitude();
                    String lon = Double.toString(longitude1);
                     altitude1 = location.getAltitude();
                    String alt = Double.toString(altitude1);

                }

            }

        });


    }*/




    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }





}

