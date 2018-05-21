package com.nowfloats.NavigationDrawer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nowfloats.NavigationDrawer.API.MessageTag_Async_Task;
import com.nowfloats.test.com.nowfloatsui.buisness.util.Util;
import com.nowfloats.util.BoostLog;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.nowfloats.util.MixPanelController;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thinksity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class Card_Full_View_Fragment extends Fragment {

    public static final String ImageKey = "imageKey";
    public static final String MainTextKey = "mainText";
    public static final String DateTextKey = "dateText";
    public static final String MessageIdKey = "messageIdTag";
    public static final String UrlKey = "UrlKey";
    private static final int STORAGE_CODE = 120;
    static View.OnClickListener mylongOnClickListener;
    private Activity appContext;

    public Card_Full_View_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_card_full_view, container, false);
        Bundle bundle = getArguments();

        Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");

        Typeface robotoMedium = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Medium.ttf");


        ((Card_Full_View_MainActivity) getActivity()).setActionBarTitle(getString(R.string.home));

        CardView cardView = (CardView) mainView.findViewById(R.id.card_view);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<Intent> targetShareIntents=new ArrayList<Intent>();
//                Intent shareIntent=new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.setType("text/plain");
//                List<ResolveInfo> resInfos=getActivity().getPackageManager().queryIntentActivities(shareIntent, 0);
//                if(!resInfos.isEmpty()) {
//                    System.out.println("Have package");
//                    for (ResolveInfo resInfo : resInfos) {
//                        String packageName = resInfo.activityInfo.packageName;
//                        Log.i("Package Name", packageName);
//                        if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana") || packageName.contains("com.instagram.android")) {
//                            Intent intent = new Intent();
//                            intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
//                            intent.setAction(Intent.ACTION_SEND);
//                            intent.setType("text/plain");
//                            intent.putExtra(Intent.EXTRA_TEXT, "Text");
//                            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
//                            intent.setPackage(packageName);
//                            targetShareIntents.add(intent);
//                        }
//                    }
//                    if (!targetShareIntents.isEmpty()) {
//                        System.out.println("Have Intent");
//                        Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
//                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
//                        startActivity(chooserIntent);
//                    } else {
//                        System.out.println("Do not Have Intent");
//                        //showDialaog(this);
//                    }
//
//
//                }

            }
        });



       // mylongOnClickListener = new MyLongClickListener(getActivity());


        if (bundle != null) {
            String imagePath = bundle.getString(ImageKey);
            String mainText = bundle.getString(MainTextKey);
            String dateText = bundle.getString(DateTextKey);
            String messageid = bundle.getString(MessageIdKey);
            String urlKey = bundle.getString(UrlKey);

            //Log.d("Card Frag", "Card Fragment : "+imagePath+" , "+mainText+ " , "+dateText);
            //Log.d("Card Frag","Main View : "+mainView);
            setValues(mainView, imagePath, mainText, dateText,messageid,urlKey);

        }

        return mainView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        appContext = (Activity)context;
    }

    public class MyLongClickListener implements View.OnClickListener {
        private final Context context;


        private MyLongClickListener(Context context)
        {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            final String[] INTENT_FILTER = new String[] {
                    "com.twitter.android",
                    "com.facebook.katana"
            };

            List<Intent> targetShareIntents=new ArrayList<Intent>();
            Intent shareIntent=new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            List<ResolveInfo> resInfos=getActivity().getPackageManager().queryIntentActivities(shareIntent, 0);
            if(!resInfos.isEmpty()) {
                System.out.println("Have package");
                for (ResolveInfo resInfo : resInfos) {
                    String packageName = resInfo.activityInfo.packageName;
                    Log.i("Package Name", packageName);
                    if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana") || packageName.contains("com.instagram.android")) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, "Text");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        intent.setPackage(packageName);
                        targetShareIntents.add(intent);
                    }
                }
                if (!targetShareIntents.isEmpty()) {
                    System.out.println("Have Intent");
                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), getString(R.string.choose_app_to_share));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                    startActivity(chooserIntent);
                } else {
                    System.out.println("Do not Have Intent");
                    //showDialaog(this);
                }


            }

        }


    }


    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle("Home");

    }

    private void setValues(View mainView, final String imageUri, final String mainText, String dateText, final String msgID, final String updateUrl) {
        //Log.d("Set Values","values  :"+imagePath+" , "+mainText+" , "+dateText);
        ImageView imageView = (ImageView) mainView.findViewById(R.id.mainImageView);
        TextView mainTextView = (TextView) mainView.findViewById(R.id.headingTextView);
        TextView dateTextView = (TextView) mainView.findViewById(R.id.dateTextView);
        TextView messageTag = (TextView) mainView.findViewById(R.id.messagetag);

        mainView.findViewById(R.id.shareData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

                    if (ActivityCompat.shouldShowRequestPermissionRationale(appContext,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        Methods.showDialog(appContext,"Storage Permission", "To share your image we need storage permission.");
                    }else{
                        ActivityCompat.requestPermissions(appContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_CODE);
                    }
                    return;
                }
                MixPanelController.track("SharePost", null);
                // final Intent shareIntent = null;
                final ProgressDialog pd = ProgressDialog.show(appContext, "", "Sharing . . .");

                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!Util.isNullOrEmpty(imageUri) && !imageUri.contains("/Tile/deal.png")) {
                    if (Methods.isOnline(appContext)) {
                        String url;
                        if (imageUri.contains("BizImages")) {
                            url = Constants.NOW_FLOATS_API_URL + "" + imageUri;
                        } else {
                            url = imageUri;
                        }
                        Picasso.with(appContext)
                                .load(url)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                        pd.dismiss();
                                        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        View view = new View(appContext);
                                        view.draw(new Canvas(mutableBitmap));
                                        try {
                                            String path = MediaStore.Images.Media.insertImage(appContext.getContentResolver(), mutableBitmap, "Nur", null);
                                            BoostLog.d("Path is:", path);
                                            Uri uri = Uri.parse(path);
                                            shareIntent.putExtra(Intent.EXTRA_TEXT, mainText + " View more at: " +updateUrl);
                                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                            shareIntent.setType("image/*");


                                            if (shareIntent.resolveActivity(appContext.getPackageManager()) != null) {
                                                appContext.startActivityForResult(Intent.createChooser(shareIntent, appContext.getString(R.string.share_message)), 1);
                                            } else {
                                                Methods.showSnackBarNegative(appContext,appContext.getString(R.string.no_app_available_for_action));
                                            }
                                        }catch (Exception e){
                                            ActivityCompat.requestPermissions(appContext, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA}, 2);
                                        }
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        pd.dismiss();
                                        Methods.showSnackBarNegative(appContext, appContext.getString(R.string.failed_to_download_image));

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });


                    } else {
                        pd.dismiss();
                        Methods.showSnackBarNegative(appContext, appContext.getString(R.string.can_not_share_image_offline_mode));
                    }


                } else {
                    pd.dismiss();
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, mainText + " View more at: " +updateUrl);
                    if (shareIntent.resolveActivity(appContext.getPackageManager()) != null) {
                        appContext.startActivityForResult(Intent.createChooser(shareIntent, appContext.getString(R.string.share_message)), 1);
                    } else {
                        Methods.showSnackBarNegative(appContext, appContext.getString(R.string.no_app_available_for_action));
                    }

                }
            }

        });
        MessageTag_Async_Task tag = new MessageTag_Async_Task(getActivity(),messageTag,msgID);
        tag.execute();

        Typeface myCustomFont = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Light.ttf");
        Typeface myCustomFont_Medium = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Medium.ttf");
        String baseName = "";

        mainTextView.setTypeface(myCustomFont);
        dateTextView.setTypeface(myCustomFont_Medium);

        mainTextView.setText(mainText);
        dateTextView.setText(dateText);

        if(Util.isNullOrEmpty(imageUri))
        {
            imageView.setVisibility(View.GONE);
        }
        else if(imageUri.contains("deal.png") || Util.isNullOrEmpty(imageUri))
        {
            imageView.setVisibility(View.GONE);
        }
        else if(imageUri.contains("BizImages") )
        {
            imageView.setVisibility(View.VISIBLE);
            baseName = Constants.BASE_IMAGE_URL+"" + imageUri;
            Picasso.with(getActivity()).load(baseName).noPlaceholder().into(imageView);
//                        imageLoader.displayImage(baseName,imageView,options);
        }

        else if(imageUri.contains("/storage/emulated") || imageUri.contains("/mnt/sdcard") )
        {
            imageView.setVisibility(View.VISIBLE);
            Bitmap bmp = Util.getBitmap(imageUri.toString(),getActivity());
            imageView.setImageBitmap(bmp);
        }
        else
        {
            imageView.setVisibility(View.VISIBLE);
            baseName = imageUri ;
            Glide.with(this).load(baseName).into(imageView);
//                        imageLoader.displayImage(baseName,imageView,options);
        }
    }
}
