package com.nowfloats.NavigationDrawer.API;

import com.nowfloats.NavigationDrawer.model.WildFireDataModel;
import com.nowfloats.NavigationDrawer.model.WildFireKeyStatsModel;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Admin on 30-11-2017.
 */

public interface WildFireApis {
    String WILD_FIRE_END_POINT = "http://wmt.withfloats.com";
    RestAdapter adapter = new RestAdapter.Builder().setEndpoint(WILD_FIRE_END_POINT).build();
    @GET("/wildfire/api/v1/account/keywordstats")
    void getKeyWordsStats(@Field("clientId") String clientId, @Field("accountId") String accountId, Callback<ArrayList<WildFireKeyStatsModel>> response);

    @GET("/WildFire/v1/account/detailswithexternalsourceid/{sourceId}")
    void getWildFireData(@Path("sourceId") String sourceId, @Field("clientId") String clientId, Callback<WildFireDataModel> response);
}
