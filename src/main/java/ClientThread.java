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
	private FileWriter file = new FileWriter("../../../historique.json");

	ClientThread(Socket s, String id, Map<User,Socket> liste, ArrayList<Groupe> listeGroupes, JSONArray jsonHistorique) throws IOException {
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
				} else if(line.length()>=12 && line.substring(0, 12).equals("Conversation")) {
					System.out.println("on veut essayer d'afficher la conv bon sang de bonsoir");
					String contact = line.substring(12,line.length());
					afficherConversation(contact);
				}
				else {
					//DISCUSSION BASIQUE

					if(!interlocuteur.equals("tous")) {
						for (Map.Entry<User, Socket> entry : listeClients.entrySet()) {
							if (entry.getKey().getPseudo().equals(interlocuteur)) {
								socOutClients = new PrintStream(entry.getValue().getOutputStream());
								socOutClients.println(identifiant + " : " + line);
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
		   elementsMessage.put("destinataire", interlocuteur);
		   elementsMessage.put("contenu", line);
		   JSONObject messageActuel= new JSONObject();
		   messageActuel.put("message",elementsMessage);
		   jsonHistorique.add(messageActuel);
	   }

	   public void parse() throws IOException {
			file.write(jsonHistorique.toJSONString());
			file.flush();
	   }


	   public void afficherConversation(String contact) {
		   for (int i = 0, size = jsonHistorique.size(); i < size; i++) {
			   JSONObject objectInArray = (JSONObject) jsonHistorique.get(i);
			   String destinataire = (String) objectInArray.get("destinataire");
			   String expediteur = (String) objectInArray.get("expediteur");
			   System.out.println("le destinaraire est " + destinataire + " et l'expediteur est " + expediteur);
			   if((contact.equals(destinataire) && identifiant.equals(expediteur)) || (contact.equals(expediteur) && identifiant.equals(destinataire))) {
				   System.out.println(expediteur + " : " + objectInArray.get("contenu"));
			   }
		   }
	   }

  }

  
