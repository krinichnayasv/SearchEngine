package model;

import javax.persistence.*;

@Entity
@Table (name = "field")
public class Field {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "name", length = 255, nullable = false)
    private String name;

    @Column (name = "selector", length = 255, nullable = false)
    private String selector;

    @Column(name = "weight", nullable = false)
    private float weight;


    public Field() {   }

    public Field(String name, String selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
