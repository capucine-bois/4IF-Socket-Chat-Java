import java.io.*;
import java.net.*;

public class EchoClient {

 
  /**
  *  main method
  *  accepts a connection, receives a message from client then sends an echo to the client
  **/
    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
          System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
          System.exit(1);
        }

        try {
            Socket echoSocket = new Socket(args[0],new Integer(args[1]).intValue()); // on crée le socket avec l'hote et le port
            BufferedReader socIn = new BufferedReader(
                new InputStreamReader(echoSocket.getInputStream()));   // socIn = ce qui vient du serveur 
            PrintStream socOut= new PrintStream(echoSocket.getOutputStream()); //socOut = ce qui va vers le serveur
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); //pour la lecture clavier
            //création client
            Client client = new Client(echoSocket,socOut, stdIn, socIn);
            boolean enCours = true;
            client.init(enCours);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to:"+ args[0]);
            System.exit(1);
        }
    }
}


