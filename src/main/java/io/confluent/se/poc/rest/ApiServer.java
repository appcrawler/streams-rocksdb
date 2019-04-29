package io.confluent.se.poc.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

import org.glassfish.jersey.servlet.ServletContainer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
public class ApiServer {
  Server server; 
  public void ApiServer() {
  }

  public void start() {

    System.out.println("starting jetty server...");
    server = new Server(8080);
    ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);

    servletContextHandler.setContextPath("/");
    server.setHandler(servletContextHandler);

    ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
    servletHolder.setInitOrder(0);
    servletHolder.setInitParameter(
                "jersey.config.server.provider.packages",
                "io.confluent.se.poc.rest"
        );


    try {
      server.start();
      System.out.println("started jetty server...");
      //server.dumpStdErr();
      //server.join();
    } 
    catch (Exception e) {           
      e.printStackTrace();
    }  
  }
}
