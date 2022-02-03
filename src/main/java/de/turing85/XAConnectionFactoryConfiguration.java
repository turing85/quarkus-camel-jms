package de.turing85;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.transaction.TransactionManager;

import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;

@Dependent
public class XAConnectionFactoryConfiguration {

  // This class should be remove if https://github.com/quarkusio/quarkus/issues/14871 resolved
  // And the ConnectionFactory could be integrated with TransactionManager
  @Produces
  @Named("xaConnectionFactory")
  public ConnectionFactory getXAConnectionFactory(TransactionManager tm, ArtemisRuntimeConfig config) {
    XAConnectionFactory cf = new ActiveMQXAConnectionFactory(
        config.url, config.username.orElse(null), config.password.orElse(null));
    return new ConnectionFactoryProxy(cf, new TransactionHelperImpl(tm));
  }
}