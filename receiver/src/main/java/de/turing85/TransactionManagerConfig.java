package de.turing85;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import io.smallrye.common.annotation.Identifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Dependent
public class TransactionManagerConfig {
  public static final String GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME =
      "globalPlatformTransactionManager";

  @Produces
  @ApplicationScoped
  @Identifier(GLOBAL_PLATFORM_TRANSACTION_MANAGER_NAME)
  public PlatformTransactionManager globalPlatformTransactionManager(
      UserTransaction userTransaction,
      @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager transactionManager) {
    return new JtaTransactionManager(userTransaction, transactionManager);
  }
}
