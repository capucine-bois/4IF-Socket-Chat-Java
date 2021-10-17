import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class EchoServerMultiThreaded {

    public static void main(String[] args) {
        ServerSocket listenSocket;
        Map<User, Socket> listeClients = new HashMap<>();
        JSONArray jsonHistorique = new JSONArray();
        JSONArray listeUsersPersistant = new JSONArray();
        ReentrantLock mutex = new ReentrantLock();
        ReentrantLock mutexUser = new ReentrantLock();


        try {
            BufferedReader socIn;
            listenSocket = new ServerSocket(1234); //port
            String pseudo ;
            System.out.println("Server ready...");
            PrintStream socOut;
            User user;

            // Ouverture du JSON HISTORIQUE DES MESSAGES
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader("./historique.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);
                jsonHistorique = (JSONArray) obj;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Ouverture du JSON USERS
            try (FileReader reader = new FileReader("./users.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);
                listeUsersPersistant = (JSONArray) obj;
                for (Object element : listeUsersPersistant) {
                    JSONObject objectInArray = (JSONObject) element;
                    String pseudoUser = (String) objectInArray.get("pseudo");
                    listeClients.put(new User(pseudoUser),new Socket());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            while (true) {
                Socket clientSocket = listenSocket.accept();
                socIn = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                pseudo = socIn.readLine();
                User userPrec = getUserByPseudo(pseudo, listeClients);
                // si le client se reconnecte (et donc existe deja dans la base, il n'est pas nouveau)
                if (userPrec != null && listeClients.containsKey(userPrec)) {
                    if (userPrec.getEtat()) { // deux memes pseudos sont connectés
                        socOut = new PrintStream(clientSocket.getOutputStream());
                        socOut.println("\nerreur_pseudoErreur, ce pseudo est deja utilise par un utilisateur actuellement en ligne. Saisissez '.' et relancer le chat en choississant un autre identifiant.");
                    } else {
                        listeClients.replace(userPrec, clientSocket);
                        userPrec.setEtat(true);
                        //initialisation du client
                        ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients,jsonHistorique,mutex);
                        ct.start();
                    }
                } else {
                    user = new User(pseudo);
                    listeClients.put(user, clientSocket);
                    user.setEtat(true);
                    //remplir la liste persistante des users
                    try {
                        mutexUser.lock();
                        JSONObject userObject = new JSONObject();
                        userObject.put("pseudo", pseudo);
                        listeUsersPersistant.add(userObject);
                        parse(listeUsersPersistant); //Exporter le fichier JSON
                    } finally {
                        mutexUser.unlock();
                    }
                    ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, jsonHistorique,mutex);
                    ct.start();
                }

            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    public static User getUserByPseudo(String pseudo, Map<User, Socket> liste) {
        User userPrec = null;
        for (Map.Entry<User, Socket> entry : liste.entrySet()) {
            if (entry.getKey().getPseudo().equals(pseudo)) userPrec = entry.getKey();
        }
        return userPrec;
    }

    public static void parse(JSONArray listeUsersPeristant){
        try (FileWriter file = new FileWriter("./users.json")) {
            file.write(listeUsersPeristant.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

  
