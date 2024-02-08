package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import com.google.protobuf.*;
import java.nio.charset.StandardCharsets;


public class Chat {
    public static String input;
    public static String dest = "";
    public static String destFormated;
    public static String messege;
    public static String user;
    public static byte[] buffer;
    public static boolean flagGroup;
    

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("18.208.211.193"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("password"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel(); //Criação de um novo canal
    
    //login usuario
    Scanner sc = new Scanner(System.in);
    System.out.print("User: ");
    user = sc.nextLine().trim();
    
    
                      //(queue-name, durable, exclusive, auto-delete, params); 
    channel.queueDeclare(user, false,   false,     false,       null);
    
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
        
        ChatProto.Messege msg = ChatProto.Messege.parseFrom(body);
        
        // Extraindo dados da mensagem
        String reciever = msg.getReciever();
        String sender = msg.getSender();
        String date = msg.getDate();
        String hour = msg.getHour();
        String group = msg.getGroup();
        
        ChatProto.Content content = msg.getContent();
        String type = content.getType();
        byte[] corp = content.getCorp().toByteArray();
        String name = content.getName();
        
        // Converter pra string -- depois filtar por tipo?
        String txt = new String(corp, StandardCharsets.UTF_8);
        
        
        // verificar se a msg foi em grupo
        if (!group.equals("") && sender.equals(user)) return;
        
        System.out.print("\n(" + date +" às " + hour + ") " + sender + group + " diz: " + txt);

        System.out.print("\n" + dest + ">> ");
      }
    };
                      //(queue-name, autoAck, consumer);    
    channel.basicConsume(user, true, consumer);
    
    // welcome
    //welcome();
    
    // exibir prompt
    while (true) {
      System.out.print(dest + ">> ");
      input = sc.nextLine().trim();
      
      // Direct Messege
      if (input.startsWith("@")) {
        dest = input;
        destFormated = destFormat(dest);
        flagGroup = false;
        continue;
        //break;
      }
      
      else if (input.startsWith("#")) {
        dest = input;
        destFormated = destFormat(dest);
        flagGroup = true;
        continue;
        //break;
      }
      
      // Group
      else if (input.startsWith("!")) {
        String[] promptArr = input.split(" ", -1);
        
        switch (promptArr[0]) {
            case "!addGroup":
                //createGroup(promptArr[1]);
                channel.exchangeDeclare(promptArr[1], "fanout");
                channel.queueBind(user, promptArr[1], "");
                break;
                
            case "!addUser":
                channel.queueBind(promptArr[1], promptArr[2], "");
                break;
                
            case "!delFromGroup":
                channel.queueUnbind(promptArr[1], promptArr[2], "");
                break;
                
            case "!removeGroup":
                channel.exchangeDelete(promptArr[1]);
                break;
        }
      }
      
      else if (input.equalsIgnoreCase("exit")) break;
      
      
      else if (input.equals("")) System.out.println("Favor fornercer o input");
      
      else if (!input.equals("") && dest.equals("")) System.out.println("Favor informar o destinatário");
      
      else{
        
          try {
              serializeMsg();
          
              // DM Msg
              if (flagGroup == false) channel.basicPublish("", destFormated, null, buffer);
                  
              // Group Msg
              else channel.basicPublish(destFormated, "", null, buffer);
              
          } 
          catch (com.rabbitmq.client.AlreadyClosedException e) {
                System.out.println("Usuario invalido\nFAVOR LOGAR NOVAMENTE");
                break;
                
          }
          catch (Exception e) {
                System.out.println(e);
                System.out.println("\n***Usuario invalido\nFAVOR LOGAR NOVAMENTE\n***");
                break;
          }
          
      }
    }
    

      sc.close();
      channel.queueDelete(user);
      channel.close();
      connection.close();
  }
  
  
  public static void welcome() {
     System.out.println("Welcome!");
    /*
    System.out.println("    ____ _           _     ____ \n" +            
                       "/ ___| |__   __ _| |_  | __ )  _____  __ \n" +
                       "| |   | '_ \ / _` | __| |  _ \ / _ \ \/ / \n" +
                       "| |___| | | | (_| | |_  | |_) | (_) >  < \n"  + 
                        "\____|_| |_|\__,_|\__| |____/ \___/_/\_\ \n"); 
                        */
                        
  }
  
  
  public static void createGroup(String groupName) {
      //System.out.println(channel.exchangeDeclare(groupName, "fanout"));
  }
  
  
  public static String destFormat(String dest) {
      return dest.substring(1);
  }
  
  
  public static void serializeMsg() {
        try {
            
            ChatProto.Messege.Builder bMsg = ChatProto.Messege.newBuilder();
            ChatProto.Content.Builder content = ChatProto.Content.newBuilder();
           
            Date now = new Date();
            SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
            TimeZone timeZone = TimeZone.getTimeZone("America/Sao_Paulo");
            
            date_format.setTimeZone(timeZone);
            hour.setTimeZone(timeZone);
            
            content.setType("");
            content.setCorp(ByteString.copyFrom(input.getBytes("UTF-8")));
            content.setName("");
            
            bMsg.setReciever(destFormated);
            bMsg.setSender(user);
            bMsg.setDate(date_format.format(now));
            bMsg.setHour(hour.format(now));
            
            // Verify DM msg or Group msg
            if (flagGroup == true) bMsg.setGroup(dest);
            else bMsg.setGroup("");
            
            bMsg.setContent(content);
    
            ChatProto.Messege msg = bMsg.build();
            buffer = msg.toByteArray();
          
        } catch(Exception e) {
            System.out.println(e);
        }
  }
  
}
  