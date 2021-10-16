import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.*;

public class ClientThread
        extends Thread {

    private Socket clientSocket;
    private String identifiant;
    private Map<User, Socket> listeClients;
    private ArrayList<Groupe> listeGroupes = new ArrayList<>();
    private ArrayList<Groupe> listeGroupesUser = new ArrayList<>();
    private JSONArray jsonHistorique ;
    private ReentrantLock mutex;

    ClientThread(Socket s, String id, Map<User, Socket> liste, ArrayList<Groupe> listeGroupes, JSONArray jsonHistorique, ReentrantLock mutex){
        this.listeClients = liste;
        this.identifiant = id;
        this.clientSocket = s;
        this.mutex = mutex;
        if (listeGroupes != null) {
            this.listeGroupes = listeGroupes;
            for (Groupe groupe : listeGroupes) {
                if (groupe.isUserInThisGroup(getUserByPseudo(identifiant, listeClients))) {
                    this.listeGroupesUser.add(groupe);
                }
            }
        }
        this.jsonHistorique = jsonHistorique;

    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void run() {
        try {

            BufferedReader socIn ;
            socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintStream socOutClients ;
            PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
            User userActuel = getUserByPseudo(identifiant, listeClients);
            String interlocuteur = "";
            while (true) {
                String line = socIn.readLine();
                //pour sauter une ligne dans les listes d'utilisateur
                if (line.equals("Afficher listeClients")) { // si le client a demandé à voir la liste des clients
                    StringBuilder listeToPrint = new StringBuilder();
                    for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
                        if (!entry.getKey().getPseudo().equals(identifiant)) {
                            listeToPrint.append("	-").append(entry.getKey().getPseudo());
                            if (entry.getKey().getEtat()) {
                                listeToPrint.append(" (en ligne)\n");
                            } else {
                                listeToPrint.append(" (hors ligne)\n");
                            }
                        }
                    }
                    socOut.println("listToPrint"+listeToPrint);
                } else if (line.equals("deconnexion")) {
                    userActuel.setEtat(false);
                } else if (line.length() >= 2 && line.startsWith("1:") && !line.substring(2).equals("Revenir au menu")) {
                    //le client a choisi quelqu'un a qui parler
                    interlocuteur = line.substring(2);
                    if (!listeClients.containsKey(getUserByPseudo(interlocuteur, listeClients))) {
                        socOut.println("user_not_found");
                    }
                } else if (line.length() >= 9 && line.startsWith("pour tous")) {
                    interlocuteur = "tous";
                } else if (line.length() >= 2 && line.startsWith("2:")) {
                    interlocuteur = line.substring(2);
                } else if (line.length() >= 12 && line.startsWith("Conversation")) {
                    String contact = line.substring(12);
                    if (!listeClients.containsKey(getUserByPseudo(contact, listeClients))) {
                        socOut.println();
                    } else {
                        try {
                            mutex.lock();
                            socOut.println(afficherConversation(contact));
                        } finally {
                            mutex.unlock();
                        }
                    }
                } else {
                    //DISCUSSION BASIQUE

                    if (!interlocuteur.equals("tous")) {
                        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
                            if (entry.getKey().getPseudo().equals(interlocuteur)) {
                                socOutClients = new PrintStream(entry.getValue().getOutputStream());
                                socOutClients.println(identifiant + " : " + line);
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
                    } else {
                        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
                            if (!entry.getKey().getPseudo().equals(identifiant)) {
                                socOutClients = new PrintStream(entry.getValue().getOutputStream());
                                socOutClients.println(identifiant + " : " + line);
                            }
                        }
                    }

                }
                //System.out.println(identifiant + " a dit " + line);
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

    public void fillJson(String interlocuteur, String line) {
        JSONObject elementsMessage = new JSONObject();
        elementsMessage.put("expediteur", identifiant);
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
            if ((contact.equals(destinataire) && identifiant.equals(expediteur)) || (contact.equals(expediteur) && identifiant.equals(destinataire))) {
                listeHistorique.append(expediteur).append(" : ").append(objectInArray.get("contenu")).append("\n");
            }
        }
        return listeHistorique.toString();
    }

}

  
