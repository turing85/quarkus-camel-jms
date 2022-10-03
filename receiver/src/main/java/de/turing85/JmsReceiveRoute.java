package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.sql;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import java.time.Duration;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.throttling.ThrottlingExceptionRoutePolicy;
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
    final ThrottlingExceptionRoutePolicy policy = new ThrottlingExceptionRoutePolicy(
        1,
        Duration.ofSeconds(30).toMillis(),
        Duration.ofSeconds(10).toMillis(),
        List.of(Throwable.class));
    // @formatter:off
    from(
        jms("queue:numbers")
            .clientId("camel-receiver")
            .advanced()
                .transactionManager(globalPlatformTransactionManager))
        .routeId("message-receiver")
        .routePolicy(policy)
        .log(policy.dumpState())
        .log(LoggingLevel.INFO, "Received: ${body}")
            .doTry()
                .to(sql("INSERT INTO data(data) VALUES(:#${body});")
                    .dataSource(dataSource))
                .log(LoggingLevel.INFO, "Inserted: ${body}")
            .doCatch(Exception.class)
                .markRollbackOnly()
            .end();
    // @formatter:on
  }
}
