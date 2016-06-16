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


package tokyo.ryogo.dropkick.fragments;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import tokyo.ryogo.dropkick.R;


/**
 * 「このアプリについて」を表示するためのダイアログ
 */

public class VersionInfoDialogFragment extends DialogFragment {

    private Button mOKButton;


    public VersionInfoDialogFragment() {
        //何も書かない
    }

    // 新しいインスタンスを作成
    public static VersionInfoDialogFragment newInstance() {

        VersionInfoDialogFragment fragment = new VersionInfoDialogFragment();
        return fragment;
    }

    @Override
    /// Dialogが作成される時に呼ばれる
    public Dialog onCreateDialog(Bundle b)
    {
        Dialog dialog = super.onCreateDialog(b);

        // ウィンドウのタイトルを消す
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;

    }

    @Override
    // Viewが作成されるときに呼ばれる（OnCreateDialogの後）
    public View onCreateView(LayoutInflater i, ViewGroup c, Bundle b)
    {
        View content = i.inflate(R.layout.fragment_version_info, null  );

        //各種情報をセット
        ((TextView)content.findViewById(R.id.applicationVersion)).setText(getVersion());
        mOKButton = (Button)content.findViewById(R.id.buttonOK);
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dismiss();
            }
        });


        return content;
    }

    // バージョン情報を整形して返す
    private String getVersion(){
        StringBuilder sb = new StringBuilder("Ver.");
        sb.append(getVersionName(getActivity()));
        sb.append(" (Code: ");
        sb.append(getVersionCode(getActivity()));
        sb.append(" )");
        return sb.toString();
    }

    // バージョンコードを取得する
    private static int getVersionCode(Context context){
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return versionCode;
    }

    // バージョン名を取得する
    private static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
