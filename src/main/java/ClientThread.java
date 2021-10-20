import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.*;

public class ClientThread
        extends Thread {

    private Socket clientSocket;
    private String pseudo;
    private Map<User, Socket> listeClients;
    private ArrayList<Groupe> listeGroupes = new ArrayList<>();
    private JSONArray jsonHistorique;
    private JSONArray jsonMessagesGroupes;
    private ReentrantLock mutex;

    ClientThread(Socket s, String pseudo, Map<User, Socket> liste, JSONArray jsonHistorique, ReentrantLock mutex,ArrayList<Groupe> listeGrpes, JSONArray jsonMessagesGroupes){
        this.listeClients = liste;
        this.listeGroupes=listeGrpes;
        this.pseudo = pseudo;
        this.clientSocket = s;
        this.mutex = mutex;
        this.jsonHistorique = jsonHistorique;
        this.jsonMessagesGroupes= jsonMessagesGroupes;
    }


    public void run() {
        boolean inLine = true;
        try {
            if(inLine) {
                //initialisation des variables
                BufferedReader socIn;
                socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
                User userActuel = getUserByPseudo(pseudo, listeClients);
                String interlocuteur = "";
                while (true) {
                    String line = socIn.readLine();
                    if (line.equals("Afficher listeClients")) // si le client a demandé à voir la liste des clients
                    {
                        callAfficherListeClients(socOut);
                    }else if(line.equals("Afficher listeGroupes"))
                    {
                        callAfficherListeGroupes(socOut);
                    } else if (line.equals("déconnexion")) {
                        userActuel.setEtat(false);
                        inLine = false;
                        System.out.println("Deconnexion from " + clientSocket.getLocalAddress());
                        break;
                    } else if (line.length() >= 2 && line.startsWith("1:") && !line.substring(2).equals("Revenir au menu")) {
                        interlocuteur = line.substring(2);
                        callChoixInterlocuteur(line, socOut);
                    } else if (line.length() >= 2 && line.startsWith("1bis:") && !line.substring(5).equals("Revenir au menu")) {
                        interlocuteur = "group_name" + line.substring(5); // ici interlocuteur = nom groupe
                        callChoixGroupe(line, socOut);
                    } else if (line.length() >= 9 && line.startsWith("pour tous")) {
                        interlocuteur = "tous";
                    } else if (line.length() >= 12 && line.startsWith("Conversation")) {
                        callAfficherConversation(line, socOut);
                    } else if (line.length() >= 12 && line.startsWith("GroupeConversation")) {
                        callAfficherGroupeConversation(line, socOut);
                    } else {
                        //DISCUSSION BASIQUE
                        if (!interlocuteur.equals("tous") && !interlocuteur.startsWith("group_name")) {
                            callParlerAQuelquun(line, interlocuteur);
                        } else if(!interlocuteur.equals("tous") && interlocuteur.startsWith("group_name")) {
                            callParlerAGroupe(line, interlocuteur.substring(10));
                        }else{
                            callParlerATous(line);
                        }

                    }
                }
            }



        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }


    public static User getUserByPseudo(String pseudo, Map<User, Socket> liste) {
        User userPrec = null;
        for (Map.Entry<User, Socket> entry : liste.entrySet()) {
            if (entry.getKey().getPseudo().equals(pseudo)) {
                userPrec = entry.getKey();
                break;
            }
        }
        return userPrec;
    }

    public static Groupe getGroupByName(String name, ArrayList<Groupe> listeGroupes) {
        Groupe groupePrec = null;
        for (Groupe groupe : listeGroupes) {
            if (groupe.getName().equals(name)) {
                groupePrec = groupe;
                break;
            }
        }
        return groupePrec;
    }

    public void fillJson(String interlocuteur, String line) {
        JSONObject elementsMessage = new JSONObject();
        elementsMessage.put("expediteur", pseudo);
        elementsMessage.put("destinataire", interlocuteur);
        elementsMessage.put("contenu", line);
        jsonHistorique.add(elementsMessage);
    }


    public void parse() {
        try (FileWriter file = new FileWriter("./historique.json")) {
            file.write(jsonHistorique.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String afficherConversation(String contact) {
        StringBuilder listeHistorique = new StringBuilder();
        for (Object element : jsonHistorique) {
            JSONObject objectInArray = (JSONObject) element;
            String destinataire = (String) objectInArray.get("destinataire");
            String expediteur = (String) objectInArray.get("expediteur");
            if ((contact.equals(destinataire) && pseudo.equals(expediteur)) || (contact.equals(expediteur) && pseudo.equals(destinataire))) {
                listeHistorique.append(expediteur).append(" : ").append(objectInArray.get("contenu")).append("\n");
            }
        }
        return listeHistorique.toString();
    }

    public String afficherGroupeConversation(String groupName) {
        StringBuilder listeHistorique = new StringBuilder();
        for (Object elementGroup : jsonMessagesGroupes) {
            JSONObject objectInArray = (JSONObject) elementGroup;
            String destinataire = (String) objectInArray.get("destinataire");
            String expediteur = (String) objectInArray.get("expediteur");
            if (groupName.equals(destinataire)) {
                listeHistorique.append(expediteur).append(" : ").append(objectInArray.get("contenu")).append("\n");
            }
        }
        return listeHistorique.toString();
    }

    public void callAfficherListeClients(PrintStream socOut){
        StringBuilder listeToPrint = new StringBuilder();
        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
            if (!entry.getKey().getPseudo().equals(pseudo)) {
                listeToPrint.append("	-").append(entry.getKey().getPseudo());
                if (entry.getKey().getEtat()) {
                    listeToPrint.append(" (en ligne)\n");
                } else {
                    listeToPrint.append(" (hors ligne)\n");
                }
            }
        }
        socOut.println("listToPrint"+listeToPrint+"A qui voulez-vous parler?\n");
    }


    public void callAfficherListeGroupes(PrintStream socOut){
        int count = 0;
        StringBuilder listeToPrint = new StringBuilder();
        for (Groupe groupe : listeGroupes) {
                listeToPrint.append("	-").append(groupe.getName()).append(" (");
                //aficher les membres du groupe
            for(User u :groupe.getMembres()){
                listeToPrint.append(u.getPseudo());
                if(count ==groupe.getMembres().size()){
                    //groupe.getMembres().size()
                    listeToPrint.append(", ");
                }
                count++;
            }
        }
        listeToPrint.append(")\n");
        System.out.println(listeToPrint);
        socOut.println("listToPrint"+listeToPrint+"A qui voulez-vous parler?\n");
    }


    public void callChoixInterlocuteur(String line, PrintStream socOut){
        //le client a choisi quelqu'un a qui parler
        String interlocuteur = line.substring(2);
        if (!listeClients.containsKey(getUserByPseudo(interlocuteur, listeClients))) {
            socOut.println("user_not_found");
        }
    }

    public void callChoixGroupe(String line, PrintStream socOut){
        //le client a choisi quelqu'un a qui parler
        String groupe = line.substring(5);
        if (!listeGroupes.contains(getGroupByName(groupe, listeGroupes))) {
            socOut.println("groupe_not_found");
        }
    }

    public void callAfficherConversation(String line, PrintStream socOut){
        String contact = line.substring(12);
        if (!listeClients.containsKey(getUserByPseudo(contact, listeClients))) {
            socOut.println();
        } else {
            try {
                mutex.lock();
                socOut.print("\n\n\n"+afficherConversation(contact));
            } finally {
                mutex.unlock();
            }
        }
    }

    public void callAfficherGroupeConversation(String line, PrintStream socOut){
        String nomGroupe = line.substring(18);
        User userActuel= getUserByPseudo(pseudo, listeClients);
        Groupe group = getGroupByName(nomGroupe, listeGroupes);
        if (!listeGroupes.contains(group)) { // vérifier que le groupe existe bien
            socOut.println(); // on ne renvoie rien pour passer à l'appel ChoisirGroupe de Client.java
        } else if (!listeGroupeContientPseudo(group.getMembres(), pseudo)) { // si l'utilisateur ne fait pas encore partie du groupe on le rajoute
            group.addMember(userActuel);
            try {
                mutex.lock();
                socOut.print("\n\n\n"+afficherGroupeConversation(nomGroupe));
            } finally {
                mutex.unlock();
            }
        }else{  // l'utilisateur fait déjà partie du groupe
            try {
                mutex.lock();
                socOut.print("\n\n\n"+afficherGroupeConversation(nomGroupe));
            } finally {
                mutex.unlock();
            }
        }
    }

    public void callParlerATous(String line) throws IOException {
        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
            if (!entry.getKey().getPseudo().equals(pseudo)) {
                if(entry.getKey().getEtat()) {
                    PrintStream socOutClients = new PrintStream(entry.getValue().getOutputStream());
                    socOutClients.println("(A tout le monde) " + pseudo + " : " + line);
                }
            }
        }
    }


    public void callParlerAQuelquun(String line, String interlocuteur) throws IOException {
        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
            if (entry.getKey().getPseudo().equals(interlocuteur)) {
                if(entry.getKey().getEtat()) {
                    PrintStream socOutClients = new PrintStream(entry.getValue().getOutputStream());
                    socOutClients.println(pseudo + " : " + line);
                }
                try {
                    mutex.lock();
                    fillJson(interlocuteur, line);
                    parse(); //Exporter le fichier JSON
                } finally {
                    mutex.unlock();
                }
                break;
            }
        }
    }

    public void callParlerAGroupe(String line, String nomGroupe) throws IOException {
        PrintStream socOutClients = null;
        for (Groupe groupe : listeGroupes) {
            if (groupe.getName().equals(nomGroupe)) { //vérifier le bon groupe
                ArrayList<User> listUsers = groupe.getMembres();
                for(User u : listUsers){ // envoyer aux bons membres du groupe
                    if(u.getEtat() && u.getPseudo()!=pseudo) {
                        //on récupère la socket client
                        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
                            if (entry.getKey().getPseudo() == u.getPseudo()) {
                                socOutClients = new PrintStream(entry.getValue().getOutputStream());
                                break;
                            }
                        }
                        socOutClients.println(pseudo + " : " + line);
                    }
                }
                /*try {
                    mutex.lock();
                    fillJson(nomGroupe, line, pseudo); //pseudo ici = expéditeur dans jsonMessageGroupes
                    parse(); //Exporter le fichier JSON
                } finally {
                    mutex.unlock();
                }*/
                break;
            }
        }
    }

    public boolean listeGroupeContientPseudo(ArrayList<User> liste, String pseudo){
        boolean contient = false;
        for(User u : liste){
            if (u.getPseudo().equals(pseudo)){
                contient = true;
                break;
            }
        }
        return contient;
    }
}