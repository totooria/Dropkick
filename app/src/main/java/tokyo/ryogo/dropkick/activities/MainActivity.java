/*
 * Copyright (C) 2016 Ryogo Amamiya ( http://ryogo.tokyo/ )
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package tokyo.ryogo.dropkick.activities;


import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import tokyo.ryogo.dropkick.BuildConfig;
import tokyo.ryogo.dropkick.R;
import tokyo.ryogo.dropkick.fragments.ChooseListDialogFragment;
import tokyo.ryogo.dropkick.fragments.ConfirmDialogFragment;
import tokyo.ryogo.dropkick.fragments.VersionInfoDialogFragment;
import tokyo.ryogo.dropkick.sns.ISns;
import tokyo.ryogo.dropkick.sns.Status;
import tokyo.ryogo.dropkick.sns.twitter.TwitterWrapper;
import tokyo.ryogo.dropkick.usecases.CommonPreference;
import tokyo.ryogo.dropkick.usecases.HashTagHistory;
import tokyo.ryogo.dropkick.util.Utility;

import static tokyo.ryogo.dropkick.util.Utility.isNullOrEmpty;


/**
 * メインアクティビティ
 */
public class MainActivity extends AppCompatActivity
                            implements ConfirmDialogFragment.DialogOnButtonClickedEventListener,
                                         ChooseListDialogFragment.ChooseListOnSelectedEventListener {

    // Twitter関係クラス
    private ISns mTwitter;

    //プリファレンスアクセス用クラスのインスタンス
    private CommonPreference mPreference;

    // ハッシュタグ履歴クラス
    private HashTagHistory mHashTagHistory;

    //ハッシュタグについての情報を保持する変数
    private HashTagInformation mHashTagUsage;

    //ハッシュタグについての情報
    class HashTagInformation{
        private final String mHashTag;
        private int mCount = 1;

        HashTagInformation(String hashTag){
            mHashTag = hashTag;
        }

        HashTagInformation(String hashTag, int count){
            mHashTag = hashTag;
            mCount = count;
        }

        String getHashTag(){
            return mHashTag;
        }

        int getCount(){
            return mCount;
        }

        int incrementCount(){
            mCount++;
            return mCount;
        }
    }

    // UI要素たち
    private Button mButtonPost;
    private Button mButtonShowTagHistory;
    private EditText mTweetBodyText;
    private EditText mHashTagText;
    private TextView mTextHashTagUsage;

    //現在表示中のダイアログ
    private DialogFragment mCurrentDialog;

    //オプションメニュー用
    private static final int MENU_EDIT_HISTORY = Menu.FIRST ;
    private static final int MENU_AUTHORIZATION = MENU_EDIT_HISTORY +1;
    private static final int MENU_SHOW_VERSION = MENU_AUTHORIZATION + 1;

    //プリファレンス用
    private static final String KEY_HASH_TAG_LAST_USED = "hash_tag_last_used";
    private static final String KEY_HASH_TAG_CURRENT_USING_COUNT = "hash_tag_current_using_count";
    private static final String KEY_HASH_TAG_EDIT_TEXT_VALUE = "hash_tag_edit_text_value";
    private static final String KEY_SAVED_VERSION = "saved_version";

    //サブアクティビティの識別用ID
    private static final int REQUEST_SUB_ACTIVITY_EDIT_HISTORY = 0xA;

    // ダイアログ識別用タグ
    private static final String DIALOG_TAG_CLEAR_OAUTH = "ClearOAuth";
    private static final String DIALOG_TAG_CHOOSE_HASH_TAG = "ChooseHashTag";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 画面を初期化
        setContentView(R.layout.activity_main);

        // 設定アクセス用クラスをインスタンス化
        mPreference = new CommonPreference(getApplication());

        // Twitterラッパークラスをインスタンス化
        mTwitter = new TwitterWrapper();
        mTwitter.initialize(getApplication());  //初期化

        // OAuth認証情報がない場合
        if (!mTwitter.isConfiguredAccount()) {
            //認証画面を開く
            mTwitter.configureAccount();
            //このアクティビティは閉じる
            finish();
            return;
        }

        // ハッシュタグ履歴を初期化して読み込む
        mHashTagHistory = new HashTagHistory();
        mHashTagHistory.load(mPreference);

        // UIイベントを登録
        registerUIEvents();

        // 本文ボックスにフォーカスを当てる
        mTweetBodyText.requestFocus();

        // ハードウェアキーボードが接続されている場合は、
        // ヒントを表示する
        if (isHardwareKeyboardAvailable()){
            showToast(R.string.msg_physical_keyboard_detected);
        }


    }

    private void registerUIEvents(){

        // UI要素を変数にいれておく
        mButtonPost = (Button)findViewById(R.id.buttonPost);
        mButtonShowTagHistory = (Button) findViewById(R.id.buttonShowTagHistory);
        Button mButtonInputWChar = (Button) findViewById(R.id.buttonInputWChar);
        mTweetBodyText = (EditText)findViewById(R.id.editTweetBody);
        mHashTagText = (EditText)findViewById(R.id.editHashTag);
        mTextHashTagUsage = (TextView)findViewById(R.id.textHashTagUsage);


        //UIイベントを登録-------------------------------------------------
        mButtonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postStatus();
            }
        });

        // HISTORYボタン
        mButtonShowTagHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHashTagSelectionDialog();
            }
        });

        // HISTORY長押しでテキストボックスを消去
        mButtonShowTagHistory.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                mHashTagText.setText(""); //ロングタップでハッシュタグ欄をクリア
                return true;               //trueを返すとonClickは発生しない
            }
        });

        // 草生やしボタン
        mButtonInputWChar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View w) {
                // 入力しても良い状態なら
                if(mButtonPost.isEnabled()){
                    int startSelection = mTweetBodyText.getSelectionStart();
                    int endSelection = mTweetBodyText.getSelectionEnd();

                    String tweetBody = mTweetBodyText.getText().toString();

                    String tweetFirst = tweetBody.substring(0, startSelection);
                    String tweetLast = endSelection < tweetBody.length() ? tweetBody.substring(endSelection) : "";

                    try {
                        StringBuilder sb = new StringBuilder(tweetFirst);
                        sb.append("ｗ");
                        sb.append(tweetLast);

                        mTweetBodyText.setText(sb.toString());
                        mTweetBodyText.setSelection(startSelection + 1);
                    }catch (NullPointerException e){
                        showToast("w-function failure : " + e.getMessage());
                    }

                }
            }
        });


        // ショートカットキー操作を可能にするためにキー入力を監視するクラス
        View.OnKeyListener keyListener = new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // いままさにキーが押された状態の時
                if(event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {

                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_F2:
                            // ハッシュタグ履歴表示ボタンが押せる状況ならば押す
                            clickButtonIfEnabled(mButtonShowTagHistory);
                            return true;
                        case KeyEvent.KEYCODE_F11:
                        case KeyEvent.KEYCODE_F12:
                            // 投稿ボタンが押せる状況ならば押す
                            clickButtonIfEnabled(mButtonPost);
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            // Ctrlが押されている場合投稿ボタンが押せる状況ならば押す
                            if(event.isCtrlPressed()) {
                                // 投稿ボタンが押せる状況ならば押す
                                clickButtonIfEnabled(mButtonPost);
                                return true;
                            }else{
                                break;
                            }
                        default:
                            return false;
                    }
                }
                return false;
            }
        };

        // 本文の入力値の変更を監視するクラス
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                //Do nothing.
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Do nothing
                return;
            }

            @Override
            public void afterTextChanged(Editable s) {


                int bodyCount = mTweetBodyText.getText().length();
                int charCount = bodyCount;
                int tagCount  = mHashTagText.getText().length();

                //ハッシュタグがある場合はスペース1文字分も勘定に入れる
                if (tagCount > 0) {
                    charCount += (1 + tagCount);
                }

                //ハッシュタグ+本文 ＞ 0字の時は、Postボタンに現在の文字数を表示する
                if (charCount > 0) {
                    mButtonPost.setText(
                            String.format(
                                    MainActivity.this.getResources().getString(
                                            R.string.button_post_with_char_count),
                                    charCount )
                    );
                }else{
                    mButtonPost.setText(R.string.button_post);
                }
            }
        };

        //上記クラスをイベントリスナに追加
        mTweetBodyText.addTextChangedListener(textWatcher);
        mHashTagText.addTextChangedListener(textWatcher);
        mTweetBodyText.setOnKeyListener(keyListener);
        mHashTagText.setOnKeyListener(keyListener);

    }

    @Override
    protected void onResume(){
        super.onResume();

        //ハッシュタグの使用状況をロードする
        loadUsage();

        //ハッシュタグの履歴をロードする
        loadHashTagHistory();

        //画面状態をロードする
        loadActivityState();

    }

    @Override
    protected void onPause(){
        super.onPause();

        //ハッシュタグをセーブする
        saveUsage();

        // ハッシュタグの履歴をセーブする
        saveHashTagHistory();

        //画面状態をセーブする
        saveActivityState();

    }

    // アクティビティを前回の状態に復元する
    private void loadActivityState(){

        //前回入力されていたハッシュタグ文字列をロード
        String savedValue = mPreference.LoadString(KEY_HASH_TAG_EDIT_TEXT_VALUE, "");

        //テキストボックスへ反映
        mHashTagText.setText(savedValue);
    }

    // アクティビティの状態を保存する
    private void saveActivityState(){
        //ハッシュタグの現在の入力値を保存
        mPreference.SaveString(KEY_HASH_TAG_EDIT_TEXT_VALUE, mHashTagText.getText().toString());
    }

    // ハッシュタグの履歴を読み込む
    private void loadHashTagHistory(){
        // ハッシュタグ履歴の配列を読み込み
        mHashTagHistory.load(mPreference);
    }

    // ハッシュタグの履歴を保存する
    private void saveHashTagHistory(){
        //ハッシュタグ履歴を保存
        mHashTagHistory.save(mPreference);
    }

    /***
     * 子アクティビティから戻ってきたとき
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SUB_ACTIVITY_EDIT_HISTORY: // 履歴編集ウィンドウから戻った場合
                if( resultCode == EditHistoryActivity.EDIT_HISTORY_OK_BUTTON_CLICKED) loadHashTagHistory();   //OKボタンで戻ってきた場合は、ハッシュタグを再読込
                break;
        }
    }

    /**
     ** オプションメニューを作成する
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(Menu.NONE, MENU_EDIT_HISTORY, Menu.NONE, R.string.menu_edit_history);
        menu.add(Menu.NONE, MENU_AUTHORIZATION, Menu.NONE, R.string.menu_authorization);
        menu.add(Menu.NONE, MENU_SHOW_VERSION, Menu.NONE, R.string.menu_show_version);
        return true;
    }

    /**
     ** オプションメニューを選択された時に呼ばれる
     **/
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case MENU_EDIT_HISTORY: // 履歴編集メニュー

                // 現在の履歴を一度保存する
                saveHashTagHistory();

                //履歴編集画面のアクティビティを開くためのインテントを作る
                Intent intent = new Intent(getApplication(), EditHistoryActivity.class);

                //インテントを開始
                startActivityForResult(intent, REQUEST_SUB_ACTIVITY_EDIT_HISTORY);

                return true;
            case MENU_AUTHORIZATION:    // 認証再実行メニュー

                // 確認ダイアログを作成して表示
                mCurrentDialog =
                        ConfirmDialogFragment.newInstance(
                                getString(R.string.app_name),
                                getString(R.string.label_confirm_clear_authorization),
                                true);
                mCurrentDialog.show(getFragmentManager(), DIALOG_TAG_CLEAR_OAUTH); // タグはDIALOG_TAG_CLEAR_OAUTH

                return true;
            case MENU_SHOW_VERSION: //バージョン表示メニュー

                // バージョン情報ダイアログを作成して表示
                VersionInfoDialogFragment dialogFragment =
                        VersionInfoDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), null);

                return true;
        }
        return false;
    }

    // ダイアログのボタンが押された
    @Override
    public void dialogOnButtonClicked(boolean isCanceled){
        if(mCurrentDialog != null && mCurrentDialog.getTag() != null) {
            switch (mCurrentDialog.getTag()) {
                case DIALOG_TAG_CLEAR_OAUTH:
                    // 認証情報をクリア画面から呼ばれた
                    if(!isCanceled) {
                        // OKボタンが押された

                        //認証情報を消す（次回PIN入力画面を出すため）
                        mTwitter.clearAccount();

                        // OAuth認証画面を開く
                        mTwitter.configureAccount();
                    }
                    break;
            }
        }
    }

    // 選択リストダイアログで項目が選択された
    @Override
    public void chooseListOnListSelected(String item){
        if(mCurrentDialog != null && mCurrentDialog.getTag() != null) {
            switch (mCurrentDialog.getTag()) {
                case DIALOG_TAG_CHOOSE_HASH_TAG:
                    // ハッシュタグ選択画面から呼ばれた

                    // ハッシュタグのテキストボックスに選択された内容を入力
                    mHashTagText.setText(item);

                    // 選ばれた項目を一番上にする
                    updateHashTagList();

                    break;
            }
        }
    }


    // つぶやきを投稿する
    private void postStatus(){

        // 投稿処理用の無名クラスを宣言
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {

                if (BuildConfig.DEBUG) {
                    return true;
                } else {
                    return mTwitter.updateStatus();
                }
            }

            @Override
            // 投稿完了後のメソッド
            protected void onPostExecute(Boolean result) {
                if (result) {
                    // ハッシュタグ利用状況を更新する
                    updateUsageLabel();

                    // 本文を空にする
                    mTweetBodyText.setText("");

                    if (BuildConfig.DEBUG) {
                        showToast(R.string.msg_debug_mode_enabled);
                    }else{
                        showToast(R.string.msg_tweet_success);
                    }
                } else {
                    showToast(mTwitter.getLastErrorMessage());
                }
                // Postボタンを押せるようにする
                mButtonPost.setEnabled(true);
            }

            @Override
            // 投稿実行前のメソッド
            protected void onPreExecute() {
                //Postボタンを押せなくする
                mButtonPost.setEnabled(false);

                //ハッシュタグ保存
                updateHashTagList();
            }

        };

        //ハッシュタグの文字列処理を行う
        formatHashTag();

        //ツイート内容をセットする
        boolean isStatusSetOk = mTwitter.setStatus(new Status(mTweetBodyText.getText().toString(), getHashTagArray()));

        if (isStatusSetOk){
            // 成功したら非同期で投稿を実行
            task.execute();
        }else{
            // 失敗したらメッセージ表示
            showToast(mTwitter.getLastErrorMessage());
        }


    }


    // 書き込み前にハッシュタグに使用できない文字を探して削除する
    private void formatHashTag() {
        String tags = mHashTagText.getText().toString();
        tags = Utility.replaceIllegalCharactersForHashTag(tags);
        mHashTagText.setText(tags);
    }

    // 入力されているハッシュタグをスペースで区切って配列に変換する
    private String[] getHashTagArray(){
        return mHashTagText.getText().toString().split(" ");
    }

    /***
     * 使用状況を更新
      */
    private void updateUsageLabel(){

        String tags = mHashTagText.getText().toString();

        if(!isNullOrEmpty(tags)) {
            //ハッシュタグ使用の場合はそのタグの使用状況を更新する
            if (mHashTagUsage != null && mHashTagUsage.getHashTag().equals(tags)) {
                // 前回と同じハッシュタグの場合は、使用回数を+1
                mHashTagUsage.incrementCount();
            }else{
                // 前回と違うハッシュタグの場合は、新しくインスタンスを作成
                mHashTagUsage = new HashTagInformation(tags);
            }

            //表示を更新
            String caption = String.format
                    (getString(R.string.label_tweet_detail2),mHashTagUsage.getCount());
            mTextHashTagUsage.setText(caption);

        }else {
            //ハッシュタグ未使用の場合は表示を消す
            mTextHashTagUsage.setText("");
            mHashTagUsage = null;
        }

    }

    // ユーザーによって入力されたハッシュタグをハッシュタグリストに追加する
    private void updateHashTagList() {

        //入力されたハッシュタグを取得
        String currentHashTag = mHashTagText.getText().toString();

        // 空文字でも空白でも無いときのみ処理
        if (!isNullOrEmpty(currentHashTag)) {

            // ハッシュタグリストに追加
            mHashTagHistory.add(currentHashTag);

        }
    }


     // プリファレンスから前回終了時のハッシュタグ情報のインスタンスを初期化する
    private void loadUsage() {

        // 最後に使ったハッシュタグの情報を読む
        String hashTagLastUsed = mPreference.LoadString(KEY_HASH_TAG_LAST_USED);

        //使用回数を読み込む
        int hashTagCurrentUsingCount =
                Integer.parseInt(mPreference.LoadString(KEY_HASH_TAG_CURRENT_USING_COUNT, String.valueOf(0)));

        if (!hashTagLastUsed.isEmpty()) {
            // 最後に使ったハッシュタグが空ではない場合
            mHashTagUsage = new HashTagInformation(hashTagLastUsed, hashTagCurrentUsingCount);
        }else{
            // 空の場合は使っていないとみなし、nullにしておく
            mHashTagUsage = null;
        }

    }


    // 現在の使用状況を保存
    private void saveUsage(){

        if (mHashTagUsage != null) {

            // 最後に使ったハッシュタグの情報も保存しておく
            mPreference.SaveString(KEY_HASH_TAG_LAST_USED, mHashTagUsage.getHashTag());

            //タグの使用回数を書き込み
            mPreference.SaveString(KEY_HASH_TAG_CURRENT_USING_COUNT, String.valueOf(mHashTagUsage.getCount()));

            //バージョン情報を書き込み
            mPreference.SaveString(KEY_SAVED_VERSION, Integer.toString(getCurrentVersion()));

        }
    }


    // ハッシュタグ選択ダイアログを表示する
    private void showHashTagSelectionDialog() {

        // ハッシュタグリストが初期化されていない場合はロード
        if ( mHashTagHistory == null){
            loadHashTagHistory();
        }

        //ハッシュタグ履歴がひとつもない場合はメッセージを表示
        if (mHashTagHistory.count() == 0){

            // 確認ダイアログを表示
            mCurrentDialog =
                    ConfirmDialogFragment.newInstance(
                            getString(R.string.app_name),
                            getString(R.string.label_no_hash_tag_history),
                            false);
            mCurrentDialog.show(getFragmentManager(), null);

            return;
        }

        // ハッシュタグ選択ダイアログを表示
        mCurrentDialog =
                ChooseListDialogFragment.newInstance(
                       getString(R.string.label_select_hash_tag), mHashTagHistory.toArray());
        mCurrentDialog.show(getFragmentManager(), DIALOG_TAG_CHOOSE_HASH_TAG);


    }


    private void showToast(int resID) {
        showToast(getString(resID));
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int getCurrentVersion(){
        PackageManager pm = this.getPackageManager();
        try{
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
            return packageInfo.versionCode;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return -1;
        }
    }

    // 物理キーボードが存在するか
    private boolean isHardwareKeyboardAvailable() {
        // http://stackoverflow.com/questions/2415558/how-to-detect-hardware-keyboard-presence
        Configuration configuration = MainActivity.this.getResources().getConfiguration();
        return (configuration.keyboard & Configuration.KEYBOARD_QWERTY) ==  Configuration.KEYBOARD_QWERTY;
    }

    // 引数で渡されたボタンが、有効な場合のみクリックする。無効の場合は何もしない。
    private void clickButtonIfEnabled(Button button){
        if(button.isEnabled()) button.callOnClick();
    }

}
