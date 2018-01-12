package com.nowfloats.domain;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nowfloats.CustomWidget.roboto_lt_24_212121;
import com.nowfloats.CustomWidget.roboto_md_60_212121;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.API.DomainApiService;
import com.nowfloats.NavigationDrawer.model.DomainDetails;
import com.nowfloats.NavigationDrawer.model.EmailBookingModel;
import com.nowfloats.Store.NewPricingPlansActivity;
import com.nowfloats.Store.PricingPlansActivity;
import com.nowfloats.signup.UI.Model.Get_FP_Details_Model;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.BuildConfig;
import com.thinksity.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;


public class DomainDetailsActivity extends AppCompatActivity implements View.OnClickListener,DomainApiService.DomainCallback {

    private Toolbar toolbar;

    private TextView headerText;

    private UserSessionManager session;

    private DomainApiService domainApiService;

    private ProgressDialog progressDialog;

    private static final int DOMAIN_EXPIRY_GRACE_PERIOD = 30;

    private static final int EMAIL_BOOKING_NUM = 3;
    private static final String FP_WEB_WIDGET_DOMAIN = "DOMAINPURCHASE";

    private int domainExpiryDays = 0, planExpiryDays = 0;

    private long currentTime, totalNoOfDays = 0;

    private HashMap<String, Integer> hmPrices = new HashMap<>();

    private ArrayList<String> arrDomainExtensions;

    private String domainType = "", domainExpiryDate = "", domainCreatedDate = "",domainName="";

    private Get_FP_Details_Model get_fp_details_model;
    int processingStatus = -1, totalBookedEmails, totalFailedEmails;
    SharedPreferences pref;
    EmailBookingModel bookingModelList;
    EmailAdapter emailAdapter;
    private ArrayList<EmailBookingModel.AddEmailModel> emailBookedList = new ArrayList<>();
    private boolean isDomainBookFailed = false;
    TextView domainNameTv, domainCreatedTv, domainExpiredTv, statusTv,expireMsgTv, bookedEmailTv;
    CardView domainDetailsCard, emailDetailsCard;
    LinearLayout chooseDomainLayout, expiredLayout,emptyLayout;
    TextView proceedBtn, confirmRequestTv, addEmail;
    RadioButton chooseBtn;
    private MaterialDialog domainBookDialog;
    public enum EmailType{
        ADDED,IN_PROCESS,FAILED,ADD_EMAIL;

        public static EmailType getEmailType(int x) {
            switch(x) {
                case 0:
                    return ADDED;
                case 1:
                    return IN_PROCESS;
                case 2:
                    return FAILED;
                case 3:
                    return ADD_EMAIL;
            }
            return null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        domainExpiryDays = 0;
        planExpiryDays = 0;
        totalNoOfDays = 1000 * 60 * 60 * 24;

        currentTime = System.currentTimeMillis();
        setContentView(R.layout.activity_domain_details);

        get_fp_details_model = (Get_FP_Details_Model) getIntent().getExtras().get("get_fp_details_model");

        initializeControls();
        loadData();
    }

