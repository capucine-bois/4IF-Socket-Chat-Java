import java.io.*;
import java.net.*;
import java.util.*;

public class EchoServerMultiThreaded  {


       public static void main(String args[]){ 
        ServerSocket listenSocket;
        Map<User,Socket> listeClients = new HashMap<User,Socket>();
		ArrayList<Groupe> listeGroupes =  new ArrayList<>();
                
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
			ClientThread ct = new ClientThread(clientSocket, pseudo, listeClients, listeGroupes);

			//System.out.println(listeGroupes);

			//informer le client des gens déjà connectés
			for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
				socOut = new PrintStream(entry.getValue().getOutputStream());
				if(!entry.getKey().getPseudo().equals(pseudo) && entry.getKey().getEtat()) socOut.println(pseudo + " est connecte.");
			}
			PrintStream socOutActuelle = new PrintStream(clientSocket.getOutputStream());
			// Afficher la liste des personnes déjà connectés
			for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
				if(!entry.getKey().getPseudo().equals(pseudo) && entry.getKey().getEtat()) socOutActuelle.println(entry.getKey().getPseudo() + " est connecte.");
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

  
