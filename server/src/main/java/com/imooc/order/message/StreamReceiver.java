package com.imooc.order.message;

import com.imooc.order.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(StreamClient.class)
@Slf4j
public class StreamReceiver {

    @StreamListener(StreamClient.INPUT)
    @SendTo(StreamClient.OUTPUT)
    public Object processInput(Object message){
        log.info("StreamReceiver:{}", message);
        return message;
    }

    @StreamListener(StreamClient.OUTPUT)
    public void processOutput(OrderDTO message){
        log.info("StreamReceiver:{}", message);
    }

//    @StreamListener(value = StreamClient.INPUT)
//    public void process(OrderDTO message){
//        log.info("StreamReceiver:{}", message);
//    }
}
