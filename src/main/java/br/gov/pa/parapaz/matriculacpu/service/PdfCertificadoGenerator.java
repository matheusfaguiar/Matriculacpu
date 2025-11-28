package br.gov.pa.parapaz.matriculacpu.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class PdfCertificadoGenerator {

    private static final float PAGE_WIDTH = 842;  // A4 paisagem
    private static final float PAGE_HEIGHT = 595;

    /** Configuração de layout por modelo */
    private static final Map<String, LayoutConfig> layouts = new HashMap<>();

    static {
        layouts.put("usina", new LayoutConfig(
                new BoxConfig(267, 193, 480, 31), // nomeAluno
                new BoxConfig(251, 223, 472, 26), // nomeCurso
                new BoxConfig(190, 249, 259, 25), // lotacao
                new BoxConfig(650, 249, 40, 24),  // cargaHoraria
                new BoxConfig(154, 277, 350, 23), // periodo
                new BoxConfig(230, 324, 400, 23)  // dataExtenso
        ));
        layouts.put("unidade", layouts.get("usina"));
    }

    /**
     * Gera o certificado em PDF.
     * Se preview = true → retorna byte[].
     * Se preview = false → salva no caminho especificado.
     */
    public byte[] gerarCertificado(String modelo,
                                   String nomeAluno,
                                   String nomeCurso,
                                   String lotacao,
                                   String cargaHoraria,
                                   LocalDate dataInicio,
                                   LocalDate dataFim,
                                   String cidade,
                                   String matrizCurricular,
                                   String caminhoCompleto,
                                   boolean preview) throws Exception {

        LayoutConfig layout = layouts.getOrDefault(modelo, layouts.get("usina"));

        // USAR DATA URI EM VEZ DE File - CORREÇÃO PARA RAILWAY
        String frenteDataURI = getImageAsDataURI("templates-certificados/" + modelo + "/frente.jpg");
        String versoDataURI = getImageAsDataURI("templates-certificados/" + modelo + "/verso.jpg");

        // Monta HTML das duas páginas
        String htmlFrente = gerarHtmlFrente(nomeAluno, nomeCurso, lotacao, cargaHoraria,
                dataInicio, dataFim, layout, frenteDataURI);
        String htmlVerso = gerarHtmlVerso(matrizCurricular, versoDataURI);

        // Monta HTML final
        String htmlCompleto = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8" />
                <style>
                    @page {
                        size: """ + (int) PAGE_WIDTH + "pt " + (int) PAGE_HEIGHT + "pt;" +
                        """
                        margin: 0;
                    }
                    body {
                        margin: 0;
                        padding: 0;
                        width: """ + (int) PAGE_WIDTH + "pt;" +
                        """
                        height: """ + (int) PAGE_HEIGHT + "pt;" +
                        """
                        font-family: Arial, sans-serif;
                    }
                    .page {
                        width: """ + (int) PAGE_WIDTH + "pt;" +
                        """
                        height: """ + (int) PAGE_HEIGHT + "pt;" +
                        """
                        position: relative;
                        page-break-after: always;
                    }
                    .background-cover {
                        width: 100%;
                        height: 100%;
                        position: absolute;
                        top: 0;
                        left: 0;
                        background-size: cover;
                        background-repeat: no-repeat;
                        background-position: center;
                    }
                    .text-container {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                    }
                    .text-box {
                        position: absolute;
                        text-align: center;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #000060; /* Azul escuro */
                        font-weight: bold;
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                        white-space: nowrap;
                    }
                    .verso-content {
                        color: #000060;
                        font-family: Arial, sans-serif;
                    }
                    table {
                        border: none;
                        margin-top: 0.5em; /* Espaço acima de cada tabela */
                    }
                    /* Remove as linhas visíveis */
                    table, th, td {
                        border: none;
                    }
                </style>
            </head>
            <body>
            """ + htmlFrente + htmlVerso + """
            </body>
            </html>
            """;

        if (preview) {
            // Gera PDF em memória e retorna byte[]
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlCompleto, new File(".").toURI().toString());
                builder.toStream(baos);
                builder.run();
                return baos.toByteArray();
            }
        } else {
            // Gera e salva PDF no caminho especificado
            Path filePath = Paths.get(caminhoCompleto);
            Files.createDirectories(filePath.getParent()); // Garante que o diretório existe

            try (FileOutputStream os = new FileOutputStream(filePath.toFile())) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlCompleto, new File(".").toURI().toString());
                builder.toStream(os);
                builder.run();
            }

            return null;
        }
    }

    // -------------------------------------------------------------------------
    // NOVO MÉTODO PARA LIDAR COM IMAGENS NO RAILWAY
    // -------------------------------------------------------------------------

    private String getImageAsDataURI(String imagePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(imagePath);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            String mimeType = determineMimeType(imagePath);
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            return "data:" + mimeType + ";base64," + base64;
        }
    }

    private String determineMimeType(String filename) {
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg"; // default
    }

    // -------------------------------------------------------------------------
    // Métodos auxiliares de montagem de HTML
    // -------------------------------------------------------------------------

    private String gerarHtmlFrente(String nomeAluno, String nomeCurso, String lotacao,
                                   String cargaHoraria, LocalDate dataInicio, LocalDate dataFim,
                                   LayoutConfig layout, String fundoDataURI) {

        String periodo = dataInicio.format(DateTimeFormatter.ofPattern("dd/MM/yy")) +
                " a " + dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yy"));

        String dataExtenso = dataFim.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy"));
        dataExtenso = traduzMesesParaPortugues(dataExtenso);

        // fundoDataURI já vem como data URI
        String fundoUri = fundoDataURI;

        return String.format("""
            <div class="page">
                <div class="background-cover" style="background-image: url('%s');"></div>
                <div class="text-container">
                    %s
                    %s
                    %s
                    %s
                    %s
                    %s
                </div>
            </div>
            """,
                fundoUri,
                gerarCaixa(nomeAluno, layout.nomeAluno, 24, "nome-aluno"),
                gerarCaixa(nomeCurso, layout.nomeCurso, 20, "nome-curso"),
                gerarCaixa(lotacao, layout.lotacao, 20, "lotacao"),
                gerarCaixa(cargaHoraria, layout.cargaHoraria, 20, "carga-horaria"),
                gerarCaixa(periodo, layout.periodo, 18, "periodo"),
                gerarCaixa(dataExtenso, layout.dataExtenso, 18, "data-extenso")
        );
    }

    private String gerarHtmlVerso(String matrizCurricular, String fundoDataURI) {
        String conteudoLimpo = matrizCurricular
            .replaceAll("<col ([^>/]*?)>", "<col $1 />")
            .replaceAll("<br>", "<br />")
            .replaceAll("&nbsp;", "&#160;"); // opcional, se quiser padronizar

        conteudoLimpo = limparEntidadesHTML(conteudoLimpo);
        String fundoUri = fundoDataURI;
        float fontSizeVerso = ajustarFonteVerso(conteudoLimpo, 12f, 600f, 365f, 1.2f);

        return String.format("""
            <div class="page">
                <div class="background-cover" style="background-image: url('%s');"></div>
                <div class="verso-content" style="position:absolute; top:120pt; left:122pt; width:600pt; height:365pt; overflow:hidden; font-size:%.1fpt; line-height:1.2;">
                    %s
                </div>
            </div>
            """, fundoUri, fontSizeVerso, conteudoLimpo);
    }

    private String traduzMesesParaPortugues(String data) {
        return data.replace("January", "janeiro")
                .replace("February", "fevereiro")
                .replace("March", "março")
                .replace("April", "abril")
                .replace("May", "maio")
                .replace("June", "junho")
                .replace("July", "julho")
                .replace("August", "agosto")
                .replace("September", "setembro")
                .replace("October", "outubro")
                .replace("November", "novembro")
                .replace("December", "dezembro");
    }

    private String limparEntidadesHTML(String html) {
        if (html == null) return "";
        String resultado = html.replace("&nbsp;", "&#160;")
                .replace("&oacute;", "ó").replace("&aacute;", "á").replace("&eacute;", "é")
                .replace("&iacute;", "í").replace("&otilde;", "õ").replace("&atilde;", "ã")
                .replace("&ccedil;", "ç").replace("&uacute;", "ú").replace("&Uacute;", "Ú")
                .replace("&Aacute;", "Á").replace("&Eacute;", "É").replace("&Iacute;", "Í")
                .replace("&Oacute;", "Ó").replace("&Otilde;", "Õ").replace("&Atilde;", "Ã")
                .replace("&Ccedil;", "Ç")
                .replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&lt;", "<").replace("&gt;", ">");
        return resultado;
    }

    private String gerarCaixa(String texto, BoxConfig box, float fontSizeInicial, String classe) {
        float fontSize = ajustarFonte(texto, fontSizeInicial, box.width);
        return String.format("""
            <div class="text-box %s" style="top:%.0fpt; left:%.0fpt; width:%.0fpt; height:%.0fpt; font-size:%.0fpt;">
                %s
            </div>
            """, classe, box.y, box.x, box.width, box.height, fontSize, texto);
    }

    private float ajustarFonte(String texto, float fontSizeInicial, float larguraCaixa) {
        if (texto == null || texto.isEmpty()) return fontSizeInicial;
        int comprimento = texto.length();
        float ratio = larguraCaixa / (comprimento * fontSizeInicial * 0.5f);
        float fontSize = fontSizeInicial * Math.min(1.0f, ratio);
        return Math.max(fontSize, 8f);
    }

    private float ajustarFonteVerso(String texto, float fontSizeInicial, float larguraCaixa, float alturaCaixa, float lineHeight) {
        if (texto == null || texto.isEmpty()) return fontSizeInicial;

        float fontSize = fontSizeInicial;
        float charWidth = fontSize * 0.5f;
        int charsPorLinha = (int) (larguraCaixa / charWidth);
        int linhas = (int) Math.ceil((double) texto.length() / charsPorLinha);
        int maxLinhas = (int) Math.floor(alturaCaixa / (fontSize * lineHeight));

        while (linhas > maxLinhas && fontSize > 6f) {
            fontSize -= 0.5f;
            charWidth = fontSize * 0.5f;
            charsPorLinha = (int) (larguraCaixa / charWidth);
            linhas = (int) Math.ceil((double) texto.length() / charsPorLinha);
            maxLinhas = (int) Math.floor(alturaCaixa / (fontSize * lineHeight));
        }

        return fontSize;
    }

    /** Configurações internas */
    private static class LayoutConfig {
        BoxConfig nomeAluno, nomeCurso, lotacao, cargaHoraria, periodo, dataExtenso;
        LayoutConfig(BoxConfig nomeAluno, BoxConfig nomeCurso, BoxConfig lotacao,
                     BoxConfig cargaHoraria, BoxConfig periodo, BoxConfig dataExtenso) {
            this.nomeAluno = nomeAluno;
            this.nomeCurso = nomeCurso;
            this.lotacao = lotacao;
            this.cargaHoraria = cargaHoraria;
            this.periodo = periodo;
            this.dataExtenso = dataExtenso;
        }
    }

    private static class BoxConfig {
        float x, y, width, height;
        BoxConfig(float x, float y, float width, float height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
    }
}