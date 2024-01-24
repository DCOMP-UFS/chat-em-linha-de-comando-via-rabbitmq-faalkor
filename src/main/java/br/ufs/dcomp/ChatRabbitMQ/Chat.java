package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Scanner;

public class Chat {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("ip-da-instancia-da-aws"); // Alterar
    factory.setUsername("usuário-do-rabbitmq-server"); // Alterar
    factory.setPassword("senha-do-rabbitmq-server"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    String QUEUE_NAME = "minha-fila";
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {

        String message = new String(body, "UTF-8");
        System.out.println(message);

      }
    };
                      //(queue-name, autoAck, consumer);    
    channel.basicConsume(QUEUE_NAME, true,    consumer);
    menu();
  }
  
  
  public static void menu() {
    try {
        Scanner sc = new Scanner(System.in);
        System.out.print("User: ");
        String user = sc.nextLine();
        login(user);
        clearConsole();
        while (true){
          System.out.print(">> ");
          String command = sc.nextLine();
          if (command.startsWith("@")){
            //chavear chat
          }
          
          if(command.equalsIgnoreCase("exit")) break;           
        }
        sc.close();
        
    } 
    catch (Exception e) {
      System.out.println(e);
    }
  }
  
  
  public final static void clearConsole() {
    try {
      final String os = System.getProperty("os.name");
      if (os.contains("Windows")) {
          new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      }
      else {
          Runtime.getRuntime().exec("clear");
      }
    }
    catch (final Exception e) {
      System.out.println(e);
    }
  }
  
  
  public static void login(String user) {
    try {
      // Criar fila do usuario no RabbitMQ 
    } catch (Exception e) {
        System.out.println(e);
    }
  }
}