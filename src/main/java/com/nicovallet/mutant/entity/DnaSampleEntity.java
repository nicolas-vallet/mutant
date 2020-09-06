package com.nicovallet.mutant.entity;

import com.nicovallet.mutant.entity.support.StringArrayToClobConverter;

import javax.persistence.*;

@Entity
@Table(name = "DNA_SAMPLE")
public class DnaSampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Lob
    @Column(name = "DNA", nullable = false)
    @Convert(converter = StringArrayToClobConverter.class)
    private String[] dna;

    @Column(name = "HASH", length = 40, unique = true)
    private String hash;

    @Column(name = "IS_MUTANT", nullable = false)
    private boolean isMutant;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String[] getDna() {
        return dna;
    }

    public void setDna(String[] dna) {
        this.dna = dna;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isMutant() {
        return isMutant;
    }

    public void setMutant(boolean mutant) {
        isMutant = mutant;
    }
}
