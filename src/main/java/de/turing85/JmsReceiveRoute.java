package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.sql;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.TransactedDefinition;
import org.springframework.transaction.PlatformTransactionManager;

@ApplicationScoped
public class JmsReceiveRoute extends RouteBuilder {
  private final PlatformTransactionManager globalPlatformTransactionManager;
  private final AgroalDataSource dataSource;

  public JmsReceiveRoute(
      @Named(TransactionManagerConfig.GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME)
      PlatformTransactionManager globalPlatformTransactionManager,

      @SuppressWarnings("CdiInjectionPointsInspection")
      @DataSource("data")
      AgroalDataSource dataSource) {
    this.globalPlatformTransactionManager = globalPlatformTransactionManager;
    this.dataSource = dataSource;
  }

  @Override
  public void configure() {
    from(
        jms("queue:out")
            .subscriptionShared(true)
            .durableSubscriptionName("in")
            .transacted(true)
            .advanced()
                .transactionManager(globalPlatformTransactionManager))
        .routeId("message-receiver")
        .transacted(TransactedDefinition.PROPAGATION_REQUIRED)
        .log(LoggingLevel.INFO, "Received: ${body}")
        .circuitBreaker()
            .faultToleranceConfiguration()
                .timeoutEnabled(true)
                .timeoutDuration(5000)
                .failureRatio(50)
                .successThreshold(75)
            .end()
            .to(sql("INSERT INTO data(data) VALUES(:#${body});")
                .dataSource(dataSource))
        .endCircuitBreaker()
        .log(LoggingLevel.INFO, "Inserted: ${body}");
  }
}
