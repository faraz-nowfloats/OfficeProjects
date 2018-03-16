package nowfloats.nfkeyboard.keyboards;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import hani.momanii.supernova_emoji_library.emoji.Emojicon;
import nowfloats.nfkeyboard.R;
import nowfloats.nfkeyboard.adapter.BaseAdapterManager;
import nowfloats.nfkeyboard.interface_contracts.CandidateToPresenterInterface;
import nowfloats.nfkeyboard.interface_contracts.ImeToPresenterInterface;
import nowfloats.nfkeyboard.interface_contracts.ItemClickListener;
import nowfloats.nfkeyboard.interface_contracts.PresenterToImeInterface;
import nowfloats.nfkeyboard.interface_contracts.UrlToBitmapInterface;
import nowfloats.nfkeyboard.models.AllSuggestionModel;
import nowfloats.nfkeyboard.models.networkmodels.Product;
import nowfloats.nfkeyboard.network.CallBack;
import nowfloats.nfkeyboard.network.Float;
import nowfloats.nfkeyboard.network.NetworkAdapter;
import nowfloats.nfkeyboard.network.Updates;
import nowfloats.nfkeyboard.util.KeyboardUtils;
import nowfloats.nfkeyboard.util.MethodUtils;
import nowfloats.nfkeyboard.util.MixPanelUtils;
import nowfloats.nfkeyboard.util.SharedPrefUtil;
import timber.log.Timber;

/**
 * Created by Admin on 26-02-2018.
 */

