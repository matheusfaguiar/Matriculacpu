package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "tipo_curso")
public class TipoCurso {
    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "nome", length = 40)
    private String nome;
    
    @OneToMany(mappedBy = "tipoCurso")
    private List<Curso> cursos;
    
    // Construtores
    public TipoCurso() {}
    
    public TipoCurso(String nome) {
        this.nome = nome;
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }
}