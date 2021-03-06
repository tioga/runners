package org.tiogasolutions.runners.grizzly;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class GrizzlyServer {

  private static final Logger log = LoggerFactory.getLogger(GrizzlyServer.class);

  protected HttpServer httpServer;

  protected final ResourceConfig resourceConfig;
  protected final GrizzlyServerConfig serverConfig;

  public GrizzlyServer(GrizzlyServerConfig serverConfig, Application application) {
    this.serverConfig = serverConfig;
    this.resourceConfig = ResourceConfig.forApplication(application);
  }

  /**
   * The Jersey specific implementation of the JAX-RS application provided at
   * instantiation. If a ResourceConfig was specified at instantiation, then
   * that instance will be returned. If an instanceof Application was specified
   * at construction, then it is wrapped in an instance of ResourceConfigAdapter.
   * @return the property's value.
   */
  public ResourceConfig getResourceConfig() {
    return resourceConfig;
  }

  public HttpServer getHttpServer() {
    return httpServer;
  }

  public GrizzlyServerConfig getServerConfig() {
    return serverConfig;
  }

  /**
   * Convenience method for getConfig().getBaseUri();
   * @return the property's value.
   */
  public URI getBaseUri() {
    return serverConfig.getBaseUri();
  }

  /**
   * The server's current configuration.
   * @return the property's value.
   */
  public GrizzlyServerConfig getConfig() {
    return serverConfig;
  }

  /** Starts the server. */
  public void start() {
    try {
      // If it's running, shut it down.
      ShutdownUtils.shutdownRemote(serverConfig);

      // Create a new instance of our server.
      httpServer = GrizzlyHttpServerFactory.createHttpServer(serverConfig.getBaseUri(), resourceConfig);

      log.info("Application started at {}", getBaseUri());
      log.info("WADL available at {}application.wadl", getBaseUri());

      // Start our own shutdown handler.
      createShutdownHandler().start(httpServer);

      if (serverConfig.isToOpenBrowser()) {
        log.info("Opening web browser to {}", getBaseUri());
        URI baseUri = getBaseUri();
        java.awt.Desktop.getDesktop().browse(baseUri);
      }

      Thread.currentThread().join();

    } catch (Throwable e) {
      log.error("Exception starting server", e);
      e.printStackTrace();
    }
  }

  protected ShutdownHandler createShutdownHandler() {
    return new ShutdownHandler(serverConfig);
  }

  /** Shuts down *this* currently running Grizzly server. */
  public void shutdown() {
    if (httpServer != null) {
      httpServer.shutdown(30, TimeUnit.SECONDS);
    }
  }

  public void register(Class type) {
    resourceConfig.register(type);
  }

  public void packages(String...packages) {
    resourceConfig.packages(packages);
  }
}
