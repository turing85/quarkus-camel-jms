package de.turing85;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jdbc;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class JmsRoute extends RouteBuilder {

  private final ConnectionFactory xaConnectionFactory;

  public JmsRoute(ConnectionFactory xaConnectionFactory) {
    this.xaConnectionFactory = xaConnectionFactory;
  }

  @Override
  public void configure() {
    from(
        jms("queue:out::in")
            .connectionFactory(xaConnectionFactory)
            .clientId("camel-consumer")
            .subscriptionName("camel-consumer-sub")
            .transacted(true)
            .acknowledgementModeName("CLIENT_ACKNOWLEDGE")
            .advanced()
                .lazyCreateTransactionManager(false))
        .transacted("PROPAGATION_REQUIRED")
        .log(LoggingLevel.INFO, "Body: ${body}")
        .setBody(simple("INSERT INTO data(data) VALUES('${body}');"))
        .to(jdbc("camel").transacted(true).resetAutoCommit(false))
        .log(LoggingLevel.INFO, "Inserted: ${body}");
  }
}
