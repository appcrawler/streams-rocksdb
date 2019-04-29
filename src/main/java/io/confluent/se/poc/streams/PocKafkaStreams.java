package io.confluent.se.poc.streams;

import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import io.confluent.se.poc.rest.*;

public class PocKafkaStreams {
  static ReadOnlyKeyValueStore<String, String> kvStore;

  public static void main(String[] args) throws Exception {
    PocKafkaStreams pks = new PocKafkaStreams();
    pks.start();
  }

  public void start() throws Exception {
    ApiServer restServer = new ApiServer();
    restServer.start();
    Properties settings = new Properties (); 
    settings.put(StreamsConfig.APPLICATION_ID_CONFIG,"se-tutorial-v0.1.0"); 
    settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    //settings.put(StreamsConfig.STATE_DIR_CONFIG, "my-state-store");
    final Serde<String> stringSerde = Serdes.String(); 
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> customers = builder.stream("customers",Consumed.with(stringSerde, stringSerde));

    KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore("CustStore");

    customers.to("CUSTOMERS_STREAMKEY",Produced.with(stringSerde, stringSerde));
    KTable<String, String> materializedCustomers = builder.table("CUSTOMERS_STREAMKEY",Materialized.<String, String>as(storeSupplier)
        .withKeySerde(Serdes.String())
        .withValueSerde(Serdes.String()));

    Topology topology = builder.build();
    KafkaStreams streams = new KafkaStreams (topology, settings); 

    CompletableFuture<KafkaStreams.State> stateFuture = new CompletableFuture<>();
    streams.setStateListener((newState, oldState) -> {
      if(stateFuture.isDone()) {
        return;
      }

      if(newState == KafkaStreams.State.RUNNING || newState == KafkaStreams.State.ERROR) {
        stateFuture.complete(newState);
      }
    });

    streams.start();

    while (stateFuture.get() != KafkaStreams.State.RUNNING) {}

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      streams.close(); 
    }));

    kvStore = streams.store("CustStore", QueryableStoreTypes.keyValueStore());
  }

  public static String getRecommendations(String id) throws Exception {
    return kvStore.get(id);
  }
}
