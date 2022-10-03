package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JmsSendRoute extends RouteBuilder {
  protected static final String COUNTER_HEADER_NAME = "counter";

  private final Duration period;
  private final AtomicInteger counter;

  public JmsSendRoute(
      @ConfigProperty(name = "camel.send-route.period") Duration period) {
    this.period = period;
    this.counter = new AtomicInteger();
  }

  @Override
  public void configure() {
    from(
        timer("sender-timer")
            .fixedRate(true)
            .period(period.toMillis()))
        .routeId("message-sender")
        .setBody(exchange -> counter.getAndIncrement())
        .setHeader(COUNTER_HEADER_NAME, body().getExpression())
        .log(LoggingLevel.INFO, "Body: ${body}")
        .to(jms("queue:numbers")
            .clientId("camel-sender"))
        .log("Sent: ${body}");
  }
}
