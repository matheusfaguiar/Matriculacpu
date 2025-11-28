package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "questionario")
public class Questionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", nullable = false)
    private Matricula matricula;

    @NotNull(message = "Matrícula é obrigatória")
    private Integer matriculaId;

    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @Column(name = "esta_estudando")
    private Boolean estaEstudando;

    @Column(columnDefinition = "TEXT")
    private String motivoNaoEstudar;

    @Column(length = 80)
    private String nomeEscola;

    @Column(length = 20)
    private String serieEscolar;

    @Column(length = 20)
    private String turno;

    @Column(length = 20)
    private String tipoEscola;

    @Column(name = "fez_enem")
    private Boolean fezEnem;

    @Column(name = "etnia_cor", length = 20)
    private String etniaCor;

    @Column(length = 20)
    private String religiao;

    @Column(name = "participa_programa_social")
    private Boolean participaProgramaSocial;

    @Column(name = "qual_programa_social", length = 100)
    private String qualProgramaSocial;

    @Column(name = "possui_transtorno_deficiencia")
    private Boolean possuiTranstornoDeficiencia;

    @Column(name = "tipo_deficiencia", length = 100)
    private String tipoDeficiencia;

    @Column(name = "uso_medicacao_controlada")
    private Boolean usoMedicacaoControlada;

    @Column(columnDefinition = "TEXT")
    private String quaisMedicacoes;

    @Column(name = "acompanhado_ubs")
    private Boolean acompanhadoUbs;

    @Column(name = "tipo_acompanhamento", length = 255)
    private String tipoAcompanhamento;

    @Column(name = "como_soube_parapaz", length = 100)
    private String comoSoubeParapaz;

    @Column(name = "qtd_pessoas_familia")
    private Integer qtdPessoasFamilia;

    @Column(name = "renda_familiar", precision = 10, scale = 2)
    private String rendaFamiliar;

    @Column(name = "condicao_moradia", length = 100)
    private String condicaoMoradia;

    @Column(name = "tipo_moradia", length = 100)
    private String tipoMoradia;

    @Column(name = "numero_comodos")
    private Integer numeroComodos;

    @Column(name = "situacao_ocupacional_chefe", length = 100)
    private String situacaoOcupacionalChefe;

    @Column(name = "profissao_ocupacao", length = 50)
    private String profissaoOcupacao;

    @Column(name = "contato_urgencia", length = 50)
    private String contatoUrgencia;

    @Column(name = "nome_responsavel", length = 100)
    private String nomeResponsavel;

    @Column(name = "grau_parentesco_responsavel", length = 20)
    private String grauParentescoResponsavel;

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public void setMatricula(Matricula matricula) {
        this.matricula = matricula;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Boolean getEstaEstudando() {
        return estaEstudando;
    }

    public void setEstaEstudando(Boolean estaEstudando) {
        this.estaEstudando = estaEstudando;
    }

    public String getMotivoNaoEstudar() {
        return motivoNaoEstudar;
    }

    public void setMotivoNaoEstudar(String motivoNaoEstudar) {
        this.motivoNaoEstudar = motivoNaoEstudar;
    }

    public String getNomeEscola() {
        return nomeEscola;
    }

    public void setNomeEscola(String nomeEscola) {
        this.nomeEscola = nomeEscola;
    }

    public String getSerieEscolar() {
        return serieEscolar;
    }

    public void setSerieEscolar(String serieEscolar) {
        this.serieEscolar = serieEscolar;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getTipoEscola() {
        return tipoEscola;
    }

    public void setTipoEscola(String tipoEscola) {
        this.tipoEscola = tipoEscola;
    }

    public Boolean getFezEnem() {
        return fezEnem;
    }

    public void setFezEnem(Boolean fezEnem) {
        this.fezEnem = fezEnem;
    }

    public String getEtniaCor() {
        return etniaCor;
    }

    public void setEtniaCor(String etniaCor) {
        this.etniaCor = etniaCor;
    }

    public String getReligiao() {
        return religiao;
    }

    public void setReligiao(String religiao) {
        this.religiao = religiao;
    }

    public Boolean getParticipaProgramaSocial() {
        return participaProgramaSocial;
    }

    public void setParticipaProgramaSocial(Boolean participaProgramaSocial) {
        this.participaProgramaSocial = participaProgramaSocial;
    }

    public String getQualProgramaSocial() {
        return qualProgramaSocial;
    }

    public void setQualProgramaSocial(String qualProgramaSocial) {
        this.qualProgramaSocial = qualProgramaSocial;
    }

    public Boolean getPossuiTranstornoDeficiencia() {
        return possuiTranstornoDeficiencia;
    }

    public void setPossuiTranstornoDeficiencia(Boolean possuiTranstornoDeficiencia) {
        this.possuiTranstornoDeficiencia = possuiTranstornoDeficiencia;
    }

    public String getTipoDeficiencia() {
        return tipoDeficiencia;
    }

    public void setTipoDeficiencia(String tipoDeficiencia) {
        this.tipoDeficiencia = tipoDeficiencia;
    }

    public Boolean getUsoMedicacaoControlada() {
        return usoMedicacaoControlada;
    }

    public void setUsoMedicacaoControlada(Boolean usoMedicacaoControlada) {
        this.usoMedicacaoControlada = usoMedicacaoControlada;
    }

    public String getQuaisMedicacoes() {
        return quaisMedicacoes;
    }

    public void setQuaisMedicacoes(String quaisMedicacoes) {
        this.quaisMedicacoes = quaisMedicacoes;
    }

    public Boolean getAcompanhadoUbs() {
        return acompanhadoUbs;
    }

    public void setAcompanhadoUbs(Boolean acompanhadoUbs) {
        this.acompanhadoUbs = acompanhadoUbs;
    }

    public String getTipoAcompanhamento() {
        return tipoAcompanhamento;
    }

    public void setTipoAcompanhamento(String tipoAcompanhamento) {
        this.tipoAcompanhamento = tipoAcompanhamento;
    }

    public String getComoSoubeParapaz() {
        return comoSoubeParapaz;
    }

    public void setComoSoubeParapaz(String comoSoubeParapaz) {
        this.comoSoubeParapaz = comoSoubeParapaz;
    }

    public Integer getQtdPessoasFamilia() {
        return qtdPessoasFamilia;
    }

    public void setQtdPessoasFamilia(Integer qtdPessoasFamilia) {
        this.qtdPessoasFamilia = qtdPessoasFamilia;
    }

    public String getRendaFamiliar() {
        return rendaFamiliar;
    }

    public void setRendaFamiliar(String rendaFamiliar) {
        this.rendaFamiliar = rendaFamiliar;
    }

    public String getCondicaoMoradia() {
        return condicaoMoradia;
    }

    public void setCondicaoMoradia(String condicaoMoradia) {
        this.condicaoMoradia = condicaoMoradia;
    }

    public String getTipoMoradia() {
        return tipoMoradia;
    }

    public void setTipoMoradia(String tipoMoradia) {
        this.tipoMoradia = tipoMoradia;
    }

    public Integer getNumeroComodos() {
        return numeroComodos;
    }

    public void setNumeroComodos(Integer numeroComodos) {
        this.numeroComodos = numeroComodos;
    }

    public String getSituacaoOcupacionalChefe() {
        return situacaoOcupacionalChefe;
    }

    public void setSituacaoOcupacionalChefe(String situacaoOcupacionalChefe) {
        this.situacaoOcupacionalChefe = situacaoOcupacionalChefe;
    }

    public String getProfissaoOcupacao() {
        return profissaoOcupacao;
    }

    public void setProfissaoOcupacao(String profissaoOcupacao) {
        this.profissaoOcupacao = profissaoOcupacao;
    }

    public String getContatoUrgencia() {
        return contatoUrgencia;
    }

    public void setContatoUrgencia(String contatoUrgencia) {
        this.contatoUrgencia = contatoUrgencia;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public String getGrauParentescoResponsavel() {
        return grauParentescoResponsavel;
    }

    public void setGrauParentescoResponsavel(String grauParentescoResponsavel) {
        this.grauParentescoResponsavel = grauParentescoResponsavel;
    }

    public Integer getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Integer matriculaId) {
        this.matriculaId = matriculaId;
    }   
}
