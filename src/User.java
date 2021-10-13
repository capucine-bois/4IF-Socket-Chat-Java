import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class User {
    private String pseudo;
    private long id;
    private static AtomicLong count = new AtomicLong();
    private Groupe groupeActuel = new Groupe();
    private ArrayList<Groupe> myGroups = new ArrayList<Groupe>();
    boolean connecte;
    //private char genre;

    public User(String pseudo) {
        this.id = count.getAndIncrement();
        this.pseudo = pseudo;
        this.connecte=false;
    }

    public void addGroupe(Groupe groupe){
        myGroups.add(groupe);
    }

    public void setGroupeActuel(Groupe groupe){
        this.groupeActuel = groupe;
    }

    public String getPseudo(){  return pseudo;  }

    public void setStatut(boolean statut){connecte=statut;}
}
