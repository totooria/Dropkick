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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * リストから1つを選択するダイアログを表示するためのアクティビティ
 * 呼び出し側で ChooseListOnSelectedEventListener を実装しなければならない。
 */
public class ChooseListDialogFragment extends DialogFragment {


    private static final String TITLE = "title";
    private static final String ITEMS = "items";

    private String mTitle;
    private String[] mItems;

    //OKボタンが押されたことを通知するリスナー
    private ChooseListOnSelectedEventListener mOnSelectedListener;

    // コンストラクタ（ないとおかしくなる）
    public ChooseListDialogFragment(){
        // 何も書かない
    }

    /**
     * 新しいインスタンスを作成
     */
    public static ChooseListDialogFragment newInstance(String title, String[] items) {

        ChooseListDialogFragment fragment = new ChooseListDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putStringArray(ITEMS, items);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
            mItems = getArguments().getStringArray(ITEMS);
        }

        //ダイアログの作成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        //builder.setMessage(mMessage);

        // リスト項目とイベントを設定
        builder.setItems(mItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOnSelectedListener.chooseListOnListSelected(mItems[which]);
            }
        });

        // ダイアログを作成してあとはAndroidにお任せ
        return  builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        //リスナーのセット
        super.onAttach(activity);
        try {
            mOnSelectedListener = (ChooseListOnSelectedEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnOkButtonListener");
        }
    }

    @Override
    public void onDetach() {
        //リスナーの削除
        super.onDetach();
        mOnSelectedListener = null;
    }

    public interface ChooseListOnSelectedEventListener {
        void chooseListOnListSelected(String hashTag);
    }

}
