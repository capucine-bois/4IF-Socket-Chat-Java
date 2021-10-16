import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EchoServerMultiThreaded {


    public static void main(String[] args) {
        ServerSocket listenSocket;
        Map<User, Socket> listeClients = new HashMap<>();
        ArrayList<Groupe> listeGroupes = new ArrayList<>();
        JSONArray jsonHistorique = new JSONArray();
        ReentrantLock mutex = new ReentrantLock();


        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            BufferedReader socIn;
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
            String pseudo ;
            System.out.println("Server ready...");
            PrintStream socOut;
            User user;

            // Ouverture du JSON
            JSONParser jsonParser = new JSONParser();
            try (FileReader reader = new FileReader("./historique.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);
                jsonHistorique = (JSONArray) obj;
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
                        ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, listeGroupes, jsonHistorique,mutex);
                        ct.start();
                    }
                } else {
                    user = new User(pseudo);
                    listeClients.put(user, clientSocket);
                    user.setEtat(true);
                    //initialisation du client
                    ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, listeGroupes, jsonHistorique,mutex);

                    //informer le client des gens déjà connectés
                    for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
                        socOut = new PrintStream(entry.getValue().getOutputStream());
                        if (!entry.getKey().getPseudo().equals(pseudo) && entry.getKey().getEtat())
                            socOut.println(pseudo + " est en ligne.");
                    }
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

}

  
