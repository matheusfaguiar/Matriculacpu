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
*   PostgreSQL (ou outro banco de dados compatível)

### Passos para Instalação

1. Clone o repositório.
2. Configure um banco de dados MySQl conforme o arquivo BDStructure.sql
3. Forneça as variáveis de ambiente para o sistema
   2.a ${SPRING_PROFILES_ACTIVE:prod} (dev/prod)
   2.b$ {MYSQLHOST} (endereço de acesso ao banco de dados)
   2.c ${MYSQLPORT} (porta de acesso ao banco de dados
   2.d ${MYSQLDATABASE} (nome do banco de dados)
${MYSQLUSER} (usuário de acesso)
${MYSQLPASSWORD} (senha)
${GOOGLE_CLIENT_ID} (cliente Google/Firebase)
${GOOGLE_CLIENT_SECRET} (senha Google/Firebase)
${EMAIL_USERNAME} (email de envio de emails)
${EMAIL_APP_PASSWORD} (senha do email)
${SENDGRID_API_KEY} (chave de uso SendGrid)
${EMAIL_PROVIDER:sendgrid} (provedor de envio de emails)
${TINYMCE_API_KEY}  (chave de acesso ao TinyMCE)
(Verificar demais configurações em application.properties)
