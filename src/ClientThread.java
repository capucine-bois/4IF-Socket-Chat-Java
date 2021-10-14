import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

public class ClientThread
	extends Thread {
	
	private Socket clientSocket;
    private String identifiant;
	private Map<User,Socket> listeClients;
	private ArrayList<Groupe> listeGroupes = new ArrayList<>();
	private ArrayList<Groupe> listeGroupesUser = new ArrayList<>();


	ClientThread(Socket s, String id, Map<User,Socket> liste, ArrayList<Groupe> listeGroupes) {
		this.listeClients=liste;
        this.identifiant = id;
		this.clientSocket = s;
		/*
		if(listeGroupes != null) {
			this.listeGroupes = listeGroupes;
			for (int i = 0; i < listeGroupes.size(); i++) {
				if (listeGroupes.get(i).isUserInThisGroup(getUserByPseudo(identifiant, listeClients))) {
					this.listeGroupesUser.add(listeGroupes.get(i));
				}
			}
		}*/
	}

	public String getIdentifiant(){
		return identifiant;
	}

	public void run() {
    	  try {
    		BufferedReader socIn = null;
    		socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintStream socOutClients = null;
			PrintStream socOut = new PrintStream(clientSocket.getOutputStream());
			User userActuel = getUserByPseudo(identifiant,listeClients);
			String interlocuteur = "";
    		while (true) {
				String line = socIn.readLine();
				//pour sauter une ligne dans les listes d'utilisateur
				String sautDeLigne=System.getProperty("line.separator");
				if(line.equals("Afficher listeClients")) { // si le client a demandé à voir la liste des clients
					String listeToPrint = "";
					for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
						if (!entry.getKey().getPseudo().equals(identifiant)) {
							listeToPrint+=entry.getKey().getPseudo()+sautDeLigne;
						}
					}
					socOut.println(listeToPrint);

				} else if (line.equals("Afficher clients connectés")) { // si le client veut voir seulement les clients connectés
					String listeToPrint = "";
					for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
						if (!entry.getKey().getPseudo().equals(identifiant) && entry.getKey().getEtat()) { // on teste si l'utilisateur est connecté
							listeToPrint+=entry.getKey().getPseudo()+sautDeLigne;
						}
					}
					socOut.println(listeToPrint);
				} else if(line.equals("deconnexion")) {
					userActuel.setEtat(false);
				} else  {
					//DISCUSSION BASIQUE
					if (line.substring(0, 2).equals("1:")) {
						//le client a choisi quelqu'un a qui parler
						interlocuteur = line.substring(2,line.length());
					}
					for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
						System.out.println("o passe dans le for");
						if (entry.getKey().getPseudo().equals(interlocuteur)) {
							System.out.println("on passe dans le if");
							socOutClients = new PrintStream(entry.getValue().getOutputStream());
							socOutClients.println(identifiant + ": " + line);
							break;
						}
					}

				}

				System.out.println(identifiant + " a dit " + line);
    		}
    	} catch (Exception e) {
        	System.err.println("Error in EchoServer:" + e); 
        }
       }

       public static User getUserByPseudo(String pseudo, Map<User, Socket> liste){
		   User userPrec = null;
		   for (Map.Entry<User, Socket> entry : liste.entrySet()) {
			   if(entry.getKey().getPseudo().equals(pseudo)){
			   	userPrec = entry.getKey();
			   	break;
			   }
		   }
		   return userPrec;
	   }
  }

  
