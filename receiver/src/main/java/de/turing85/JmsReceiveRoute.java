package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jdbc;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;

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

  @Override
  public void configure() {
    // @formatter:off
    from(
        jms("queue:numbers")
            .clientId("camel-receiver"))
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
