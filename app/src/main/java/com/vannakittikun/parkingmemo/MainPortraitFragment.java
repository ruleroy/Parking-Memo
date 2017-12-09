package com.vannakittikun.parkingmemo;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Created by Rule on 12/7/2017.
 */

public class MainPortraitFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final int RESULT_OK = 1;
    private static final int RESULT_CANCELED = 0;

    private final int MY_LOCATION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private MyDBHandler myDBHandler;

    private LocationRequest mLocationRequest;

    private Location currentLocation;

    private RatingBar ratingBar;
    private ImageButton parkHere;
    private TextView accuracyRating;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private ImageButton removeHere;
    private TextView address;
    private Button dbg;
    private Button addNote;
    private Button addImage;
    private TextView notes;
    private ImageView carImage;
    private ImageButton locateCar;
    private ImageButton startCompass;
    Fragment compass;
    Marker carMarker;

    Geocoder geocoder;

    GetGeoLocation getGeoLocation;

    private int currentSession;
    private boolean parked;
    private boolean compassOn = false;
    private String note = "";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        myDBHandler = new MyDBHandler(getActivity());

        ratingBar = getActivity().findViewById(R.id.ratingBar);
        accuracyRating = getActivity().findViewById(R.id.accuracyRating);
        address = getActivity().findViewById(R.id.address);
        notes = getActivity().findViewById(R.id.notes);
        locateCar = getActivity().findViewById(R.id.locateCar);

        locateCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = myDBHandler.getParkingLatLng(currentSession);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
        });

        carImage = getActivity().findViewById(R.id.carImage);
        carImage.setDrawingCacheEnabled(true);
        if (myDBHandler.isStillParking()) {
            currentSession = myDBHandler.getCurrentParkingSession();
            Float accuracy = myDBHandler.getParkingAccuracy(currentSession);

            parked = true;
            parkHere.setVisibility(View.GONE);
            locateCar.setVisibility(View.VISIBLE);
            slidingUpPanelLayout.setPanelHeight(500);
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            address.setText(myDBHandler.getParkingAddress(currentSession));
            ratingBar.setRating(accuracy);
            accuracyRating.setText("(" + accuracy + ")");
            notes.setText(myDBHandler.getParkingNote(currentSession));
            carImage.setImageBitmap(myDBHandler.getParkingImage(currentSession));
            carImage.setAdjustViewBounds(true);

        } else {
            parked = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_portrait, container, false);
        dbg = view.findViewById(R.id.dbg);
        dbg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent dbmanager = new Intent(getActivity(), AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });

        slidingUpPanelLayout = view.findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setOverlayed(true);
        slidingUpPanelLayout.setPanelHeight(500);
        if (!parked) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        parkHere = view.findViewById(R.id.parkHere);
        parkHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parkHere(view);
            }
        });

        removeHere = view.findViewById(R.id.unparkHere);
        removeHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parkHere(view);
            }
        });

        addImage = view.findViewById(R.id.addImage);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.pickImage(getActivity(), MainPortraitFragment.this);
            }
        });

        addNote = view.findViewById(R.id.addNote);
        addNote.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add note");

                final EditText input = new EditText(getActivity());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note = input.getText().toString();
                        notes.setText(note);
                        myDBHandler.updateNote(currentSession, note);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        compass = new CompassFragment();
        startCompass = view.findViewById(R.id.startCompass);
        startCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(compassOn){
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().remove(compass).commit();
                    compassOn = false;
                } else {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().add(R.id.frameContainer, compass).commit();
                    compassOn = true;
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_PICK) {
                ImagePicker.beginCrop(this, getActivity(), resultCode, data);
            } else if (requestCode == ImagePicker.REQUEST_CROP) {
                Bitmap bitmap = ImagePicker.getImageCropped(getActivity(), resultCode, data, ImagePicker.ResizeType.FIXED_SIZE, 400);
                carImage.setImageBitmap(bitmap);
                carImage.setAdjustViewBounds(true);
                myDBHandler.updateImage(currentSession, bitmap);
                Log.d("IMAGE PICKER", "bitmap picked: " + bitmap);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void parkHere(View view) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
            return;
        } else {
            if (parked) {
                myDBHandler.unparkAll();
                notes.setText("None");
                parked = false;
                parkHere.setVisibility(View.VISIBLE);
                locateCar.setVisibility(View.GONE);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                carMarker.remove();
                carImage.setImageResource(R.color.common_google_signin_btn_text_dark_disabled);

            } else {
                parked = true;
                notes.setText("None");
                myDBHandler.unparkAll();

                locateCar.setVisibility(View.VISIBLE);
                mFusedLocationClient.removeLocationUpdates(parkCallback);
                mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, parkCallback, Looper.myLooper());

                parkHere.setVisibility(View.GONE);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
            return;
        } else {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, firstStartCallback, Looper.myLooper());
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        }
    }

    LocationCallback firstStartCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivityFirst", "Location: " + location.getLatitude() + " " + location.getLongitude());
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("MAP_ACCURACY", Float.toString(location.getAccuracy()));
                currentLocation = location;
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            }
            mFusedLocationClient.removeLocationUpdates(firstStartCallback);
        }

    };

    LocationCallback parkCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivityFirst", "Location: " + location.getLatitude() + " " + location.getLongitude());
                Float accuracy = getAccuracyRating(location.getAccuracy());
                myDBHandler.addParking(location.getLatitude(), location.getLongitude(), accuracy);
                currentSession = myDBHandler.getCurrentParkingSession();

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("MAP_ACCURACY", Float.toString(location.getAccuracy()));
                currentLocation = location;
                //Toast.makeText(getActivity(), "Accuracy: " + location.getAccuracy() + " Accuracy Rating: " + getAccuracyRating(location.getAccuracy()), Toast.LENGTH_SHORT).show();

                ratingBar.setRating(accuracy);
                accuracyRating.setText("(" + accuracy + ")");

                new GetGeoLocation(new GetGeoLocationResponse() {

                    @Override
                    public void onTaskDone(String responseData) {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        final String title = responseData;
                        carMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(responseData).draggable(true));
                        address.setText(responseData);

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                myDBHandler.updateAddress(currentSession, title);
                                myDBHandler.updateImage(currentSession, carImage.getDrawingCache());
                            }
                        };

                        new Thread(r).start();
                    }

                    @Override
                    public void onError() {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        carMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("").draggable(true));
                    }
                }).execute("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyBCBB0Ra9XUKV8XpfIvNfJKAige-D0dLMI&latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=true");

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

            }
            mFusedLocationClient.removeLocationUpdates(parkCallback);
        }

    };


    public float getAccuracyRating(float acc) {
        DecimalFormat df = new DecimalFormat("#.#");
        float accuracy = ((100 + (100 * (-acc)) / 100) * 5) / 100;

        if (acc > 100) {
            return 0;
        }

        return Float.parseFloat(df.format(accuracy));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Failed to connect");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                        System.exit(0);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
    }

    public void setupMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
            return;
        } else {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);

            mMap.getUiSettings().setCompassEnabled(false);

            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    Log.d("System out", "onMarkerDragStart..." + marker.getPosition().latitude + "..." + marker.getPosition().longitude);
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    Log.d("System out", "onMarkerDragEnd..." + marker.getPosition().latitude + "..." + marker.getPosition().longitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }

                @Override
                public void onMarkerDragEnd(final Marker marker) {
                    Log.i("System out", "onMarkerDrag...");
                    LatLng latLng = marker.getPosition();
                    myDBHandler.updateParking(currentSession, latLng.latitude, latLng.longitude);
                    new GetGeoLocation(new GetGeoLocationResponse() {
                        @Override
                        public void onTaskDone(String responseData) {
                            String title = responseData;
                            marker.setTitle(title);
                            address.setText(title);
                            myDBHandler.updateAddress(currentSession, title);
                        }

                        @Override
                        public void onError() {
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            //carMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("").draggable(true));
                        }
                    }).execute("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyBCBB0Ra9XUKV8XpfIvNfJKAige-D0dLMI&latlng=" + latLng.latitude + "," + latLng.longitude + "&sensor=true");
                }
            });
            mGoogleApiClient.connect();

            if (myDBHandler.isStillParking()) {
                carMarker = mMap.addMarker(new MarkerOptions().position(myDBHandler.getParkingLatLng(currentSession)).title(myDBHandler.getParkingAddress(currentSession)).draggable(true));
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap();

                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setMessage("Please enable Location permission.");
                    builder1.setCancelable(false);

                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getActivity().finish();
                                    System.exit(0);
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                return;
            }
        }
    }


}
