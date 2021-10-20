import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.security.acl.Group;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EchoServerMultiThreaded {
    // Initialisation des variables
    private ServerSocket listenSocket;
    private Map<User, Socket> listeClients = new HashMap<>(); // association des utilisateurs avec leur socket
    private ArrayList<Groupe> listeGroupes= new ArrayList<>();
    private JSONArray listeGroupsPersistant = new JSONArray(); // liste des groupes existants
    private JSONArray jsonHistorique = new JSONArray(); // historique des messages
    private JSONArray jsonMessagesGroupes = new JSONArray(); // historique des messages
    private JSONArray listeUsersPersistant = new JSONArray(); // liste des utilisateurs existants
    private ReentrantLock mutex = new ReentrantLock(); //mutex pour protéger l'accès à jsonhistorique
    private ReentrantLock mutexGroupe = new ReentrantLock(); //mutex pour protéger l'accès à jsonhistorique

    public EchoServerMultiThreaded() {
        init();
    }

    public void init() {
        try {
            BufferedReader socIn;
            listenSocket = new ServerSocket(1234); //port
            String pseudo;
            System.out.println("Server ready...");
            PrintStream socOut;
            User user;

            // Ouverture du fichier json historique des messages
            jsonHistorique = fillJsonHistorique(jsonHistorique);
            //jsonMessagesGroupes = fillJsonMessagesGroupes(jsonMessagesGroupes);

            // Ouverture du JSON USERS
            fillJsonUser();
            //Ouverture du JSON GROUPES
            fillJsonGroupes();




            // tant que le serveur est en route
            while (true) {
                // Noouvelle connexion d'un client
                Socket clientSocket = listenSocket.accept();
                socIn = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                System.out.println("Connexion from:" + clientSocket.getInetAddress());

                // on récupère l'utilisateur s'il existe
                pseudo = socIn.readLine();
                User userPrec = getUserByPseudo(pseudo, listeClients);

                // si le client se reconnecte (et donc existe deja dans la base, il n'est pas nouveau)
                if (userPrec != null && listeClients.containsKey(userPrec)) {
                    if (userPrec.getEtat()) { // deux memes pseudos sont connectés
                        socOut = new PrintStream(clientSocket.getOutputStream());
                        socOut.println("\nerreur_pseudoErreur, ce pseudo est deja utilise par un utilisateur actuellement en ligne. Saisissez '.' et relancer le chat en choississant un autre identifiant.");
                    } else {
                        // on met à jour la socket de l'utilisateur et on passe son état à "connecté"
                        listeClients.replace(userPrec, clientSocket);
                        userPrec.setEtat(true);
                        //initialisation du client
                        ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients,jsonHistorique,mutex, mutexGroupe, listeGroupes, jsonMessagesGroupes, listeGroupsPersistant);
                        ct.start();
                    }
                } else {
                    // on crée un nouvel utilisateur qu'on ajoute à notre liste
                    user = new User(pseudo);
                    listeClients.put(user, clientSocket);
                    user.setEtat(true);
                    //remplir la liste persistante des users
                    fillListePersistanteUser(pseudo,listeUsersPersistant);
                    //initialisation du client
                    ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, jsonHistorique,mutex, mutexGroupe, listeGroupes, jsonMessagesGroupes, listeGroupsPersistant);
                    ct.start();
                }

            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:"+listenSocket + e);

        }
    }


    public User getUserByPseudo(String pseudo, Map<User, Socket> liste) {
        User userPrec = null;
        for (Map.Entry<User, Socket> entry : liste.entrySet()) {
            if (entry.getKey().getPseudo().equals(pseudo)) userPrec = entry.getKey();
        }
        return userPrec;
    }

    // on remplit le fichier user.json
    public void parseUserJson(JSONArray listeUsersPeristant){
        try (FileWriter file = new FileWriter("./users.json")) {
            file.write(listeUsersPeristant.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // On remplit le jsonArray jsonHistorique avec le fichier de conversation historique.json
    public JSONArray fillJsonHistorique(JSONArray jsonHistorique) throws IOException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("./historique.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            jsonHistorique = (JSONArray) obj;
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        return jsonHistorique;
    }

    // On remplit le jsonArray jsonHistorique avec le fichier de conversation historique.json
    public JSONArray fillJsonMessagesGroupes(JSONArray jsonMessagesGroupes) throws IOException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("./messagesGroupes.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            jsonMessagesGroupes = (JSONArray) obj;
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        return jsonMessagesGroupes;
    }


    // ajoute un utilisateur à la liste des utilisateurs persistante
    public JSONArray fillListePersistanteUser(String pseudo, JSONArray listeUsersPersistant) {
        JSONObject userObject = new JSONObject();
        userObject.put("pseudo", pseudo);
        listeUsersPersistant.add(userObject);
        parseUserJson(listeUsersPersistant); //Exporter le fichier JSON
        return listeUsersPersistant;
    }

    public void fillJsonUser() {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("./users.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            listeUsersPersistant = (JSONArray) obj;
            for (Object element : listeUsersPersistant) {
                JSONObject objectInArray = (JSONObject) element;
                String pseudoUser = (String) (objectInArray.get("pseudo"));
                listeClients.put(new User(pseudoUser),new Socket());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void fillJsonGroupes() {
        JSONParser jsonParser = new JSONParser();
        Map<User,Socket> mapMembers = new HashMap<>();
        ArrayList<String> listeMembresGroupe = new ArrayList<>();
        try (FileReader reader = new FileReader("./groupes.json")) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
            listeGroupsPersistant = (JSONArray) obj;
            for (Object element : listeGroupsPersistant) {
                JSONObject objectInArray = (JSONObject) element;
                String nomGroupe = (String) (objectInArray.get("nomGroupe"));
                JSONArray listeMembres=(JSONArray) objectInArray.get("membres");
                for (Object elementDeMembres : listeMembres) { // pour chaque membre
                    JSONObject userInArray = (JSONObject) elementDeMembres;
                    String pseudoMembre = (String) (userInArray.get("pseudo"));
                    listeMembresGroupe.add(pseudoMembre);
                }
                listeGroupes.add(new Groupe(nomGroupe,listeMembresGroupe));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EchoServerMultiThreaded serveur = new EchoServerMultiThreaded();

    }
}


  
