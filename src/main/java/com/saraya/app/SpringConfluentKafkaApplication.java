package com.saraya.app;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@SpringBootApplication
public class SpringConfluentKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringConfluentKafkaApplication.class, args);
	}

}
@RequiredArgsConstructor
@Component
class Producer {
	private final KafkaTemplate<Long,String> template;

	Faker faker;

	@EventListener(ApplicationStartedEvent.class)
	public void generate(){
		faker= Faker.instance();
		Flux<Long> interval = Flux.interval(Duration.ofMillis(1_000));
		Flux<String> quotes = Flux.fromStream(Stream.generate(() -> faker.hobbit().quote()));
		Flux.zip(interval,quotes).map(it->template.send("hobbit",faker.random().nextLong(30L), it.getT2()))
				.blockLast();
	}
}

@Component
class Consumer{
	@KafkaListener(topics = "hobbit",groupId = "hobbit_quotes")
	public void consume(String quote){
		System.out.printf("message %s \n",quote);

	}
}