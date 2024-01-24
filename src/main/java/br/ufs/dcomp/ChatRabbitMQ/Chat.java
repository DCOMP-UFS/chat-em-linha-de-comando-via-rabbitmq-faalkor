package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;

public class Chat {

  public static void main(String[] argv) throws Exception {
    Scanner sc = new Scanner(System.in);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("54.226.237.127"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("password"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    //login usuario
    System.out.print("User: ");
    String user = sc.nextLine();
    
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(user, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
        
        String message = new String(body, "UTF-8");
        System.out.println(message);

      }
    };
                      //(queue-name, autoAck, consumer);    
    channel.basicConsume(user, true, consumer);
    
    
    // exibir prompt
    while (true) {
      System.out.print(">> ");
      String command = sc.nextLine();
      if (command.startsWith("@")){
        //chavear chat
      }
          
      if(command.equalsIgnoreCase("exit")) break;           
      }
      sc.close();
  }
  
  
  
}
  