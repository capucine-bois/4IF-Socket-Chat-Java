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
                    if(message.substring(0,8).equals("users_co")){
                        Scanner sc = new Scanner(System.in);
                        message=message.substring(10,message.length());
                        String lecture="";
                        while(!lecture.equals("end")){
                            listePseudoUsersCo.add(sc.nextLine());
                        }
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

        });
        t.start();


        while (ok) {
            if(i==0){
                System.out.println("Saisissez votre identifiant");
                pseudo=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                i++;
            } else if(i==1) {
                System.out.println("Voici les clients connectés :");
                socOut.println("1");
                for(int j=0; j<listePseudoUsersCo.size(); j++){
                    System.out.println(listePseudoUsersCo.get(j));
                }
                i++;
            }else{
                line=stdIn.readLine(); //on écrit une ligne au clavier
                if (line.equals(".")) {
                    //socOut.println("getUser");

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
}
