package com.example.raunak.nav;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyService extends Service{
    List<LatLng> list;
    MapsActivity map;
    private GoogleApiClient mGoogleApiClient;
    Location location;

    public MyService() {
        Log.i("inservice", "hey");

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    MapsActivity m;
    Boolean alarm=false;

    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        MapsActivity.mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

        NavigateTo n = new NavigateTo();
        String homeLoc = intent.getStringExtra("home");
        String destLoc = intent.getStringExtra("dest");
        n.execute(homeLoc,destLoc);

        LocationManager lm= (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 0, new LocationListener() {
            Ringtone r=null;

            @Override
            public void onLocationChanged(Location location) {
                //Toast.makeText(MyService.this,"Location Changed",Toast.LENGTH_LONG).show();
                LatLng l = new LatLng(location.getLatitude(), location.getLongitude());
                Log.i("fromservice", "hey "+l.toString());
                Bitmap b= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.nav);
                    android.support.v4.app.NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLargeIcon(b)
                                    .setContentTitle("YOU ARE GOING THE WRONG WAY !!!")
                                    .setContentText("check correct directions");
                    Intent resultIntent = new Intent(getApplicationContext(), MapsActivity.class);

// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    getApplicationContext(),
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );

                    mBuilder.setContentIntent(resultPendingIntent);
// Sets an ID for the notification
                    int mNotificationId = 001;
// Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.





                if(list!=null) {
                    if (PolyUtil.isLocationOnPath(l, list, true, 10)) {
                        if(alarm)
                        {
                            r.stop();
                            alarm=false;
                        }
                        Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "wrong", Toast.LENGTH_LONG).show();
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());

                        if(!alarm) {
                            try {

                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                                r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                                alarm=true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }



            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return 1;
        }




    Log.i("inservice","hey there");



    return START_NOT_STICKY ;
}



    class NavigateTo extends AsyncTask<String,Void,String> {

            String API_URL = "https://maps.googleapis.com/maps/api/directions/json?origin=";

            @Override
            protected String doInBackground(String... strings) {
                try {
                    String homeLoc = strings[0];
                    String destLoc = strings[1];
                    String urlstring = API_URL + homeLoc + "&destination=" + destLoc + "&key=AIzaSyDfKSsb63-HocswPNL5EGEUW54tfP6JDWI";
                    URL url = new URL(urlstring);
                    Log.i("URLSTRING", urlstring);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                    return null;
                }
            }

            protected void onPostExecute(String response) {
                Log.i("YO", response);
                if (response == null) {
                    response = "THERE WAS AN ERROR";
                }
                try {
                    JSONObject json = new JSONObject(response);
                    String code = json.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").get("points").toString();
                    list = PolyUtil.decode(code);
                    for (LatLng l : list) {
                        Log.i("line", l.toString());
                    }

                    Polyline line = MapsActivity.mMap.addPolyline(new PolylineOptions()
                            .addAll(list)
                            .width(10)
                            .color(Color.RED));


                } catch (Exception e) {
                    Log.e("YObitch", e.toString());
                }




            }
        }







}
