package com.dhaya.utils.servers;

import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.http.HttpRequest;
import com.linecorp.armeria.common.http.HttpResponseWriter;
import com.linecorp.armeria.common.http.HttpStatus;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.http.AbstractHttpService;

import java.util.concurrent.CompletableFuture;

import static com.linecorp.armeria.common.http.HttpSessionProtocols.HTTP;

/**
 * Created by dhaya on 4/25/17.
 */
public class ArmeriaServer {
    public static void main(String[] args) {
        ArmeriaServer as = new ArmeriaServer();
        as.run();
    }

    private void run() {

        ServerBuilder sb = new ServerBuilder();
        sb.port(8080, HTTP);

        sb.serviceAt("/", new AbstractHttpService() {
            @Override
            protected void doGet(ServiceRequestContext ctx,
                                 HttpRequest req, HttpResponseWriter res) {
                res.respond(HttpStatus.OK, MediaType.PLAIN_TEXT_UTF_8, "Hello, world!!");
            }
        });

        Server server = sb.build();
        CompletableFuture<Void> future = server.start();
        future.join();
    }
}