    private void initializeControls() {

        toolbar = (Toolbar) findViewById(R.id.app_bar_site_appearance);

        headerText = (TextView) toolbar.findViewById(R.id.titleTextView);

        domainCreatedTv = (TextView) findViewById(R.id.tv_domain_created_date);
        domainExpiredTv = (TextView) findViewById(R.id.tv_domain_expire_date);
        domainNameTv = (TextView) findViewById(R.id.tv_domain_name);
        statusTv = (TextView) findViewById(R.id.tv_status);
        domainDetailsCard = (CardView) findViewById(R.id.cv_domain_details);
        emailDetailsCard = (CardView) findViewById(R.id.cv_email_details);
        chooseDomainLayout = (LinearLayout) findViewById(R.id.ll_choose_domain);
        emptyLayout = (LinearLayout) findViewById(R.id.ll_empty_view);
        expiredLayout = (LinearLayout) findViewById(R.id.ll_plan_expired);
        expireMsgTv = (TextView) expiredLayout.findViewById(R.id.tv_expire_msg);
        bookedEmailTv = (TextView) emailDetailsCard.findViewById(R.id.tv_email_booked);
        proceedBtn = (TextView) findViewById(R.id.btn_proceed);
        ((TextView)emailDetailsCard.findViewById(R.id.tv_note_message)).setText(Methods.fromHtml(getString(R.string.email_180_message)));
        chooseBtn = (RadioButton) findViewById(R.id.rb_book_a_domain);
        chooseBtn.setText(Methods.fromHtml("Book new domain <i>(included in your package)</i>"));
        addEmail = emailDetailsCard.findViewById(R.id.tv_add_email);
        addEmail.setPaintFlags(addEmail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        confirmRequestTv = emailDetailsCard.findViewById(R.id.tv_confirm_request);
        setSupportActionBar(toolbar);
        headerText.setText(getResources().getString(R.string.side_panel_row_domain_details));

        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        session = new UserSessionManager(this, this);
        domainApiService = new DomainApiService(this);
        pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        proceedBtn.setOnClickListener(this);

//        RecyclerView emailRunningRv = findViewById(R.id.rv_email_running);
//        emailRunningRv.setLayoutManager(new LinearLayoutManager(this));
//        final EmailAdapter adapter = new EmailAdapter(new ArrayList<EmailBookingModel.EmailDomainName>());
//        emailRunningRv.setAdapter(adapter);
//        emailRunningRv.setNestedScrollingEnabled(false);
//        addEmailTv.setPaintFlags(addEmailTv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
//        addEmailTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                enterEmailDialog();
//            }
//        });
    }

    private void initializePrices() {
        hmPrices.put(".COM", 680);
        hmPrices.put(".NET", 865);
        hmPrices.put(".CO.IN", 375);
        hmPrices.put(".IN", 490);
        hmPrices.put(".ORG", 500);
    }

    private void linkDomain() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(DomainDetailsActivity.this)
                //.title(getString(R.string.have_an_existing_domain))
                .customView(R.layout.dialog_link_domain, false)
                .positiveColorRes(R.color.primaryColor);

            final MaterialDialog materialDialog = builder.show();
            View maView = materialDialog.getCustomView();
            final RadioButton rbPointExisting = (RadioButton) maView.findViewById(R.id.rbPointExisting);
            final RadioButton rbPointNFWeb = (RadioButton) maView.findViewById(R.id.rbPointNFWeb);
            final EditText edtComments = (EditText) maView.findViewById(R.id.edtComments);
            Button btnBack = (Button) maView.findViewById(R.id.btnBack);
            Button btnSubmitRequest = (Button) maView.findViewById(R.id.btnSubmitRequest);
            edtComments.setText(String.format(getString(R.string.link_comments), session.getFpTag()));
            edtComments.setSelection(edtComments.getText().toString().length());
            btnSubmitRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String subject = "";
                    if (rbPointNFWeb.isChecked()) {
                        subject = rbPointNFWeb.getText().toString();
                    } else if (rbPointExisting.isChecked()) {
                        subject = rbPointExisting.getText().toString();
                    }

                    if (TextUtils.isEmpty(subject)) {
                        Methods.showSnackBarNegative(DomainDetailsActivity.this,
                                getString(R.string.please_select_subject));
                    } else if (TextUtils.isEmpty(edtComments.getText().toString())) {
                        Methods.showSnackBarNegative(DomainDetailsActivity.this,
                                getString(R.string.please_enter_message));
                    } else {

                        MixPanelController.track(MixPanelController.LINK_DOMAIN, null);
                        materialDialog.dismiss();
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("Subject", subject);
                        hashMap.put("Mesg", edtComments.getText().toString());
                        domainApiService.linkDomain(hashMap, getLinkDomainParam());
                    }
                }
            });
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    materialDialog.dismiss();
                }
            });

    }
    private HashMap<String, String> getLinkDomainParam() {
        HashMap<String, String> offersParam = new HashMap<>();
        offersParam.put("authClientId", Constants.clientId);
        offersParam.put("fpTag", session.getFpTag());
        return offersParam;
    }


    private HashMap<String, String> getDomainAvailabilityParam(String domainType) {
        HashMap<String, String> offersParam = new HashMap<>();
        offersParam.put("clientId", Constants.clientId);
        offersParam.put("domainType", domainType);
        return offersParam;
    }

    @Override
    public void domainAvailabilityStatus(DomainApiService.DomainAPI domainAPI) {
        hideLoader();
        if (domainAPI == DomainApiService.DomainAPI.RENEW_DOMAIN){
            Methods.showSnackBarPositive(DomainDetailsActivity.this, "Domain is available");
            showCustomDialog(getString(R.string.renew_domain),
                    getString(R.string.are_you_sure_do_you_want_to_book_domain),
                    getString(R.string.yes), getString(R.string.no), DialogFrom.DOMAIN_AVAILABLE);

        }else if(domainAPI == DomainApiService.DomainAPI.RENEW_NOT_AVAILABLE){
            showCustomDialog(getString(R.string.domain_booking_failed),getString(R.string.drop_us_on_email_or_call) ,
                    getString(R.string.ok), null, DialogFrom.FAILED);

        } else if (domainAPI == DomainApiService.DomainAPI.CHECK_DOMAIN) {
            Methods.showSnackBarPositive(DomainDetailsActivity.this, "Domain is available");

            showCustomDialog(getString(R.string.book_a_new_domain),
                    getString(R.string.are_you_sure_do_you_want_to_book_domain),
                    getString(R.string.yes), getString(R.string.no), DialogFrom.DOMAIN_AVAILABLE);


        } else if (domainAPI == DomainApiService.DomainAPI.LINK_DOMAIN) {
            showCustomDialog(getString(R.string.domain_booking_process),"You have successfully requested to link a domain, Our team will contact you with in 48 hours." ,
                    getString(R.string.ok), null, DialogFrom.DEFAULT);
        } else {
            if (domainBookDialog != null)
                Methods.showSnackBarNegative(domainBookDialog.getView(), getString(R.string.domain_not_available));
            else {
                Methods.showSnackBarNegative(DomainDetailsActivity.this, getString(R.string.link_domain_not_available));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        showLoader(getString(R.string.please_wait));
        initializePrices();
        domainApiService.getDomainDetails(DomainDetailsActivity.this,session.getFpTag(), getDomainDetailsParam());
    }


    @Override
    public void getDomainDetails(DomainDetails domainDetails) {

        if (domainDetails != null && domainDetails.response == DomainDetails.DOMAIN_RESPONSE.ERROR){
            Methods.showSnackBarNegative(this,getString(R.string.something_went_wrong));
            hideLoader();
        }else if(domainDetails != null && domainDetails.response == DomainDetails.DOMAIN_RESPONSE.NO_DATA){
            if(!TextUtils.isEmpty(get_fp_details_model.getExpiryDate())) {
                setPricingPlansDays(Long.valueOf(get_fp_details_model.getExpiryDate().replace("/Date(", "").replace(")/", "")));
            }else{
                applyDomainLogic();
            }
            hideLoader();
        } else if (domainDetails != null && domainDetails.response == DomainDetails.DOMAIN_RESPONSE.DATA) {

            if(TextUtils.isDigitsOnly(domainDetails.getProcessingStatus())){
                processingStatus = Integer.parseInt(domainDetails.getProcessingStatus());
            }
            if(!TextUtils.isEmpty(domainDetails.getActivatedOn())){
                long activatedDate = Long.parseLong(domainDetails.getActivatedOn().replace("/Date(", "").replace(")/", ""));
                Calendar dbCalender = Calendar.getInstance();
                dbCalender.setTimeInMillis(activatedDate);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.ENGLISH);
                dateFormat.setCalendar(dbCalender);
                domainCreatedDate = dateFormat.format(dbCalender.getTime());
                dbCalender.add(Calendar.YEAR, domainDetails.getValidityInYears());

                domainExpiryDays = (int) ((dbCalender.getTimeInMillis() - currentTime) / totalNoOfDays);
                domainExpiryDate = dateFormat.format(dbCalender.getTime());
            }
            /*tvDomainStatus.setVisibility(View.VISIBLE);
            edtDomainName.setText(domainDetails.getDomainName());*/

            domainType = domainDetails.getDomainType().toLowerCase();
            domainName = domainDetails.getDomainName().toLowerCase();
            domainNameTv.setText(domainName+domainType);
            domainCreatedTv.setText(Methods.fromHtml("Purchase Date: <b>"+domainCreatedDate+"</b>"));
            domainExpiredTv.setText(Methods.fromHtml("Valid Till: <b>"+domainExpiryDate+"</b>"));
            if( !TextUtils.isEmpty(domainDetails.getErrorMessage()) && domainDetails.getIsProcessingFailed()){
                //error domain failed
                isDomainBookFailed = true;
            }
          /*  if(!TextUtils.isEmpty(get_fp_details_model.getExpiryDate())) {
                setPricingPlansDays(Long.valueOf(get_fp_details_model.getExpiryDate().replace("/Date(", "").replace(")/", "")));
            }else{
                applyDomainLogic();
            }*/
            domainApiService.emailsBookingStatus(Constants.clientId,session.getFpTag());
//            ArrayList<EmailBookingModel.EmailBookingStatus> bookingStatus = new ArrayList<EmailBookingModel.EmailBookingStatus>(4);
//            for (int i =0; i<4;i++){
//                EmailBookingModel.EmailBookingStatus model = new EmailBookingModel.EmailBookingStatus();
//                model.setEmail("hello@nowfloats.com");
//
//                if (i ==0){
//                    model.setBookingStatus(DomainApiService.EmailBookingStatus.COMPLETED.ordinal());
//                    model.setIsBooked(false);
//                    bookingStatus.add(model);
//                }else if(i == 1){
//                    model.setBookingStatus(DomainApiService.EmailBookingStatus.DNSFETCH_COMPLETED.ordinal());
//                    model.setIsBooked(false);
//                    bookingStatus.add(model);
//                }
//                else if (i == 2){
//                    model.setBookingStatus(DomainApiService.EmailBookingStatus.COMPLETED.ordinal());
//                    model.setIsBooked(true);
//                    bookingStatus.add(model);
//                }else{
//                    model.setEmail("");
//                    model.setIsBooked(true);
//                    bookingStatus.add(model);
//                }
//            }
//            emailBookingStatus(bookingStatus);
        }
        else if (!Methods.isOnline(this)){
            hideLoader();
            Methods.snackbarNoInternet(this);
        }

    }

    @Override
    public void emailBookingStatus(ArrayList<EmailBookingModel.EmailBookingStatus> bookingStatuses){
        hideLoader();

        for (EmailBookingModel.EmailBookingStatus emailBookingStatus : bookingStatuses) {
            if (TextUtils.isEmpty(emailBookingStatus.getEmail()) && emailBookingStatus.getIsBooked()){
                totalBookedEmails++;
            }else if (emailBookingStatus.getIsBooked()){
                emailBookedList.add(new EmailBookingModel.AddEmailModel(emailBookingStatus.getEmail(),
                        EmailType.getEmailType(EmailType.ADDED.ordinal())));

            }else if (emailBookingStatus.getBookingStatus()< DomainApiService.EmailBookingStatus.COMPLETED.ordinal()){
                emailBookedList.add(new EmailBookingModel.AddEmailModel(emailBookingStatus.getEmail(),
                        EmailType.getEmailType(EmailType.IN_PROCESS.ordinal())));
            }else{
                emailBookedList.add(new EmailBookingModel.AddEmailModel(emailBookingStatus.getEmail(),
                        EmailType.getEmailType(EmailType.FAILED.ordinal())));
                totalFailedEmails++;
            }

        }
        Collections.sort(emailBookedList);

        if(totalBookedEmails>0) {
            bookedEmailTv.setVisibility(View.VISIBLE);
            bookedEmailTv.setText(String.format(Locale.ENGLISH, "You already have booked %s %s from other sources.", totalBookedEmails,totalBookedEmails > 1?"emailIds":"emailId"));
        }

        if(!TextUtils.isEmpty(get_fp_details_model.getExpiryDate())) {
            setPricingPlansDays(Long.valueOf(get_fp_details_model.getExpiryDate().replace("/Date(", "").replace(")/", "")));
        }else{
            applyDomainLogic();
        }
    }
    @Override
    public void getEmailBookingList(ArrayList<String> ids, String error){
        hideLoader();
        JSONObject json = new JSONObject();

        if (!TextUtils.isEmpty(error)){
            Methods.showSnackBarNegative(this,error);
            try {
                json.put("value","serverError");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else if (ids != null){
            try {
                json.put("value","booked");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            confirmRequestTv.setVisibility(View.GONE);
            for (EmailBookingModel.AddEmailModel model : emailBookedList){
                model.setType(EmailType.IN_PROCESS);
            }
            Collections.sort(emailBookedList);
            if (emailAdapter != null)
                emailAdapter.notifyDataSetChanged();
            showCustomDialog("Submit Email Request","We are processing your requests, It may take too long.","Ok","",DialogFrom.NO_CLOSE);
        }else{
            try {
                json.put("value","error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Methods.showSnackBarNegative(this,getString(R.string.something_went_wrong_try_again));
        }
        MixPanelController.track(MixPanelController.DOMAIN_EMAIL_BOOK,json);
    }
    @Override
    public void getDomainSupportedTypes(ArrayList<String> arrExtensions) {
        hideLoader();
        if (arrExtensions != null && arrExtensions.size() > 0) {
//            arrDomainExtensions = new ArrayList<String>(Arrays.asList(domainSupportedTypes.domainExtension.split(",")));
           /*
               remove below domains as per Rachit
            */
            this.arrDomainExtensions = arrExtensions;

            if (arrDomainExtensions.contains(".IN")) {
                arrDomainExtensions.remove(".IN");
                //String firstIndexValue = arrDomainExtensions.get(0);
                arrDomainExtensions.add(0, ".IN");
            }

            arrDomainExtensions.remove(".CA");
            arrDomainExtensions.remove(".CO.ZA");
            bookDomain(null,null, DomainApiService.DomainAPI.CHECK_DOMAIN);

        } else {
            Methods.showSnackBarNegative(DomainDetailsActivity.this, getString(R.string.domain_details_getting_error));
        }

    }

    private void setPricingPlansDays(long storeExpiryDays) {
        planExpiryDays = (int) ((storeExpiryDays - currentTime) / totalNoOfDays);
        applyDomainLogic();
    }

    private void enterEmailDialog(final String emailId){
        final ArrayList<EmailBookingModel.EmailDomainName> emailDomainNames = (ArrayList<EmailBookingModel.EmailDomainName>) bookingModelList.getEmailDomainNames();
        class EmailInfo {
            private TextInputLayout usernameLayout, passwordLayout,rePasswordLayout, firstNameLayout, lastNameLayout;
            private TextInputEditText username, password,rePassword, firstName, lastName;
            private MaterialDialog dialog;
            private EmailBookingModel.EmailDomainName editModel;
            TextView userSuggestionEmail;
            void addDialog(MaterialDialog dialog){
                this.dialog = dialog;
                View view = dialog.getCustomView();
                if (view == null){
                     return;
                }
                usernameLayout = view.findViewById(R.id.til_username);
                passwordLayout = view.findViewById(R.id.til_password);
                rePasswordLayout = view.findViewById(R.id.til_re_password);
                firstNameLayout = view.findViewById(R.id.til_firstname);
                lastNameLayout = view.findViewById(R.id.til_lastname);

                username = view.findViewById(R.id.et_username);
                password = view.findViewById(R.id.et_password);
                rePassword = view.findViewById(R.id.et_repassword);
                firstName = view.findViewById(R.id.et_firstname);
                lastName = view.findViewById(R.id.et_lastname);
                password.setTransformationMethod(new PasswordTransformationMethod());
                rePassword.setTransformationMethod(new PasswordTransformationMethod());
                userSuggestionEmail = view.findViewById(R.id.email_tv);

                if (!TextUtils.isEmpty(emailId)){
                    for (EmailBookingModel.EmailDomainName emailData : emailDomainNames){
                        if(emailId.equalsIgnoreCase(emailData.getUsername()+"@"+domainNameTv.getText().toString().trim())){
                            editModel = emailData;
                            username.setText(emailData.getUsername());
                            password.setText(emailData.getPassword());
                            rePassword.setText(emailData.getPassword());
                            firstName.setText(emailData.getFirstName());
                            lastName.setText(emailData.getLastName());
                            break;
                        }
                    }
                }
                username.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (userSuggestionEmail.getVisibility() ==View.GONE){
                            userSuggestionEmail.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        userSuggestionEmail.setText(editable.toString()+"@"+domainNameTv.getText().toString());
                        if (editable.toString().length() == 0){
                            userSuggestionEmail.setVisibility(View.GONE);
                        }
                    }
                });
            }

            private boolean validateUser(){
                //check username list contain this string
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username.getText().toString()+"@"+domainNameTv.getText().toString()).matches()) {
                    usernameLayout.setError("Add valid username");
                    return false;
                }
                else if (TextUtils.isEmpty(emailId) ||
                        !emailId.equalsIgnoreCase(username.getText().toString().trim()+"@"+domainNameTv.getText().toString().trim())){
                    for (EmailBookingModel.AddEmailModel domainData: emailBookedList){

                        if (domainData.getType() != EmailType.FAILED && domainData.getEmailId()
                                .equalsIgnoreCase(username.getText().toString().trim()+"@"+domainNameTv.getText().toString().trim())){
                            usernameLayout.setError("Username already used in previous emailId");
                            return false;
                        }
                    }
                }

                usernameLayout.setErrorEnabled(false);
                return true;
            }

            private boolean validatePassword(){
                if (password.getText().toString().equalsIgnoreCase(username.getText().toString().trim())){
                    passwordLayout.setError("Password can't be same as username");
                    return false;
                }else if(password.getText().toString().equalsIgnoreCase(domainName)){
                    passwordLayout.setError("Password can't be same as domain");
                    return false;
                }else if (password.getText().toString().length()<8) {
                    passwordLayout.setError("Field should has at least 8 character");
                    return false;
                }else{
                    passwordLayout.setErrorEnabled(false);
                    return true;
                }
            }
            private boolean validateRePassword(){

                if (!rePassword.getText().toString().equals(password.getText().toString())) {
                    rePasswordLayout.setError("Password is not matching");
                    return false;
                }else{
                    rePasswordLayout.setErrorEnabled(false);
                    return true;
                }
            }
            private boolean validateFirstName(){
                if (firstName.getText().toString().trim().length()==0 ) {
                    firstNameLayout.setError("Field can't be empty");
                    return false;
                }
                else{
                    firstNameLayout.setErrorEnabled(false);
                    return true;
                }
            }

            private boolean validateLastName(){
                if (lastName.getText().toString().trim().length()==0 ) {
                    lastNameLayout.setError("Field can't be empty");
                    return false;
                } else{
                    lastNameLayout.setErrorEnabled(false);
                    return true;
                }
            }

            void validateInfo() {

                if(validateUser() && validatePassword() && validateRePassword() && validateFirstName() && validateLastName()){
                    if (editModel == null) {
                        editModel = new EmailBookingModel.EmailDomainName();
                        emailBookedList.add(new EmailBookingModel.AddEmailModel(username.getText().toString().trim()+"@"+domainNameTv.getText().toString().trim(),EmailType.ADD_EMAIL));

                    }else{
                        for (EmailBookingModel.AddEmailModel domainData: emailBookedList){
                            if (domainData.getType() == EmailType.ADD_EMAIL && domainData.getEmailId().equalsIgnoreCase(emailId)){
                                domainData.setEmailId(username.getText().toString().trim()+"@"+domainNameTv.getText().toString().trim());
                            }
                        }
                    }
                    editModel.setFirstName(firstName.getText().toString().trim());
                    editModel.setLastName(lastName.getText().toString().trim());
                    editModel.setPassword(password.getText().toString().trim());
                    editModel.setUsername(username.getText().toString().trim());
                    if (!emailDomainNames.contains(editModel)){
                        emailDomainNames.add(editModel);
                    }
                    emailAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                   if ((emailBookedList.size()+totalBookedEmails-totalFailedEmails)>=EMAIL_BOOKING_NUM){
                        addEmail.setVisibility(View.GONE);
                   }
                   if(bookingModelList.getEmailDomainNames().size()>0){
                       confirmRequestTv.setVisibility(View.VISIBLE);
                   }
                }
            }
        }

        final EmailInfo info = new EmailInfo();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_email_user_info,false)
                .positiveColorRes(R.color.primary_color)
                .negativeColorRes(R.color.light_gray)
                .positiveText("Add")
                .negativeText("Cancel")
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        info.validateInfo();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
        info.addDialog(dialog);

    }

    private void applyDomainLogic() {
        if (planExpiryDays <= 0) {

             if(domainExpiryDays <= 0){
                 setDomainDetailsCard(false,"NowFloats Plan and Domain Expired");
                 expiredLayout.setVisibility(View.VISIBLE);
                 expireMsgTv.setText(getString(R.string.renew_nowfloats_and_domain_plan));
                 expiredLayout.findViewById(R.id.btn_plan_expired).setVisibility(View.VISIBLE);
                 expiredLayout.findViewById(R.id.ll_domain_expired).setVisibility(View.GONE);
                 expiredLayout.findViewById(R.id.btn_plan_expired).setOnClickListener(this);
             }
             else{
                 setDomainDetailsCard(true,null);
             }

            //card background light_gray
            // nowfloats plan expired

        } else if(isDomainBookFailed){
             // will not come here
             emptyLayout.setVisibility(View.VISIBLE);
             TextView mainMessage = (TextView) emptyLayout.findViewById(R.id.tv_main_message);
             TextView description = (TextView) emptyLayout.findViewById(R.id.tv_note_message);
             mainMessage.setText("Domain Booking Failed");
             description.setText(getString(R.string.drop_us_contact));
        } else if(TextUtils.isEmpty(domainType)){
             // first domain purchase widget exist
            if(planExpiryDays <=90){
                showExpiryDialog(LIGHT_HOUSE_DAYS_LEFT, planExpiryDays);
            }
            if(get_fp_details_model.getFPWebWidgets() != null && get_fp_details_model.getFPWebWidgets().contains(FP_WEB_WIDGET_DOMAIN)){
                chooseDomainLayout.setVisibility(View.VISIBLE);
            }else{
                emptyLayout.setVisibility(View.VISIBLE);
                TextView mainMessage = (TextView) emptyLayout.findViewById(R.id.tv_main_message);
                TextView description = (TextView) emptyLayout.findViewById(R.id.tv_note_message);
                mainMessage.setText("No Domain Feature Available");
                description.setText("Your plan does not include domain purchase. To get domain, please buy Boost plan");
                //  your package does not have option to buy
            }

        }else if (domainExpiryDays <= 0) {
             // renew button
             //first check domain purchase widget exist
             if(planExpiryDays <=90){
                 showExpiryDialog(LIGHT_HOUSE_DAYS_LEFT, planExpiryDays);
             }
             if(get_fp_details_model.getFPWebWidgets() != null && get_fp_details_model.getFPWebWidgets().contains(FP_WEB_WIDGET_DOMAIN)) {
                 setDomainDetailsCard(false,"Domain Expired");
                 expiredLayout.setVisibility(View.VISIBLE);
                 expiredLayout.findViewById(R.id.ll_domain_expired).setVisibility(View.VISIBLE);
                 expireMsgTv.setText(String.format(getString(R.string.renew_domain_message),domainNameTv.getText().toString(),session.getFpTag().toLowerCase()+getString(R.string.nowfloats_com)));
                 expiredLayout.findViewById(R.id.btn_plan_expired).setVisibility(View.GONE);
                 expiredLayout.findViewById(R.id.btn_renew_domain).setOnClickListener(this);
                 expiredLayout.findViewById(R.id.btn_link_domain).setOnClickListener(this);
             }else{
                 emptyLayout.setVisibility(View.VISIBLE);
                 TextView mainMessage = (TextView) emptyLayout.findViewById(R.id.tv_main_message);
                 TextView description = (TextView) emptyLayout.findViewById(R.id.tv_note_message);
                 mainMessage.setText("No Domain Feature Available");
                 description.setText("Your plan does not include domain purchase. To get domain, please buy Boost plan");
             }

             // first renew after that book new domain

        }else{
             setDomainDetailsCard(true,null);
         }
    }

    private static final int LIGHT_HOUSE_EXPIRE = 0;
    private static final int WILD_FIRE_EXPIRE = 1;
    private static final int DEMO_EXPIRE = 3;
    private static final int DEMO_DAYS_LEFT = 4;
    private static final int LIGHT_HOUSE_DAYS_LEFT = 5;

    private void setDomainDetailsCard(boolean active,String statusMessage){

        domainDetailsCard.setVisibility(View.VISIBLE);
        emailDetailsCard.setVisibility(View.VISIBLE);
        ImageView domainImg = (ImageView) domainDetailsCard.findViewById(R.id.img_domain);
        ImageView emailImg = (ImageView) emailDetailsCard.findViewById(R.id.img_email);
        TextView domainMessageTv = (TextView) domainDetailsCard.findViewById(R.id.tv_domain_message);
        switch (domainType.toLowerCase()){
            case ".in":
                domainImg.setImageResource(R.drawable.domain_in);
                break;
            case ".com":
                domainImg.setImageResource(R.drawable.domain_com);
                break;
            case ".co.in":
                domainImg.setImageResource(R.drawable.domain_co_in);
                break;
            case ".net":
                domainImg.setImageResource(R.drawable.domain_net);
                break;
            case ".org":
                domainImg.setImageResource(R.drawable.domain_org);
                break;
            default:
                break;
        }

        if(active){
            domainMessageTv.setVisibility(View.VISIBLE);
            domainImg.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
            emailImg.setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int margins = Methods.dpToPx(15, this);
            params.setMargins(margins, Methods.dpToPx(10, this), margins, 0);
            domainDetailsCard.setLayoutParams(params);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(margins, Methods.dpToPx(10, this), margins, margins);
            emailDetailsCard.setLayoutParams(params1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                domainDetailsCard.setElevation(3);
                emailDetailsCard.setElevation(3);
            }

            RecyclerView emailRunningRv = emailDetailsCard.findViewById(R.id.rv_email_running);
            emailDetailsCard.findViewById(R.id.ll_email_active).setVisibility(View.VISIBLE);
            emailRunningRv.setVisibility(View.VISIBLE);
            emailRunningRv.setLayoutManager(new LinearLayoutManager(this));
            emailRunningRv.setNestedScrollingEnabled(false);
            emailAdapter = new EmailAdapter(emailBookedList,active);
            emailRunningRv.setAdapter(emailAdapter);

            confirmRequestTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (emailBookedList.size()+totalBookedEmails-totalFailedEmails<EMAIL_BOOKING_NUM) {
                        if (!Methods.isOnline(DomainDetailsActivity.this)) {
                            Methods.snackbarNoInternet(DomainDetailsActivity.this);
                        } else if (bookingModelList.getEmailDomainNames().size() > 0) {
                            showLoader(getString(R.string.please_wait));
                                MixPanelController.track(MixPanelController.DOMAIN_EMAIL_REQUEST,null);
                            domainApiService.bookEmail(Constants.clientId, bookingModelList);
                        } else {
                            Toast.makeText(DomainDetailsActivity.this, "Please add emails", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(DomainDetailsActivity.this, "Email max limit achieved", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            addEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    enterEmailDialog(null);
                }
            });

            if (planExpiryDays<=0 || totalBookedEmails+emailBookedList.size()-totalFailedEmails>=EMAIL_BOOKING_NUM ||domainExpiryDays < 180){
                addEmail.setVisibility(View.GONE);
                confirmRequestTv.setVisibility(View.GONE);
            }/*else if (domainExpiryDays < 180){
//                TextView note = emailDetailsCard.findViewById(R.id.tv_note_message);
//                note.setVisibility(View.VISIBLE);
                //note.setText(Methods.fromHtml(getString(R.string.email_180_message)));
                addEmail.setVisibility(View.GONE);
                confirmRequestTv.setVisibility(View.GONE);
            }*/ else{
                bookingModelList = new EmailBookingModel();
                bookingModelList.setFpTag(session.getFpTag());
                bookingModelList.setEmailDomainNames(new ArrayList<EmailBookingModel.EmailDomainName>(EMAIL_BOOKING_NUM));
            }
        }else{
            domainMessageTv.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                domainDetailsCard.setElevation(0);
                emailDetailsCard.setElevation(0);
            }
            domainDetailsCard.setBackgroundColor(ContextCompat.getColor(this,R.color.e0e0e0));
            domainImg.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));

            emailDetailsCard.setBackgroundColor(ContextCompat.getColor(this,R.color.e0e0e0));
            emailDetailsCard.findViewById(R.id.view_divider).setVisibility(View.VISIBLE);
            emailImg.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));
            emailDetailsCard.findViewById(R.id.tv_domain_created_date).setVisibility(emailBookedList.size() == 0 ? View.VISIBLE : View.GONE);
            RecyclerView emailExpiredRv = emailDetailsCard.findViewById(R.id.rv_email_expired);
            emailExpiredRv.setVisibility(View.VISIBLE);
            emailExpiredRv.setLayoutManager(new LinearLayoutManager(this));
            emailExpiredRv.setHasFixedSize(true);
            emailExpiredRv.setAdapter(new EmailAdapter(emailBookedList,active));
        }

        if (!TextUtils.isEmpty(statusMessage)){
            statusTv.setVisibility(View.VISIBLE);
            domainDetailsCard.setContentPadding(0,Methods.dpToPx(5,this),0,0);
            statusTv.setText(statusMessage);
        }
    }
    private void showExpiryDialog(int showDialog, float days) {

        String callUsButtonText, cancelButtonText, dialogTitle, dialogMessage;
        int dialogImage, dialogImageBgColor;

        switch (showDialog) {
            case LIGHT_HOUSE_EXPIRE:
                callUsButtonText = getString(R.string.buy_in_capital);
                cancelButtonText = getString(R.string.later_in_capital);
                dialogTitle = getString(R.string.renew_light_house_plan);
                dialogMessage = getString(R.string.light_house_plan_expired_some_features_visible);
                dialogImage = R.drawable.androidexpiryxxxhdpi;
                dialogImageBgColor = Color.parseColor("#ff0010");
                break;
            case WILD_FIRE_EXPIRE:
                dialogTitle = getString(R.string.renew_wildfire_plan);
                dialogMessage = getString(R.string.continue_auto_promoting_on_google);
                callUsButtonText = getString(R.string.renew_in_capital);
                cancelButtonText = getString(R.string.ignore_in_capital);
                dialogImage = R.drawable.wild_fire_expire;
                dialogImageBgColor = Color.parseColor("#ffffff");
                break;
            case DEMO_EXPIRE:
                dialogImage = R.drawable.androidexpiryxxxhdpi;
                dialogImageBgColor = Color.parseColor("#ff0010");
                callUsButtonText = getString(R.string.buy_in_capital);
                cancelButtonText = getString(R.string.later_in_capital);
                dialogTitle = getString(R.string.buy_light_house_plan);
                dialogMessage = getString(R.string.demo_plan_expired);
                break;
            case DEMO_DAYS_LEFT:
                dialogImage = R.drawable.androidexpiryxxxhdpi;
                dialogImageBgColor = Color.parseColor("#ff0010");
                callUsButtonText = getString(R.string.buy_in_capital);
                cancelButtonText = getString(R.string.later_in_capital);
                dialogTitle = getString(R.string.buy_light_house_plan);
                if (days < 1) {
                    dialogMessage = String.format(getString(R.string.demo_plan_will_expire), "< 1");
                } else {
                    dialogMessage = String.format(getString(R.string.demo_plan_will_expire), (int) Math.floor(days) + " ");
                }
                break;
            case LIGHT_HOUSE_DAYS_LEFT:
                callUsButtonText = getString(R.string.buy_in_capital);
                cancelButtonText = getString(R.string.later_in_capital);
                dialogTitle = getString(R.string.renew_light_house_plan);

                if (days < 1) {
                    dialogMessage = String.format(getString(R.string.light_house_pla_will_expire), "< 1");
                } else {
                    dialogMessage = String.format(getString(R.string.light_house_pla_will_expire), (int) Math.floor(days) + " ");
                }

                dialogImage = R.drawable.androidexpiryxxxhdpi;
                dialogImageBgColor = Color.parseColor("#ff0010");
                break;
            default:
                return;
        }

        MaterialDialog mExpireDailog = new MaterialDialog.Builder(this)
                .customView(R.layout.pop_up_restrict_post_message, false)
                .backgroundColorRes(R.color.white)
                .positiveText(callUsButtonText)
                .negativeText(cancelButtonText)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(DomainDetailsActivity.this, BuildConfig.APPLICATION_ID.equalsIgnoreCase("com.biz2.nowfloats")
                                ?NewPricingPlansActivity.class: PricingPlansActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        dialog.dismiss();
                    }
                })
                .show();

        View view = mExpireDailog.getCustomView();

        roboto_md_60_212121 title = (roboto_md_60_212121) view.findViewById(R.id.textView1);
        title.setText(dialogTitle);

        ImageView expireImage = (ImageView) view.findViewById(R.id.img_warning);
        expireImage.setBackgroundColor(dialogImageBgColor);
        expireImage.setImageDrawable(ContextCompat.getDrawable(this, dialogImage));

        roboto_lt_24_212121 message = (roboto_lt_24_212121) view.findViewById(R.id.pop_up_create_message_body);
        message.setText(Methods.fromHtml(dialogMessage));
    }

    private void showLoader(final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(DomainDetailsActivity.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                }
                progressDialog.setMessage(message);
                progressDialog.show();
            }
        });
    }

    private void hideLoader() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    /*
   * Domain Details Param
   */
    private HashMap<String, String> getDomainDetailsParam() {
        HashMap<String, String> offersParam = new HashMap<>();
        offersParam.put("clientId", Constants.clientId);
        return offersParam;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_proceed:
                if (chooseBtn.isChecked()){
                    showLoader(getString(R.string.please_wait));
                    domainApiService.getDomainSupportedTypes(getDomainDetailsParam());
                }else{
                    linkDomain();
                }
                break;
            case R.id.btn_book_domain:
                showLoader(getString(R.string.please_wait));
                domainApiService.getDomainSupportedTypes(getDomainDetailsParam());
                break;
            case R.id.btn_renew_domain:
                bookDomain(domainName, domainType, DomainApiService.DomainAPI.RENEW_DOMAIN);

                break;
            case R.id.btn_link_domain:
                linkDomain();
                break;
            case R.id.btn_plan_expired:
                Intent intent = new Intent(DomainDetailsActivity.this, BuildConfig.APPLICATION_ID.equalsIgnoreCase("com.biz2.nowfloats")
                        ?NewPricingPlansActivity.class: PricingPlansActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            default:
                break;
        }
    }

    private void bookDomain(String domainName, String domainType, final DomainApiService.DomainAPI domainApi) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                //.title(getString(R.string.book_a_new_domain))
                .customView(R.layout.dialog_book_a_domain, false)
                .positiveColorRes(R.color.primaryColor);


        if (!isFinishing()) {
            domainBookDialog = builder.show();
            View maView = domainBookDialog.getCustomView();
            final EditText edtDomainName = (EditText) maView.findViewById(R.id.edtDomainName);
            final Spinner spDomainTypes = (Spinner) maView.findViewById(R.id.spDomainTypes);
            TextView tvCompanyName = (TextView) maView.findViewById(R.id.tvCompanyName);
            TextView tvTag = (TextView) maView.findViewById(R.id.tvTag);
            TextView tvAddress = (TextView) maView.findViewById(R.id.tvAddress);
            TextView tvCity = (TextView) maView.findViewById(R.id.tvCity);
            final EditText edtZip = (EditText) maView.findViewById(R.id.edtZip);
            TextView tvCountryCode = (TextView) maView.findViewById(R.id.tvCountryCode);
            TextView tvISDCode = (TextView) maView.findViewById(R.id.tvISDCode);
            TextView tvCountry = (TextView) maView.findViewById(R.id.tvCountry);
            TextView tvEmail = (TextView) maView.findViewById(R.id.tvEmail);
            TextView tvPrimaryNumber = (TextView) maView.findViewById(R.id.tvPrimaryNumber);
            final TextView tvPrice = (TextView) maView.findViewById(R.id.tvPrice);
            final TextView tvPriceDef = (TextView) maView.findViewById(R.id.tvPriceDef);
            Button btnActivateDomain = (Button) maView.findViewById(R.id.btnActivateDomain);
            Button btnBack = (Button) maView.findViewById(R.id.btnBack);
//            btnActivateDomain.setEnabled(false);
//            btnActivateDomain.setClickable(false);

            if(!TextUtils.isEmpty(domainName)) {
                arrDomainExtensions = new ArrayList<>();
                arrDomainExtensions.add(domainType);
                edtDomainName.setText(domainName);
                spDomainTypes.setEnabled(false);
                edtDomainName.setEnabled(false);
                if (hmPrices.containsKey(domainType)) {
                    tvPrice.setText(hmPrices.get(domainType) + "*");
                } else {
                    tvPrice.setText("");
                }
            }else if(domainApi == DomainApiService.DomainAPI.RENEW_DOMAIN){
                domainBookDialog.dismiss();
                showCustomDialog(getString(R.string.domain_booking_failed),getString(R.string.drop_us_on_email_or_call) ,
                        getString(R.string.ok), null, DialogFrom.FAILED);
                return;
            }
            tvPriceDef.setText(String.format(getString(R.string.price_of_domain), arrDomainExtensions.get(0)));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(DomainDetailsActivity.this,
                    android.R.layout.simple_spinner_item, arrDomainExtensions);
            spDomainTypes.setAdapter(arrayAdapter);

            spDomainTypes.setSelection(0);

            tvTag.setText(get_fp_details_model.getAliasTag());
            tvCompanyName.setText(get_fp_details_model.getTag());
            tvAddress.setText(get_fp_details_model.getAddress());
            tvCity.setText(get_fp_details_model.getCity());
            if (!TextUtils.isEmpty(get_fp_details_model.getPinCode())) {
                edtZip.setText(get_fp_details_model.getPinCode());
                edtZip.setBackgroundDrawable(null);
                edtZip.setClickable(false);
                edtZip.setEnabled(false);
            }
            tvCountryCode.setText(get_fp_details_model.getLanguageCode());
            tvISDCode.setText(get_fp_details_model.getCountryPhoneCode());
            tvCountry.setText(get_fp_details_model.getCountry());
            tvEmail.setText(get_fp_details_model.getEmail());
            tvPrimaryNumber.setText(get_fp_details_model.getPrimaryNumber());

            btnActivateDomain
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Methods.hideKeyboard(DomainDetailsActivity.this);
                            String domainName = edtDomainName.getText().toString();
                            if (TextUtils.isEmpty(domainName)) {
                                Methods.showSnackBarNegative(DomainDetailsActivity.this,
                                        getString(R.string.enter_domain_name));
                            } else if (TextUtils.isEmpty(edtZip.getText().toString())) {
                                Methods.showSnackBarNegative(DomainDetailsActivity.this,
                                        getString(R.string.enter_zip_code));
                            } else {
                                showLoader(getString(R.string.please_wait));
                                get_fp_details_model.setDomainName(domainName);
                                get_fp_details_model.setDomainValidityInYears("1");
                                get_fp_details_model.setDomainType(spDomainTypes.getSelectedItem().toString());
                                get_fp_details_model.setPinCode(edtZip.getText().toString());
                                domainApiService.checkDomainAvailability(domainName, getDomainAvailabilityParam((String) spDomainTypes.getSelectedItem()),domainApi);
                            }
                        }
                    });

            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    domainBookDialog.dismiss();
                }
            });

            spDomainTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tvPriceDef.setText(String.format(getString(R.string.price_of_domain), arrDomainExtensions.get(position)));
                    if (hmPrices.containsKey(arrDomainExtensions.get(position))) {
                        tvPrice.setText(hmPrices.get(arrDomainExtensions.get(position)) + "*");
                    } else {
                        tvPrice.setText("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }
    private enum DialogFrom {
        DOMAIN_AVAILABLE,
        CONTACTS_AND_EMAIL_REQUIRED,
        CATEGORY_REQUIRED,
        ADDRESS_REQUIRED,
        DEFAULT,
        FAILED,
        NO_CLOSE
    }

    private void showCustomDialog(String title, String message, String postiveBtn, String negativeBtn,
                                  final DialogFrom dialogFrom) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(DomainDetailsActivity.this)
                .title(title)
                .customView(R.layout.dialog_link_layout, false)
                .positiveText(postiveBtn)
                .negativeText(negativeBtn)
                .positiveColorRes(R.color.primaryColor)
                .negativeColorRes(R.color.primaryColor)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        switch (dialogFrom) {

                            case DOMAIN_AVAILABLE:
                                prepareAndPublishDomain();
                                break;
                            case FAILED:
                                break;
                            case DEFAULT:
                                finish();
                                break;
                            case NO_CLOSE:
                                break;
                        }
                    }
                    /*
                    ((SidePanelFragment.OnItemClickListener) activity).
                onClick(getResources().getString(R.string.business_profile));
                     */

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);

                        switch (dialogFrom) {

                            case DOMAIN_AVAILABLE:
                                MixPanelController.track(MixPanelController.DOMAIN_SEARCH, null);
                                break;
                            case DEFAULT:

                                break;
                        }
                    }
                });

        final MaterialDialog materialDialog = builder.show();
        View maView = materialDialog.getCustomView();

        TextView tvMessage = (TextView) maView.findViewById(R.id.toast_message_to_contact);
        tvMessage.setText(message);
    }

    private void prepareAndPublishDomain() {
        MixPanelController.track(MixPanelController.BOOK_DOMAIN, null);
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("clientId", Constants.clientId);
        hashMap.put("domainName", get_fp_details_model.getDomainName());
        hashMap.put("domainType", get_fp_details_model.getDomainType());
        hashMap.put("existingFPTag", session.getFpTag());
        hashMap.put("addressLine1", get_fp_details_model.getAddress());
        hashMap.put("city", get_fp_details_model.getCity());
        hashMap.put("companyName", get_fp_details_model.getTag());
        hashMap.put("contactName", TextUtils.isEmpty(get_fp_details_model.getContactName()) ?
                session.getFpTag() : get_fp_details_model.getContactName());
        hashMap.put("country", get_fp_details_model.getCountry());
        hashMap.put("countryCode", get_fp_details_model.getCountryPhoneCode());
        hashMap.put("email", get_fp_details_model.getEmail());
        hashMap.put("lat", get_fp_details_model.getLat());
        hashMap.put("lng", get_fp_details_model.getLng());
        hashMap.put("validityInYears",get_fp_details_model.getDomainValidityInYears());
        hashMap.put("phoneISDCode", get_fp_details_model.getCountryPhoneCode());
        if (get_fp_details_model.getCategory() != null && get_fp_details_model.getCategory().size() > 0)
            hashMap.put("primaryCategory", get_fp_details_model.getCategory().get(0).getKey());
        else
            hashMap.put("primaryCategory", "");
        hashMap.put("primaryNumber", get_fp_details_model.getPrimaryNumber());
        hashMap.put("regService", "");
        hashMap.put("state", get_fp_details_model.getPaymentState());
        hashMap.put("zip", get_fp_details_model.getPinCode());
        domainApiService.buyDomain(hashMap);
    }

    @Override
    public void domainBookStatus(String response) {

        if (!TextUtils.isEmpty(response) &&
                response.equalsIgnoreCase(getString(R.string.domain_booking_process_message))) {

            showCustomDialog(getString(R.string.domain_booking_process),
                    getString(R.string.domain_booking_process_message),
                    getString(R.string.ok), null, DialogFrom.DEFAULT);

        } else {

            if (TextUtils.isEmpty(response)) {
                response = getString(R.string.domain_booking_failed);
            }
            showCustomDialog(getString(R.string.book_a_new_domain),
                    response,
                    getString(R.string.ok), null, DialogFrom.DEFAULT);
        }
    }

    @Override
    public void getFpDetails(Get_FP_Details_Model model) {

    }

    private class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.MyWorkingEmailHolder>{

        ArrayList<EmailBookingModel.AddEmailModel> list;
        boolean isDomainActive;
        EmailAdapter(ArrayList<EmailBookingModel.AddEmailModel> list, boolean isDomainActive){
            this.list = list;
            this.isDomainActive = isDomainActive;
        }
        @Override
        public MyWorkingEmailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            EmailType type = EmailType.getEmailType(viewType);
            if (type == null){
                return null;
            }
          /*  switch (type){
                case ADD_EMAIL:
                    v = LayoutInflater.from(DomainDetailsActivity.this).inflate(R.layout.layout_email_item,parent,false);
                    break;
                case ADDED:

                    break;
            }*/
            View v = LayoutInflater.from(DomainDetailsActivity.this).inflate(R.layout.layout_email_item,parent,false);
            return new MyWorkingEmailHolder(v,type);
        }


        @Override
        public void onBindViewHolder(MyWorkingEmailHolder holder, int position) {
            switch (holder.type){
                case ADD_EMAIL:
                    //holder.titleTv.setVisibility(View.GONE);
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.edit.setVisibility(View.VISIBLE);
                    holder.status.setVisibility(View.GONE);
                    holder.titleTv.setText("Request not submit");
                    break;
                case ADDED:
                    //holder.titleTv.setVisibility(View.VISIBLE);
                    holder.status.setVisibility(isDomainActive?View.VISIBLE:View.GONE);
                    holder.status.setImageResource(R.drawable.domain_available);
                    holder.delete.setVisibility(View.GONE);
                    holder.edit.setVisibility(View.GONE);
                    holder.titleTv.setText(String.format(Locale.ENGLISH,"Email Address %d:",position+1));
                    break;
                case FAILED:
                    //holder.titleTv.setVisibility(View.GONE);
                    holder.delete.setVisibility(View.GONE);
                    holder.edit.setVisibility(View.GONE);
                    holder.status.setVisibility(isDomainActive?View.VISIBLE:View.GONE);
                    holder.status.setImageResource(R.drawable.domain_not_available);
                    holder.titleTv.setText("Request failed");
                    break;
                case IN_PROCESS:
                    //holder.titleTv.setVisibility(View.GONE);
                    holder.delete.setVisibility(View.GONE);
                    holder.edit.setVisibility(View.GONE);
                    holder.status.setVisibility(isDomainActive?View.VISIBLE:View.GONE);
                    holder.status.setImageResource(R.drawable.domain_check);
                    holder.titleTv.setText("Request pending");
                    break;

            }

            holder.domainTv.setText(list.get(position).getEmailId());
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).getType().ordinal();
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MyWorkingEmailHolder extends RecyclerView.ViewHolder{

            TextView titleTv, domainTv;
            EmailType type;
            ImageView delete, edit,status;
            public MyWorkingEmailHolder(View itemView, EmailType type) {
                super(itemView);
                titleTv = itemView.findViewById(R.id.tv_email_title);
                domainTv = itemView.findViewById(R.id.tv_email_domain);
                edit = itemView.findViewById(R.id.img_edit);
                delete = itemView.findViewById(R.id.img_delete);
                status = itemView.findViewById(R.id.img_email_status);
                this.type = type;
                status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (emailBookedList.get(getAdapterPosition()).getType()){
                            case FAILED:
                                Toast.makeText(DomainDetailsActivity.this, "Request failed", Toast.LENGTH_SHORT).show();
                                break;
                            case ADDED:
                                Toast.makeText(DomainDetailsActivity.this, "Request successful", Toast.LENGTH_SHORT).show();
                                break;
                            case IN_PROCESS:
                                Toast.makeText(DomainDetailsActivity.this, "Request is pending", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MaterialDialog dialog = new MaterialDialog.Builder(DomainDetailsActivity.this)
                                .content("Do you want to delete this email?")
                                .positiveColorRes(R.color.primaryColor)
                                .negativeColorRes(R.color.primaryColor)
                                .positiveText("Cancel")
                                .negativeText("Delete")
                                .autoDismiss(true)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        for (EmailBookingModel.EmailDomainName emailData : bookingModelList.getEmailDomainNames()){
                                            if (emailBookedList.get(getAdapterPosition()).getEmailId()
                                                    .equalsIgnoreCase(emailData.getUsername()+"@"+domainNameTv.getText().toString().trim())){
                                                emailBookedList.remove(getAdapterPosition());
                                                bookingModelList.getEmailDomainNames().remove(emailData);
                                                notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                        if ((emailBookedList.size()+totalBookedEmails - totalFailedEmails)<EMAIL_BOOKING_NUM){
                                            addEmail.setVisibility(View.VISIBLE);
                                        }

                                        confirmRequestTv.setVisibility(bookingModelList.getEmailDomainNames().size()>0?View.VISIBLE:View.GONE);
                                    }
                                }).show();

                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        enterEmailDialog(emailBookedList.get(getAdapterPosition()).getEmailId());
                    }
                });
            }
        }
    }

}
