/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

import org.apache.flink.api.common.functions.RichMapFunction;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;


class SampleSyncFunction extends RichMapFunction<String, String> {

    private final String postRequestUrl;

    SampleSyncFunction(String postRequestURL)
    {
        this.postRequestUrl = postRequestURL;
    }

    @Override
    public String map(String value) throws Exception {

        // Define client
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500);
        AsyncHttpClient client = Dsl.asyncHttpClient(clientBuilder);

        // prepare body
        String payloadJson = "{\"name\": \"" + value.replaceAll("\\s+", "") + "\"}";

        // get response and return
        Response response = client.preparePost(postRequestUrl).setBody(payloadJson).execute().toCompletableFuture().get();
        client.close();
        return response.getResponseBody();
    }

}