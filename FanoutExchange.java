package com.amqp.exchanges.all;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Unconditional message broadcast.
 */
public class FanoutExchange {

    public static void declareExchange() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Declare my-fanout-exchange
        channel.exchangeDeclare("my-fanout-exchange", BuiltinExchangeType.FANOUT, true);
        channel.close();
    }

    public static void declareQueues() throws IOException, TimeoutException {
        //Create a channel - do not share the Channel instance
        Channel channel = ConnectionManager.getConnection().createChannel();

        //Create the Queues
        channel.queueDeclare("Mobile", true, false, false, null);
        channel.queueDeclare("AC", true, false, false, null);
        channel.queueDeclare("Light", true, false, false, null);

        channel.close();
    }

    public static void declareBindings() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        //Create bindings - (queue, exchange, routingKey) - routingKey != null
        channel.queueBind("Mobile", "my-fanout-exchange", "");
        channel.queueBind("AC", "my-fanout-exchange", "");
        channel.queueBind("Light", "my-fanout-exchange", "");
        channel.close();
    }

    public static void subscribeMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        channel.basicConsume("Light", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("Light: " + new String(message.getBody()));
        }), consumerTag -> {
            System.out.println(consumerTag);
        });

        channel.basicConsume("AC", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("AC: " + new String(message.getBody()));
        }), consumerTag -> {
            System.out.println(consumerTag);
        });

        channel.basicConsume("Mobile", true, ((consumerTag, message) -> {
            System.out.println(consumerTag);
            System.out.println("Mobile: " + new String(message.getBody()));
        }), consumerTag -> {
            System.out.println(consumerTag);
        });
    }

    public static void publishMessage() throws IOException, TimeoutException {
        Channel channel = ConnectionManager.getConnection().createChannel();
        String message = "Main Power is ON";
        channel.basicPublish("my-fanout-exchange", "", null, message.getBytes());
        channel.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        FanoutExchange.declareQueues();
        FanoutExchange.declareExchange();
        FanoutExchange.declareBindings();

        //Threads created to publish-subscribe asynchronously
        Thread subscribe = new Thread() {
            @Override
            public void run() {
                try {
                    FanoutExchange.subscribeMessage();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread publish = new Thread() {
            @Override
            public void run() {
                try {
                    FanoutExchange.publishMessage();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };

        subscribe.start();
        publish.start();
    }
}