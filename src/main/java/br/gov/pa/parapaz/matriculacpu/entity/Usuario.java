package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perfil")
    private Perfil perfil;

    @Column(name = "firebase_uid", length = 120)
    private String firebaseUid;

    @Column(name = "email", length = 60)
    private String email;

    @Column(name = "nome", length = 255)
    private String nome;

    @Column(name = "nome_social", length = 255)
    private String nomeSocial;

    @Column(name = "identidade_genero", length = 20)
    private String identidadeGenero;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_login", length = 45)
    private LocalDateTime ultimoLogin;

    @Column(name = "provider", length = 50)
    private String provider;

    @OneToMany(mappedBy = "usuario")
    private List<Matricula> matriculas;

    @Column(nullable = true)
    private String cpf;

    @Column(nullable = true)
    private String rg;

    @Column(name = "rg_orgao", nullable = true)
    private String rgOrgao;

    @Column(nullable = true)
    private String naturalidade;

    @Column(nullable = true)
    private String uf;

    @Column(name = "nome_mae", nullable = true)
    private String nomeMae;

    @Column(name = "nome_pai", nullable = true)
    private String nomePai;

    @Column(nullable = true)
    private String telefone;

    @Column(name = "data_nascimento", nullable = true)
    private LocalDate dataNascimento;

    @Column(name = "NIS", nullable = true)
    private String NIS;

    @Column(name = "CNS", nullable = true)
    private String CNS;

    @Column(nullable = false)
    private boolean cadastroCompleto = false;

    // Construtores
    public Usuario() {}

    public Usuario(Perfil perfil, String firebaseUid, String email, String nome) {
        this.perfil = perfil;
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.nome = nome;
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters e Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; 
    }

    public void setNome(String nome) { this.nome = nome; }

    public String getNomeSocial() { 
        return nomeSocial; 
    }
    public void setNomeSocial(String nomeSocial) { this.nomeSocial = nomeSocial; }

    public String getIdentidadeGenero() { return identidadeGenero; }
    public void setIdentidadeGenero(String identidadeGenero) { this.identidadeGenero = identidadeGenero; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public List<Matricula> getMatriculas() { return matriculas; }
    public void setMatriculas(List<Matricula> matriculas) { this.matriculas = matriculas; }


    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getRgOrgao() { return rgOrgao; }
    public void setRgOrgao(String rgOrgao) { this.rgOrgao = rgOrgao; }

    public String getNaturalidade() { return naturalidade; }
    public void setNaturalidade(String naturalidade) { this.naturalidade = naturalidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }

    public String getNomePai() { return nomePai; }
    public void setNomePai(String nomePai) { this.nomePai = nomePai; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public boolean isCadastroCompleto() { return cadastroCompleto; }
    public void setCadastroCompleto(boolean cadastroCompleto) { this.cadastroCompleto = cadastroCompleto; }

    public String getNIS() { return NIS; }
    public void setNIS(String NIS) { this.NIS = NIS; }

    public String getCNS() { return CNS; }
    public void setCNS(String CNS) { this.CNS = CNS; }

    public String getNomeExibicao(){
        if(this.nomeSocial != null && !this.nomeSocial.isEmpty()){
            return this.nomeSocial;
        }
        return this.nome;
    }
}
