package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "lotacao")
public class Lotacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "nome", length = 200)
    private String nome;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_lotacao")
    private TipoLotacao tipoLotacao;
    
    @OneToMany(mappedBy = "lotacao")
    private List<Curso> cursos;

    @Column(name = "cidade", length = 32)
    private String cidade;

    @Column(name = "endereco", length = 255)
    private String endereco;
    
    // Construtores
    public Lotacao() {}
    
    public Lotacao(String nome, TipoLotacao tipoLotacao) {
        this.nome = nome;
        this.tipoLotacao = tipoLotacao;
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

    public TipoLotacao getTipoLotacao() {
        return tipoLotacao;
    }

    public void setTipoLotacao (TipoLotacao tipoLotacao) {
        this.tipoLotacao = tipoLotacao;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}