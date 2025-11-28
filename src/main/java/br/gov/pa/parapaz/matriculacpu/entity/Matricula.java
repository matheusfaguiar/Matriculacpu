package br.gov.pa.parapaz.matriculacpu.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso")
    private Curso curso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "aluno_aprovado")
    private Boolean alunoAprovado;
    
    @Column(name = "url_certificado", length = 200)
    private String urlCertificado;
    
    @Column(name = "efetivada")
    private boolean efetivada;

    @Column(name = "data_efetivacao")
    private LocalDateTime dataEfetivacao;
    
    // Construtores
    public Matricula() {}
    
    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // CORREÇÃO: Getter com nome correto (getAlunoAprovado)
    public Boolean getAlunoAprovado() {
        return alunoAprovado;
    }

    // CORREÇÃO: Setter aceitando Boolean (não boolean)
    public void setAlunoAprovado(Boolean alunoAprovado) {
        this.alunoAprovado = alunoAprovado;
    }

    public String getUrlCertificado() {
        return urlCertificado;
    }

    public void setUrlCertificado(String urlCertificado) {
        this.urlCertificado = urlCertificado;
    }

    public boolean isEfetivada() {
        return efetivada;
    }

    public void setEfetivada(boolean efetivada) {
        this.efetivada = efetivada;
    }

    public LocalDateTime getDataEfetivacao() {
        return dataEfetivacao;
    }

    public void setDataEfetivacao(LocalDateTime dataEfetivacao) {
        this.dataEfetivacao = dataEfetivacao;
    }

    // Método para verificar se tem certificado disponível
    public boolean isCertificadoDisponivel() {
        return this.alunoAprovado != null && 
               this.alunoAprovado && 
               this.urlCertificado != null && 
               !this.urlCertificado.trim().isEmpty();
    }

    // Método para verificar se está aguardando certificado
    public boolean isAguardandoCertificado() {
        return this.alunoAprovado != null && 
               this.alunoAprovado && 
               (this.urlCertificado == null || this.urlCertificado.trim().isEmpty());
    }

    //status da matrícula
    public String getStatus() {
        LocalDate hoje = LocalDate.now();
        
        // Matrícula pendente
        if (!this.efetivada) {
            return "PENDENTE";
        }
        
        // Curso não encontrado
        if (this.curso == null) {
            return "CURSO_NAO_ENCONTRADO";
        }
        
        // Matriculado (curso ainda não começou)
        if (this.curso.getDataInicio() != null && 
            this.curso.getDataInicio().isAfter(hoje)) {
            return "MATRICULADO";
        }
        
        // Em curso
        if (this.curso.getDataInicio() != null && 
            !this.curso.getDataInicio().isAfter(hoje) && // data início <= hoje
            (this.curso.getDataFim() == null || 
            !this.curso.getDataFim().isBefore(hoje))) { // data fim >= hoje
            return "EM_CURSO";
        }
        
        // Curso finalizado - verificar aprovação
        if (this.curso.getDataFim() != null && 
            this.curso.getDataFim().isBefore(hoje)) {
            
            // Aguardando avaliação (alunoAprovado é null)
            if (this.alunoAprovado == null) {
                return "AGUARDANDO_AVALIACAO";
            }
            
            // Aprovado
            if (Boolean.TRUE.equals(this.alunoAprovado)) {
                return "APROVADO";
            }
            
            // Reprovado
            if (Boolean.FALSE.equals(this.alunoAprovado)) {
                return "REPROVADO";
            }
        }
        
        return "INDEFINIDO";
    }

    // Método auxiliar para cores CSS
    public String getStatusCor() {
        return switch (getStatus()) {
            case "PENDENTE" -> "warning";
            case "MATRICULADO" -> "info";
            case "EM_CURSO" -> "primary";
            case "APROVADO" -> "success";
            case "REPROVADO" -> "danger";
            case "AGUARDANDO_AVALIACAO" -> "secondary";
            default -> "secondary";
        };
    }

    // Método auxiliar para ícones
    public String getStatusIcone() {
        return switch (getStatus()) {
            case "PENDENTE" -> "fas fa-clock";
            case "MATRICULADO" -> "fas fa-user-check";
            case "EM_CURSO" -> "fas fa-book-open";
            case "APROVADO" -> "fas fa-check";
            case "REPROVADO" -> "fas fa-times";
            case "AGUARDANDO_AVALIACAO" -> "fas fa-hourglass-half";
            default -> "fas fa-question";
        };
    }

    // Método auxiliar para texto amigável
    public String getStatusTexto() {
        return switch (getStatus()) {
            case "PENDENTE" -> "Pendente";
            case "MATRICULADO" -> "Matriculado";
            case "EM_CURSO" -> "Em curso";
            case "APROVADO" -> "Aprovado";
            case "REPROVADO" -> "Reprovado";
            case "AGUARDANDO_AVALIACAO" -> "Em avaliação";
            default -> "Status indefinido";
        };
    }
}