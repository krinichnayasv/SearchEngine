package model;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table(name = "lemma"
        , indexes = { @Index(name = "unique_SL", columnList = "site_id, lemma", unique = true) }
)
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "lemma", length = 255, nullable = false)
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
  //  @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site site;


    public Lemma() {   }

    public Lemma(String lemma, int frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    public Lemma(int id, String lemma, int frequency, Site site) {
        this.id = id;
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }


}
