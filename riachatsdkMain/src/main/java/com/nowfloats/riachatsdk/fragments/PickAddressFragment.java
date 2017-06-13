package com.nowfloats.riachatsdk.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.nowfloats.riachatsdk.R;
import com.nowfloats.riachatsdk.services.FetchAddressIntentService;
import com.nowfloats.riachatsdk.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

/**
 * Created by NowFloats on 27-03-2017 by Romio Ranjan Jena.
 */

public class PickAddressFragment extends DialogFragment implements LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private TextInputEditText etStreetAddr, etCountry, etPin, etLocality, etHousePlotNum, etLandmark;

    private Button btnSave;

    private AutoCompleteTextView etCity;

    private TextView tvAddress;

    private LinearLayout llManual, llUseGPS;

    private GoogleMap mGoogleMap;

    private AddressResultReceiver mResultReceiver;

//    protected String mAddressOutput;
//    protected String mAreaOutput;
//    protected String mCityOutput;
//    protected String mStateOutput;
//    protected String mCountryOutput;
//    protected String mPin;
//    protected String mLocality;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private LocationManager mLocationManager;

    private LatLng mCenterLatLong;

    private OnResultReceive mResultListener;

    private static final String ARG_MAP_TYPE = "map_type";

    private PICK_TYPE pick_type;

    private HashMap<String, String> Country_CodeMap = new HashMap<>();

    private AutocompleteFilter filter;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> signUpCountryList = new ArrayList<>();
    private List<String> citys = new ArrayList<>();

    private GoogleApiClient mGoogleApiClient;

    public static PickAddressFragment newInstance(PICK_TYPE pick_type) {

        PickAddressFragment fragment = new PickAddressFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MAP_TYPE, pick_type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public enum PICK_TYPE {
        USE_GPS,
        MANUAL
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pick_type = (PICK_TYPE) getArguments().get(ARG_MAP_TYPE);
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pick_address, container, false);
        setCancelable(false);

        llManual = (LinearLayout) v.findViewById(R.id.llManual);
        llUseGPS = (LinearLayout) v.findViewById(R.id.llUseGPS);
        btnSave = (Button) v.findViewById(R.id.btn_save);

        llManual.setVisibility(View.VISIBLE);
        btnSave.setText(getActivity().getResources().getString(R.string.locate_on_map));

        etCity = (AutoCompleteTextView) v.findViewById(R.id.et_city);
        etStreetAddr = (TextInputEditText) v.findViewById(R.id.et_street_address);
        etCountry = (TextInputEditText) v.findViewById(R.id.et_country);
        etPin = (TextInputEditText) v.findViewById(R.id.et_pincode);
        etLocality = (TextInputEditText) v.findViewById(R.id.et_locality);
        etHousePlotNum = (TextInputEditText) v.findViewById(R.id.et_house_plot_num);
        etLandmark = (TextInputEditText) v.findViewById(R.id.et_landmark);
        tvAddress = (TextView) v.findViewById(R.id.tvAddress);

        btnSave = (Button) v.findViewById(R.id.btn_save);

        llManual.bringToFront();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (verifyData()) {
                    btnSave.setVisibility(View.INVISIBLE);
                    if (btnSave.getText().toString().equalsIgnoreCase(getResources().getString(R.string.locate_on_map))) {

                        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                getResources().getDisplayMetrics().heightPixels - 100);

//                        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
//                        llManual.setAnimation(animation);
                        llManual.setVisibility(View.GONE);
                        llUseGPS.setVisibility(View.VISIBLE);
                        btnSave.setText(getResources().getString(R.string.done));
                        if (pick_type == PICK_TYPE.MANUAL) {
                                    reverseGeoCode();
                        }

//                        animation.setAnimationListener(new Animation.AnimationListener() {
//                            @Override
//                            public void onAnimationStart(Animation animation) {
////
//                                Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
//                                fadeIn.setDuration(3000);
//                                llUseGPS.setAnimation(fadeIn);
//                                fadeIn.setAnimationListener(new Animation.AnimationListener() {
//                                    @Override
//                                    public void onAnimationStart(Animation animation) {
//
//                                    }
//
//                                    @Override
//                                    public void onAnimationEnd(Animation animation) {
//                                        btnSave.setText(getResources().getString(R.string.done));
//                                    }
//
//                                    @Override
//                                    public void onAnimationRepeat(Animation animation) {
//
//                                    }
//                                });
//                                fadeIn.start();
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animation animation) {
//                                llManual.setVisibility(View.GONE);
//                                if (pick_type == PICK_TYPE.MANUAL) {
//                                    reverseGeoCode();
//                                }
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animation animation) {
//
//                            }
//                        });
//                        animation.start();

                    } else {

                        if (mResultListener != null) {

                            mResultListener.OnResult(etStreetAddr.getText().toString().trim(),
                                    etLocality.getText().toString(), etCity.getText().toString(), "", etCountry.getText().toString(), mCenterLatLong.latitude, mCenterLatLong.longitude,
                                    etPin.getText().toString(),
                                    etHousePlotNum.getText().toString(), etLandmark.getText().toString());

                            Fragment fragment = ((AppCompatActivity) getActivity())
                                    .getSupportFragmentManager()
                                    .findFragmentById(R.id.map);

                            if (fragment != null) {
                                ((AppCompatActivity) getActivity())
                                        .getSupportFragmentManager().beginTransaction()
                                        .remove(fragment)
                                        .commit();
                            }
                        }
                    }
                }

            }
        });

        mResultReceiver = new AddressResultReceiver(new Handler());

        try {
            // Loading map
            initMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.place_pick_dialog_bg);
        } catch (Exception e) {
            e.printStackTrace();
        }


        etStreetAddr.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    reverseGeoCode();
                }
                return false;
            }
        });

        if (pick_type == PICK_TYPE.MANUAL) {

            btnSave.setVisibility(View.VISIBLE);

            if (mGoogleApiClient == null) {

                mGoogleApiClient = new GoogleApiClient
                        .Builder(getActivity())
                        .addApi(Places.GEO_DATA_API)
                        .addApi(Places.PLACE_DETECTION_API)
                        .enableAutoManage((FragmentActivity) getActivity(), this)
                        .build();
            }

            loadCountries();

            etCountry.setFocusable(false);
            etCountry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCountry();
                }
            });

            etCity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //ArrayList<String> citys=new ArrayList<String>();
                    //cityEditText.setAdapter(null);
                    try {
                        String country_code = null;
                        if (Country_CodeMap != null) {
                            country_code = Country_CodeMap.get(etCity.getText().toString());
                        }
                        makeAutoCompleteFilter(country_code);

                        final PendingResult<AutocompletePredictionBuffer> result =
                                Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, etCity.getText().toString().trim(),
                                        null, filter);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AutocompletePredictionBuffer a = result.await();
                                //Log.v("ggg","ok");
                                citys.clear();
                                for (int i = 0; i < a.getCount(); i++) {
                                    //Log.v("ggg",a.get(i).getFullText(new StyleSpan(Typeface.NORMAL)).toString()+" length "+citys.size());
                                    citys.add(a.get(i).getPrimaryText(new StyleSpan(Typeface.NORMAL)).toString());
                                }

                                a.release();

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter = new ArrayAdapter<>(getActivity(),
                                                android.R.layout.simple_dropdown_item_1line, citys);
                                        if (!getActivity().isFinishing()) {
                                            etCity.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                            }
                        }).start();
                    } catch (Exception e) {

                    }
                }
            });
        } else {
            btnSave.setVisibility(View.GONE);
        }

        return v;
    }

    private void makeAutoCompleteFilter(String country_code) {

        filter = null;
        AutocompleteFilter.Builder builder = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES);

        if (country_code != null) {
            builder.setCountry(country_code.toUpperCase());
        }
        filter = builder.build();

    }

    private void reverseGeoCode() {

        String content = "";
        Geocoder gc = new Geocoder(getActivity());
        if (gc.isPresent()) {
            List<Address> list = null;
            try {
                Address address = null;
                list = gc.getFromLocationName(etStreetAddr.getText().toString() + ","
                        + etPin.getText().toString() + "," + etCity.getText().toString() + "," + etCountry.getText().toString(), 10);

                double lat = 0, lng = 0;
                if (list != null && list.size() > 0) {

                    address = list.get(0);
                    lat = address.getLatitude();
                    lng = address.getLongitude();
                    /*for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {

                        content += address.getAddressLine(i)+" ";
                    }*/
//                    if (!TextUtils.isEmpty(content)) {

//                    etStreetAddr.setText(content);
//                    etStreetAddr.setSelection(etStreetAddr.getText().toString().length());
//
//                    etPin.setText(list.get(0).getPostalCode());

                    LatLng latLong = new LatLng(lat, lng);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLong).zoom(18f).build();
                    mGoogleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
//                    }
                }

                displayAddressOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean verifyData() {
        if (etStreetAddr.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.empty_address_warn), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etCity.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.empty_city_warn), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etCountry.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.empty_country_warn), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etPin.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.empty_pin_warn), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etLocality.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.locality_warn), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etHousePlotNum.getText().toString().trim().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.house_no_warn), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initMap() {
        if (mGoogleMap == null) {
            ((SupportMapFragment) ((AppCompatActivity) getActivity())
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.map))
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mGoogleMap = googleMap;
                            if (ActivityCompat.checkSelfPermission(getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                                    PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(getActivity(),
                                            Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                            PackageManager.PERMISSION_GRANTED) {
                                mGoogleMap.setMyLocationEnabled(true);
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                            }

                            if (mGoogleMap != null) {
                                mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

                                if (ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(getActivity(),
                                                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                                PackageManager.PERMISSION_GRANTED) {
                                    mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, PickAddressFragment.this);
                                }

                                if (ActivityCompat.checkSelfPermission(getActivity(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(getActivity(),
                                                Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                                PackageManager.PERMISSION_GRANTED) {
                                    mGoogleMap.setMyLocationEnabled(true);
                                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                                }

                            } else {
                                Toast.makeText(getActivity(),
                                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                                        .show();
                            }
                            setCameraChangeListener();
                            fetchAddress();
                        }
                    });


        } else {
            setCameraChangeListener();
        }

    }

    public void setResultListener(OnResultReceive onResultReceive) {
        mResultListener = onResultReceive;
    }

    private void setCameraChangeListener() {
        mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mCenterLatLong = mGoogleMap.getCameraPosition().target;
                mGoogleMap.clear();
                btnSave.setVisibility(View.VISIBLE);

            }
        });
    }

    private void fetchAddress() {

        try {

            if(mCenterLatLong!=null){

                Location mLocation = new Location("");
                mLocation.setLatitude(mCenterLatLong.latitude);
                mLocation.setLongitude(mCenterLatLong.longitude);

                startIntentService(mLocation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startIntentService(Location mLocation) {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);

        intent.putExtra(Constants.LocationConstants.RECEIVER, mResultReceiver);

        intent.putExtra(Constants.LocationConstants.LOCATION_DATA_EXTRA, mLocation);

        getActivity().startService(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(18f).build();
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mLocationManager.removeUpdates(this);

        if (pick_type == PICK_TYPE.USE_GPS) {
            mCenterLatLong = latLng;
            fetchAddress();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
//            mAddressOutput = resultData.getString(Constants.LocationConstants.RESULT_DATA_KEY);
//            mAreaOutput = resultData.getString(Constants.LocationConstants.LOCATION_DATA_AREA);
//            mCityOutput = resultData.getString(Constants.LocationConstants.LOCATION_DATA_CITY);
//            mStateOutput = resultData.getString(Constants.LocationConstants.LOCATION_DATA_STREET);
//            mCountryOutput = resultData.getString(Constants.LocationConstants.LOCATION_DATA_COUNTRY);
//            mPin = resultData.getString(Constants.LocationConstants.LOCATION_DATA_PIN);
//            mLocality = resultData.getString(Constants.LocationConstants.LOCATION_DATA_LOCALITY);

//            displayAddressOutput();

            etCity.setText(resultData.getString(Constants.LocationConstants.LOCATION_DATA_CITY));
            etCountry.setText(resultData.getString(Constants.LocationConstants.LOCATION_DATA_COUNTRY));

            if (!TextUtils.isEmpty(resultData.getString(Constants.LocationConstants.LOCATION_DATA_STREET))) {
                etStreetAddr.setText(resultData.getString(Constants.LocationConstants.LOCATION_DATA_STREET).replaceAll("[\r\n]+", " ") + "");
            }

            etStreetAddr.setSelection(etStreetAddr.getText().toString().length());

            etPin.setText(resultData.getString(Constants.LocationConstants.LOCATION_DATA_PIN));
            etLocality.setText(resultData.getString(Constants.LocationConstants.LOCATION_DATA_LOCALITY));


            displayAddressOutput();

        }

    }

    protected void displayAddressOutput() {


        String address = etHousePlotNum.getText().toString() + ", " + etLocality.getText().toString()+", "+etLandmark.getText().toString() + ", " +
                etStreetAddr.getText().toString().trim() + ", " +
                etCity.getText().toString() + ", " + etCountry.getText().toString() + ", " +
                etPin.getText().toString();

        tvAddress.setText(address);
        btnSave.setVisibility(View.VISIBLE);
    }

    private void loadCountries() {
        String[] locales = Locale.getISOCountries();

        for (String countryCode : locales) {
            Locale obj = new Locale("", countryCode);
            signUpCountryList.add(obj.getDisplayCountry());
            Country_CodeMap.put(obj.getDisplayCountry(), obj.getCountry());
        }
        Collections.sort(signUpCountryList);
    }

    public interface OnResultReceive {
        void OnResult(String address, String area, String city, String state, String country, double lat, double lon, String pin, String housePlotNum, String landmark);
    }


    public void selectCountry() {
        final List<String> stringList = signUpCountryList;
        String[] countryList = new String[stringList.size()];
        countryList = stringList.toArray(countryList);

        new MaterialDialog.Builder(getActivity())
                .title("Select Country")
                .items(countryList)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        etCountry.setText(text);
                        return false;
                    }
                })
                .show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage((FragmentActivity) getActivity());
            mGoogleApiClient.disconnect();
        }
    }

}
