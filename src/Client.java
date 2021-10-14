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
        // Lecture de message
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

        String personne = "";
        String personneCo ="";
        while (ok) {
            if(i==0){
                System.out.println("Saisissez votre identifiant");
                pseudo=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                i++;
            }else{
                if(i==1) {
                    String choix = choix();
                    String action [] = afficherMenu(choix);
                    System.out.println(action[0]);
                    if (!action[0].equals("retour menu")) {
                        personne = action[0];
                        personneCo = action[1];
                        i++;
                    }

                } else { // cas où i ne vaut pas 1 ou 0 donc on veut forcément écrire
                    line=stdIn.readLine(); //on écrit une ligne au clavier
                    if (line.equals(".")) {
                        socOut.println("deconnexion");
                        System.exit(0);
                        ok = false;
                        break; // on break quand on écrit '.'
                    }
                    //on envoie la ligne au serveur en fonction du choix de l'utilisateur
                    if(!personne.equals("")) { // cas où l'user veut envoyer un message à une personne quelle soit co ou non
                        socOut.println("personne non co : " + personne + " et le message est : " + line);
                    } else if (!personneCo.equals("")) { // cas où l'utilisateur veut envoyer un message à un utilisateur connecté
                        socOut.println("personne co : " + personneCo + " et le message est : " + line);
                    } else { // on envoie un message à tout le monde
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
    public String choix() throws IOException{
        System.out.println("Choisissez une action : ");
        System.out.println("1 : Parler à une personne");
        System.out.println("2 : Parler à une personne connectée");
        System.out.println("3 : Parler à tout le monde");
        System.out.println("--------------------------");
        String action = stdIn.readLine();
        return action;
    }

    public String afficherListePersonnes() throws IOException {
        // on récupère la liste des personnes qui ont un compte
        socOut.println("Afficher listeClients");
        System.out.println("Voici la liste des utilisateurs");
        System.out.println(socIn.readLine());
        String action = stdIn.readLine();

        return action;
    }

    public String afficherListePersonnesConnectees() throws IOException {
        socOut.println("Afficher clients connectés");
        System.out.println("Voici la liste des utilisateurs connectés");
        System.out.println(socIn.readLine());
        String action = stdIn.readLine();
        return action;
    }

    // méthode d'affichage du menu, en cas de choix or des propositions on rappelle la fonction
    public String[] afficherMenu(String choix) throws IOException {
        String personne ="", personneCo="";
        String retour [] = new String[2];
        switch(choix) {
            case "1" :
                personne = afficherListePersonnes();
                // if personne bien dans la liste on passe à la suite et i++
                // else on affiche à nouveau le menu
                break;
            case "2" :
                personneCo = afficherListePersonnesConnectees();
                // if personne bien dans la liste on passe à la suite et i++
                // else on affiche à nouveau le menu
                break;
            case "3" :
                break;
            default :
                personne = "retour menu";
                break;
        }
        retour[0] = personne;
        retour[1] = personneCo;
        return retour;

    }

}
