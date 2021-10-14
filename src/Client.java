import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private Socket echoSocket = null;
    private PrintStream socOut = null;
    private BufferedReader stdIn = null;
    private BufferedReader socIn = null;
    private ArrayList<String> listePseudoUsersCo = new ArrayList<>();

    int i;

    Client(Socket echoS, PrintStream out, BufferedReader stdin, BufferedReader in) throws IOException {
        this.echoSocket= echoS;
        this.socOut= out;
        this.stdIn = stdin;
        this.socIn=in;
        this.i = 0;
    }

    public void init(boolean ok)  throws IOException{
        String line, pseudo, nomGroupe;
        final boolean running = ok;
        // création du thread
        Thread t = new Thread(() -> {
            int i = 0;
            while (running) {
                try {
                    String message = socIn.readLine();
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        });
        t.start();


        while (ok) {
            if (i == 0) {
                System.out.println("Saisissez votre identifiant");
                pseudo = stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                i++;
            } else  if(i==1){
                System.out.println("A qui voulez vous parler");
                line=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println("1:"+line);
                i++;
            }else{
                line=stdIn.readLine(); //on écrit une ligne au clavier
                if (line.equals(".")) {
                    System.exit(0);
                    ok = false;
                    break; // on break quand on écrit '.'
                }
                socOut.println(line); //on envoie la ligne au serveur
            }
        }
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }
    public String choix() throws IOException{
        System.out.println("Choisissez une action : ");
        System.out.println("1 : parler a quelqu'un");
        System.out.println("--------------------------");
        String action = stdIn.readLine();
        return action;
    }

}
