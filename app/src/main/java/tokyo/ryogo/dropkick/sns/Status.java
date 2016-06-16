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

/**
 *  マイクロブログで言うところの「ステータス」or「post」を表すクラス
 */

// SNSに書き込む内容を表現するクラス
public class Status {

    private final String Body;
    private final String[] HashTag;

    public Status(String body, String[] hashTag){
        Body = body;
        HashTag = hashTag;
    }

    public String getBody() {
        return Body;
    }

    public String[] getHashTag() {
        return HashTag;
    }

}
