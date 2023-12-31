package com.nowfloats.BusinessProfile.UI.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.framework.views.customViews.CustomToolbar;
import com.nowfloats.BusinessProfile.UI.Model.FacebookFeedPullModel;
import com.nowfloats.CustomWidget.roboto_lt_24_212121;
import com.nowfloats.CustomWidget.roboto_md_60_212121;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NFXApi.NfxRequestClient;
import com.nowfloats.NavigationDrawer.API.twitter.FacebookFeedPullRegistrationAsyncTask;
import com.nowfloats.NavigationDrawer.SiteMeter.Site_Meter_Fragment;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.twitter.TwitterConnection;
import com.nowfloats.util.BoostLog;
import com.nowfloats.util.Constants;
import com.nowfloats.util.DataBase;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.nowfloats.util.WebEngageController;
import com.squareup.picasso.Picasso;
import com.thinksity.BuildConfig;
import com.thinksity.R;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.framework.webengageconstant.EventLabelKt.EVENT_LABEL_FB_PAGE_SHARING_FAILED;
import static com.framework.webengageconstant.EventNameKt.FB_PAGE_SHARING_FAILED;

public class Social_Sharing_Activity extends AppCompatActivity implements NfxRequestClient.NfxCallBackListener, TwitterConnection.TwitterResult {
    private static final int PAGE_NO_FOUND = 404;
    private static final int FB_PAGE_CREATION = 101;
    private final static String FB_PAGE_DEFAULT_LOGO = "https://s3.ap-south-1.amazonaws.com/nfx-content-cdn/logo.png";
    private final static String FB_PAGE_COVER_PHOTO = "https://cdn.nowfloats.com/fpbkgd-kitsune/abstract/24.jpg";
    private final int LIGHT_HOUSE_EXPIRE = 0;
    private final int WILD_FIRE_EXPIRE = 1;
    private final int DEMO_EXPIRE = 3;
    private final int FBTYPE = 0;
    private final int FBPAGETYPE = 1;
    private final int TWITTERTYPE = 2;
    private final int FB_DECTIVATION = 3;
    private final int FB_PAGE_DEACTIVATION = 4;
    private final int TWITTER_DEACTIVATION = 11;
    private final int FROM_AUTOPOST = 1;
    private final int FROM_FB_PAGE = 0;
    int size = 0;
    boolean[] checkedPages;
    UserSessionManager session;
    TextView connectTextView, topFeatureTextView;
    SharedPreferences.Editor prefsEditor;
    ArrayList<String> items;
    Handler handler = new Handler();
    private CustomToolbar  toolbar;
    //final Facebook facebook = new Facebook(Constants.FACEBOOK_API_KEY);
    private SharedPreferences pref = null;
    private ImageView facebookHome;
    private ImageView facebookPage;


