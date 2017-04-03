package com.example.raunak.nav;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

/**
 * Created by raunak on 2/4/17.
 */
public class Category  implements Serializable {
     private List<LatLng> objects;
    transient private Thread myThread;
    public Category(List<LatLng> d) {
        objects=d;
    }






    public List<LatLng> getObjects() {
        return objects;
    }







}