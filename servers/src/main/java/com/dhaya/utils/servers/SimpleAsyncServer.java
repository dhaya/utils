package com.dhaya.utils.servers;


import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executor;

// 175K qps using:  wrk -t12 -c400 -d30s http://localhost:8080
public class SimpleAsyncServer {
    private static final Executor executor = ExecutorFactory.newForkJoinPoolExecutor(16);

    public static void main(String[] args) throws Exception {
        SimpleAsyncServer ss = new SimpleAsyncServer();
        ss.run();
    }

    private void run() throws Exception {
        Server server = new Server();

        ServerConnector connector = getConnector(server);
        server.addConnector(connector);
        server.setStopAtShutdown(true);
        server.setStopTimeout(5000);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder asyncHolder = context.addServlet(AsyncServlet.class,"/async");
        asyncHolder.setAsyncSupported(true);
        server.setHandler(context);

        server.start();
        server.join();
    }

    private ServerConnector getConnector(Server server) {
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

    private static class JettyAsyncListener implements AsyncListener {
        volatile boolean timedOut;

        public void onComplete(AsyncEvent asyncEvent) throws IOException {
        }

        public void onTimeout(AsyncEvent asyncEvent) throws IOException {
            timedOut = true;
        }

        public void onError(AsyncEvent asyncEvent) throws IOException {
        }

        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        }
    }

    public static class AsyncServlet extends HttpServlet
    {
        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
        {
            final AsyncContext ctxt = req.startAsync();
            ctxt.setTimeout(1000);
            final JettyAsyncListener listener = new JettyAsyncListener();
            ctxt.addListener(listener);
            ctxt.start(new Runnable()
            {
                public void run()
                {
                    try {
                        //Thread.sleep(5000);
                        // ctxt.getWriter() better than response.getOutputStream() in terms of performance
                        if (!listener.timedOut)
                            ctxt.getResponse().getWriter().write("Hello World!");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (!listener.timedOut)
                            ctxt.complete();
                    }
                }
            });
        }
    }
}
