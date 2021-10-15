import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Message {
    private String contenu;
    private long id;
    private User expediteur = new User();
    private User destinataire = new User();
    private static AtomicLong count = new AtomicLong();

    public Message(){}

    public Message(User expediteur, String content, User destinataire){
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.contenu=content;
        this.id = count.getAndIncrement();
    }

    public String getContenu(){ return contenu; }

}
