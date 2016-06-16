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



package tokyo.ryogo.dropkick.sns;


import android.content.Context;

/**
 * マイクロブログなどの操作に必要と思われる機能を抽象化したインターフェイス
 */
public interface ISns {

    // 初期化する
    void initialize(Context context);

    // 初期設定が済んでいるか？
    boolean isConfiguredAccount();

    // 初期設定を行う
    void configureAccount();

    // 設定を消す
    void clearAccount();

    // 書き込む内容をセットする
    boolean setStatus(Status status);

    // 書き込む内容をクリアする
    void clearStatus();

    // 実際に書き込む
    boolean updateStatus();

    // エラーメッセージを取得する
    String getLastErrorMessage();


}
