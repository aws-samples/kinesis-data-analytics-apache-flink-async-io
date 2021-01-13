/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.asynchttpclient.*;

import java.util.Collections;
import java.util.concurrent.*;
import java.util.function.Supplier;


class SampleAsyncFunction extends RichAsyncFunction<String, String> {

    private static final long serialVersionUID = 2098635244857937717L;
    private transient AsyncHttpClient client;
    private final String postRequestUrl;

    SampleAsyncFunction(String url)
    {
        this.postRequestUrl = url;
    }

    /**
     * Instantiate the connection to an async client here to use within asyncInvoke
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500);
        client = Dsl.asyncHttpClient(clientBuilder);

    }

    /**
     * Close the connection to any async clients instantiated for the asyncInvoke
     */
    @Override
    public void close() throws Exception {
        super.close();
        client.close();
    }

    /**
     * 1. This example uses an AsyncHttpClient to call API Gateway -> Lambda to return a result to the function.
     * Replace the postRequestURL with whatever your api / database / key value store calls for.
     *
     * 2. Ensure that the asyncInvoke completes a completable future object to ensure requests are made
     * concurrently and no calls are blocking.
     *
     * 3. Handle errors properly either using a sideoutput for exceptions or returning a default value.
     */
    @Override
    public void asyncInvoke(final String input, final ResultFuture<String> resultFuture) {
        String payloadJson = "{\"name\": \"" + input.replaceAll("\\s+", "") + "\"}";
        Future<Response> future = client.preparePost(postRequestUrl).setBody(payloadJson).execute();
        CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                try {
                    Response response = future.get();
                    return response.getResponseBody();

                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        }).thenAccept((String body) -> {
            resultFuture.complete(Collections.singleton(body));
        });
    }

}