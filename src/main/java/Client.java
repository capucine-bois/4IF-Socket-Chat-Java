import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private Socket echoSocket ;
    private PrintStream socOut;
    private BufferedReader stdIn;
    private BufferedReader socIn;
    private ArrayList<String> listePseudoUsersCo = new ArrayList<>();
    private ReentrantLock mutex = new ReentrantLock();
    private int i;

    Client(Socket echoS, PrintStream out, BufferedReader stdin, BufferedReader in) throws IOException {
        this.echoSocket= echoS;
        this.socOut= out;
        this.stdIn = stdin;
        this.socIn=in;
        this.i = 0;
    }

    public void init() throws IOException, InterruptedException {
        String line, pseudo;
        // création du thread
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    String message = socIn.readLine();
                    String erreurPseudo = "erreur_pseudo";
                    String listUtilisateurs= "listToPrint";
                    if (message.length()>=erreurPseudo.length() && message.startsWith(erreurPseudo)){
                        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                        System.out.println(message.substring(erreurPseudo.length()));
                        try {
                            mutex.lock();
                            i=3;
                        } finally {
                            mutex.unlock();
                        }
                    }else if (message.equals("user_not_found")) {
                        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                        System.out.println("L'utilisateur renseigne n'existe pas. Tapez sur Entree pour revenir au menu.");
                        try {
                            mutex.lock();
                            i = 1;
                        } finally {
                            mutex.unlock();
                        }
                    }else if(message.startsWith(listUtilisateurs)){
                        if(message.equals(listUtilisateurs+"A qui voulez-vous parler?")) {
                            //cas ou aucun utilisateur n'existe dans le base et que la liste affichée lors de l'option 1 est donc vide
                            System.out.println("Désolé, aucun autre utilisateur n'existe pour le moment ! Tapez deux fois Entree pour revenir au Menu.");
                        }else {
                            System.out.println("Voici la liste des utilisateurs : \n" +message.substring(11));
                        }
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
        while (true) {
            if(i==0){
                System.out.println("\u001B[33m---------------------------------------------------------------");
                System.out.println("\u001B[33m                   BIENVENUE DANS LE CHAT                      ");
                System.out.println("\u001B[33m---------------------------------------------------------------");

                System.out.println("Saisissez votre identifiant");
                pseudo=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                try {
                    mutex.lock();
                    i++;
                } finally {
                    mutex.unlock();
                }
            }else{
                if(i==1) {
                    String choix = choix();
                    String action = afficherMenu(choix);
                    if (!action.equals("retour menu")) {
                        try {
                            mutex.lock();
                            i=2;
                        } finally {
                            mutex.unlock();
                        }
                    }

                } else { // cas où i ne vaut pas 1 ou 0 donc on veut forcément écrire
                    line = stdIn.readLine(); //on écrit une ligne au clavier
                    if (line.equals(".")) {
                        socOut.println("deconnexion");
                        closeSession();
                        System.exit(0);
                    } else if (line.equals("Revenir au menu")) {
                        try {
                            mutex.lock();
                            i=1;
                        } finally {
                            mutex.unlock();
                        }
                    } else {
                        socOut.println(line);
                    }
                }
            }
        }

    }
    public String choix() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        System.out.println("Choisissez une action : ");
        System.out.println("1 : Parler à une personne");
        System.out.println("2 : Parler à tout le monde");
        System.out.println("3 : Deconnexion");
        System.out.println("--------------------------");
        return stdIn.readLine();
    }

    public void afficherListePersonnes() {
        // on récupère la liste des personnes qui ont un compte
        socOut.println("Afficher listeClients");
    }

    public String choisirInterlocuteur() throws IOException {
        String personneChoisie = stdIn.readLine();
        String retour = "";
        // On tape le pseudo de la personne à qui on veut parler
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
    public String afficherMenu(String choix) throws IOException{
        String retour = "";
        switch(choix) {
            case "1" :
                afficherListePersonnes();
                retour = choisirInterlocuteur();
                break;
            case "2" :
                socOut.println("pour tous");
                break;
            case "3" :
            case "." :
                socOut.println("deconnexion");
                closeSession();
                System.exit(0);
                break;

            default :
                retour = "retour menu";
                break;
        }
        return retour;
    }

    public void closeSession() throws IOException {
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }
}
