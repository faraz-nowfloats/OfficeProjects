package com.nowfloats.Image_Gallery;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.thinksity.R;
import com.thinksity.databinding.ActivityImageGalleryBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class ImageGalleryActivity extends AppCompatActivity {

    public static final String TAG_IMAGE = "TAG_IMAGE";
    ActivityImageGalleryBinding binding;
    ArrayList<String> purchasedWidgetList = new ArrayList<String>();
    private Image_Gallery_Fragment image_gallery_fragment;
    private int noOfImages = 0;
    private static final String TAG = "ImageGalleryActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_gallery);
        Log.i(TAG, "onCreate: ");
        MixPanelController.track(MixPanelController.IMAGE_GALLERY, null);
        if (Constants.storeSecondaryImages==null||Constants.storeSecondaryImages.isEmpty()){
            binding.btnAdd.setVisibility(View.GONE);
            binding.footer.setVisibility(View.GONE);

        }else {
            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.footer.setVisibility(View.VISIBLE);

        }
        setSupportActionBar(binding.appBar.toolbar);
        Methods.isOnline(ImageGalleryActivity.this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle("");
        }

        binding.appBar.toolbarTitle.setText(getResources().getString(R.string.image_gallery));

        image_gallery_fragment = new Image_Gallery_Fragment();
        image_gallery_fragment.imageChangeListener = new Image_Gallery_Fragment.ImageChangeListener() {
            @Override
            public void onImagePicked() {
                handleZerothCase();
            }
        };
        findViewById(R.id.fm_site_appearance).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fm_site_appearance, image_gallery_fragment, TAG_IMAGE).
                commit();

        if (savedInstanceState != null && image_gallery_fragment != null) {
            Boolean startCreateMode = savedInstanceState.getBoolean("create_image", false);
            if (startCreateMode) {
                image_gallery_fragment.addImage();
            }
        }
        binding.btnAdd.setOnClickListener(view -> image_gallery_fragment.addImage());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                break;

            case R.id.menu_add:
                image_gallery_fragment.addImage();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i(TAG, "onActivityResult: "+Constants.storeSecondaryImages.size());
        if (requestCode==202){
            handleZerothCase();
        }
    }

    private void handleZerothCase(){
        if (Constants.storeSecondaryImages == null || Constants.storeSecondaryImages.isEmpty()){
            binding.btnAdd.setVisibility(View.GONE);
            binding.footer.setVisibility(View.GONE);
        }else {
            binding.btnAdd.setVisibility(View.VISIBLE);
            binding.footer.setVisibility(View.VISIBLE);

        }
    }
}
