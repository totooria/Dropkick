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

/* Twitter4Jのインスタンスを作成するためのファクトリと認証関連の読み書きをするクラス
 *
 * 参考:
 * http://android.roof-balcony.com/shori/strage/preference/
 */


import tokyo.ryogo.dropkick.R;


import tokyo.ryogo.dropkick.util.Utility;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;



public class DKTwitter {

    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";


    // Twitterインスタンスを取得します。アクセストークンが保存されていれば自動的にセットします。
    public static Twitter getTwitterInstance(Context context) {
        String consumerKey =  TwitterSecret.getTwitterConsumerKey(context.getString(R.string.cryption));
        String consumerSecret = TwitterSecret.getTwitterConsumerKeySecret(context.getString(R.string.cryption));

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);

        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

     // アクセストークンをプリファレンスに保存します。
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        String TokenSecretEncrypted=null;

        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());

        if(!Utility.isNullOrEmpty(accessToken.getTokenSecret())){
            try{
                TokenSecretEncrypted = Utility.encrypt(context.getString(R.string.cryption), accessToken.getTokenSecret());
                editor.putString(TOKEN_SECRET, TokenSecretEncrypted);
            } catch (Exception e) {
                editor.putString(TOKEN_SECRET, "");
            }
        }

        editor.apply(); // commitで同期書き込み
    }

    // アクセストークンをプリファレンスから読み込みます。
    public static AccessToken loadAccessToken(Context context) {
        String tokenSecret = null;

        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecretEncrypted = preferences.getString(TOKEN_SECRET, null);

        if(!Utility.isNullOrEmpty(tokenSecretEncrypted)) {
            try {
                tokenSecret = Utility.decrypt(context.getString(R.string.cryption), tokenSecretEncrypted);
            } catch (Exception e) {
                tokenSecret = null;
            }
        }

        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    // アクセストークンをクリアする
    public static void ClearAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);
        editor.apply(); // commitで同期書き込み
    }

    // アクセストークンが存在する場合はtrueを返す。
    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }

}