    //Rahul Twitter
    private ImageView twitter;
    private ImageView ivFbPageAutoPull;
    private TextView facebookHomeStatus, facebookPageStatus, twitterStatus, fbPullStatus;
    private CheckBox facebookHomeCheckBox, facebookPageCheckBox, twitterCheckBox;
    private CheckBox facebookautopost;
    private int numberOfUpdates = 0;
    private boolean numberOfUpdatesSelected = false;
    private Activity activity;
    private SharedPreferences mSharedPreferences = null;
    private ProgressDialog progressDialog = null;
    private int mNewPosition = -1;
    private CallbackManager callbackManager;
    private TextView arrowTextView;
    private TwitterConnection twitterConnection;
    private String fpPageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_social_sharing2);


        session = new UserSessionManager(getApplicationContext(), Social_Sharing_Activity.this);
        // Facebook_Auto_Publish_API.autoPublish(Social_Sharing_Activity.this,session.getFPID());
        Methods.isOnline(Social_Sharing_Activity.this);
        pref = this.getSharedPreferences(Constants.PREF_NAME, Activity.MODE_PRIVATE);
        prefsEditor = pref.edit();
        mSharedPreferences = this.getSharedPreferences(TwitterConnection.PREF_NAME, MODE_PRIVATE);
        activity = Social_Sharing_Activity.this;

        toolbar = (CustomToolbar) findViewById(R.id.toolbar);

        Typeface myCustomFont = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
        Typeface myCustomFont_Medium = Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf");

        setSupportActionBar(toolbar);
       /* headerText = (TextView) toolbar.findViewById(R.id.titleTextView);
        headerText.setText(getResources().getString(R.string.third_party_integration));*/

        if (getSupportActionBar() != null) {
            setTitle("Social Sharing");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        facebookHome = (ImageView) findViewById(R.id.social_sharing_facebook_profile_image);
        facebookPage = (ImageView) findViewById(R.id.social_sharing_facebook_page_image);
        twitter = (ImageView) findViewById(R.id.social_sharing_twitter_image);
        ivFbPageAutoPull = (ImageView) findViewById(R.id.auto_pull_facebook_page_image);

        facebookHomeStatus = (TextView) findViewById(R.id.social_sharing_facebook_profile_flag_text);
        facebookPageStatus = (TextView) findViewById(R.id.social_sharing_facebook_page_flag_text);
        twitterStatus = (TextView) findViewById(R.id.social_sharing_twitter_flag_text);
        fbPullStatus = (TextView) findViewById(R.id.tv_fb_page_name);
        connectTextView = (TextView) findViewById(R.id.connectTextView);
        //autoPostTextView = (TextView) findViewById(R.id.autoPostTextView);
        topFeatureTextView = (TextView) findViewById(R.id.topFeatureText);
        arrowTextView = (TextView) findViewById(R.id.guidelines_arrow_text);
        //Quikr added
        CardView card = (CardView) findViewById(R.id.quikr_card);
        card.setVisibility(View.GONE);
//        if (!Constants.PACKAGE_NAME.equals("com.biz2.nowfloats")) {
//            card.setVisibility(View.GONE);
//        } else {
//            final String[] quikrArray = getResources().getStringArray(R.array.quikr_widget);
//            if ("91".equals(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRYPHONECODE))) {
//                for (String category : quikrArray) {
//                    if (category.contains(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY).toLowerCase())) {
//                        card.setVisibility(View.VISIBLE);
//                        break;
//                    }
//                }
//            }
//        }

        facebookHomeStatus.setTypeface(myCustomFont);
        facebookPageStatus.setTypeface(myCustomFont);
        twitterStatus.setTypeface(myCustomFont);
        fbPullStatus.setTypeface(myCustomFont);

        facebookHomeCheckBox = (CheckBox) findViewById(R.id.social_sharing_facebook_profile_checkbox);
        facebookPageCheckBox = (CheckBox) findViewById(R.id.social_sharing_facebook_page_checkbox);
        twitterCheckBox = (CheckBox) findViewById(R.id.social_sharing_twitter_checkbox);
        facebookautopost = (CheckBox) findViewById(R.id.social_sharing_facebook_page_auto_post);


        connectTextView.setTypeface(myCustomFont_Medium);
        //autoPostTextView.setTypeface(myCustomFont);
        topFeatureTextView.setTypeface(myCustomFont_Medium);
        arrowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Social_Sharing_Activity.this, QuikrGuidelinesActivity.class);
                intent.putExtra("array", getResources().getStringArray(R.array.quikr_tip_points));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


            }
        });

        facebookPageCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.APPLICATION_ID.equals("com.redtim")) {
                    facebookPageCheckBox.setChecked(false);
                    Toast.makeText(Social_Sharing_Activity.this, "Facebook is not working", Toast.LENGTH_SHORT).show();
                } else if (facebookPageCheckBox.isChecked()) {
                    //Toast.makeText(Social_Sharing_Activity.this,"Reconnect with facebook",Toast.LENGTH_SHORT).show();
                    facebookPageCheckBox.setChecked(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            fbData(FROM_FB_PAGE);
                        }
                    }, 200);
                    //startActivity(new Intent(Social_Sharing_Activity.this,LinkedinWebView.class));
                    //createFBPage(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME));

                } else {
                    NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("facebookpage")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmUserAccountId("")
                            .setmAppAccessTokenKey("")
                            .setmAppAccessTokenSecret("")
                            .setmCallType(FB_PAGE_DEACTIVATION)
                            .setmName("");
                    requestClient.connectNfx();

                    showLoader(getString(R.string.wait_while_unsubscribing));
                }
            }
        });

        facebookHomeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BuildConfig.APPLICATION_ID.equals("com.redtim")) {
                    facebookHomeCheckBox.setChecked(false);
                    Toast.makeText(Social_Sharing_Activity.this, getString(R.string.facebook_is_not_working), Toast.LENGTH_SHORT).show();
                } else if (facebookHomeCheckBox.isChecked()) {
                    facebookHomeCheckBox.setChecked(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            fbData(FROM_FB_PAGE);
                        }
                    }, 200);
                } else {

                    NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("facebookusertimeline")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmAppAccessTokenKey("")
                            .setmAppAccessTokenSecret("")
                            .setmUserAccountId("")
                            .setmCallType(FB_DECTIVATION)
                            .setmName("");
                    requestClient.connectNfx();

                    NfxRequestClient pageRequestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("facebookpage")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmAppAccessTokenKey("")
                            .setmAppAccessTokenSecret("")
                            .setmUserAccountId("")
                            .setmCallType(FB_PAGE_DEACTIVATION)
                            .setmName("");
                    pageRequestClient.connectNfx();
                    showLoader(getString(R.string.wait_while_unsubscribing));

                }
            }
        });

        facebookautopost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String paymentState = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PAYMENTSTATE);
                String paymentLevel = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PAYMENTLEVEL);
                if (BuildConfig.APPLICATION_ID.equals("com.redtim")) {
                    facebookautopost.setChecked(false);
                    Toast.makeText(Social_Sharing_Activity.this, getString(R.string.facebook_is_not_working), Toast.LENGTH_SHORT).show();
                } else if (paymentState.equals("-1")) {
                    try {

                        if (Constants.PACKAGE_NAME.equals("com.kitsune.biz")) {
                            return;
                        }

                        if (Integer.parseInt(paymentLevel) > 10) {
                            showDialog1(LIGHT_HOUSE_EXPIRE, -1);
                        } else {
                            showDialog1(DEMO_EXPIRE, -1);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    facebookautopost.setChecked(false);
                } else {
                    if (facebookautopost.isChecked()) {
                        // connecting to auto pull
                        facebookautopost.setChecked(false);
                        fbData(FROM_AUTOPOST);
                    } else {
                        //disconnected to auto pull
                        updateAutopull(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME), false);
                    }
                }

            }
        });


        twitterCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (twitterCheckBox.isChecked()) {
                    twitterCheckBox.setChecked(false);
                    if (!Methods.isOnline(Social_Sharing_Activity.this)) {
                        showAlertBox();
                    } else {
                        if (twitterConnection == null) {
                            twitterConnection = new TwitterConnection(Social_Sharing_Activity.this, Social_Sharing_Activity.this);
                        }
                        twitterConnection.authorize();
                    }
                    //Rahul twitter
                } else {
                    NfxRequestClient requestClient1 = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                            .setmFpId(session.getFPID())
                            .setmType("twitter")
                            .setmUserAccessTokenKey("")
                            .setmUserAccessTokenSecret("")
                            .setmAppAccessTokenKey("")
                            .setmAppAccessTokenSecret("")
                            .setmUserAccountId(String.valueOf(""))
                            .setmCallType(TWITTER_DEACTIVATION)
                            .setmName("");
                    requestClient1.connectNfx();
                    showLoader(getString(R.string.wait_while_unsubscribing));

                }
            }
        });
        findViewById(R.id.iv_help_tool).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = getString(R.string.updates_will_reflect_on_your_website);
                showDialog("Tip!", message, "Done");
            }
        });

        InitShareResources();
        setStatus();
    }

    private void showDialog1(int showDialog, float days) {

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
            case DEMO_EXPIRE:
                dialogImage = R.drawable.androidexpiryxxxhdpi;
                dialogImageBgColor = Color.parseColor("#ff0010");
                callUsButtonText = getString(R.string.buy_in_capital);
                cancelButtonText = getString(R.string.later_in_capital);
                dialogTitle = getString(R.string.buy_light_house_plan);
                dialogMessage = getString(R.string.demo_plan_expired);
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

    private void updateAutopull(String name, boolean autoPublish) {
        numberOfUpdatesSelected = false;
        FacebookFeedPullModel.Update obj = new FacebookFeedPullModel().new Update();
        try {
            obj.setFpId(session.getFPID());
            obj.setAutoPublish(autoPublish);
            obj.setClientId(Constants.clientId);
            obj.setFacebookPageName(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FacebookFeedPullRegistrationAsyncTask fap = new FacebookFeedPullRegistrationAsyncTask(Social_Sharing_Activity.this, fbPullStatus, ivFbPageAutoPull, facebookautopost, session);
        fap.autoUpdate(obj);
    }

    private void autoPostSelectListener(String pageName) {
        /*called = true;
        boolean FbRegistered = pref.getBoolean("FacebookFeedRegd", false);
        if (FbRegistered == false) {
            if (!Util.isNullOrEmpty(Constants.fbPageFullUrl)) {
                pullFacebookFeedDialog();
            } else {
                Util.toast("Please select a Facebook page", getApplicationContext());
                facebookautopost.setChecked(false);
            }
        } else {
            final JSONObject obj = new JSONObject();
            try {
                obj.put("fpId", session.getFPID());
                obj.put("autoPublish", true);
                obj.put("clientId", Constants.clientId);
                obj.put("FacebookPageName", Constants.fbFromWhichPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FacebookFeedPullAutoPublishAsyncTask fap = new FacebookFeedPullAutoPublishAsyncTask(Social_Sharing_Activity.this, obj, true, facebookPageStatus);
            fap.execute();
        }*/
        if (numberOfUpdatesSelected) {
            FacebookFeedPullModel.Registration obj = new FacebookFeedPullModel().new Registration();
            try {
                obj.setTag(session.getFpTag());
                obj.setAutoPublish(true);
                obj.setClientId(Constants.clientId);
                obj.setFacebookPageName(pageName);
                obj.setCount(numberOfUpdates);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FacebookFeedPullRegistrationAsyncTask fap = new FacebookFeedPullRegistrationAsyncTask(Social_Sharing_Activity.this, fbPullStatus, ivFbPageAutoPull, facebookautopost, session);
            fap.autoRegister(obj);
        } else {
            facebookautopost.setChecked(false);
        }
    }


    private void selectNumberUpdatesDialog(final String name) {
        final String[] array = getResources().getStringArray(R.array.post_updates);
        new MaterialDialog.Builder(Social_Sharing_Activity.this)
                .title(getString(R.string.post_on_website))
                .items(array)
                .negativeText(getString(R.string.cancel))
                .cancelable(false)
                .positiveText(getString(R.string.ok))
                .negativeColorRes(R.color.light_gray)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        facebookautopost.setChecked(false);
                        dialog.dismiss();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        int position = dialog.getSelectedIndex();
                        if (position == 0) {
                            numberOfUpdates = 5;
                            numberOfUpdatesSelected = true;
                        } else if (position == 1) {
                            numberOfUpdates = 10;
                            numberOfUpdatesSelected = true;
                        } else {
                            // == 0 ? 5 : dialog.getSelectedIndex() ==1 ? 10 : 5;
                            Toast.makeText(Social_Sharing_Activity.this, R.string.please_select_any_facebook_page, Toast.LENGTH_SHORT).show();
                            numberOfUpdatesSelected = false;
                        }
                        autoPostSelectListener(name);
                        dialog.dismiss();
                    }
                })
                .widgetColorRes(R.color.primaryColor)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {

                        //session.storeShowUpdates(false);
                        if (position == 0) {
                            numberOfUpdates = 5;
                        }

                        if (position == 1) {
                            numberOfUpdates = 10;
                        }
                        //dialog.dismiss();
                        return true;
                    }
                }).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);//added

        if (twitterConnection != null) {
            twitterConnection.onActivityResult(requestCode, resultCode, data);
        }
        //facebook.authorizeCallback(requestCode, resultCode, data);//removed
    }
