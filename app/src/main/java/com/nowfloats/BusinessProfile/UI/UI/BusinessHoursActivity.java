package com.nowfloats.BusinessProfile.UI.UI;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nowfloats.BusinessProfile.UI.API.UploadProfileAsyncTask;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.util.Constants;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static android.view.View.NO_ID;

/**
 * Created by Admin on 27-09-2017.
 */

public class BusinessHoursActivity extends AppCompatActivity implements View.OnTouchListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
    EditText etSunOpen,etSunClose,etMonOpen,etTueOpen,etWedOpen,etThuOpen,etFriOpen,etSatOpen,etSatClose,etFriClose,etMonClose,etThuClose,etTueClose,etWedClose;
    int currentId = NO_ID;
    Toolbar toolbar;
    TextView titleTextView,saveTextView;
    LinearLayout linearLayoutAllTime;
    private UserSessionManager session;
    CheckBox checkBoxAllTime;
    private SwitchCompat switchSun ,switchMon,switchTue,switchWed,switchThu,switchFri,switchSat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_hours_v2);
        Methods.isOnline(BusinessHoursActivity.this);
        session = new UserSessionManager(getApplicationContext(),BusinessHoursActivity.this);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        titleTextView = (TextView) toolbar.findViewById(R.id.titleTextView);
        titleTextView.setText("Business Hours");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        saveTextView = (TextView) toolbar.findViewById(R.id.saveTextView);
        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MixPanelController.track(EventKeysWL.SAVE_CONTACT_INFO, null);
                if (Methods.isOnline(BusinessHoursActivity.this)) {
                    uploadbusinessTimingsInfo();
                }else{
                    Methods.snackbarNoInternet(BusinessHoursActivity.this);
                }
            }
        });
        etSunOpen = (EditText) findViewById(R.id.et_sun_open);
        etMonOpen = (EditText) findViewById(R.id.et_mon_open);
        etTueOpen = (EditText) findViewById(R.id.et_tue_open);
        etWedOpen = (EditText) findViewById(R.id.et_wed_open);
        etThuOpen = (EditText) findViewById(R.id.et_thu_open);
        etFriOpen = (EditText) findViewById(R.id.et_fri_open);
        etSatOpen = (EditText) findViewById(R.id.et_sat_open);
        etSunClose = (EditText) findViewById(R.id.et_sun_close);
        etMonClose = (EditText) findViewById(R.id.et_mon_close);
        etTueClose = (EditText) findViewById(R.id.et_tue_close);
        etWedClose = (EditText) findViewById(R.id.et_wed_close);
        etThuClose = (EditText) findViewById(R.id.et_thu_close);
        etFriClose = (EditText) findViewById(R.id.et_fri_close);
        etSatClose = (EditText) findViewById(R.id.et_sat_close);

        switchSun = (SwitchCompat) findViewById(R.id.switch_sun);
        switchMon = (SwitchCompat) findViewById(R.id.switch_mon);
        switchTue = (SwitchCompat) findViewById(R.id.switch_tue);
        switchWed = (SwitchCompat) findViewById(R.id.switch_wed);
        switchThu = (SwitchCompat) findViewById(R.id.switch_thu);
        switchFri = (SwitchCompat) findViewById(R.id.switch_fri);
        switchSat = (SwitchCompat) findViewById(R.id.switch_sat);

        checkBoxAllTime = (CheckBox) findViewById(R.id.chbx_same_time);
        linearLayoutAllTime = (LinearLayout) findViewById(R.id.ll_same_time);
        checkBoxAllTime.setOnClickListener(this);

        switchSun.setOnClickListener(this);
        switchMon.setOnClickListener(this);
        switchTue.setOnClickListener(this);
        switchWed.setOnClickListener(this);
        switchThu.setOnClickListener(this);
        switchFri.setOnClickListener(this);
        switchSat.setOnClickListener(this);

        etSunOpen.setOnTouchListener(this);
        etMonOpen.setOnTouchListener(this);
        etTueOpen.setOnTouchListener(this);
        etWedOpen.setOnTouchListener(this);
        etThuOpen.setOnTouchListener(this);
        etFriOpen.setOnTouchListener(this);
        etSatOpen.setOnTouchListener(this);
        etSunClose.setOnTouchListener(this);
        etMonClose.setOnTouchListener(this);
        etTueClose.setOnTouchListener(this);
        etWedClose.setOnTouchListener(this);
        etThuClose.setOnTouchListener(this);
        etFriClose.setOnTouchListener(this);
        etSatClose.setOnTouchListener(this);
        updateTimings();
    }

    private void uploadbusinessTimingsInfo(){
        String defaultTime = "00";
        String mondayTime,tuesdayTime,wednesdayTime,thursdayTime,fridayTime,saturdayTime,sundayTime;
        String[] profilesattr =new String[20];
        profilesattr[0] = "TIME";
        JSONArray ja = new JSONArray();
        JSONObject dayData = new JSONObject();

        if(switchMon.isChecked()) {
            mondayTime = etMonOpen.getText().toString().trim()+","+etMonClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_START_TIME, etMonOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_END_TIME,etMonClose.getText().toString().trim());
        }else{
            mondayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_END_TIME,"00");
        }

        if(switchTue.isChecked()) {
            tuesdayTime = etTueOpen.getText().toString().trim()+","+etTueClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_START_TIME,etTueOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_END_TIME,etTueClose.getText().toString().trim());
        }else{
            tuesdayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_END_TIME,"00");
        }

        if(switchWed.isChecked()) {
            wednesdayTime =  etWedOpen.getText().toString().trim()+","+etWedClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_START_TIME,etWedOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_END_TIME,etWedClose.getText().toString().trim());
        }else{
            wednesdayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_END_TIME,"00");
        }

        if(switchThu.isChecked()) {
            thursdayTime = etThuOpen.getText().toString().trim()+","+etThuClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_START_TIME,etThuOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_END_TIME,etThuClose.getText().toString().trim());
        }else{
            thursdayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_END_TIME,"00");
        }

        if(switchFri.isChecked()) {
            fridayTime = etFriOpen.getText().toString().trim()+","+etFriClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_START_TIME,etFriOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_END_TIME,etFriClose.getText().toString().trim());
        }else{
            fridayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_END_TIME,"00");
        }
        if(switchSat.isChecked()) {
            saturdayTime =  etSatOpen.getText().toString().trim()+","+etSatClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_START_TIME,etSatOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_END_TIME,etSatClose.getText().toString().trim());
        }else{
            saturdayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_END_TIME,"00");
        }
        if(switchSun.isChecked()) {
            sundayTime =  etSunOpen.getText().toString().trim()+","+etSunClose.getText().toString().trim();
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_START_TIME,etSunOpen.getText().toString().trim());
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_END_TIME,etSunClose.getText().toString().trim());
        }else{
            sundayTime = defaultTime+","+defaultTime;
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_START_TIME,"00");
            session.storeFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_END_TIME,"00");
        }
        JSONObject offerObj = new JSONObject();
        try {
            //offerObj.put("clientId", Constants.clientId);
            //offerObj.put("fpTag", (Constants.StoreTag).toUpperCase());
            offerObj.put("key","TIMINGS");
            offerObj.put("value",
                    sundayTime+"#" +
                            mondayTime+"#" +
                            tuesdayTime+"#" +
                            wednesdayTime+"#" +
                            thursdayTime+"#" +
                            fridayTime+"#" +
                            saturdayTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ja.put(offerObj);
        try {
            dayData.put("clientId", Constants.clientId);
            dayData.put("fpTag", (session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG)).toUpperCase());
            dayData.put("updates", ja);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UploadProfileAsyncTask upa = new UploadProfileAsyncTask(BusinessHoursActivity.this,dayData,profilesattr);
        upa.execute();
    }

    private void updateTimings(){

        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_START_TIME).toLowerCase().endsWith("pm"))
        {

            etMonOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_START_TIME));
            etMonClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_MONDAY_END_TIME));

        } else {
            switchMon.setChecked(false);
            setTextTimeOnSwitch(R.id.et_mon_open,R.id.et_mon_close,0,0,0,0);
        }

        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_START_TIME).toLowerCase().endsWith("pm"))
        {
            etTueOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_START_TIME));
            etTueClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TUESDAY_END_TIME));
        } else {
            switchTue.setChecked(false);
            setTextTimeOnSwitch(R.id.et_tue_open,R.id.et_tue_close,0,0,0,0);

        }

        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_START_TIME).toLowerCase().endsWith("pm"))
        {

            etWedOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_START_TIME));
            etWedClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_WEDNESDAY_END_TIME));
        } else {
            switchWed.setChecked(false);
            setTextTimeOnSwitch(R.id.et_wed_open,R.id.et_wed_close,0,0,0,0);

        }
       if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_START_TIME).toLowerCase().endsWith("pm"))
        {
            etThuOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_START_TIME));
            etThuClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_THURSDAY_END_TIME));
        } else {
           switchThu.setChecked(false);
           setTextTimeOnSwitch(R.id.et_thu_open,R.id.et_thu_close,0,0,0,0);
        }
        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_START_TIME).toLowerCase().endsWith("pm"))
        {

            etFriOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_START_TIME));
            etFriClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_FRIDAY_END_TIME));
        } else {
            switchFri.setChecked(false);
            setTextTimeOnSwitch(R.id.et_fri_open,R.id.et_fri_close,0,0,0,0);

        }

        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_START_TIME).toLowerCase().endsWith("pm"))
        {

            etSatOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_START_TIME));
            etSatClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SATURDAY_END_TIME));
        } else {

            switchSat.setChecked(false);
            setTextTimeOnSwitch(R.id.et_sat_open,R.id.et_sat_close,0,0,0,0);
        }

        if(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_START_TIME).toLowerCase().endsWith("am")
                || session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_START_TIME).toLowerCase().endsWith("pm"))
        {
            etSunOpen.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_START_TIME));
            etSunClose.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_SUNDAY_END_TIME));

        } else {
            switchSun.setChecked(false);
            setTextTimeOnSwitch(R.id.et_sun_open,R.id.et_sun_close,0,0,0,0);
        }

    }
    private void timePicker(){
       /* TimePickerDialog dialog = TimePickerDialog.newInstance(this,0,0,false);
        dialog.setThemeDark(false);
        dialog.show(getFragmentManager(), "Timepickerdialog");*/
        View parentView = LayoutInflater.from(this).inflate(R.layout.dialog_with_spinner,null);
        final Spinner openSpinner = (Spinner) parentView.findViewById(R.id.spinner_all_open);
        final Spinner closeSpinner = (Spinner) parentView.findViewById(R.id.spinner_all_close);
        final CheckBox checkBox = (CheckBox) parentView.findViewById(R.id.chbx_same_time);

        openSpinner.setOnItemSelectedListener(this);
        closeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.business_hours_arrays, R.layout.layout_simple_text_item);
        openSpinner.setAdapter(adapter);
        closeSpinner.setAdapter(adapter);
        MaterialDialog dialog  = new MaterialDialog.Builder(this)
                .customView(parentView,true)
                .autoDismiss(false)
                .title("Select Time")
                .positiveText("set")
                .negativeText("cancel")
                .positiveColorRes(R.color.primary_color)
                .negativeColorRes(R.color.gray_transparent)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        setSelectedTime(checkBox.isChecked(),openSpinner.getSelectedItem().toString(),closeSpinner.getSelectedItem().toString());
                        dialog.dismiss();
                        currentId = NO_ID;
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        currentId = NO_ID;
                        dialog.dismiss();
                    }
                })
                .build();
        dialog.show();

    }

    private void setSelectedTime(boolean isChecked,String openTime, String closeTime) {
        if(isChecked){
            etMonClose.setText(closeTime);
            etMonOpen.setText(openTime);
            etTueClose.setText(closeTime);
            etTueOpen.setText(openTime);
            etWedClose.setText(closeTime);
            etWedOpen.setText(openTime);
            etThuClose.setText(closeTime);
            etThuOpen.setText(openTime);
            etFriClose.setText(closeTime);
            etFriOpen.setText(openTime);
            etSatClose.setText(closeTime);
            etSatOpen.setText(openTime);
            etSunClose.setText(closeTime);
            etSunOpen.setText(openTime);
        }else {
            switch (currentId) {
                case 0:
                    etMonClose.setText(closeTime);
                    etMonOpen.setText(openTime);
                    break;
                case 1:
                    etTueClose.setText(closeTime);
                    etTueOpen.setText(openTime);
                    break;
                case 2:
                    etWedClose.setText(closeTime);
                    etWedOpen.setText(openTime);
                    break;
                case 3:
                    etThuClose.setText(closeTime);
                    etThuOpen.setText(openTime);
                    break;
                case 4:
                    etFriClose.setText(closeTime);
                    etFriOpen.setText(openTime);
                    break;
                case 5:
                    etSatClose.setText(closeTime);
                    etSatOpen.setText(openTime);
                    break;
                case 6:
                    etSunClose.setText(closeTime);
                    etSunOpen.setText(openTime);
                    break;
            }
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        String s = convertToString(hourOfDay,minute);
        ((EditText)findViewById(currentId)).setText(s);
        currentId = NO_ID;
    }

    private String convertToString(int hourOfDay,int minute){
        return String.format(Locale.ENGLISH,"%02d:%02d",hourOfDay>12?hourOfDay-12:hourOfDay,minute);
    }
    /*private String ConvertToTime(String time){
        String hours = time.substring(0,time.indexOf(":")+1);
        String minutes = time.substring(time.indexOf(":"),time.indexOf(" "));
        if (time.toLowerCase().endsWith("am")){

        }else if(time.toLowerCase().endsWith("pm")){

        }
        Integer.parseInt(hours)
    }*/
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.et_mon_open:
                case R.id.et_mon_close:
                    currentId = 0;
                    break;
                case R.id.et_tue_open:
                case R.id.et_tue_close:
                    currentId = 1;
                    break;
                case R.id.et_wed_open:
                case R.id.et_wed_close:
                    currentId = 2;
                    break;
                case R.id.et_thu_open:
                case R.id.et_thu_close:
                    currentId = 3;
                    break;
                case R.id.et_fri_open:
                case R.id.et_fri_close:
                    currentId = 4;
                    break;
                case R.id.et_sat_open:
                case R.id.et_sat_close:
                    currentId = 5;
                    break;
                case R.id.et_sun_open:
                case R.id.et_sun_close:
                    currentId = 6;
                    break;
            }
            timePicker();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_business__hours, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==android.R.id.home){
            //  NavUtils.navigateUpFromSameTask(this);
            // getSupportFragmentManager().popBackStack();
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextTimeOnSwitch(int openId,int closeId,int openHoursTime,int openMinuteTime, int closeHoursTime, int closeMinuteTime){
        if(openId !=-1)
        {
            ((EditText) findViewById(openId)).setText(convertToString(openHoursTime, openMinuteTime));
        }

        if (closeId != -1)
        {
            ((EditText)findViewById(closeId)).setText(convertToString(closeHoursTime,closeMinuteTime));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_sun:
                if(!switchSun.isChecked())
                setTextTimeOnSwitch(R.id.et_sun_open,R.id.et_sun_close,0,0,0,0);
                break;
            case R.id.switch_mon:
                if(!switchMon.isChecked())
                setTextTimeOnSwitch(R.id.et_mon_open,R.id.et_mon_close,0,0,0,0);
                break;
            case R.id.switch_tue:
                if(!switchTue.isChecked())
                setTextTimeOnSwitch(R.id.et_tue_open,R.id.et_tue_close,0,0,0,0);
                break;
            case R.id.switch_wed:
                if(!switchWed.isChecked())
                setTextTimeOnSwitch(R.id.et_wed_open,R.id.et_wed_close,0,0,0,0);
                break;
            case R.id.switch_thu:
                if(!switchThu.isChecked())
                setTextTimeOnSwitch(R.id.et_thu_open,R.id.et_thu_close,0,0,0,0);
                break;
            case R.id.switch_fri:
                if(!switchFri.isChecked())
                setTextTimeOnSwitch(R.id.et_fri_open,R.id.et_fri_close,0,0,0,0);
                break;
            case R.id.switch_sat:
                if(!switchSat.isChecked())
                setTextTimeOnSwitch(R.id.et_sat_open,R.id.et_sat_close,0,0,0,0);
                break;
            case R.id.chbx_same_time:
                if(checkBoxAllTime.isChecked()) {
                    linearLayoutAllTime.setVisibility(View.VISIBLE);
                    findViewById(R.id.et_all_close).setOnTouchListener(BusinessHoursActivity.this);
                    findViewById(R.id.et_all_open).setOnTouchListener(BusinessHoursActivity.this);
                }else{

                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}