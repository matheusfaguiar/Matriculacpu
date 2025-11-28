package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lotacao_tecnico")
public class LotacaoTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lotacao")
    private Lotacao lotacao;

    // Construtores
    public LotacaoTecnico() {
    }

    public LotacaoTecnico(Usuario usuario, Lotacao lotacao) {
        this.usuario = usuario;
        this.lotacao = lotacao;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Lotacao getLotacao() {
        return lotacao;
    }

    public void setLotacao(Lotacao lotacao) {
        this.lotacao = lotacao;
    }
}

