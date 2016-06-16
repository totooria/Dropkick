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



package tokyo.ryogo.dropkick.sns.twitter;



import android.content.Context;
import android.content.Intent;

import tokyo.ryogo.dropkick.R;
import tokyo.ryogo.dropkick.activities.TwitterOAuthActivity;
import tokyo.ryogo.dropkick.sns.ISns;
import tokyo.ryogo.dropkick.sns.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Twitter4Jのラッパークラス。
 * ISnsを実装する。
 */
public class TwitterWrapper implements ISns {

    private String mTweetText;
    private String mLastErrorMessage;
    private Context mContext;
    private Twitter mTwitter;

    public final int TWITTER_STATUS_MAX_CHARACTERS = 140;

    // 初期化します
    public void initialize(Context context){
        mContext = context;
        mTwitter = null;
        mTweetText=null;
    }

    // 認証情報があるか
    public boolean isConfiguredAccount() {
        return DKTwitter.hasAccessToken(mContext);
    }

    // 初期設定を行う
    public void configureAccount() {

        //認証画面のアクティビティを開くためのインテントを作る
        Intent intent = new Intent(mContext, TwitterOAuthActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //インテントを開始
        mContext.startActivity(intent);

    }

    // 認証情報をクリア
    public void clearAccount() {
        DKTwitter.ClearAccessToken(mContext);
    }

    // 書き込む内容をセットする
    public boolean setStatus(Status status){

        // 本文が空なら受け付けない
        if(status.getBody().isEmpty()){
            mLastErrorMessage = mContext.getString(R.string.msg_tweet_empty);
            return false;
        }

        // テキストを整形する
        String tweetText = formatStatus(status);

        // 整形後のテキストが
        if (!tweetText.isEmpty()) {
            // 0文字以上

            if(tweetText.length() <= TWITTER_STATUS_MAX_CHARACTERS){
                // 140字以下なら書き込み用変数にセット
                mTweetText= tweetText;
                return true;
            }else{
                // 140字以上はエラー
                mLastErrorMessage = mContext.getString(R.string.msg_over_characters);
                return  false;
            }
        }

        return  false;  //ここには来ないはず
    }

    private String formatStatus(Status status){

        // 本文部分の文字列をコピー
        StringBuilder text = new StringBuilder(status.getBody());

        if(!status.getHashTag()[0].isEmpty()) {
            // ハッシュタグの配列が空じゃなければ
            // コピーした文字列の後ろに、スペース+ハッシュタグを1つずつ追加する
            for (int i = 0; i < status.getHashTag().length; i++) {
                text.append(" ");
                text.append(status.getHashTag()[i]);
            }
        }

        //出来た文字列をコピーする
        return text.toString();

    }

    // 書き込む内容をクリアする
    public void clearStatus() {
        mTweetText = "";
    }

    // セットされた内容を実際に書き込む
    public boolean updateStatus(){

        // Twitterインスタンスを作成していなかった場合は作成
        if(mTwitter == null) {
            mTwitter = DKTwitter.getTwitterInstance(mContext);
        }

        try {
            // ステータスをアップデート
            mTwitter.updateStatus(mTweetText);
            return true;
        }catch(TwitterException e){
            // エラー時はメッセージをセット
            mLastErrorMessage = mContext.getString(R.string.msg_err_internal) + e.getMessage();
            return false;
        }

    }

    // エラーメッセージを取得する
    public String getLastErrorMessage(){
        return mLastErrorMessage;
    }
}
