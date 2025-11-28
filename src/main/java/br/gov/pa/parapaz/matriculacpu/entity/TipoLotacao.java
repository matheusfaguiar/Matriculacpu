package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tipo_lotacao")
public class TipoLotacao {
    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "descricao", length = 100)
    private String descricao;
    
    @OneToMany(mappedBy = "tipoLotacao")
    private List<Lotacao> lotacoes;
    
    // Construtores
    public TipoLotacao() {}
    
    public TipoLotacao(String descricao) {
        this.descricao = descricao;
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<Lotacao> getLocais() {
        return lotacoes;
    }

    public void setLocais(List<Lotacao> lotacoes) {
        this.lotacoes = lotacoes;
    }
}