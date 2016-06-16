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


package tokyo.ryogo.dropkick.util;

import android.util.Base64;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 汎用的なツール類を押し込めたクラス
 *
 */
public class Utility {
    /**
     * 2つの日付の差を求めます。
     * java.util.Date 型の日付 date1 - date2 が何日かを返します。
     * http://chat-messenger.net/blog-entry-88.html
     *
     * 計算方法は以下となります。
     * 1.最初に2つの日付を long 値に変換します。
     * 　※この long 値は 1970 年 1 月 1 日 00:00:00 GMT からの
     * 　経過ミリ秒数となります。
     * 2.次にその差を求めます。
     * 3.上記の計算で出た数量を 1 日の時間で割ることで
     * 　日付の差を求めることができます。
     * 　※1 日 ( 24 時間) は、86,400,000 ミリ秒です。
     *
     * @param date1    日付 java.util.Date
     * @param date2    日付 java.util.Date
     * @return    2つの分の差
     */
    public static int calculateDifferenceMinutes(Date date1, Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long one_minute_time = 1000 * 60;
        long diffMinutes = (datetime1 - datetime2) / one_minute_time;
        return (int) diffMinutes;
    }

    /***
     * 引数の文字列が、
     * 未定義・NULL・空文字列・空白文字のみの文字列
     * いずれかの場合、trueを返す
     */
    public static boolean isNullOrEmpty(String str)
    {
        if(str == null || str.length() == 0){
            return true;
        }else{
            //空白かスペースかタブ **以外**の文字列がひとつでもあればマッチする！
            Pattern p = Pattern.compile("[^\\t\\s　]");
            Matcher m = p.matcher(str);
            return !m.find();
        }
    }

    /***
     *
     * ハッシュタグには使えない文字を置き換えて返すメソッド
     */
    public static String replaceIllegalCharactersForHashTag(String input){
        input = input.replace('|', '_');
        input = input.replace('　', ' ');
        input = input.replace('＃', '#');
        return input;
    }


    // 暗号化する
    public static String encrypt(String key, String plainText)
        throws javax.crypto.IllegalBlockSizeException,
            java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException,
            java.io.UnsupportedEncodingException,
            javax.crypto.BadPaddingException,
            javax.crypto.NoSuchPaddingException {

        // 暗号化方式はBlowfish暗号化
        javax.crypto.spec.SecretKeySpec sksSpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "Blowfish");

        // 暗号エンジンのインスタンスを取得
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("Blowfish");

        // 暗号エンジンを初期化
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);

        // 暗号化を実行
        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        //BASE64でエンコード
        String encode = Base64.encodeToString(encrypted, Base64.DEFAULT);

        return encode;
    }

    // 暗号を復号する（BASE64文字をデコードしたバイト配列を復号する）
    public static String decrypt(String key, String encryptedText)
            throws javax.crypto.IllegalBlockSizeException,
            java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException,
            java.io.UnsupportedEncodingException,
            javax.crypto.BadPaddingException,
            javax.crypto.NoSuchPaddingException {

        //BASE64から解読
        byte[] encrypted = Base64.decode(encryptedText, Base64.DEFAULT);

        return decrypt(key, encrypted);

    }


    // 暗号を復号する（バイト配列をそのまま復号する）
    public static String decrypt(String key, byte[] encrypted)
            throws javax.crypto.IllegalBlockSizeException,
            java.security.InvalidKeyException,
            java.security.NoSuchAlgorithmException,
            java.io.UnsupportedEncodingException,
            javax.crypto.BadPaddingException,
            javax.crypto.NoSuchPaddingException {

        // 暗号化方式はBlowfish暗号化
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), "Blowfish");

        // 暗号エンジンのインスタンスを取得
        Cipher cipher = Cipher.getInstance("Blowfish");

        // 暗号エンジンを初期化
        cipher.init(Cipher.DECRYPT_MODE, sksSpec);

        // 復号して文字列を返す
        return new String(cipher.doFinal(encrypted));
    }
}
