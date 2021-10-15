import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Message {
    private String contenu;
    private long id;
    private String expediteur ;
    private String destinataire ;
    private static AtomicLong count = new AtomicLong();

    public Message(){}

    public Message(String expediteur, String content, String destinataire){
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.contenu=content;
        this.id = count.getAndIncrement();
    }

    public String getContenu(){ return contenu; }

}
