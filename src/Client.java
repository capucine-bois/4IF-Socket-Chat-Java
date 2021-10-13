import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    private Socket echoSocket = null;
    private PrintStream socOut = null;
    private BufferedReader stdIn = null;
    private BufferedReader socIn = null;
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
            while (running) {
                try {
                    System.out.println(socIn.readLine());
                    String message = socIn.readLine();
                    if(message.substring(0,11).equals("retour_menu")){
                        i=1;
                        message=message.substring(13,message.length());
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
                String action = choix();
                if(action.equals("1")) {
                    System.out.println("Vous voulez creer un groupe, saisissez son nom : ");
                    nomGroupe = stdIn.readLine(); //on écrit une ligne au clavier
                    socOut.println("1" + nomGroupe);
                }else if(action.equals("2")) {
                    System.out.println("Vous voulez rejoindre un groupe, saisissez son numéro : ");
                    socOut.println("2"); // demander au serveur d'afficher le catalogue des groupes
                    String numeroGroupeChoisi = stdIn.readLine(); //on écrit une ligne au clavier
                    socOut.println("3" + numeroGroupeChoisi);
                }
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
        System.out.println("1 : creer un groupe");
        System.out.println("2 : rejoindre un groupe");
        System.out.println("--------------------------");
        String action = stdIn.readLine();
        return action;
    }

}
