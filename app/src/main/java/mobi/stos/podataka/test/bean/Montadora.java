package mobi.stos.podataka.test.bean;


import java.io.Serializable;

import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.PrimaryKey;

/**
 * Created by links_000 on 19/04/2016.
 */
@Entity
public class Montadora implements Serializable {

    @PrimaryKey
    private int id;
    @Column(nullable = false, length = 50)
    private String nome;
    private boolean status;

    public Montadora() {
    }

    public Montadora(String nome, boolean status) {
        this.nome = nome;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
