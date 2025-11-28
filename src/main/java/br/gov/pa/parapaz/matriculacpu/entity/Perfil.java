package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "perfil")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "descricao", length = 100)
    private String descricao;
    
    @OneToMany(mappedBy = "perfil")
    private List<Usuario> usuarios;
    
    // Construtores
    public Perfil() {}
    
    public Perfil(String descricao) {
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

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}