//added


    @Override
    protected void onStart() {
        super.onStart();
    }

    public void getFacebookPages(AccessToken accessToken, final int from) {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/me/accounts",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // Insert your code here
                        processGraphResponse(response, from);
                    }
                });

        request.executeAsync();
    }

    private void processGraphResponse(final GraphResponse response, final int from) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject pageMe = response.getJSONObject();
                    Constants.FbPageList = pageMe.getJSONArray("data");
                    if (Constants.FbPageList != null) {
                        size = Constants.FbPageList.length();

                        checkedPages = new boolean[size];
                        if (size > 0) {
                            items = new ArrayList<String>();
                            for (int i = 0; i < size; i++) {
                                items.add(i, (String) ((JSONObject) Constants.FbPageList
                                        .get(i)).get("name"));
                                //BoostLog.d("ILUD Test: ", (String) ((JSONObject) Constants.FbPageList
                                //.get(i)).get("name"));
                            }

                            for (int i = 0; i < size; i++) {
                                checkedPages[i] = false;
                            }


                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {

                    Social_Sharing_Activity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (items != null && items.size() > 0) {
                                final String[] array = items.toArray(new String[items.size()]);
                                if (!isFinishing()) {
                                    new MaterialDialog.Builder(Social_Sharing_Activity.this)
                                            .title(getString(R.string.select_page))
                                            .items(array)
                                            .widgetColorRes(R.color.primaryColor)
                                            .cancelable(false)
                                            .positiveText("Ok")
                                            .negativeText("Cancel")
                                            .negativeColorRes(R.color.light_gray)
                                            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, View view, int position, CharSequence text) {

                                                    //dialog.dismiss();
                                                    mNewPosition = position;
                                                    return true;
                                                }
                                            })
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    mNewPosition = dialog.getSelectedIndex();
                                                    if (mNewPosition == -1) {
                                                        Toast.makeText(Social_Sharing_Activity.this, "Please select any Facebook page", Toast.LENGTH_SHORT).show();
                                                        /*if (from == FROM_FB_PAGE) {
                                                            facebookPageCheckBox.setChecked(false);
                                                        } else if (from == FROM_AUTOPOST) {
                                                            facebookautopost.setChecked(false);
                                                        }*/
                                                    } else {
                                                        String strName = array[mNewPosition];
                                                        String FACEBOOK_PAGE_ID = null;
                                                        String page_access_token = null;
                                                        try {
                                                            FACEBOOK_PAGE_ID = (String) ((JSONObject) Constants.FbPageList.get(mNewPosition)).get("id");
                                                            page_access_token = ((String) ((JSONObject) Constants.FbPageList.get(mNewPosition)).get("access_token"));
                                                        } catch (JSONException e) {

                                                        }
                                                        if (from == FROM_FB_PAGE && !Util.isNullOrEmpty(FACEBOOK_PAGE_ID) && !Util.isNullOrEmpty(page_access_token)) {
                                                            session.storePageAccessToken(page_access_token);
                                                            session.storeFacebookPageID(FACEBOOK_PAGE_ID);
                                                            if (Util.isNullOrEmpty(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME))
                                                                    || !pref.getBoolean("FBFeedPullAutoPublish", false)
                                                                    || !strName.equals(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME))) {
                                                                pageSeleted(mNewPosition, strName, session.getFacebookPageID(), session.getPageAccessToken());
                                                            } else {
                                                                //facebookPageCheckBox.setChecked(false);
                                                                showDialog("Alert", getString(R.string.you_cannot_select_the_same_facebook_page_to_share_your_updates), getString(R.string.done_));
                                                            }
                                                            //pageSeleted(position, strName, session.getFacebookPageID(), session.getPageAccessToken());
                                                        } else if (from == FROM_AUTOPOST) {
                                                            if (Util.isNullOrEmpty(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME))) {
                                                                selectNumberUpdatesDialog(FACEBOOK_PAGE_ID);
                                                            } else if (!strName.equals(session.getFacebookPage())) {
                                                                updateAutopull(FACEBOOK_PAGE_ID, true);
                                                            } else {
                                                                //Toast.makeText(getApplicationContext(), "You can't post and pull from the same Facebook page", Toast.LENGTH_SHORT).show();
                                                                //facebookautopost.setChecked(false);
                                                                showDialog("Alert", getString(R.string.you_cannot_select_the_same_facebook_page_to_auto_update_your_website), getString(R.string.done_));
                                                            }
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                }

                                                @Override
                                                public void onNegative(MaterialDialog dialog) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            } else {
                                if (from == FROM_AUTOPOST) {
                                    Methods.materialDialog(activity, "Alert", getString(R.string.look_like_no_facebook_page));
                                } else {
                                    NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                                            .setmFpId(session.getFPID())
                                            .setmType("facebookpage")
                                            .setmCallType(PAGE_NO_FOUND)
                                            .setmName("");
                                    requestClient.nfxNoPageFound();
                                    showLoader(getString(R.string.please_wait));
                                }
                            }
                        }
                    });
                }

            }
        }).start();
    }

    public void pageSeleted(int id, final String pageName, String pageID, String pageAccessToken) {
        String s = "";
        JSONObject obj;
        session.storeFacebookPage(pageName);
        JSONArray data = new JSONArray();

        NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                .setmFpId(session.getFPID())
                .setmType("facebookpage")
                .setmUserAccessTokenKey(pageAccessToken)
                .setmUserAccessTokenSecret("null")
                .setmAppAccessTokenKey("")
                .setmAppAccessTokenSecret("")
                .setmUserAccountId(pageID)
                .setmCallType(FBPAGETYPE)
                .setmName(pageName);
        requestClient.connectNfx();

        showLoader(getString(R.string.wait_while_subscribing));

        DataBase dataBase = new DataBase(activity);
        dataBase.updateFacebookPage(pageName, pageID, pageAccessToken);

        obj = new JSONObject();
        try {
            obj.put("id", pageID);
            obj.put("access_token", pageAccessToken);
            data.put(obj);

            Constants.fbPageFullUrl = "https://www.facebook.com/pages/" + pageName + "/" + pageID;
            Constants.fbFromWhichPage = pageName;
            prefsEditor.putString("fbPageFullUrl",
                    Constants.fbPageFullUrl);
            prefsEditor.putString("fbFromWhichPage",
                    Constants.fbFromWhichPage);
            prefsEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        obj = new JSONObject();
        try {
            obj.put("data", data);
            Constants.FbPageList = data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String fbPageData = obj.toString();
        if (!Util.isNullOrEmpty(fbPageData)) {
            if (fbPageData.equals("{\"data\":[]}")) {
                prefsEditor.putString("fbPageData", "");
                Constants.fbPageShareEnabled = false;
                prefsEditor.putBoolean("fbPageShareEnabled",
                        Constants.fbPageShareEnabled);
                prefsEditor.commit();
                Constants.FbPageList = null;
                //InitShareResources();

            } else {
                Constants.fbPageShareEnabled = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_social__sharing, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setCallback();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void setCallback() {
        setResult(Constants.fbPageShareEnabled ? RESULT_OK : RESULT_CANCELED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void fbData(final int from) {
        //AccessToken.getCurrentAccessToken()
        List<String> readPermissions = Arrays.asList(Constants.FACEBOOK_READ_PERMISSIONS);
        final List<String> publishPermissions = Arrays.asList(Constants.FACEBOOK_PUBLISH_PERMISSIONS);
        final LoginManager loginManager = LoginManager.getInstance();

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Set<String> permissions = loginResult.getAccessToken().getPermissions();
                boolean contain = permissions.containsAll(publishPermissions);
                // Log.v("ggg",contain+"permission"+loginResult.getAccessToken().getPermissions());
                if (!contain) {
                    loginManager.logInWithPublishPermissions(Social_Sharing_Activity.this, publishPermissions);
                } else {

                    //Log.v("ggg",FACEBOOK_ACCESS_TOKEN+"ppnull");
                    if (Profile.getCurrentProfile() == null && from == FROM_FB_PAGE) {
                        getFacebookProfile(loginResult.getAccessToken(), from);
                    } else {
                        //Log.v("ggg",Profile.getCurrentProfile().toString());
                        if (from == FROM_FB_PAGE) {
                            saveFbLoginResults(Profile.getCurrentProfile().getName(),
                                    loginResult.getAccessToken().getToken(),
                                    Profile.getCurrentProfile().getId());
                        }
                        getFacebookPages(loginResult.getAccessToken(), from);
                    }
                }
            }

            @Override
            public void onCancel() {
                onFBPageError(from);
            }

            @Override
            public void onError(FacebookException error) {
                onFBPageError(from);
                WebEngageController.trackEvent(FB_PAGE_SHARING_FAILED, EVENT_LABEL_FB_PAGE_SHARING_FAILED, session.getFpTag());
                //Log.v("ggg",error.toString()+"fberror");
            }
        });
        loginManager.logInWithReadPermissions(this, readPermissions);
    }

    private void showLoader(final String message) {
        if (isFinishing()) return;
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideLoader() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void getFacebookProfile(final AccessToken accessToken, final int from) {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        GraphRequest meRequest = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            JSONObject resp = response.getJSONObject();
                            saveFbLoginResults(resp.getString("name"),
                                    accessToken.getToken(),
                                    resp.getString("id"));
                            getFacebookPages(accessToken, from);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        meRequest.setParameters(parameters);
        meRequest.executeAsync();
    }

    private void saveFbLoginResults(String userName, String accessToken, String id) {
        //String FACEBOOK_USER_NAME = Profile.getCurrentProfile().getName();
        Constants.FACEBOOK_USER_ACCESS_ID = accessToken;
        Constants.FACEBOOK_USER_ID = id;
        NfxRequestClient requestClient = new NfxRequestClient(Social_Sharing_Activity.this)
                .setmFpId(session.getFPID())
                .setmType("facebookusertimeline")
                .setmUserAccessTokenKey(accessToken)
                .setmUserAccessTokenSecret("null")
                .setmUserAccountId(id)
                .setmAppAccessTokenKey("")
                .setmAppAccessTokenSecret("")
                .setmCallType(FBTYPE)
                .setmName(userName);
        requestClient.connectNfx();
        showLoader(getString(R.string.wait_while_subscribing));

        BoostLog.d("FPID: ", session.getFPID());
        session.storeFacebookName(userName);

        session.storeFacebookAccessToken(accessToken);
        DataBase dataBase = new DataBase(activity);
        dataBase.updateFacebookNameandToken(userName, accessToken);

        prefsEditor.putString("fbId", Constants.FACEBOOK_USER_ID);
        prefsEditor.putString("fbAccessId", Constants.FACEBOOK_USER_ACCESS_ID);
        prefsEditor.putString("fbUserName", userName);
        prefsEditor.apply();
    }

    void onFBPageError(int from) {
        //Log.v("ggg","fbpage error");
        if (from == FROM_AUTOPOST) {
            facebookAutoPullConnect(false);
        } else if (from == FROM_FB_PAGE) {
            facebookPageConnected(false);
            Constants.fbPageShareEnabled = false;
            prefsEditor.putBoolean("fbPageShareEnabled", false).apply();
        }
        LoginManager.getInstance().logOut();
        com.facebook.AccessToken.refreshCurrentAccessTokenAsync();

    }


    public void InitShareResources() {
        Constants.FACEBOOK_USER_ID = pref.getString("fbId", "");
        Constants.FACEBOOK_USER_ACCESS_ID = pref.getString("fbAccessId", "");
        Constants.fbShareEnabled = pref.getBoolean("fbShareEnabled", false);
//        Constants.FACEBOOK_PAGE_ID 			= pref.getString("fbPageId", "");
        Constants.FACEBOOK_PAGE_ACCESS_ID = pref.getString("fbPageAccessId", "");
        Constants.fbPageShareEnabled = pref.getBoolean("fbPageShareEnabled", false);
        Constants.twitterShareEnabled = mSharedPreferences.getBoolean(TwitterConnection.PREF_KEY_TWITTER_LOGIN, false);
        Constants.FbFeedPullAutoPublish = pref.getBoolean("FBFeedPullAutoPublish", false);
        Constants.fbPageFullUrl = pref.getString("fbPageFullUrl", "");
        Constants.fbFromWhichPage = pref.getString("fbFromWhichPage", "");

    }


    private void setStatus() {
        //Log.v("ggg","resime" +facebookHomeCheckBox.isChecked());
        Methods.isOnline(Social_Sharing_Activity.this);
        if (pref.getInt("fbStatus", 0) == 2) {
            Methods.showSnackBarNegative(this, getString(R.string.your_facebook_session_has_expired));
        }

        if (!Util.isNullOrEmpty(session.getFacebookName()) && (pref.getInt("fbStatus", 0) == 1 || pref.getInt("fbStatus", 0) == 3)) {
            //Log.v("ggg"," ok");
            facebookProfileConnected(true);
        } else {
            facebookProfileConnected(false);
        }

        if (!Util.isNullOrEmpty(session.getFacebookPage()) && pref.getInt("fbPageStatus", 0) == 1) {
            facebookPageConnected(true);
        } else {
            facebookPageConnected(false);
        }

        if (!Util.isNullOrEmpty(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME)) && pref.getBoolean("FBFeedPullAutoPublish", false)) {
            facebookAutoPullConnect(true);
        } else {
            facebookAutoPullConnect(false);
        }
        if (!isAuthenticated()) {
            //twitter.setImageDrawable(getResources().getDrawable(R.drawable.twitter_icon_n_inactive));
            // String fbUName = pref.getString(TwitterConnection.PREF_USER_NAME, "");
            twitterProfileConnect(false);
            //twitterStatus.setText("Disconnected");
        } else {
            twitterProfileConnect(true);
        }
    }

    private void twitterProfileConnect(boolean isConnect) {
        if (isConnect) {
            String twitterName = mSharedPreferences.getString(TwitterConnection.PREF_USER_NAME, "");
            twitterStatus.setVisibility(View.VISIBLE);
            twitterStatus.setText("@" + twitterName);
            twitter.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twitter_icon_active));
            twitter.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
        } else {
            twitter.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.twitter_icon_n_inactive));
            twitter.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));
            twitterStatus.setVisibility(View.GONE);
        }
        Constants.fbShareEnabled = isConnect;
        twitterCheckBox.setChecked(isConnect);
    }

    private void facebookProfileConnected(boolean isConnect) {
        if (isConnect) {
            facebookHome.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.facebook_icon));
            facebookHome.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
            facebookHomeStatus.setVisibility(View.VISIBLE);
            facebookHomeStatus.setText(session.getFacebookName());
        } else {
            facebookHome.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.facebook_icon_inactive));
            facebookHome.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));
            facebookHomeStatus.setText("");
        }
        facebookHomeCheckBox.setChecked(isConnect);
        Constants.fbShareEnabled = isConnect;
    }

    private void facebookAutoPullConnect(boolean isConnect) {
        if (isConnect) {
            ivFbPageAutoPull.setImageResource(R.drawable.facebook_page);
            ivFbPageAutoPull.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
            fbPullStatus.setText(session.getFPDetails(Key_Preferences.FB_PULL_PAGE_NAME));
            fbPullStatus.setVisibility(View.VISIBLE);
        } else {
            ivFbPageAutoPull.setImageResource(R.drawable.facebookpage_icon_inactive);
            ivFbPageAutoPull.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));
            fbPullStatus.setText("");
        }
        facebookautopost.setChecked(isConnect);
    }

    private void facebookPageConnected(boolean isConnect) {
        if (isConnect) {
            facebookPage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.facebook_page));
            facebookPage.setColorFilter(ContextCompat.getColor(this, R.color.primaryColor));
            facebookPageStatus.setVisibility(View.VISIBLE);
            facebookPageStatus.setText(session.getFacebookPage());
        } else {
            facebookPage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.facebookpage_icon_inactive));
            facebookPage.setColorFilter(ContextCompat.getColor(this, R.color.light_gray));

            facebookPageStatus.setText("");
        }
        facebookPageCheckBox.setChecked(isConnect);
        Constants.fbPageShareEnabled = isConnect;
    }

    //check about aleady authenticated
    protected boolean isAuthenticated() {
        return mSharedPreferences.getBoolean(TwitterConnection.PREF_KEY_TWITTER_LOGIN, false);
    }

    private void saveTwitterInformation(TwitterSession twitterSession) {
        {
            try {

                String username = twitterSession.getUserName();
                NfxRequestClient requestClient = new NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                        .setmFpId(session.getFPID())
                        .setmType("twitter")
                        .setmUserAccessTokenKey(twitterSession.getAuthToken().token)
                        .setmUserAccessTokenSecret(twitterSession.getAuthToken().secret)
                        .setmUserAccountId(String.valueOf(twitterSession.getUserId()))
                        .setmAppAccessTokenKey(com.nowfloats.twitter.BuildConfig.twitter_consumer_key)
                        .setmAppAccessTokenSecret(com.nowfloats.twitter.BuildConfig.twitter_consumer_secret)
                        .setmCallType(TWITTERTYPE)
                        .setmName(username);
                requestClient.connectNfx();

                showLoader(getString(R.string.wait_while_subscribing));

                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString(TwitterConnection.PREF_KEY_OAUTH_TOKEN, twitterSession.getAuthToken().token);
                e.putString(TwitterConnection.PREF_KEY_OAUTH_SECRET, twitterSession.getAuthToken().secret);
                //e.putBoolean(TwitterConnection.PREF_KEY_TWITTER_LOGIN, true);
                e.putString(TwitterConnection.PREF_USER_NAME, username);
                //Log.v("ggg",username+"twittername");
                e.apply();

            } catch (TwitterException e1) {
                BoostLog.d("Failed to Save", e1.getMessage());
            }
        }
    }

    public void logoutFromTwitter() {
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.remove(TwitterConnection.PREF_KEY_OAUTH_TOKEN);
        e.remove(TwitterConnection.PREF_KEY_OAUTH_SECRET);
        e.remove(TwitterConnection.PREF_KEY_TWITTER_LOGIN);
        e.remove(TwitterConnection.PREF_USER_NAME);
        //Log.v("ggg","twitternameremoved");
        e.apply();
        Constants.twitterShareEnabled = false;
    }

    private void showAlertBox() {
        AlertDialog malertDialog = null;
        AlertDialog.Builder mdialogBuilder = null;
        if (mdialogBuilder == null) {
            mdialogBuilder = new AlertDialog.Builder(Social_Sharing_Activity.this);
            mdialogBuilder.setTitle(getString(R.string.alert));
            mdialogBuilder.setMessage(getString(R.string.no_network));

            mdialogBuilder.setPositiveButton(getString(R.string.enable),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // launch setting Activity
                            startActivityForResult(new Intent(
                                            android.provider.Settings.ACTION_SETTINGS),
                                    0);
                        }
                    });

            mdialogBuilder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert);

            if (malertDialog == null) {
                malertDialog = mdialogBuilder.create();
                malertDialog.show();
            }

        }

    }

    private void showDialog(String headText, String message, final String actionButton) {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(Methods.fromHtml(message));
        builder.setTitle(headText);
        builder.setPositiveButton(actionButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (actionButton.contains("Take Me There")) {
                    addSiteHealth();
                }
                dialog.dismiss();
            }
        });
        dialog = builder.show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface face = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        textView.setTypeface(face);
        textView.setTextColor(Color.parseColor("#808080"));

    }

    private void addSiteHealth() {
        FragmentManager manager = getSupportFragmentManager();
        Site_Meter_Fragment frag = (Site_Meter_Fragment) manager.findFragmentByTag("siteHealth");
        if (frag == null) {
            frag = new Site_Meter_Fragment();
        }
        manager.beginTransaction().replace(R.id.parent_layout, frag, "siteHealth")
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .addToBackStack(null)
                .commit();
    }


    /*
     * This callback is called from NfxClient when there is a successfull or failure post
     * to the api for NFX. Cases are there to determine the type of call.
     */
    @Override
    public void nfxCallBack(String response, int callType, String name) {
        hideLoader();
        if (response.equals("error")) {
            Toast.makeText(this, getString(R.string.something_went_wrong_try_again_), Toast.LENGTH_SHORT).show();
            return;
        }
        BoostLog.d("ggg: ", response + callType + ":");
        switch (callType) {
            case FBTYPE:
                session.storeFacebookName(name);
                facebookProfileConnected(true);
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbShareEnabled", true);
                prefsEditor.putInt("fbStatus", 1);
                prefsEditor.apply();
                break;
            case FBPAGETYPE:
                session.storeFacebookPage(name);
                facebookPageConnected(true);
                prefsEditor.putBoolean("fbPageShareEnabled", true);
                prefsEditor.putInt("fbPageStatus", 1);
                prefsEditor.apply();
                MixPanelController.track(EventKeysWL.FACEBOOK_ANAYTICS, null);
                break;
            case TWITTERTYPE:
                Constants.twitterShareEnabled = true;
                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putBoolean(TwitterConnection.PREF_KEY_TWITTER_LOGIN, true).apply();
                e.putString(TwitterConnection.PREF_USER_NAME, name).apply();
                twitterProfileConnect(true);
                MixPanelController.track(EventKeysWL.CREATE_MESSAGE_ACTIVITY_TWITTER, null);
                break;
            case FB_PAGE_DEACTIVATION:
                DataBase dataBase = new DataBase(activity);
                dataBase.updateFacebookPage("", "", "");
                session.storeFacebookPage("");
                session.storeFacebookPageID("");
                session.storeFacebookAccessToken("");
                facebookPageConnected(false);
                //facebookPageStatus.setText("Disconnected");
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbPageShareEnabled", false);
                prefsEditor.apply();
                break;
            case FB_DECTIVATION:
                DataBase fb_dataBase = new DataBase(activity);
                fb_dataBase.updateFacebookNameandToken("", "");
                session.storeFacebookName("");
                //Log.v("ggg",session.getFacebookName()+"deactivate name");
                session.storeFacebookAccessToken("");
                facebookProfileConnected(false);
                //facebookHomeStatus.setText("Disconnected");
                prefsEditor = pref.edit();
                prefsEditor.putBoolean("fbShareEnabled", false);
                prefsEditor.apply();
                break;
            case TWITTER_DEACTIVATION:
                twitterProfileConnect(false);
                logoutFromTwitter();
                SharedPreferences.Editor twitterPrefEditor = mSharedPreferences.edit();
                twitterPrefEditor.putBoolean(TwitterConnection.PREF_KEY_TWITTER_LOGIN, false);
                twitterPrefEditor.apply();
                Constants.twitterShareEnabled = false;
                break;
            case PAGE_NO_FOUND:
                MixPanelController.track(MixPanelController.FACEBOOK_PAGE_NOT_FOUND, null);
                final String paymentState = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_PAYMENTSTATE);
                if (!Constants.PACKAGE_NAME.equals("com.biz2.nowfloats")) {
                    Methods.materialDialog(activity, "Alert", getString(R.string.look_like_no_facebook_page));
                } else {
                    final MaterialDialog builder = new MaterialDialog.Builder(this)
                            .customView(R.layout.dialog_no_facebook_page, false).build();
                    ((Button) builder.getCustomView().findViewById(R.id.create_page))
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    builder.dismiss();
                                    if ((!TextUtils.isEmpty(paymentState) && "1".equalsIgnoreCase(paymentState))) {
                                        createFBPage(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME));
                                    } else {
                                        Methods.materialDialog(activity, "Alert", getString(R.string.your_account_is_expired_please_renew_it));
                                    }
                                }
                            });
                    if (!isFinishing())
                        builder.show();
                }
                break;
            case FB_PAGE_CREATION:

                switch (response) {
                    case "success_fbDefaultImage":
                        pageCreatedDialog(true);
                        MixPanelController.track(MixPanelController.FACEBOOK_PAGE_CREATED_WITH_DEFAULT_IMAGE, null);
                        break;
                    case "success_logoImage":
                        MixPanelController.track(MixPanelController.FACEBOOK_PAGE_CREATED_WITH_LOGO, null);
                        pageCreatedDialog(false);
                        break;
                    case "profile_incomplete":
                        MixPanelController.track(MixPanelController.FACEBOOK_PAGE_PROFILE_INCOMPLETE, null);
                        showDialog(getString(R.string.site_health_should_be), getString(R.string.business_profile_incomplete), "Take Me There");
                        break;
                    case "error_creating_page":
                        MixPanelController.track(MixPanelController.FACEBOOK_PAGE_ERROR_IN_CREATE, null);
                        Methods.showSnackBarNegative(Social_Sharing_Activity.this, getString(R.string.something_went_wrong));
                        break;
                    case "invalid_name":
                        MixPanelController.track(MixPanelController.FACEBOOK_PAGE_INVALID_NAME, null);
                        pageSuggestionDialog();
                        break;
                    default:
                        Toast.makeText(this, "Something went wrong!!! Please try later.", Toast.LENGTH_SHORT).show();
                        break;
                }
        }
    }

    private void createFBPage(String fpName) {
        MixPanelController.track(MixPanelController.CREATE_FACEBOOK_PAGE, null);
        fpPageName = fpName;
        String businessDesciption = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_DESCRIPTION);

        String businessCategory = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY);

        String mobileNumber = session.getFPDetails(Key_Preferences.MAIN_PRIMARY_CONTACT_NUM);

        String fpURI = "";
        String rootAlisasURI = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ROOTALIASURI);
        String normalURI = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG).toLowerCase()
                + getResources().getString(R.string.tag_for_partners);
        if (rootAlisasURI != null && !rootAlisasURI.equals("null") && rootAlisasURI.trim().length() > 0) {
            fpURI = rootAlisasURI;
        } else {
            fpURI = normalURI;
        }

        showLoader(getString(R.string.please_wait));

        NfxRequestClient requestClient = new
                NfxRequestClient((NfxRequestClient.NfxCallBackListener) Social_Sharing_Activity.this)
                .setmFpId(session.getFPID())
                .setmCallType(FB_PAGE_CREATION);

        requestClient.createFBPage(fpName, businessDesciption, businessCategory,
                mobileNumber, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_LogoUrl),
                session.getFPDetails(Key_Preferences.GET_FP_DETAILS_IMAGE_URI),
                fpURI, session.getFPDetails(Key_Preferences.GET_FP_DETAILS_ADDRESS),
                session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CITY),
                session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY));

    }

    private void pageSuggestionDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_fb_page_edit, null);
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(view, false)
                .build();
        final EditText pageName = (EditText) view.findViewById(R.id.et_page_name);
        pageName.setText(session.getFPDetails(Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME) + " " + session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CITY));
        pageName.requestFocus();
        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String page = pageName.getText().toString().trim();
                if (page.length() > 0) {
                    dialog.dismiss();
                    createFBPage(page);
                } else {
                    Toast.makeText(Social_Sharing_Activity.this, getString(R.string.page_name_cant_be_empty_), Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!isFinishing()) {
            dialog.show();
        }

    }

    private void pageCreatedDialog(boolean showDefaultImageMessage) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_fb_page_created, null);
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(view, false)
                .canceledOnTouchOutside(false)
                .build();
        Button connect = (Button) view.findViewById(R.id.btn_connect);
        TextView pageName = (TextView) view.findViewById(R.id.tv_fb_page_name);
        view.findViewById(R.id.llayout_message).setVisibility(showDefaultImageMessage ? View.VISIBLE : View.GONE);
        pageName.setText(fpPageName);
        ImageView logoImage = (ImageView) view.findViewById(R.id.img_logo);
        ImageView featureImage = (ImageView) view.findViewById(R.id.img_feature);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Picasso.get()
                .load(FB_PAGE_COVER_PHOTO)
                .resize(0, 200)
                .placeholder(R.drawable.general_services_background_img)
                .into(featureImage);

        String logoURI;
        if (showDefaultImageMessage) {
            logoURI = FB_PAGE_DEFAULT_LOGO;
        } else {
            logoURI = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_LogoUrl);
            if (!logoURI.contains("http")) {
                logoURI = "https://" + logoURI;
            }
        }

        Picasso.get()
                .load(logoURI)
                .resize(0, 75)
                .placeholder(R.drawable.facebook_page2)
                .into(logoImage);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                fbData(FROM_FB_PAGE);
            }
        });

        if (!isFinishing()) {
            dialog.show();
        }
    }

    @Override
    public void onTwitterConnected(Result<TwitterSession> result) {
        if (result == null) {
            Methods.showSnackBarNegative(this, getString(R.string.something_went_wrong));
        } else {
            TwitterSession twitter = result.data;
            saveTwitterInformation(twitter);
        }
    }
}
