import java.util.ArrayList;

public class Groupe {
    private String name;
    private ArrayList<User> membres;

    public Groupe(String name, ArrayList<User>  membres) {
        this.name = name;
        this.membres = membres;
    }

    public String getName(){  return name;  }

    public ArrayList<User> getMembres() { return membres; }

    public void addMember(User userToAdd){
        membres.add(userToAdd);
    }

}
