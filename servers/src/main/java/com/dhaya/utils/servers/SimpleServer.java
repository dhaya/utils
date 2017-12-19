package com.dhaya.utils.servers;


import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.util.concurrent.Executor;

// 250K qps using:  wrk -t12 -c400 -d30s http://localhost:8080
public class SimpleServer {
    public static void main(String[] args) throws Exception {
        SimpleServer ss = new SimpleServer();
        ss.run();
    }

    private void run() throws Exception {
        Server server = new Server();

        ServerConnector connector = getConnector(server);
        server.addConnector(connector);
        server.setStopAtShutdown(true);
        server.setStopTimeout(5000);

        server.setHandler(new TestHandler());

        server.start();
        server.join();
    }

    private ServerConnector getConnector(Server server) {
        // Empirically seem to get throughput when the number of threads
        // is 2 * number of hyperthreaded cores.
        Executor executor = ExecutorFactory.newForkJoinPoolExecutor(16);
        ServerConnector connector = new ServerConnector(server, executor, null,null,-1,-1,new HttpConnectionFactory());
        connector.setPort(8080);
        connector.setAcceptQueueSize(100);
        connector.setReuseAddress(true);
        connector.setSelectorPriorityDelta(0);
        connector.setSoLingerTime(-1);
        connector.setIdleTimeout(30000L);
        connector.setStopTimeout(30000L);
        return connector;
    }


}
