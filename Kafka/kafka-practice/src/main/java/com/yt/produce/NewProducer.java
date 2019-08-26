package com.yt.produce;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class NewProducer {

    public static void main(String[] args) {
        Properties prop = new Properties();
        prop.put("bootstrap.servers","centos101:9092");
        //等待所有节点应答
        prop.put("ack","all");
        //消息失败最大重试次数
        prop.put("retries",0);
        // 一批消息处理大小
        prop.put("batch.size", 16384);
        // 请求延时
        prop.put("linger.ms", 1);
        // 发送缓存区内存大小
        prop.put("buffer.memory", 33554432);
        // key序列化
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // value序列化
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(prop);

        //生产50条消息
        for (int i=0;i<50;i++) {
            producer.send(new ProducerRecord<String,String>("second",Integer.valueOf(i).toString(),"helloWorld"+i));
        }
        producer.close();
    }

}