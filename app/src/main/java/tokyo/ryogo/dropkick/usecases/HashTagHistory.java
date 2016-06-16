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


import static tokyo.ryogo.dropkick.util.Utility.isNullOrEmpty;


import android.util.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * ハッシュタグ履歴を表現するクラス
 */
public class HashTagHistory {

    private static final String KEY_HASH_TAG = "hashtag";


    private static final int STRING_NOT_FOUND = -1;

    // ハッシュタグ保持用配列 (NULLは不許可)
    private String[] mHashTagHistory;


    // 要素数0で初期化します
    public HashTagHistory() {
        mHashTagHistory = new String[0];
    }

    // プリファレンスから読み込んで初期化します
    public HashTagHistory(CommonPreference preferences) {
        load(preferences);
    }

    // 指定したString配列を元に初期化します
    public HashTagHistory(String[] hashTagArray) {
        mHashTagHistory = hashTagArray.clone();
    }

    // プリファレンスから読み込む
    public void load(CommonPreference preferences) {

        //読み込み
        String base64String= preferences.LoadString(KEY_HASH_TAG);

        //BASE64から解読
        String decode = new String(Base64.decode(base64String, Base64.DEFAULT));

        if (decode.length() == 0) {
            //空文字列の場合は要素数0の配列を作成
            mHashTagHistory = new String[0];
        } else {
            //区切り文字で区切って配列へ格納
            mHashTagHistory = decode.split("\\|", 0);
        }

    }

    // プリファレンスへ書き込む
    public void save(CommonPreference preferences) {

        //ハッシュタグがnullでも空でもない場合は
        if ((mHashTagHistory != null) && (mHashTagHistory.length > 0)) {
            //書き込み
            preferences.SaveStringArray(KEY_HASH_TAG, mHashTagHistory, true);
        } else {
            //空文字列書き込み
            preferences.SaveString(KEY_HASH_TAG, "", true);
        }

    }

    // 要素を追加する
    // 既に要素が存在する場合は、その要素の順番を0番目に移動する
    public void add(String hashTag) {

        // 引数がNULLなら例外を投げる
        if (isNullOrEmpty(hashTag)) throw new IllegalArgumentException();


        //
        // ハッシュタグの追加イメージ
        //
        // 今まで使われたことがある → そのハッシュタグを配列の先頭に移動した上で、
        //                             旧0番目は新1番目、旧1番目は新2番目という風に順次繰り下げる
        // 今まで使われたことがない → そのハッシュタグを配列の先頭に追加し、あとは旧配列からコピーする

        if(mHashTagHistory.length == 0){
            //要素数が0なら単純にそれだけの配列を作る

            mHashTagHistory = new String[]{hashTag};

        }else {
            // 要素数が1以上なら一度リストを作ってから配列を再度生成する

            // 配列からリストを作成
            List<String> hashTagList = new ArrayList<>();

            // 0番目に追加する
            hashTagList.add(0, hashTag);

            for (String oneHashTag : mHashTagHistory) {
                // 元のリスト内に存在しない項目のみ追加する
                if (!hashTag.equals(oneHashTag)) {
                    hashTagList.add(oneHashTag);
                }
            }

            // toArray は同じ要素数の配列を作ってやらないと例外を投げる
            mHashTagHistory = hashTagList.toArray(new String[hashTagList.size()]);

        }
    }

    // 指定した文字列の要素を含むかどうか
    public boolean contains(String stringToFind) {
        int index = find(stringToFind);
        return (index != STRING_NOT_FOUND);

    }

    // 指定した文字列の要素を検索する
    // 見つかった場合は要素のindex、見つからなかった場合はSTRING_NOT_FOUNDを返す
    public int find(String stringToFind) {
        for (int i = 0; i < mHashTagHistory.length; i++) {
            if (stringToFind.equals(mHashTagHistory[i])) {
                return i;
            }
        }
        return STRING_NOT_FOUND;
    }

    // index番目の要素を取得する
    public String getAt(int index){
        checkIndex(index);

        return mHashTagHistory[index];
    }

    // index番目の要素を削除する
    public void removeAt(int index){
        checkIndex(index);

        // 配列からリストを作成
        List<String> hashTagList = Arrays.asList(mHashTagHistory);
        hashTagList.remove(index);
        mHashTagHistory = hashTagList.toArray(new String[hashTagList.size()]);

    }

    // index番目の要素を設定する
    public void setAt(int index, String value){
        checkIndex(index);

        mHashTagHistory[index] = value;
    }

    // 要素の数を返す
    public int count(){
        return mHashTagHistory.length;
    }

    // ハッシュタグ履歴をクリア
    public void clear(){
        mHashTagHistory = new String[0];
    }

    // 全要素を配列として取得する
    public String[] toArray(){
        return  mHashTagHistory.clone();
    }

    private void checkIndex(int index){
        if (index < 0 ||  mHashTagHistory.length < index) throw new ArrayIndexOutOfBoundsException();
    }
}