public class ImePresenterImpl implements ItemClickListener,
        ImeToPresenterInterface,
        CandidateToPresenterInterface,
        View.OnClickListener,
        UrlToBitmapInterface {
    private CandidateViewBaseImpl mCandidateView;
    private KeyboardViewBaseImpl mKeyboardView;
    private ManageKeyboardView manageKeyboardView;
    private static final String UTM_SOURCE = "utm_source", UTM_MEDIUM="utm_medium";
    public final static int KEY_EMOJI = -2005,KEY_IME_OPTION = -2006, KEY_SPACE = -2007, KEY_NUMBER = -2000,
            KEY_SYM = -2001, KEY_SYM_SHIFT = -2002, KEY_QWRTY = -2003, KEY_LANGUAGE_CHANGE= -2004;
    private Context mContext;
    private String packageName = "";
    private KeyboardUtils.CandidateType currentCandidateType = KeyboardUtils.CandidateType.BOOST_SHARE;
    private KeyboardUtils.KeyboardType mKeyboardTypeCurrent = KeyboardUtils.KeyboardType.QWERTY_LETTERS;
    private KeyboardBaseImpl mCurrentKeyboard;
    private boolean caps;
    private int imeOptionId;
    private ArrayList<AllSuggestionModel> updatesList, productList;
    private InputMethodManager mInputMethodManager;
    private PresenterToImeInterface imeListener;
    private AudioManager mAudioManager;
    private TabType mTabType = TabType.NO_TAB;
    private ShiftType mShiftType = ShiftType.CAPITAL;

    @Override
    public void onScrollItems(int totalItemCount, int lastVisiblePos, TabType type) {
        if(lastVisiblePos>=totalItemCount-2){
            switch (type){
                case PRODUCTS:
                    if (productList.size()>= 10){

                    }
                    break;
                case UPDATES:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public TabType getTabType() {
        return mTabType;
    }

    @Override
    public void onSpeechResult(String speech) {
        if (!TextUtils.isEmpty(speech)) {
            imeListener.getImeCurrentInputConnection().commitText(speech, 1);
            MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SPEECH_RESULT,null);
        }
        if (mTabType != TabType.KEYBOARD) {
            mTabType = TabType.KEYBOARD;
            manageKeyboardView.showKeyboardLayout();
            mCandidateView.addCandidateTypeView(currentCandidateType,TabType.KEYBOARD);
        }
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        imeListener.getImeCurrentInputConnection().commitText(emojicon.getEmoji(), 1);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        sendKeyEvent(KeyEvent.KEYCODE_DEL);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void onResourcesReady(Bitmap bitmap, String text, String imageId) {
        Uri uri = MethodUtils.getImageUri(mContext,bitmap, TextUtils.isEmpty(imageId)?
                UUID.randomUUID().toString():imageId);
        if (uri == null){
            imeListener.getImeCurrentInputConnection().commitText(text, 1);
        }else{
            doCommitContent(text, "image/png", uri);
        }

    }

    public enum TabType {
        PRODUCTS, UPDATES,KEYBOARD,SETTINGS, NO_TAB;
    }
    private enum ShiftType{
        LOCKED,CAPITAL,NORMAL;
    }

    ImePresenterImpl(Context context, PresenterToImeInterface imeListener){
        mCandidateView = new CandidateViewBaseImpl(context);
        MixPanelUtils.getInstance().setMixPanel(context);
        mCandidateView.setItemClickListener(this);
        mContext = context;
        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        this.imeListener = imeListener;
    }

    @Override
    public View onCreateInputView(){
        manageKeyboardView = (ManageKeyboardView) LayoutInflater.from(mContext).inflate(R.layout.keyboard_view, null);
        manageKeyboardView.setPresenterListener(this);
        mKeyboardView = manageKeyboardView.getKeyboard();
        setCurrentKeyboard(mKeyboardTypeCurrent);
        mKeyboardView.setOnKeyboardActionListener(new KeyboardListener());
        return manageKeyboardView;
    }

    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        MixPanelUtils.getInstance().createUser(SharedPrefUtil.fromBoostPref().getsBoostPref(mContext).getFpTag());
        packageName = attribute.packageName;
        mShiftType = ShiftType.CAPITAL;
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS){
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
            case InputType.TYPE_CLASS_PHONE:
                setKeyboardType(KeyboardUtils.KeyboardType.NUMBERS);
                currentCandidateType = KeyboardUtils.CandidateType.NULL;
                break;
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                currentCandidateType = KeyboardUtils.CandidateType.NULL;
                setKeyboardType(KeyboardUtils.KeyboardType.EMAIL_ADDRESS);
                break;
            case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                mShiftType = ShiftType.NORMAL;
            case InputType.TYPE_CLASS_TEXT:
            default:
                currentCandidateType = KeyboardUtils.CandidateType.BOOST_SHARE;
                setKeyboardType(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
        }
        imeOptionId = attribute.imeOptions;
        setCurrentKeyboard();
    }

    private void initializeValues() {
        updatesList = null;
        productList = null;
        mTabType = TabType.KEYBOARD;
    }

    private void setImeOptions(Resources res, int options) {
        int keySize = mCurrentKeyboard.getKeys().size();
        Keyboard.Key mEnterKey = null;
        if (keySize > 0) {
            mEnterKey = mCurrentKeyboard.getKeys().get(keySize - 1);
        }
        if (mEnterKey == null) {
            return;
        }
        switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "Go";
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.icon = res.getDrawable(R.drawable.ic_next);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = res.getDrawable(R.drawable.ic_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = "send";
                break;
            default:
                mEnterKey.icon = res.getDrawable(R.drawable.ic_enter_arrow);
                mEnterKey.label = null;
                break;
        }
        //mKeyboardView.invalidateKey(mCurrentKeyboard.getKeys().size()-1);
    }

    public void setKeyboardType(KeyboardUtils.KeyboardType type){
        mKeyboardTypeCurrent = type;
    }

    public void setCurrentKeyboard(){
        initializeValues();
        setCurrentKeyboard(mKeyboardTypeCurrent);
        addCandidateTypeView(currentCandidateType,mTabType);
    }

    public void setCurrentKeyboard(KeyboardUtils.KeyboardType type){
        if (mKeyboardTypeCurrent != type){
            mKeyboardTypeCurrent = type;
        }
        mCurrentKeyboard = mKeyboardView.getKeyboard(type);
        setImeOptions(mContext.getResources(), imeOptionId);
        mKeyboardView.setKeyboard(mCurrentKeyboard);
        mKeyboardView.setShifted(mShiftType != ShiftType.NORMAL);
        manageKeyboardView.showKeyboardLayout();
    }

    private IBinder getToken() {
        final Dialog dialog = imeListener.getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    public void onDestroy(){
        MixPanelUtils.getInstance().flush();
    }
    private void addCandidateTypeView(KeyboardUtils.CandidateType type, TabType tabType){
        imeListener.setCandidatesViewShown(mCandidateView.addCandidateTypeView(type, tabType));
    }

    private boolean isCommitContentSupported(
            @Nullable EditorInfo editorInfo, @NonNull String mimeType) {
        if (editorInfo == null) {
            return false;
        }

        final InputConnection ic = imeListener.getImeCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        if (!validatePackageName(editorInfo)) {
            return false;
        }

        final String[] supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        for (String supportedMimeType : supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true;
            }
        }
        return false;
    }

    private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        final String packageName = editorInfo.packageName;
        if (packageName == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }

        final InputBinding inputBinding = imeListener.getImeCurrentInputBinding();
        if (inputBinding == null) {
            Timber.e("inputBinding should not be null here. "
                    + "You are likely to be hitting b.android.com/225029");
            return false;
        }
        final int packageUid = inputBinding.getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final AppOpsManager appOpsManager =
                    (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            try {
                appOpsManager.checkPackage(packageUid, packageName);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        final PackageManager packageManager = mContext.getPackageManager();
        final String possiblePackageNames[] = packageManager.getPackagesForUid(packageUid);
        for (final String possiblePackageName : possiblePackageNames) {
            if (packageName.equals(possiblePackageName)) {
                return true;
            }
        }
        return false;
    }

    private void doCommitContent(@NonNull String description, @NonNull String mimeType,
                                 @NonNull Uri uri) {

        final EditorInfo editorInfo = imeListener.getImeCurrentEditorInfo();

        if (!validatePackageName(editorInfo)) {
            return;
        }
        final int flag;
        if (Build.VERSION.SDK_INT >= 25) {
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        } else {
            flag = 0;
            try {
                mContext.grantUriPermission(
                        editorInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e){
                Timber.e("grantUriPermission failed packageName=" + editorInfo.packageName
                        + " contentUri=" + uri);
            }
        }

        imeListener.getImeCurrentInputConnection().commitText(description,1);
        if(isCommitContentSupported(editorInfo, mimeType)) {
            MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_IMAGE_SHARING,null);
            final InputContentInfoCompat inputContentInfoCompat = new InputContentInfoCompat(
                    uri,
                    new ClipDescription(description, new String[]{mimeType}),
                    null /* linkUrl */);
            InputConnectionCompat.commitContent(
                    imeListener.getImeCurrentInputConnection(),
                    imeListener.getImeCurrentEditorInfo(), inputContentInfoCompat,
                    flag, null);
        }else if(mimeType.equalsIgnoreCase("image/png")){
            Toast.makeText(mContext, "Image not supported", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemClick(final AllSuggestionModel model) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Please grant external storage permission", Toast.LENGTH_SHORT).show();
            //MethodUtils.getPermissions(mContext);
            return;
        }
        if (model == null) {
            Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject object = new JSONObject();
        try {
            object.put("id",model.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uri = null;
        String shareUrl = null;
        try {
            if (!TextUtils.isEmpty(model.getUrl())) {
                uri = Uri.parse(model.getUrl()).buildUpon().appendQueryParameter(UTM_SOURCE, "bk")
                        .appendQueryParameter(UTM_MEDIUM, TextUtils.isEmpty(packageName)?"share":packageName).build();
            }
        }catch(Exception e){

        }
        if (uri != null){
            shareUrl = uri.toString();
        }

        switch (model.getTypeEnum()) {

            case ImageAndText:

                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_UPDATE_IMAGE_SHARE,object);
                MethodUtils.onGlideBitmapReady(this, model.getText()+",\nUrl: "+shareUrl, model.getImageUrl(), model.getId());
                break;
            case Product:
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_PRODUCT_SHARE,object);
                MethodUtils.onGlideBitmapReady(this, "Name: " + model.getText()+",\nUrl: "+shareUrl, model.getImageUrl(), model.getId());
                break;
            case Text:

                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_UPDATE_SHARE,object);
                doCommitContent(model.getText()+",\nUrl: "+shareUrl, "text/plain", null);
                break;
            default:
                break;
        }
    }


    @Override
    public View getCandidateView() {
        return mCandidateView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() ==  R.id.img_nowfloats) {
            if (mTabType != TabType.KEYBOARD) {
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_ICON_CLICKED,null);
                mTabType = TabType.KEYBOARD;
                manageKeyboardView.showKeyboardLayout();
            }
        }else if(view.getId() ==  R.id.tv_updates) {
            if (mTabType != TabType.UPDATES) {
                mTabType = TabType.UPDATES;
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SHOW_UPDATES,null);
                if ( SharedPrefUtil.fromBoostPref().getsBoostPref(mContext).isLoggedIn()){
                    manageKeyboardView.showShareLayout(updatesList == null ?
                            createModelList(TabType.UPDATES) : updatesList);
                }else{

                    if (updatesList == null){
                        updatesList = new ArrayList<>();
                    }else {
                        updatesList.clear();
                    }
                    AllSuggestionModel model = createSuggestionModel("Login",BaseAdapterManager.SectionTypeEnum.Login);
                    updatesList.add(model);
                    manageKeyboardView.showShareLayout(updatesList);
                }

            }
        }else if(view.getId() ==  R.id.tv_products) {
            if (mTabType != TabType.PRODUCTS) {
                mTabType = TabType.PRODUCTS;
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_SHOW_PRODUCT,null);
                if (SharedPrefUtil.fromBoostPref().getsBoostPref(mContext).isLoggedIn()) {
                    manageKeyboardView.showShareLayout(productList == null ?
                            createModelList(TabType.PRODUCTS) : productList);
                }else{

                    if (productList == null){
                        productList = new ArrayList<>();
                    }else {
                        productList.clear();
                    }
                    AllSuggestionModel model = createSuggestionModel("Login",BaseAdapterManager.SectionTypeEnum.Login);
                    productList.add(model);
                    manageKeyboardView.showShareLayout(productList);
                }
            }
        }else if(view.getId() ==  R.id.img_settings) {
            if (mTabType != TabType.SETTINGS) {
                mTabType = TabType.SETTINGS;
                manageKeyboardView.showSpeechInput();
                MixPanelUtils.getInstance().track(MixPanelUtils.KEYBOARD_VOICE_INPUT,null);
            }

        }
    }
    public ArrayList<AllSuggestionModel> createModelList(TabType suggestionType) {
        if(suggestionType == TabType.PRODUCTS) {
            productList = new ArrayList<>();
            AllSuggestionModel model = createSuggestionModel("", BaseAdapterManager.SectionTypeEnum.loader);
            productList.add(model);
            NetworkAdapter adapter = new NetworkAdapter();
            SharedPrefUtil boostPref = SharedPrefUtil.fromBoostPref().getsBoostPref(mContext);
            adapter.getAllProducts(boostPref.getFpTag(), mContext.getString(R.string.client_id),
                    0, "SINGLE", new CallBack<List<Product>>() {
                    @Override
                    public void onSuccess(List<Product> data) {
                        productList.clear();
                        if (data != null && data.size()>0) {
                            for (Product product : data) {
                                productList.add(product.toAllSuggestion());
                            }
                        }else{
                            AllSuggestionModel model = createSuggestionModel("Data not found",BaseAdapterManager.SectionTypeEnum.EmptyList);
                            productList.add(model);
                        }
                        if (mTabType == TabType.PRODUCTS) {
                            manageKeyboardView.onSetSuggestions(productList);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        AllSuggestionModel model = createSuggestionModel("Data not found",BaseAdapterManager.SectionTypeEnum.EmptyList);
                        productList.clear();
                        productList.add(model);
                        if (mTabType == TabType.PRODUCTS) {
                            manageKeyboardView.onSetSuggestions(productList);
                        }
                    }
                });
            return productList;
        } else if(suggestionType == TabType.UPDATES) {
            updatesList = new ArrayList<>();
            AllSuggestionModel model = createSuggestionModel("", BaseAdapterManager.SectionTypeEnum.loader);
            updatesList.add(model);
            NetworkAdapter adapter = new NetworkAdapter();
            SharedPrefUtil boostPref = SharedPrefUtil.fromBoostPref().getsBoostPref(mContext);
            adapter.getAllUpdates(boostPref.getFpId(), mContext.getString(R.string.client_id),
                    0, 0, new CallBack<Updates>() {
                        @Override
                        public void onSuccess(Updates data) {
                            updatesList.clear();
                            if (data != null && data.getFloats()!= null && data.getFloats().size()>0) {
                                for (Float update : data.getFloats()) {
                                    updatesList.add(update.toAllSuggestion());
                                }
                            }else{
                                AllSuggestionModel model = createSuggestionModel("Data not found",BaseAdapterManager.SectionTypeEnum.EmptyList);
                                updatesList.add(model);
                            }
                            if(mTabType == TabType.UPDATES) {
                                manageKeyboardView.onSetSuggestions(updatesList);
                            }
                        }

                        @Override
                        public void onError(Throwable t) {
                            AllSuggestionModel model = createSuggestionModel("Data not found",BaseAdapterManager.SectionTypeEnum.EmptyList);
                            updatesList.clear();
                            updatesList.add(model);
                            if (mTabType == TabType.UPDATES) {
                                manageKeyboardView.onSetSuggestions(updatesList);
                            }
                        }
                    });
            return updatesList;
        }
      return null;
    }

    private AllSuggestionModel createSuggestionModel(String text, BaseAdapterManager.SectionTypeEnum type){
        AllSuggestionModel model = new AllSuggestionModel("Data not found",null);
        model.setTypeEnum(type);
        return model;
    }
    private void sendKeyEvent(int keyEventCode) {
        if (imeListener.getImeCurrentInputConnection() != null) {
            imeListener.getImeCurrentInputConnection().sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
            imeListener.getImeCurrentInputConnection().sendKeyEvent(
                    new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        }
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event){
        switch (keyCode){
            case KEY_SYM_SHIFT:
                mShiftType = ShiftType.LOCKED;
                mKeyboardView.setShifted(true);
                return true;
        }
        return false;
    }
    class KeyboardListener extends AbstractKeyboardListener {

        @Override
        public void onPress(int primaryCode) {

            mKeyboardView.setPreviewEnabled(mKeyboardTypeCurrent != KeyboardUtils.KeyboardType.NUMBERS && primaryCode>=0);
            //mPopUpView.showAtLocation(mCurrentKeyboard.getKeys().get(15),mKeyboardView);
        }

        @Override
        public void onRelease(int primaryCode) {
            //mPopUpView.onRelease();
            //mKeyboardView.setPreviewEnabled(false);
        }

        @Override
        public void onKey(int primaryCode, int[] keys) {

            InputConnection inputConnection = imeListener.getImeCurrentInputConnection();
            playClick(primaryCode);

            switch(primaryCode){
                case Keyboard.KEYCODE_DELETE :
                    sendKeyEvent(KeyEvent.KEYCODE_DEL);
                    //inputConnection.deleteSurroundingText(1, 0);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    onShiftPressed();
                    break;
                case KEY_IME_OPTION:
                    sendKeyEvent(KeyEvent.KEYCODE_ENTER);
                    break;
                case KEY_NUMBER:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.NUMBERS);
                    break;
                case KEY_EMOJI:
                    mTabType = TabType.NO_TAB;
                    addCandidateTypeView(KeyboardUtils.CandidateType.BOOST_SHARE,TabType.NO_TAB);
                    manageKeyboardView.showEmojiLayout();
                    break;
                case KEY_QWRTY:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.QWERTY_LETTERS);
                    break;
                case KEY_SYM:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.SYMBOLS);
                    break;
                case KEY_SYM_SHIFT:
                    setCurrentKeyboard(KeyboardUtils.KeyboardType.SYMBOLS_SHIFT);
                    break;
                case KEY_LANGUAGE_CHANGE:
                    showLanguageMethods();
                    break;
                case KEY_SPACE:
                    primaryCode = 32;
                default:
                    char code = (char)primaryCode;
                    if(Character.isLetter(code) && mShiftType != ShiftType.NORMAL){
                        code = Character.toUpperCase(code);
                    }
                    inputConnection.commitText(String.valueOf(code),1);
            }
            if (primaryCode != Keyboard.KEYCODE_SHIFT && mShiftType == ShiftType.CAPITAL){
                mShiftType = ShiftType.NORMAL;
                mKeyboardView.setShifted(false);
            }
        }

        protected void playClick(int keyCode) {

            mKeyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            if (mAudioManager == null) {
                return;
            }
            switch (keyCode) {
                case KEY_SPACE:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                    break;
                case Keyboard.KEYCODE_DONE:
                case 10:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                    break;
                default:
                    mAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
            }
        }
        private void showLanguageMethods() {
            boolean isLanguageVisible = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (mInputMethodManager != null) {
                    // if true show keyboard language change button by calling  switchToNextInputMethod()
                    isLanguageVisible = mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
                }
            }
            if (isLanguageVisible){
                mInputMethodManager.switchToLastInputMethod(getToken());
            }else{
                Toast.makeText(mContext, "Unable to show other language", Toast.LENGTH_SHORT).show();
            }
        }

        private void onShiftPressed() {
            mShiftType = mShiftType == ShiftType.NORMAL ? ShiftType.CAPITAL : ShiftType.NORMAL;
            caps = !caps;
            mKeyboardView.setShifted(mShiftType != ShiftType.NORMAL);
            mKeyboardView.invalidateAllKeys();
        }
        @Override
        public void onText(CharSequence charSequence) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }

    }
}