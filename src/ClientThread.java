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

	/**
  	* receives a request from client then sends an echo to the client
  	* @param clientSocket the client socket
  	**/
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
					//on veut créer un groupe
					Groupe groupe = new Groupe(userActuel, line.substring(1, line.length()));
					if(groupe!=null) {
						listeGroupes.add(groupe);
						listeGroupesUser.add(groupe);
					}
					socOut.println("Groupe bien ajoute!");
					System.out.println("taille liste des groupes"+listeGroupes.size() + "\n premier : "+listeGroupes.get(0).getNom() );
				}else if(line.substring(0, 1).equals("3")){
					//on veut rejoindre un groupe existant
					String groupeChoisi = line.substring(1, line.length());
					if(Integer.parseInt(groupeChoisi)>=listeGroupes.size()){
					    socOut.println("retour_menu: Ce groupe n'existe pas, cliquez sur Entree pour revenir au Menu");
                    }else {
                        Groupe groupeChoisiParUser = listeGroupes.get(Integer.parseInt(groupeChoisi));
                        if (!listeGroupesUser.contains(groupeChoisiParUser)) { //si le client n'est pas encore dans le groupe, on le rajoute
                            groupeChoisiParUser.addUser(userActuel); // on ajoute l'utilisateur au groupe
                            listeGroupesUser.add(groupeChoisiParUser); // on ajoute le groupe à la liste de groupes associé au User
                        }
                        userActuel.setGroupeActuel(groupeChoisiParUser);
                    }
				}else if(line.substring(0, 1).equals("2")){
					//pour pouvoir tester, je crée quelques groupes que j'ajoute à la listeGroupes
					Groupe g1 = new Groupe(userActuel, "Groupe1");
					Groupe g2 = new Groupe(userActuel, "Groupe2");
					listeGroupes.add(g1);
					listeGroupes.add(g2);

					//on veut envoyer le catalogue des groupes au client
					String catalogueGroupes = "";
					for(int j= 0; j<listeGroupes.size(); j++){
						catalogueGroupes+=listeGroupes.get(j).getNom();
						catalogueGroupes+= "   Groupe numero ";
						catalogueGroupes+= j;
						catalogueGroupes+= "\n";
					}
					socOut.println(catalogueGroupes);
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

  
