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
		if(listeGroupes != null) {
			this.listeGroupes = listeGroupes;
			for (int i = 0; i < listeGroupes.size(); i++) {
				if (listeGroupes.get(i).isUserInThisGroup(getUserByPseudo(identifiant, listeClients))) {
					this.listeGroupesUser.add(listeGroupes.get(i));
				}
			}
		}
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
    		while (true) {
				String line = socIn.readLine();
				if (line.substring(0, 1).equals("1")) {
				//on veut afficher tous les users connectés
					String catalogueUsersConnectes = "";
					for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
						if(entry.getKey().connecte){
							catalogueUsersConnectes+=entry.getKey().getPseudo();
							catalogueUsersConnectes+="\n";
						}
					}
					System.out.println(catalogueUsersConnectes);
					catalogueUsersConnectes+="end";
					socOut.println("users_co "+catalogueUsersConnectes);
					/*le code users_co est envoyé au thread de Client pour lui indiquer
					qu'il doit traiter cette demande particulièrement, ici cela
					indique que le serveur lui envoie la liste des clients connectés
					 */
				}else{
				//DISCUSSION BASIQUE
				for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
					//if(entry.getValue() = socketDuGroupe)
					if (!entry.getKey().equals(identifiant)) {
						socOutClients = new PrintStream(entry.getValue().getOutputStream());
						socOutClients.println(identifiant + ": " + line);
					}
				}
				System.out.println(identifiant + " a dit " + line);
				}
    		}
    	} catch (Exception e) {
        	System.err.println("Error in EchoServer:" + e); 
        }
       }

       public static User getUserByPseudo(String pseudo, Map<User, Socket> liste){
		   User userPrec = null;
		   for (Map.Entry<User, Socket> entry : liste.entrySet()) {
			   if(entry.getKey().getPseudo()==pseudo){
			   	userPrec = entry.getKey();
			   	break;
			   }
		   }
		   return userPrec;
	   }
  }

  
