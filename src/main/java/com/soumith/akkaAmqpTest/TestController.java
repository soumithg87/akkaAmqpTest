package com.soumith.akkaAmqpTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

@RestController
@RequestMapping("/test")
public class MessageController {
	
	@Autowired
	private AmqpConnectionProvider connectionProvider;
	
	@Autowired
	private QueueDeclaration queueDeclaration;
	
	@Autowired
	@Qualifier("materializer")
	private ActorMaterializer materializer;
	
	
	@RequestMapping(value="/message", method=RequestMethod.POST)
	public void testMessage() throws IOException {
		System.out.println("********************* Got the call ...");
		
		  // #create-sink - producer
	    final Sink<ByteString, CompletionStage<Done>> amqpSink =
	        AmqpSink.createSimple(
	            AmqpSinkSettings.create(connectionProvider)
	                .withRoutingKey(AkkaConstants.queueName)
	                .withDeclaration(queueDeclaration));
	  
	    
	    String filePath = "C:\\work\\AKKA\\amqp-master\\files\\dummy.txt";
	    Path path = Paths.get(filePath);
	    
	    // List containing 78198 individual message
	    List<String> contents = Files.readAllLines(path);
	    System.out.println("********** file reading done ....");
	    int times = 100;
	    
	    // Send 78198*times message to Queue [From console i can see 400000 number of messages being sent]
	    for(int i=0;i<times;i++) {
	    	Source.from(contents).map(ByteString::fromString).runWith(amqpSink, materializer);
	    }
	    System.out.println("************* sending to queue is done");

	    
	}
}