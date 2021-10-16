public class User {
    private String pseudo;
    boolean connecte;

    public User(String pseudo) {
        this.pseudo = pseudo;
        this.connecte=false;
    }

    public String getPseudo(){  return pseudo;  }

    public boolean getEtat() {
        return connecte;
    }

    public void setEtat(boolean etat) {
        connecte = etat;
    }
}
