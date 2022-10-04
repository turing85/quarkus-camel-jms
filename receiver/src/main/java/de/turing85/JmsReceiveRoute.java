package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.sql;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import java.time.Duration;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.TransactedDefinition;
import org.apache.camel.throttling.ThrottlingExceptionRoutePolicy;
import org.springframework.transaction.PlatformTransactionManager;

@ApplicationScoped
public class JmsReceiveRoute extends RouteBuilder {
  private final ConnectionFactory connectionFactory;
  private final PlatformTransactionManager globalPlatformTransactionManager;
  private final AgroalDataSource dataSource;

  public JmsReceiveRoute(
      @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory connectionFactory,

      @Named(TransactionManagerConfig.GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME)
      PlatformTransactionManager globalPlatformTransactionManager,

      @SuppressWarnings("CdiInjectionPointsInspection")
      @DataSource("data")
      AgroalDataSource dataSource) {
    this.connectionFactory = connectionFactory;
    this.globalPlatformTransactionManager = globalPlatformTransactionManager;
    this.dataSource = dataSource;
  }

  @Override
  public void configure() {
    errorHandler(jtaTransactionErrorHandler());
    final ThrottlingExceptionRoutePolicy policy = new ThrottlingExceptionRoutePolicy(
        1,
        Duration.ofSeconds(1).toMillis(),
        Duration.ofSeconds(10).toMillis(),
        List.of(Throwable.class));
    // @formatter:off
    policy.start();
    from(jms("queue:numbers")
            .connectionFactory(connectionFactory)
            .clientId("camel-receiver")
            .advanced()
                .transactionManager(globalPlatformTransactionManager))
        .routePolicy(policy)
        .routeId("message-receiver")
        .transacted(TransactedDefinition.PROPAGATION_REQUIRED)
        .log("policy is started: %b".formatted(policy.isStarted()))
        .log(policy.dumpState())
        .log(LoggingLevel.INFO, "Received: ${body}")
        .to(sql("INSERT INTO data(data) VALUES(:#${body});")
            .dataSource(dataSource))
        .throwException(new MyException())
        .log(LoggingLevel.INFO, "Inserted: ${body}");
    // @formatter:on
  }

  static class MyException extends Exception {
    MyException() {
      super("foo");
    }
  }
}
