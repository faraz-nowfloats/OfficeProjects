package com.nowfloats.Product_Gallery;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.melnykov.fab.FloatingActionButton;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.HomeActivity;
import com.nowfloats.Product_Gallery.Model.ProductListModel;
import com.nowfloats.Product_Gallery.Service.ProductAPIService;
import com.nowfloats.util.BusProvider;
import com.nowfloats.util.Constants;
import com.nowfloats.util.EventKeysWL;
import com.nowfloats.util.Key_Preferences;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thinksity.R;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by guru on 08-06-2015.
 */
public class Product_Gallery_Fragment extends Fragment{
    public static Bus bus;
    public static LinearLayout empty_layout,progressLayout;
    GridView gridView;
    public static ProductGalleryAdapter adapter;
    public static ArrayList<ProductListModel> productItemModelList;
    private Activity activity;
    UserSessionManager session;
    int visibilityFlag = 1;
    private boolean userScrolled = false;
    private ProductAPIService apiService;
    private String currencyValue = "INR";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        bus = BusProvider.getInstance().getBus();
        session = new UserSessionManager(activity.getApplicationContext(),activity);
        apiService = new ProductAPIService();

        new Thread(new Runnable() {
            @Override
            public void run() {
            if (Constants.Currency_Country_Map==null){
                Constants.Currency_Country_Map =  new HashMap<String,String>();
                Constants.currencyArray = new ArrayList<String>();
            }
            if (Constants.Currency_Country_Map.size()==0){
                for (Locale locale : Locale.getAvailableLocales()) {
                    try{
                        if (locale!=null && locale.getISO3Country()!=null && Currency.getInstance(locale)!=null){
                            Currency currency = Currency.getInstance(locale);
                            String loc_currency = currency.getCurrencyCode();
                            String country = locale.getDisplayCountry();
                            if (!Constants.Currency_Country_Map.containsKey(country.toLowerCase())){
                                Constants.Currency_Country_Map.put(country.toLowerCase(), loc_currency);
                                Constants.currencyArray.add(country+"-"+loc_currency);
                            }
                        }
                    }catch(Exception e){
                        System.gc();
                        e.printStackTrace();
                    }
                }
            }
            try{
                currencyValue = Constants.Currency_Country_Map.get(
                        session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY).toLowerCase());
                getProducts("0");
            }catch (Exception e){e.printStackTrace();}
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Product_Gallery", "onCreateView");
        return inflater.inflate(R.layout.fragment_product__gallery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        empty_layout = (LinearLayout)view.findViewById(R.id.emptyproductlayout);
        progressLayout = (LinearLayout)view.findViewById(R.id.progress_productlayout);
        progressLayout.setVisibility(View.VISIBLE);
        gridView = (GridView)view.findViewById(R.id.product_gridview);
        final FloatingActionButton addProduct =(FloatingActionButton)view.findViewById(R.id.fab_product);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MixPanelController.track(EventKeysWL.PRODUCT_GALLERY_ADD, null);
                Intent intent = new Intent(activity,Product_Detail_Activity.class);
                intent.putExtra("new","");
                startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity,Product_Detail_Activity.class);
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("product", productItemModelList.get(position));
                intent.putExtra("product",position+"");
                Methods.launchFromFragment(activity, view, intent);
            }
        });


        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    userScrolled = true;
                }

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (visibilityFlag == 0){
                        visibilityFlag = 1;
                        YoYo.with(Techniques.SlideInUp).interpolate(new DecelerateInterpolator()).duration(200).playOn(addProduct);
                    }

                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){

                    if (visibilityFlag == 1){
                        YoYo.with(Techniques.SlideOutDown).interpolate(new AccelerateInterpolator()).duration(200).playOn(addProduct);
                        visibilityFlag = 0;

                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if((userScrolled) && (lastInScreen == totalItemCount) && (totalItemCount%10==0)){
                    userScrolled=false;
                    //TODO load more
                    getProducts(""+totalItemCount);
                }
            }
        });
    }

    private void getProducts(String skip) {
        HashMap<String,String> values = new HashMap<>();
        values.put("clientId", Constants.clientId);
        values.put("skipBy",skip);
        values.put("fpTag",session.getFPDetails(Key_Preferences.GET_FP_DETAILS_TAG));
        //invoke getProduct api
        apiService.getProductList(activity, values, bus);
    }

    @Subscribe
    public void loadMore(LoadMoreProductEvent event){
        try{
            progressLayout.setVisibility(View.GONE);
            if (event.data!=null){
                int addPos = productItemModelList.size();
                for (int i = 0; i < event.data.size(); i++) {
                    productItemModelList.add(addPos,event.data.get(i));
                    addPos++;
                }
                adapter.notifyDataSetChanged();
            }
        }catch(Exception e){e.printStackTrace(); System.gc();}
    }

    @Subscribe
    public void getProductList(ArrayList<ProductListModel> data){
        progressLayout.setVisibility(View.GONE);
        if (data!=null){
        Log.i("","PRoduct List Size--"+data.size());
            productItemModelList = data;
            adapter = new ProductGalleryAdapter(activity, currencyValue);
            gridView.setAdapter(adapter);
            gridView.invalidateViews();

            if (productItemModelList.size()==0){
                empty_layout.setVisibility(View.VISIBLE);
            }else{
                empty_layout.setVisibility(View.GONE);
            }
        }else{
            if (Product_Gallery_Fragment.productItemModelList.size() == 0) {
                Product_Gallery_Fragment.empty_layout.setVisibility(View.VISIBLE);
            } else {
                Product_Gallery_Fragment.empty_layout.setVisibility(View.GONE);
            }
            Methods.showSnackBarNegative(activity, "Something went wrong, Try again...!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
        if (productItemModelList!=null && productItemModelList.size()==0 && empty_layout!=null){
            empty_layout.setVisibility(View.VISIBLE);
        }else{
            empty_layout.setVisibility(View.GONE);
        }

        if(adapter!=null) {adapter.notifyDataSetChanged();}
        if(gridView!=null) gridView.invalidateViews();
        if (HomeActivity.plusAddButton!=null)
            HomeActivity.plusAddButton.setVisibility(View.GONE);
        if(HomeActivity.headerText!=null)
            HomeActivity.headerText.setText("Product Gallery");
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

}