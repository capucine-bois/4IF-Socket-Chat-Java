import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Groupe {
    private ArrayList<User> listUsers = new ArrayList<User>();
    private String nom;
    private long id;
    private ArrayList<Message> messages = new ArrayList<Message>();
    private static AtomicLong count = new AtomicLong();

    public Groupe(){}

    public Groupe(User user, String nom){
        listUsers.add(user);
        this.nom=nom;
        this.id = count.getAndIncrement();
    }

    public void addUser(User user){
        listUsers.add(user);
    }

    public void deleteUser(User user){
        listUsers.remove(user);
    }

    public void addMessage(Message m){
        messages.add(m);
    }

    public String getNom(){ return nom; }

    public boolean isUserInThisGroup(User user) {
        boolean present = false;
        for (int i = 0; i < listUsers.size(); i++) {
            if (listUsers.get(i) == user) {
               present = true;
               break;
            }
        }
        return present;
    }
}
