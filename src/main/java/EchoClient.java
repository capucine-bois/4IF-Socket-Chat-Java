import java.io.*;
import java.net.*;

public class EchoClient {

 
  /**
  *  main method
  *  accepts a connection, receives a message from client then sends an echo to the client
  **/
    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
            System.exit(1);
        }

        try {
            Socket echoSocket = new Socket(args[0],1234); // on crée le socket avec l'hote et le port
            BufferedReader socIn = new BufferedReader(
                new InputStreamReader(echoSocket.getInputStream()));   // socIn = ce qui vient du serveur 
            PrintStream socOut= new PrintStream(echoSocket.getOutputStream()); //socOut = ce qui va vers le serveur
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); //pour la lecture clavier

            //création client
            Client client = new Client(echoSocket,socOut, stdIn, socIn);
            client.init();

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to:"+ "localhost");
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


