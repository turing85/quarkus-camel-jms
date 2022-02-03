package de.turing85;

import io.smallrye.mutiny.Multi;
import java.time.Duration;
import java.util.Random;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {

  private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
  private static final Random RANDOM = new Random();

  @Outgoing("out")
  public Multi<String> send() {
    return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
        .map(x -> RANDOM.nextInt(1000))
        .map("Hello %d"::formatted)
        .invoke(Producer::log);
  }

  private static void log(Object object) {
    LOG.info("Sending: {}", object);
  }
}
