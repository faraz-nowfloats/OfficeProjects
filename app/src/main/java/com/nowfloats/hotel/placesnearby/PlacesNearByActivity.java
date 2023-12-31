package com.nowfloats.hotel.placesnearby;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.boost.marketplace.ui.home.MarketPlaceActivity;
import com.framework.views.fabButton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.hotel.API.HotelAPIInterfaces;
import com.nowfloats.hotel.API.model.DeletePlacesAround.DeletePlacesAroundRequest;
import com.nowfloats.hotel.API.model.GetPlacesAround.Data;
import com.nowfloats.hotel.API.model.GetPlacesAround.GetPlacesAroundModel;
import com.nowfloats.hotel.Interfaces.PlaceNearByListener;
import com.nowfloats.hotel.placesnearby.adapter.PlaceNearByAdapter;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

import static com.nowfloats.util.Key_Preferences.GET_FP_DETAILS_CATEGORY;


public class PlacesNearByActivity extends AppCompatActivity implements PlaceNearByListener {
    private static final String PLACES_TO_LOOK_AROUND = "PLACES-TO-LOOK-AROUND";
    RecyclerView recyclerView;
    PlaceNearByAdapter adapter;
    LinearLayout secondaryLayout;
    TextView buyItemButton;
    UserSessionManager session;

    List<Data> dataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_nearby);
        initView();
    }

    public void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new PlaceNearByAdapter(new ArrayList(), this);
        secondaryLayout = findViewById(R.id.secondary_layout);
        buyItemButton = findViewById(R.id.buy_item);

        session = new UserSessionManager(this, this);


        //setheader
        setHeader();
        if (session.getStoreWidgets().contains(PLACES_TO_LOOK_AROUND)) {
            recyclerView.setVisibility(View.VISIBLE);
            secondaryLayout.setVisibility(View.GONE);
            initialiseRecycler();
        } else {
            recyclerView.setVisibility(View.GONE);
            secondaryLayout.setVisibility(View.VISIBLE);
            showSecondaryLayout();
        }
    }

    void showSecondaryLayout() {
        buyItemButton.setOnClickListener(v -> initiateBuyFromMarketplace());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session.getStoreWidgets().contains(PLACES_TO_LOOK_AROUND)) {
            loadData();
        }
    }

    private void initialiseRecycler() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    public void setHeader() {
        LinearLayout rightButton, backButton;
        ImageView rightIcon;
        FloatingActionButton btnAdd;
        TextView title;

        title = findViewById(R.id.title);
        backButton = findViewById(R.id.back_button);
        rightButton = findViewById(R.id.right_icon_layout);
        rightIcon = findViewById(R.id.right_icon);
        rightIcon.setVisibility(View.INVISIBLE);
        btnAdd = findViewById(R.id.btn_add);
        title.setText("Places to Look Around");
        btnAdd.setVisibility(View.VISIBLE);
        btnAdd.setOnClickListener(v -> {
            if (session.getStoreWidgets().contains(PLACES_TO_LOOK_AROUND)) {
                Intent intent = new Intent(getApplicationContext(), PlacesNearByDetailsActivity.class);
                intent.putExtra("ScreenState", "new");
                startActivity(intent);
            }
        });
        if (!session.getStoreWidgets().contains(PLACES_TO_LOOK_AROUND)) btnAdd.setVisibility(View.GONE);
        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getBoolean("is_add")) btnAdd.performClick();
        }
        backButton.setOnClickListener(v -> onBackPressed());
    }

    void loadData() {
        try {
            JSONObject query = new JSONObject();
            query.put("WebsiteId", session.getFpTag());
            HotelAPIInterfaces APICalls = new RestAdapter.Builder()
                    .setEndpoint("https://webaction.api.boostkit.dev")
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("ggg"))
                    .build()
                    .create(HotelAPIInterfaces.class);

            APICalls.getPlacesAroundList(query, 0, 1000, new Callback<GetPlacesAroundModel>() {
                @Override
                public void success(GetPlacesAroundModel getPlacesAroundModel, Response response) {
                    if (getPlacesAroundModel == null || response.getStatus() != 200) {
                        Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dataList = getPlacesAroundModel.getData();
                    updateRecyclerView();
                }

                @Override
                public void failure(RetrofitError error) {
                    Methods.showSnackBarNegative(PlacesNearByActivity.this, getString(R.string.something_went_wrong));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateRecyclerView() {
        adapter.menuOption(-1, false);
        adapter.updateList(dataList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void itemMenuOptionStatus(int pos, boolean status) {
        adapter.menuOption(pos, status);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void editOptionClicked(Data data) {
        Intent intent = new Intent(getApplicationContext(), PlacesNearByDetailsActivity.class);
        intent.putExtra("ScreenState", "edit");
        intent.putExtra("data", new Gson().toJson(data));
        startActivity(intent);
    }

    @Override
    public void deleteOptionClicked(Data data) {
        try {
            DeletePlacesAroundRequest requestBody = new DeletePlacesAroundRequest();
            requestBody.setQuery("{_id:'" + data.getId() + "'}");
            requestBody.setUpdateValue("{$set : {IsArchived: true }}");
            requestBody.setMulti(true);

            HotelAPIInterfaces APICalls = new RestAdapter.Builder()
                    .setEndpoint("https://webaction.api.boostkit.dev")
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new AndroidLog("ggg"))
                    .setConverter(new GsonConverter(new GsonBuilder().setLenient().create()))
                    .build()
                    .create(HotelAPIInterfaces.class);

            APICalls.deletePlacesAround(requestBody, new Callback<String>() {
                @Override
                public void success(String data, Response response) {
                    if (response != null && response.getStatus() == 200) {
                        Log.d("deletePlacesAround ->", response.getBody().toString());
                        loadData();
                    } else {
                        Methods.showSnackBarNegative(PlacesNearByActivity.this, getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (error.getResponse().getStatus() == 200) {
                        loadData();
                    } else {
                        Methods.showSnackBarNegative(PlacesNearByActivity.this, getString(R.string.something_went_wrong));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initiateBuyFromMarketplace() {
        ProgressDialog progressDialog = new ProgressDialog(PlacesNearByActivity.this);
        String status = "Loading. Please wait...";
        progressDialog.setMessage(status);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Intent intent = new Intent(PlacesNearByActivity.this, MarketPlaceActivity.class);
        intent.putExtra("expCode", session.getFP_AppExperienceCode());
        intent.putExtra("fpName", session.getFPName());
        intent.putExtra("fpid", session.getFPID());
        intent.putExtra("fpTag", session.getFpTag());
        intent.putExtra("accountType", session.getFPDetails(GET_FP_DETAILS_CATEGORY));
        intent.putStringArrayListExtra("userPurchsedWidgets", new ArrayList(session.getStoreWidgets()));
        if (session.getFPEmail() != null) {
            intent.putExtra("email", session.getFPEmail());
        } else {
            intent.putExtra("email", getString(R.string.ria_customer_mail));
        }
        if (session.getUserPrimaryMobile() != null) {
            intent.putExtra("mobileNo", session.getUserPrimaryMobile());
        } else {
            intent.putExtra("mobileNo", getString(R.string.ria_customer_number));
        }
        intent.putExtra("profileUrl", session.getFPLogo());
        intent.putExtra("buyItemKey", PLACES_TO_LOOK_AROUND);
        startActivity(intent);
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            finish();
        }, 1000);
    }

}