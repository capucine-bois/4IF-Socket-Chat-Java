import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EchoServerMultiThreaded  {


       public static void main(String args[]){ 
        ServerSocket listenSocket;
        Map<User,Socket> listeClients = new HashMap<User,Socket>();
		ArrayList<Groupe> listeGroupes =  new ArrayList<>();
	   	JSONArray jsonHistorique = new JSONArray();


		   if (args.length != 1) {
		  System.out.println("Usage: java EchoServer <EchoServer port>");
		  System.exit(1);
			}
	try {
        BufferedReader socIn = null ;
		listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port
        String pseudo = "";
		System.out.println("Server ready...");
		PrintStream socOut = null;
		User user ;
		/*
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader("../../../historique.json")) {
			Object obj = jsonParser.parse(reader);
			jsonHistorique = (JSONArray) obj;
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		// Ouverture du JSON
		JSONParser jsonParser = new JSONParser();


		try (FileReader reader = new FileReader("../../../historique.json"))
		{

			//Read JSON file
			Object obj = jsonParser.parse(reader);
	 		jsonHistorique = (JSONArray) obj;


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}


		while (true) {
			Socket clientSocket = listenSocket.accept();
            socIn= new BufferedReader(
    			new InputStreamReader(clientSocket.getInputStream())); 
			System.out.println("Connexion from:" + clientSocket.getInetAddress());

			pseudo = socIn.readLine();
			User userPrec = getUserByPseudo(pseudo, listeClients);
			// si le client se reconnecte (et donc existe deja dans la base, il n'est pas nouveau)
			if (userPrec != null && listeClients.containsKey(userPrec)){
				listeClients.replace(userPrec, clientSocket);
				userPrec.setStatut(true);
			}else{
				user = new User(pseudo);
				listeClients.put(user, clientSocket);
				user.setStatut(true);
			}
			//initialisation du client
			ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, listeGroupes, jsonHistorique);


			//informer le client des gens déjà connectés
			for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
				socOut = new PrintStream(entry.getValue().getOutputStream());
				if(!entry.getKey().getPseudo().equals(pseudo) && entry.getKey().getEtat()) socOut.println(pseudo + " est connecte.");
			}
			ct.start();
		}
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
      }

      public static User getUserByPseudo(String pseudo, Map<User, Socket> liste){
       	User userPrec = null;
		  for (Map.Entry<User, Socket> entry : liste.entrySet()) {
			  if(entry.getKey().getPseudo().equals(pseudo)) userPrec = entry.getKey();
		  }
		  return userPrec;
	  }

  }

  
