/*
 * Copyright 2016 linkID Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.qos.logback.core.rolling.util;

import com.jayway.jsonpath.JsonPath;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

public class IdentifierUtil {

    private static String ECS_CONTAINER_META_URI;

    static  {
        try {
            ECS_CONTAINER_META_URI = System.getenv("ECS_CONTAINER_METADATA_URI");
        } catch (Exception e) {
            ECS_CONTAINER_META_URI = null;
        }
    }

    @NotNull
    public static String getIdentifier() {

        String identifier;

        // try get ECS Metadata
        if (ECS_CONTAINER_META_URI != null) {
            identifier = getIdentifierFromEcsMetadata(ECS_CONTAINER_META_URI);
            return identifier;
        } else {
            // ダメならUUIDで自動生成
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Docker ContainerId を Identifierとして返す。もし取得できなかった場合、UUIDでGenerateする
     * @param uri ECSメタデータ取得のためのURI。環境変数に埋め込まれる
     * @return アプリケーションログが出力されたコンテナを一意に特定するためのID
     */
    private static String getIdentifierFromEcsMetadata(String uri) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(uri).build();
        String ecsContainerMetadata;
        try (Response response = client.newCall(request).execute()) {
            ecsContainerMetadata = response.body().string();
            return JsonPath.read(ecsContainerMetadata, "$.DockerId");
        } catch (IOException e) {
            // remove snake case
            return UUID.randomUUID().toString().replaceAll("-", "");
        }
    }

}
