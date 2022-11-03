package de.turing85;

import java.time.Duration;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;

import io.smallrye.common.annotation.Identifier;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.throttling.ThrottlingExceptionRoutePolicy;
import org.springframework.transaction.PlatformTransactionManager;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.*;

@ApplicationScoped
public class JmsReceiveRoute extends RouteBuilder {
  private final ConnectionFactory connectionFactory;
  private final PlatformTransactionManager globalPlatformTransactionManager;

  public JmsReceiveRoute(
      @SuppressWarnings("CdiInjectionPointsInspection")
      @Identifier("receiver")
      ConnectionFactory connectionFactory,

      @Identifier(TransactionManagerConfig.GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME)
      PlatformTransactionManager globalPlatformTransactionManager) {
    this.connectionFactory = connectionFactory;
    this.globalPlatformTransactionManager = globalPlatformTransactionManager;
  }

  @Override
  public void configure() {
    // @formatter:off
    from(jms("queue:numbers")
        .connectionFactory(connectionFactory)
        .clientId("camel-receiver")
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
