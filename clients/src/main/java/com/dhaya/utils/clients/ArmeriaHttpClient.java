package com.dhaya.utils.clients;

import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.http.HttpClient;
import com.linecorp.armeria.common.http.AggregatedHttpMessage;

/**
 * Created by dhaya on 4/25/17.
 */
public class ArmeriaHttpClient {
    public static void main(String[] args) {
        HttpClient httpClient = Clients.newClient("none+http://localhost:8080/", HttpClient.class);

        AggregatedHttpMessage textResponse = httpClient.get("/").aggregate().join();
        System.out.println(textResponse.content().toStringUtf8());
    }
}
