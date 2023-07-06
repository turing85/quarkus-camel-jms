package de.turing85;

import java.time.Duration;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;

import io.smallrye.common.annotation.Identifier;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.throttling.ThrottlingExceptionRoutePolicy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.springframework.transaction.PlatformTransactionManager;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.*;

@ApplicationScoped
public class JmsReceiveRoute extends RouteBuilder {
  private final Logger logger;
  private final ConnectionFactory connectionFactory;
  private final PlatformTransactionManager globalPlatformTransactionManager;
  private final String clientId;
  private final String subscriptionName;

  public JmsReceiveRoute(
      Logger logger,

      @SuppressWarnings("CdiInjectionPointsInspection")
      ConnectionFactory connectionFactory,

      @Identifier(TransactionManagerConfig.GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME)
      PlatformTransactionManager globalPlatformTransactionManager,

      @ConfigProperty(name = "app.client-id") String clientId,
      @ConfigProperty(name = "app.subscription-name") String subscriptionName) {
    this.logger = logger;
    this.connectionFactory = connectionFactory;
    this.globalPlatformTransactionManager = globalPlatformTransactionManager;
    this.clientId = clientId;
    this.subscriptionName = subscriptionName;
  }

  @Override
  public void configure() {
    logger.infof("clientId = %s", clientId);
    logger.infof("subscriptionName = %s", subscriptionName);
    // @formatter:off
    from(jms("topic:numbers")
        .connectionFactory(connectionFactory)
        .clientId(clientId)
        .subscriptionShared(true)
        .subscriptionDurable(true)
        .subscriptionName(subscriptionName)
        .cacheLevelName("CACHE_NONE")
        .advanced()
        .transactionManager(globalPlatformTransactionManager))
        .routePolicy(new ThrottlingExceptionRoutePolicy(
            1,
            Duration.ofSeconds(1).toMillis(),
            Duration.ofSeconds(10).toMillis(),
            List.of(Throwable.class)))
        .routeId("message-receiver")
        .log(LoggingLevel.INFO, "Received: ${body}")
        .setHeader("number", body())
        .setBody(simple("INSERT INTO data(data) VALUES(:?number);"))
        .to(jdbc("data")
            .resetAutoCommit(false)
            .useHeadersAsParameters(true))
        .log(LoggingLevel.INFO, "Inserted: ${header.number}");
    // @formatter:on
  }
}
