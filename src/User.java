import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class User {
    private String pseudo;
    private long id;
    private static AtomicLong count = new AtomicLong();
    private Groupe groupeActuel = new Groupe();
    private ArrayList<Groupe> myGroups = new ArrayList<Groupe>();
    //private char genre;

    public User(String pseudo) {
        this.id = count.getAndIncrement();
        this.pseudo = pseudo;
    }

    public void addGroupe(Groupe groupe){
        myGroups.add(groupe);
    }

    public void setGroupeActuel(Groupe groupe){
        this.groupeActuel = groupe;
    }

    public String getPseudo(){  return pseudo;  }
}
