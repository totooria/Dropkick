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
 * 確認（OK / CANCEL）を表示するためのダイアログ
 * 呼び出し側で DialogOnButtonClickedEventListener を実装する必要がある
 */
public class ConfirmDialogFragment extends DialogFragment {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String HAS_CANCEL = "hasCancel";

    private String mTitle;
    private String mMessage;
    private boolean mHasCancel;

    //OKボタンが押されたことを通知するリスナー
    private DialogOnButtonClickedEventListener mListener;

    public ConfirmDialogFragment() {
        //何も書かない
    }

    // 確認ダイアログ( OK ボタン )のインスタンスを作成して返す
    // hasCancelをTrueにするとCancelボタンも付ける
    public static ConfirmDialogFragment newInstance(String title, String message, boolean hasCancel) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putBoolean(HAS_CANCEL, hasCancel);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
            mMessage = getArguments().getString(MESSAGE);
            mHasCancel = getArguments().getBoolean(HAS_CANCEL);
        }

        //ダイアログの作成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.dialogOnButtonClicked( false);
            }
        });
        if (mHasCancel) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mListener.dialogOnButtonClicked( true);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        //リスナーのセット
        super.onAttach(activity);
        try {
            mListener = (DialogOnButtonClickedEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnOkButtonListener");
        }
    }

    @Override
    public void onDetach() {
        //リスナーの削除
        super.onDetach();
        mListener = null;
    }

    // ボタンがクリックされたイベント用のインターフェイス
    // このDialogFragmentを使うには、このインターフェイスを実装する必要がある
    public interface DialogOnButtonClickedEventListener {
        // OK ボタンが押された時イベント
        // isCanceled = True ならキャンセルボタンが押された
        void dialogOnButtonClicked(boolean isCanceled);
    }



}
