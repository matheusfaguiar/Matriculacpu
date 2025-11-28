package br.gov.pa.parapaz.matriculacpu.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "curso")
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_curso")
    private TipoCurso tipoCurso;
    
    @Column(name = "nome", length = 40)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lotacao")
    private Lotacao lotacao;
    
    @Column(name = "carga_horaria")
    private Integer cargaHoraria;
    
    @Column(name = "vagas")
    private Integer vagas;
    
    @Column(name = "data_inicio")
    private LocalDate dataInicio;
    
    @Column(name = "data_fim")
    private LocalDate dataFim;
    
    @OneToMany(mappedBy = "curso")
    private List<Matricula> matriculas;

    @Column(name = "instrutor", length = 100)
    private String instrutor;

    @Column(name = "matriz_curricular", length = 1574)
    private String matrizCurricular; // Mudar para camelCase

    @Column(name="horario", length = 100)
    private String horario;

    private boolean certificadosGerados;
    
    // Construtores
    public Curso() {}
    
    public Curso(TipoCurso tipoCurso, String nome, Lotacao lotacao, Integer cargaHoraria, 
                 Integer vagas, LocalDate dataInicio, LocalDate dataFim) {
        this.tipoCurso = tipoCurso;
        this.nome = nome;
        this.lotacao = lotacao;
        this.cargaHoraria = cargaHoraria;
        this.vagas = vagas;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoCurso getTipoCurso() {
        return tipoCurso;
    }

    public void setTipoCurso(TipoCurso tipoCurso) {
        this.tipoCurso = tipoCurso;
    }

    public String getNome() {
       return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Lotacao getLotacao() {
        return lotacao;
    }

    public void setLotacao(Lotacao lotacao) {
        this.lotacao = lotacao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria != null ? cargaHoraria : 0;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<Matricula> matriculas) {
        this.matriculas = matriculas;
    }

    public String getInstrutor() {
        return instrutor;
    }

    public void setInstrutor(String instrutor) {
        this.instrutor = instrutor;
    }

    // CORREÇÃO: Manter consistência entre campo e getter/setter
    public String getMatrizCurricular() {
        return matrizCurricular;
    }

    public void setMatrizCurricular(String matrizCurricular) {
        this.matrizCurricular = matrizCurricular;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getPeriodo(){
        return this.dataInicio.getDayOfMonth() + "/" + this.dataInicio.getMonthValue() + "/" + this.dataInicio.getYear() +
               " a " +
               this.dataFim.getDayOfMonth() + "/" + this.dataFim.getMonthValue() + "/" + this.dataFim.getYear();
    }

    public int getTotalMatriculas() {
        return this.matriculas != null ? this.matriculas.size() : 0;
    }

    public boolean isCertificadosGerados() {
        return certificadosGerados;
    }

    public void setCertificadosGerados(boolean certificadosGerados) {
        this.certificadosGerados = certificadosGerados;
    }

    public String getNomeExibicao(){
        if (tipoCurso == null || tipoCurso.getId() == null || tipoCurso.getId() == 99) {
            return nome != null ? nome : "";
        }
        return tipoCurso.getNome() + " - " + nome;
    }
}