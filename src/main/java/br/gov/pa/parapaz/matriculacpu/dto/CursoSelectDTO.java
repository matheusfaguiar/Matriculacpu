package br.gov.pa.parapaz.matriculacpu.dto;

public class CursoSelectDTO {
    private Integer id;
    private String text;

    public CursoSelectDTO(Integer id, String text) {
        this.id = id;
        this.text = text;
    }

    public Integer getId() { return id; }
    public String getText() { return text; }
}

