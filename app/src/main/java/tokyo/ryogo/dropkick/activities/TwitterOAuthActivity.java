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

import tokyo.ryogo.dropkick.sns.twitter.DKTwitter;
import tokyo.ryogo.dropkick.R;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

/*
 * Twitterの認証をするためのアクティビティ
 */
public class TwitterOAuthActivity extends AppCompatActivity {

    private Twitter mTwitter;
    private RequestToken mRequestToken;

    private Button mOKButton;
    private EditText mPINText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_twitter_oauth);

        // UI要素を変数へ割り付け
        mOKButton = (Button)findViewById(R.id.buttonOK);
        Button mBeginAuthButton = (Button) findViewById(R.id.buttonBeginAuth);
        mPINText = (EditText)findViewById(R.id.editPIN);

        //Twitterインスタンスを初期化
        InitializeTwitterInstance();

        changeUIEnabled(UIPhase.Initial);

        mBeginAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAuthorize();
            }
        });

        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPINAuth();
            }
        });
    }

    // Twitterのインスタンスを作成
    private void InitializeTwitterInstance() {
        DKTwitter.ClearAccessToken(this);
        mTwitter = DKTwitter.getTwitterInstance(this);
    }

    // UIの有効状態を変更する
    private void changeUIEnabled(UIPhase phase){
        switch (phase) {
            case Initial:
                mOKButton.setEnabled(false);
                mPINText.setEnabled(false);
                mPINText.setText("");
                break;
            case BrowserOpend:
                mOKButton.setEnabled(true);
                mPINText.setEnabled(true);
                break;
            case PINEntered:
                mOKButton.setEnabled(false);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private enum UIPhase{
        Initial,    // 最初の状態
        BrowserOpend,   // 認証URLを開いた後の状態
        PINEntered  // PINを入力した状態
    }


     // OAuth認証を開始します。
    private void goAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            // バックグラウンドのスレッドで実行
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken();
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {


                    // 認証用URLを開く
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);


                    //編集とOKボタンを有効にする
                    changeUIEnabled(UIPhase.BrowserOpend);
                } else {
                    //失敗
                    InitializeTwitterInstance();

                    changeUIEnabled(UIPhase.Initial);
                }

                //リトライできるようにボタンを有効にする
                findViewById(R.id.buttonBeginAuth).setEnabled(true);
            }
        };

        //何回も押されたら嫌なのでボタンを無効にする
        findViewById(R.id.buttonBeginAuth).setEnabled(false);

        //……以上の処理をバックグラウンドで実行
        task.execute();
    }


    //  入力されたPINから認証します
    private void goPINAuth(){

        // PIN入力値代入
        String PINText = mPINText.getText().toString();

        //入力値チェック
        if (PINText.equals("")) {
            showToast(R.string.msg_err_invalid_pin);
            return;
        }


        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    // params[0] = et = editPIN.text
                    return mTwitter.getOAuthAccessToken(params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    // 認証成功！
                    showToast(R.string.msg_pin_auth_success);
                    successOAuth(accessToken);
                    closeActivity();
                } else {
                    // 認証失敗。。。
                    showToast(R.string.msg_pin_auth_failed);

                    // 認証は最初からやり直し
                    InitializeTwitterInstance();

                    //UIの状態を初期に戻す
                    changeUIEnabled(UIPhase.Initial);
                }
            }
        };

        //何回も押されたら嫌なのでボタンを無効にする
        changeUIEnabled(UIPhase.PINEntered);

        task.execute(PINText);

    }



    // OAUthの認証情報を保存して、メインウィンドウを表示する
    private void successOAuth(AccessToken accessToken) {
        DKTwitter.storeAccessToken(this, accessToken);

    }

    // このアクティビティを閉じて、メインに遷移させる
    private void closeActivity(){

        //メインウィンドウを表示するためのインテントを作成
        Intent intent = new Intent(this, MainActivity.class);

        //インテントを開始
        startActivity(intent);

        //このアクティビティは終了
        finish();
    }

    private void showToast(int resID) {
        Toast.makeText(this, getString(resID), Toast.LENGTH_SHORT).show();
    }
}
