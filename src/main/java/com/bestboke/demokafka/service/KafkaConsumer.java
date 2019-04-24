package com.bestboke.demokafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = {"myTopic"}, groupId = "test")
    public void receive(String message){
        System.out.println(message);
    }


}
