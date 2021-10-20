import java.util.ArrayList;

public class Groupe {
    private String name;
    private ArrayList<String> membres;

    public Groupe(String name, ArrayList<String>  membres) {
        this.name = name;
        this.membres = membres;
    }

    public String getName(){  return name;  }

    public ArrayList<String> getMembres() { return membres; }

    public void addMember(String userPseudoToAdd){
        membres.add(userPseudoToAdd);
    }

}
