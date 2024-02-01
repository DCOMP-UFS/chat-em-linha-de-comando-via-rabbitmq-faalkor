package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;



public class Chat {
    public static String dest;
    public static byte[] buffer;

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.224.61.130"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("password"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    //login usuario
    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    String user = sc.nextLine().trim();
    
    
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(user, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
        
        ChatProto.Messege msg = ChatProto.Messege.parseFrom(buffer);
        
        // Extraindo dados da mensagem
        String reciever = msg.getReciever();
        String sender = msg.getSender();
        String date = msg.getDate();
        String hour = msg.getHour();
        String group = msg.getGroup();
        
        ChatProto.Content content = msg.getContent();
        String type = content.getType();
        String corp = content.getCorp();
        String name = content.getName();
        
        // Extraindo conteudo
        //String message = new String(body, "UTF-8") ;
        System.out.print("\n(" + date + ") " + sender + " diz: " + corp);

        System.out.print("@" + dest + ">> ");
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
        ChatProto.Messege.Builder bMsg = ChatProto.Messege.newBuilder();
        ChatProto.Content.Builder content = ChatProto.Content.newBuilder();
        
        
        Date now = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm");
        TimeZone timeZone = TimeZone.getTimeZone("America/Sao_Paulo");
        date_format.setTimeZone(timeZone);
        
        content.setType("");
        content.setCorp(messege);
        content.setName("");
        
        bMsg.setReciever(dest);
        bMsg.setSender(user);
        bMsg.setDate(date_format.format(now));
        bMsg.setHour("");
        bMsg.setGroup("");
        bMsg.setContent(content);

        ChatProto.Messege msg = bMsg.build();
        buffer = msg.toByteArray();
      
        channel.basicPublish("", dest, null, buffer);
        
      }
    }
    
      sc.close();
      channel.queueDelete(user);
      channel.close();
      connection.close();
  }
  
}
  