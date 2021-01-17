package com.eco.paas.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootApplication
@ComponentScan("com.eco.paas.*")
public class PaasRabbitmqApplication implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(PaasRabbitmqApplication.class, args);

    }

    @Autowired
    MQProducer producer;
    @Autowired
    RabbitMQConfig config;

    public static String genUUID(){
        String uuid  =  UUID.randomUUID().toString().replaceAll("-","");
//        File f = new File("/tmp/123.txt");
//        try {
//            BufferedWriter bw  = new BufferedWriter(new FileWriter(f,true));
//            bw.write(uuid);
//            bw.newLine();
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return uuid;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread sender  = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        String msgId = genUUID();
                        producer.sendMsg(config.exchangeName,config.routeKey,
                                "This is Msg ==> " + new Date(),msgId);
                        r.run();
                    }
                });
                sender.setDaemon(true);
                return sender;
            }
        };
        executor.scheduleAtFixedRate(factory.newThread(new Thread()),
                0, 5, TimeUnit.MILLISECONDS);

    }
}


