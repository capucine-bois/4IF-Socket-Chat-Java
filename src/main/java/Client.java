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
                try {String message = socIn.readLine();
                    String erreurPseudo = "erreur_pseudo";
                    String listUtilisateurs= "listToPrint";
                    String creationGroupe = "group_created";
                    String groupeIntrouvable = "group_not_found";
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
                    }else if(message.startsWith(listUtilisateurs)) {
                        if (message.equals(listUtilisateurs + "A qui voulez-vous parler?")) {
                            //cas ou aucun utilisateur n'existe dans le base et que la liste affichée lors de l'option 1 est donc vide
                            System.out.println("Désolé, aucun autre utilisateur ou groupe n'existe pour le moment ! Tapez deux fois Entree pour revenir au Menu.");
                        } else {
                            System.out.println("Voici la liste des utilisateurs : \n" + message.substring(11));
                        }
                    }else if(message.startsWith(creationGroupe)) {
                        if(message.equals(creationGroupe+"_error")) {
                            System.out.println("ERREUR, ce groupe existe déjà, veuillez choisir un autre nom et réessayer d'en créer en revenant au menu (appuyer sur Entree)");
                        }else {
                            System.out.println("Le groupe a bien été créé. Cliquez sur Entree pour revenir au Menu");
                        }try {
                            mutex.lock();
                            i = 1;
                        } finally {
                            mutex.unlock();
                        }

                    }else if(message.equals(groupeIntrouvable)){
                        System.out.println("Ce groupe n'existe pas. Cliquez sur Entree pour revenir au Menu");
                        try {
                            mutex.lock();
                            i = 1;
                        } finally {
                            mutex.unlock();
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
                System.out.println("---------------------------------------------------------------");
                System.out.println("                   BIENVENUE DANS LE CHAT                      ");
                System.out.println("---------------------------------------------------------------");

                System.out.println("Saisissez votre identifiant");
                pseudo=stdIn.readLine(); //on écrit une ligne au clavier
                socOut.println(pseudo);
                Thread.sleep(50);
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
                        socOut.println("déconnexion");
                        closeSession();
                        System.exit(0);
                    } else if (line.equals("**")) {
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
    public String choix() throws IOException {
        //System.out.print("\033[2J");
        System.out.println("Choisissez une action : ");
        System.out.println("1 : Parler à une personne");
        System.out.println("2 : Parler à tout le monde");
        System.out.println("3 : Parler dans un groupe");
        System.out.println("4 : Créer un nouveau groupe");
        System.out.println("5 : Déconnexion");
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
        if(personneChoisie.equals("**")) {
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

    public void afficherListeGroupes() {
        // on récupère la liste des personnes qui ont un compte
        socOut.println("Afficher listeGroupes"); // à implémenter
    }

    public String choisirGroupe() throws IOException {
        String groupeChoisi = stdIn.readLine();
        String retour = "";
        // On tape le pseudo de la personne à qui on veut parler
        if(groupeChoisi.equals("**")) {
            retour = "retour menu";
        }
        else {
            // on charge la conversation avec l'utilisateur choisi
            socOut.println("GroupeConversation" + groupeChoisi);
            //on peut parler
            socOut.println("1bis:"+groupeChoisi);
        }
        return retour;
    }

    public String creerGroupe() throws IOException {
        System.out.println("Veuillez renseigner le nom du groupe que vous souhaitez créer :");
        String nomGroupe = stdIn.readLine();
        String retour = "";
        // On tape le pseudo de la personne à qui on veut parler
        if(nomGroupe.equals("**")) {
            retour = "retour menu";
        }else{
            socOut.println("creerGroupe"+nomGroupe);
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
                afficherListeGroupes();
                retour = choisirGroupe();
                break;
            case "4":
                retour = creerGroupe();
                break;
            case "5" :
            case "." :
                socOut.println("déconnexion");
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