import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.io.FileWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    public void init(boolean ok) throws IOException, InterruptedException {
        String line, pseudo, nomGroupe;
        final boolean running = ok;
        // création du thread
        Thread t = new Thread(() -> {
            while (running) {
                try {
                    String message = socIn.readLine();
                    // on passe dans le thread bizarre
                     if (message.length()>=13 && message.substring(0,13).equals("erreur_pseudo")){
                        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                        System.out.println(message.substring(13,message.length()));
                        i=3;
                    }else if (message.equals("user_not_found")) {
                        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                        System.out.println("L'utilisateur renseigne n'existe pas.");
                        //Thread.sleep(2000);
                        i = 1;
                    }else{
                        System.out.println(message);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        });
        t.start();

        String personne = "";
        String personneCo ="";
        while (ok) {
            if(i==0){
                System.out.println("---------------------------------------------------------------");
                System.out.println("                   BIENVENUE DANS LE CHAT                      ");
                System.out.println("---------------------------------------------------------------");

                System.out.println("Saisissez votre identifiant");
                pseudo=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                i++;
                Thread.sleep(50);
            }else{
                Thread.sleep(1000);
                if(i==1) {
                    String choix = choix();
                    String action = afficherMenu(choix);
                    if (!action.equals("retour menu")) {
                        i=3;
                    }

                } else { // cas où i ne vaut pas 1 ou 0 donc on veut forcément écrire
                    //System.out.println("moi : "); // à tester
                    line = stdIn.readLine(); //on écrit une ligne au clavier
                    if (line.equals(".")) {
                        socOut.println("deconnexion");
                        System.exit(0);
                        ok = false;
                        break; // on break quand on écrit '.'
                    } else if (line.equals("Revenir au menu")) {
                        i = 1;
                    } else {
                        socOut.println(line);
                    }
                }
            }
        }
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }
    public String choix() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        System.out.println("Choisissez une action : ");
        System.out.println("1 : Parler à une personne");
        System.out.println("2 : Parler à tout le monde");
        System.out.println("3 : Deconnexion");
        System.out.println("--------------------------");
        String action = stdIn.readLine();
        return action;
    }

    public String afficherListePersonnes() throws IOException, InterruptedException {
        String retour = "";
        // on récupère la liste des personnes qui ont un compte
        socOut.println("Afficher listeClients");
        System.out.println("Voici la liste des utilisateurs");
        Thread.sleep(10);
        // On tape le pseudo de la personne à qui on veut parler
        System.out.println("A qui voulez vous parler ?");
        String personneChoisie = stdIn.readLine();


        if(personneChoisie.equals("Revenir au menu")) {
            retour = "retour menu";
        }
        else {
            // on charge la conversation avec l'utilisateur choisi
            socOut.println("Conversation" + personneChoisie);
            //on peut parler
            socOut.println("1:"+personneChoisie);
        }

        return retour;

    }

    // méthode d'affichage du menu, en cas de choix or des propositions on rappelle la fonction
    public String afficherMenu(String choix) throws IOException, InterruptedException {
        String retour = "";
        switch(choix) {
            case "1" :
                retour = afficherListePersonnes();
                break;
            case "2" :
                socOut.println("pour tous");
                break;
            case "3" :
                socOut.println("deconnexion");
                System.exit(0);
                break;
            default :
                retour = "retour menu";
                break;
        }
        return retour;
    }

}
