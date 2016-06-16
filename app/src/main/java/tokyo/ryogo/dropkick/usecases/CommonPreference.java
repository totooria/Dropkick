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


package tokyo.ryogo.dropkick.usecases;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import tokyo.ryogo.dropkick.R;

/**
 * プリファレンスの読み書きのための汎用的なクラス
 */
public class CommonPreference {
    private final Application mApplicationContext;
    private final String PREF_SECTION_NAME;

    public CommonPreference(Application applicationContext){
        mApplicationContext = applicationContext;
        PREF_SECTION_NAME = applicationContext.getString(R.string.pref_section_name);
    }

    // keyをキーとして、文字列valueを設定に保存する。
    public void SaveString(String key, String value){

        //書き込み
        SaveString(key, value ,false);
    }

    // keyをキーとして、文字列valueを設定に保存する。
    // 非同期保存が必要な場合はsyncRequiredをTrueにする。
    public void SaveString(String key, String value, boolean syncRequired){

        //プリファレンスを開く
        SharedPreferences preferences = mApplicationContext.getSharedPreferences(PREF_SECTION_NAME, Context.MODE_PRIVATE);

        //プリファレンスの編集を行う
        SharedPreferences.Editor editor = preferences.edit();

        //書き込み
        editor.putString(key, value);

        // 同期保存が必要な場合は同期保存
        // commitかapplyしないと保存されない
        if(syncRequired){
            editor.commit();    // 同期
        }
        else{
            editor.apply();     // 非同期
        }

    }


    // keyをキーとして、文字列配列value[]を設定に保存する。（|でつないだBase64文字列として保存される）
    // 非同期保存が必要な場合はsyncRequiredをTrueにする。
    public void SaveStringArray(String key, String value[], boolean syncRequired) {

        if (value == null) throw new IllegalArgumentException();

        String saveText = value[0];

        // 配列の要素を | でつなぐ
        for (int i = 1; i < value.length; i++) {
            saveText += "|" + value[i];
        }

        //BASE64でエンコード
        String encode = Base64.encodeToString(saveText.getBytes(), Base64.DEFAULT);

        //書き込み
        SaveString(key, encode, syncRequired);

    }

    // keyをキーとして、設定を文字列として読み込む。失敗した場合は空白の文字列が返される。
    public String LoadString(String key)
    {
        return LoadString(key, "");
    }

    // keyをキーとして、設定を文字列として読み込む。失敗した場合はDefaultValueが返される。
    public String LoadString(String key, String DefaultValue){

        //プリファレンスを開く
        SharedPreferences preferences = mApplicationContext.getSharedPreferences(PREF_SECTION_NAME, Context.MODE_PRIVATE);

        //読み込み
        String savedValue = preferences.getString(key, DefaultValue);

        return savedValue;
    }
}
