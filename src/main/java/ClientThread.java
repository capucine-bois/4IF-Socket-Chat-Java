import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.*;

public class ClientThread
        extends Thread {

    private Socket clientSocket;
    private String pseudo;
    private Map<User, Socket> listeClients;
    private JSONArray jsonHistorique;
    private ReentrantLock mutex;

    ClientThread(Socket s, String pseudo, Map<User, Socket> liste, JSONArray jsonHistorique, ReentrantLock mutex){
        this.listeClients = liste;
        this.pseudo = pseudo;
        this.clientSocket = s;
        this.mutex = mutex;
        this.jsonHistorique = jsonHistorique;
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
                    } else if (line.equals("déconnexion")) {
                        userActuel.setEtat(false);
                        inLine = false;
                        System.out.println("Deconnexion from " + clientSocket.getLocalAddress());
                        break;
                    } else if (line.length() >= 2 && line.startsWith("1:") && !line.substring(2).equals("Revenir au menu")) {
                        interlocuteur = line.substring(2);
                        callChoixInterlocuteur(line, socOut);
                    } else if (line.length() >= 9 && line.startsWith("pour tous")) {
                        interlocuteur = "tous";
                    } else if (line.length() >= 12 && line.startsWith("Conversation")) {
                        callAfficherConversation(line, socOut);
                    } else {
                        //DISCUSSION BASIQUE
                        if (!interlocuteur.equals("tous")) {
                            callParlerAQuelquun(line, interlocuteur);
                        } else {
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
        socOut.println("\u001B[33mlistToPrint\u001B[0m"+listeToPrint+"\u001B[33mA qui voulez-vous parler?\u001B[0m");
    }


    public void callChoixInterlocuteur(String line, PrintStream socOut){
        //le client a choisi quelqu'un a qui parler
        String interlocuteur = line.substring(2);
        if (!listeClients.containsKey(getUserByPseudo(interlocuteur, listeClients))) {
            socOut.println("user_not_found");
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


    public void callParlerATous(String line) throws IOException {
        for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
            if (!entry.getKey().getPseudo().equals(pseudo)) {
                if(entry.getKey().getEtat()) {
                    PrintStream socOutClients = new PrintStream(entry.getValue().getOutputStream());
                    socOutClients.println("\u001B[36m(A tout le monde) " + pseudo + " : " + line + "\u001B[0m");
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

}

  
