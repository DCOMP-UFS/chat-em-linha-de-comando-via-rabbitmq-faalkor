package br.ufs.dcomp.ChatRabbitMQ;
import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class Chat {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("54.196.154.240"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("password"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    //login usuario
    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    String user = sc.nextLine().trim();
    String dest = "";
    
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
      dest = sc.nextLine().trim();
      if (dest.startsWith("@")) {
        dest = dest.substring(1);
        break;
      }
      else {
        System.out.println("Informe o destinatário. Ex: @joao");
      }
    }
    
    
    while (true) {
      System.out.print("@" + dest + ">> ");
      String messege = sc.nextLine().trim();
      if (messege.equalsIgnoreCase("exit")) break;
      if (messege.startsWith("@")){
        dest = messege.substring(1);
      } 
      else {
        Date now = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        TimeZone timeZone = TimeZone.getTimeZone("America/Sao_Paulo");
        date_format.setTimeZone(timeZone);
        messege = "(" + date_format.format(now) + ") " + user + " diz: " + messege;
        channel.basicPublish("", dest, null,  messege.getBytes("UTF-8"));
      }
    }
    
      sc.close();
      channel.queueDelete(user);
      channel.close();
      connection.close();
  }
  
}
  