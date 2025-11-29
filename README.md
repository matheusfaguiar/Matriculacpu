# MatriculaCPU

## Visão Geral

MatriculaCPU é um sistema desenvolvido como parte de Atividade Extensionista para o curso de Análise e Desenvolvimento de Sistemas da faculdade IPOG. Ele visa atender às necessidades da Coordenação das Usinas da Fundação ParáPaz, melhorando o gerenciamento de matrículas nos cursos oferecidos e automatizando a criação de certificados para os alunos.

Desenvolvido utilizando Java 17 com Spring, Thymeleaf e Bootstrap.
Ferramentas adicionais: 
- Google (Firebase/OAuth2) para autenticação e envio de emails
- Sendgrid para envio externo de emails
- TinyMCE para editor de texto da matriz curricular dos certificados

## Principais Funcionalidades

*   Listagem e detalhes dos cursos oferecidos pela Fundação ParáPaz.
*   Gerenciamento de matrículas de alunos nos cursos.
*   Criação automatizada de certificados (emissão, download).
*   Interface administrativa para gerenciamento de cursos, alunos e matrículas.
*   Autenticação e autorização de usuários com diferentes níveis de acesso.
*   Suporte para múltiplos formatos de certificado.
*   Integração com serviço de e-mail para envio de certificados.

## Início Rápido

1.  **Pré-requisitos:** Java 17, Maven, Banco de dados MySQL
2.  **Execução:** Execute a aplicação pelo Spring Boot:
mvn install
mvn spring-boot:run

## Instalação

Para instalar e executar o MatriculaCPU, você precisa ter o Java 17 e o Maven instalados.

### Pré-requisitos

*   Java 17+ ([Download JDK 17](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html))
*   Maven ([Download Maven](https://maven.apache.org/download.cgi))
*   Banco de Dados MySQL

## Início Rápido

1.  **Pré-requisitos:** Java 17, Maven, Banco de dados MySQL
2.  **Execução:** Execute a aplicação pelo Spring Boot:
    ```bash
    export SPRING_PROFILES_ACTIVE=prod
    export MYSQLHOST=localhost
    export MYSQLPORT=3306
    export MYSQLDATABASE=matriculacpu
    export MYSQLUSER=<seu_usuario_mysql>
    export MYSQLPASSWORD=<sua_senha_mysql>
    export GOOGLE_CLIENT_ID=<seu_client_id_google>
    export GOOGLE_CLIENT_SECRET=<seu_client_secret_google>
    export EMAIL_USERNAME=<seu_email>
    export EMAIL_APP_PASSWORD=<sua_senha_email>
    export SENDGRID_API_KEY=<sua_api_key_sendgrid>
    export EMAIL_PROVIDER=sendgrid
    export TINYMCE_API_KEY=<sua_api_key_tinymce>
    ```
3.   A aplicação estará disponível em `http://localhost:8080` (ou outra porta configurada).

##  Configurações Adicionais

Verifique o arquivo `src/main/resources/application.properties` para outras configurações que podem ser necessárias, como configurações de porta e email.

Para emissão de certificados, criar a pasta `src/main/resources/templates-certificados` e adicionar os arquivos de imagem `frente.jpg` e `verso.jpg`. Consultar os arquivos `CertificadoService.java` e `PdfCertificadoGenerator.java` para mais detalhes.
