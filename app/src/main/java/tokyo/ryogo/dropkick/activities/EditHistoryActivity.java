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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import tokyo.ryogo.dropkick.R;
import tokyo.ryogo.dropkick.usecases.CommonPreference;
import tokyo.ryogo.dropkick.usecases.HashTagHistory;

/**
 * ハッシュタグ履歴を編集するためのアクティビティ
 */
public class EditHistoryActivity extends AppCompatActivity {



    private HashTagHistory mHashTagHistoryToEdit;
    private ListView mListViewHistory;
    private Button mButtonSelectAll;

    public static final int EDIT_HISTORY_OK_BUTTON_CLICKED = 0;
    public static final int EDIT_HISTORY_CANCEL_BUTTON_CLICKED = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_history);

        // リターンコードを設定（戻るキーで戻った場合はこの値が返却される）
        setResult(EDIT_HISTORY_CANCEL_BUTTON_CLICKED);


        // ハッシュタグ履歴オブジェクトを作成
        mHashTagHistoryToEdit = new HashTagHistory(new CommonPreference(getApplication()));

        // ListAdapterを設定する
        // ListViewの中身を実際に決めてるのがAdapter
        // http://azunobu.hatenablog.com/entry/2015/08/19/192400
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_checked,  //Android標準レイアウトを使う
                mHashTagHistoryToEdit.toArray());

        //リストビューを取得
        mListViewHistory = (ListView) findViewById(R.id.listView);

        //実際にアダプターをセット
        mListViewHistory.setAdapter(adapter);



        //ハッシュタグ履歴がない場合は、「履歴がありません」ラベルを表示
        if(mHashTagHistoryToEdit.count() == 0) {
            findViewById(R.id.textNoHistory).setVisibility(View.VISIBLE);
        }

        // OKボタンにイベントをセット
        Button buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeHistory();
            }
        });

        // 全て選択ボタンにイベントをセット
        mButtonSelectAll = (Button) findViewById(R.id.buttonSelectAll);
        mButtonSelectAll.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selectAll();
            }
        });

    }

    private void removeHistory(){

        // 動的に追加したいのでリストを作成
        List<String> modifiedHistory = new ArrayList<>();

        // 一度でもチェックをON/OFFしたインデックスとその時の値の配列を取得
        // http://blogand.stack3.net/archives/92
        // checkedArray[0] = false
        // checkedArray[1] = false
        // checkedArray[2] = true
        // ... みたいな感じで入ってるらしい
        // この配列のlengthは元の配列の長さとは違うので注意
        SparseBooleanArray checkedArray = mListViewHistory.getCheckedItemPositions();

        // 元の配列に対してループ
        for(int i = 0; i < mHashTagHistoryToEdit.count() ; i++){
            // 元の配列で言うところのi番目がチェックされているか
            if(!checkedArray.get(i,false)){
                modifiedHistory.add(mHashTagHistoryToEdit.getAt(i)); //チェックされていなかった場合のみ、リストに追加
            }
        }



        // リストを配列に変換して、ハッシュタグ履歴オブジェクトを作成
        HashTagHistory newHistory = new HashTagHistory(modifiedHistory.toArray(new String[modifiedHistory.size()]));
        // プリファレンスに保存
        newHistory.save(new CommonPreference(getApplication()));

        // OKで戻るということで、リターンを0にセットしてみる
        setResult(EDIT_HISTORY_OK_BUTTON_CLICKED);

        //このアクティビティは終了
        finish();

    }

    //全て選択
    private void selectAll(){

        for(int i =  0; i < mListViewHistory.getChildCount() ; i++){
            mListViewHistory.setItemChecked(i,true);
        }
    }

}
