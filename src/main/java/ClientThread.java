import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import org.json.simple.*;

public class ClientThread
	extends Thread {
	
	private Socket clientSocket;
    private String identifiant;
	private Map<User,Socket> listeClients;
	private ArrayList<Groupe> listeGroupes = new ArrayList<>();
	private ArrayList<Groupe> listeGroupesUser = new ArrayList<>();
	private JSONArray jsonHistorique = new JSONArray();
	private FileWriter file = new FileWriter("historique.json");
	private JSONObject objectUser = new JSONObject();
	private JSONArray arrayMessages = new JSONArray();

	ClientThread(Socket s, String id, Map<User,Socket> liste, ArrayList<Groupe> listeGroupes, JSONArray jsonHistorique, JSONObject objectUser) throws IOException {
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
		this.jsonHistorique=jsonHistorique;
		this.objectUser = objectUser;
		if (!objectUser.isEmpty()){
			this.arrayMessages= (JSONArray) objectUser.get("listeMessages");
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

				} else if (line.equals("Afficher clients connectes")) { // si le client veut voir seulement les clients connectés
					String listeToPrint = "";
					if(!(listeClients.size() ==1)) {
						for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
							if (!entry.getKey().getPseudo().equals(identifiant) && entry.getKey().getEtat()) { // on teste si l'utilisateur est connecté
								listeToPrint += entry.getKey().getPseudo() + sautDeLigne;
							}

						}
					}else{
						listeToPrint="Aucune personne connectee.";
					}
					socOut.println(listeToPrint);
				} else if(line.equals("deconnexion")) {
					JSONObject toAdd = new JSONObject();
					toAdd.put("name", identifiant);
					toAdd.put("listeMessages",arrayMessages);
					objectUser.put("user", toAdd);
					jsonHistorique.add(objectUser);
					userActuel.setEtat(false);
					parse(); //Exporter le fichier JSON
				}
				else if(line.length()>=2 && line.substring(0, 2).equals("1:") && !line.substring(2, line.length()).equals("Revenir au menu")) {
					//le client a choisi quelqu'un a qui parler
					interlocuteur = line.substring(2,line.length());
				} else if(line.length()>=9 && line.substring(0, 9).equals("pour tous")) {
					interlocuteur = "tous";
				} else if(line.length()>=2 && line.substring(0, 2).equals("2:")) {
					interlocuteur = line.substring(2,line.length());
				} else {
					//DISCUSSION BASIQUE

					if(!interlocuteur.equals("tous")) {
						for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
							if (entry.getKey().getPseudo().equals(interlocuteur)) {
								socOutClients = new PrintStream(entry.getValue().getOutputStream());
								socOutClients.println(identifiant + ": " + line);
								fillJson(interlocuteur,line);
								break;
							}
						}
					} else if(interlocuteur.equals("tous")) {
						for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
							if (!entry.getKey().getPseudo().equals(identifiant)) {
								socOutClients = new PrintStream(entry.getValue().getOutputStream());
								socOutClients.println( identifiant + " : " + line);
								fillJson(entry.getKey().getPseudo(), line);
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

	   public void fillJson(String interlocuteur, String line){
		   JSONObject elementsMessage = new JSONObject();
		   elementsMessage.put("expediteur", identifiant);
		   elementsMessage.put("destinaire", interlocuteur);
		   elementsMessage.put("contenu", line);
		   JSONObject messageActuel= new JSONObject();
		   messageActuel.put("message",elementsMessage);
		   arrayMessages.add(messageActuel);
	   }

	   public void parse() throws IOException {
			file.write(jsonHistorique.toJSONString());
			file.flush();
	   }

  }

  
