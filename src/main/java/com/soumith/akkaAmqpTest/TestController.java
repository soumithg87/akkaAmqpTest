package com.soumith.akkaAmqpTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import akka.Done;
import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.amqp.AmqpConnectionProvider;
import akka.stream.alpakka.amqp.AmqpSinkSettings;
import akka.stream.alpakka.amqp.IncomingMessage;
import akka.stream.alpakka.amqp.NamedQueueSourceSettings;
import akka.stream.alpakka.amqp.QueueDeclaration;
import akka.stream.alpakka.amqp.javadsl.AmqpSink;
import akka.stream.alpakka.amqp.javadsl.AmqpSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

@RestController("/test")
public class TestController {
	
	
	@Autowired
	private AmqpConnectionProvider connectionProvider;
	
	@Autowired
	private QueueDeclaration queueDeclaration;
	
	@Autowired
	@Qualifier("materializer")
	private ActorMaterializer materializer;

	
	@RequestMapping(value="/message", method=RequestMethod.POST)
	public void testMessage() {
		
		  // #create-sink - producer
	    final Sink<ByteString, CompletionStage<Done>> amqpSink =
	        AmqpSink.createSimple(
	            AmqpSinkSettings.create(connectionProvider)
	                .withRoutingKey(AkkaConstants.queueName)
	                .withDeclaration(queueDeclaration));
	  
	    
	    // #run-sink
	    final List<String> input = Arrays.asList("one", "two", "three", "four", "five");
	    Source.from(input).map(ByteString::fromString).runWith(amqpSink, materializer);
	    

	    // #create-source - consumer
	    final Integer bufferSize = 1;
	    final Source<IncomingMessage, NotUsed> amqpSource =
	        AmqpSource.atMostOnceSource(
	            NamedQueueSourceSettings.create(connectionProvider, AkkaConstants.queueName)
	                .withDeclaration(queueDeclaration),
	            bufferSize);
	   
	    // #run-source
	    final CompletionStage<List<IncomingMessage>> result =
	        amqpSource.take(input.size()).runWith(Sink.seq(), materializer);
	   

	    List<String> collect = new ArrayList<String>();
		try {
			collect = result
			.toCompletableFuture()
			.get(3, TimeUnit.SECONDS)
			.stream()
			.map(m -> m.bytes().utf8String())
			.collect(Collectors.toList());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    for (String s:collect) {    	
	    	System.out.println(collect);
	    }
	    
		
	}
}
