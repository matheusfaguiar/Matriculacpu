-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: matriculadb
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `curso`
--

DROP TABLE IF EXISTS `curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `curso` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_tipo_curso` int NOT NULL,
  `nome` varchar(40) DEFAULT NULL,
  `id_lotacao` int NOT NULL,
  `carga_horaria` int NOT NULL,
  `vagas` int DEFAULT NULL,
  `data_inicio` date NOT NULL,
  `data_fim` date NOT NULL,
  `instrutor` varchar(100) DEFAULT NULL,
  `matriz_curricular` varchar(1574) DEFAULT NULL,
  `horario` varchar(100) DEFAULT NULL,
  `certificados_gerados` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tipo_curso_idx` (`id_tipo_curso`),
  KEY `FKp2t3bbasxb5vs4rboq5hu1q0s` (`id_lotacao`),
  CONSTRAINT `fk_tipo_curso` FOREIGN KEY (`id_tipo_curso`) REFERENCES `tipo_curso` (`id`),
  CONSTRAINT `FKp2t3bbasxb5vs4rboq5hu1q0s` FOREIGN KEY (`id_lotacao`) REFERENCES `lotacao` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lotacao`
--

DROP TABLE IF EXISTS `lotacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lotacao` (
  `id` int NOT NULL,
  `nome` varchar(200) DEFAULT NULL,
  `id_tipo_lotacao` int NOT NULL,
  `cidade` varchar(32) DEFAULT NULL,
  `endereco` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tipo_local_idx` (`id_tipo_lotacao`),
  CONSTRAINT `fk_tipo_local` FOREIGN KEY (`id_tipo_lotacao`) REFERENCES `tipo_lotacao` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lotacao_tecnico`
--

DROP TABLE IF EXISTS `lotacao_tecnico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lotacao_tecnico` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int DEFAULT NULL,
  `id_lotacao` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_local_idx` (`id_lotacao`),
  KEY `fk_tecnico_idx` (`id_usuario`),
  CONSTRAINT `fk_lotacao` FOREIGN KEY (`id_lotacao`) REFERENCES `lotacao` (`id`),
  CONSTRAINT `fk_lotacao_tecnico` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `matricula`
--

DROP TABLE IF EXISTS `matricula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `matricula` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_curso` int NOT NULL,
  `id_usuario` int NOT NULL,
  `efetivada` bit(1) DEFAULT NULL,
  `aluno_aprovado` bit(1) DEFAULT NULL,
  `url_certificado` varchar(200) DEFAULT NULL,
  `data_efetivacao` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_curso_idx` (`id_curso`),
  KEY `fk_tecnico_idx` (`id_usuario`),
  CONSTRAINT `fk_curso` FOREIGN KEY (`id_curso`) REFERENCES `curso` (`id`),
  CONSTRAINT `fk_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `perfil`
--

DROP TABLE IF EXISTS `perfil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfil` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descricao` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `questionario`
--

DROP TABLE IF EXISTS `questionario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questionario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_matricula` int NOT NULL,
  `endereco` varchar(255) DEFAULT NULL,
  `bairro` varchar(255) DEFAULT NULL,
  `cep` varchar(255) NOT NULL,
  `cidade` varchar(255) DEFAULT NULL,
  `esta_estudando` tinyint(1) DEFAULT NULL,
  `motivo_nao_estudar` text,
  `nome_escola` varchar(80) DEFAULT NULL,
  `serie_escolar` varchar(20) DEFAULT NULL,
  `turno` varchar(20) DEFAULT NULL,
  `tipo_escola` varchar(20) DEFAULT NULL,
  `fez_enem` tinyint(1) DEFAULT NULL,
  `etnia_cor` varchar(20) DEFAULT NULL,
  `religiao` varchar(20) DEFAULT NULL,
  `participa_programa_social` tinyint(1) DEFAULT NULL,
  `qual_programa_social` varchar(100) DEFAULT NULL,
  `possui_transtorno_deficiencia` tinyint(1) DEFAULT NULL,
  `tipo_deficiencia` varchar(100) DEFAULT NULL,
  `uso_medicacao_controlada` tinyint(1) DEFAULT NULL,
  `quais_medicacoes` text,
  `acompanhado_ubs` tinyint(1) DEFAULT NULL,
  `tipo_acompanhamento` varchar(255) DEFAULT NULL,
  `como_soube_parapaz` varchar(100) DEFAULT NULL,
  `qtd_pessoas_familia` int DEFAULT NULL,
  `renda_familiar` varchar(255) DEFAULT NULL,
  `condicao_moradia` varchar(100) DEFAULT NULL,
  `tipo_moradia` varchar(100) DEFAULT NULL,
  `numero_comodos` int DEFAULT NULL,
  `situacao_ocupacional_chefe` varchar(100) DEFAULT NULL,
  `profissao_ocupacao` varchar(50) DEFAULT NULL,
  `contato_urgencia` varchar(50) DEFAULT NULL,
  `nome_responsavel` varchar(100) DEFAULT NULL,
  `grau_parentesco_responsavel` varchar(20) DEFAULT NULL,
  `matricula_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_matricula_idx` (`id_matricula`),
  CONSTRAINT `fk_matricula` FOREIGN KEY (`id_matricula`) REFERENCES `matricula` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tipo_curso`
--

DROP TABLE IF EXISTS `tipo_curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_curso` (
  `id` int NOT NULL,
  `nome` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tipo_lotacao`
--

DROP TABLE IF EXISTS `tipo_lotacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_lotacao` (
  `id` int NOT NULL,
  `descricao` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_perfil` int NOT NULL,
  `firebase_uid` varchar(120) DEFAULT NULL,
  `email` varchar(60) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `nome_social` varchar(255) DEFAULT NULL,
  `data_criacao` datetime NOT NULL,
  `ultimo_login` datetime(6) DEFAULT NULL,
  `provider` varchar(50) NOT NULL,
  `data_nascimento` date DEFAULT NULL,
  `cpf` varchar(255) DEFAULT NULL,
  `rg` varchar(255) DEFAULT NULL,
  `rg_orgao` varchar(255) DEFAULT NULL,
  `naturalidade` varchar(255) DEFAULT NULL,
  `uf` varchar(255) DEFAULT NULL,
  `nome_mae` varchar(255) DEFAULT NULL,
  `nome_pai` varchar(255) DEFAULT NULL,
  `telefone` varchar(255) DEFAULT NULL,
  `identidade_genero` varchar(20) DEFAULT NULL,
  `nis` varchar(255) DEFAULT NULL,
  `cns` varchar(255) DEFAULT NULL,
  `cadastro_completo` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `firebase_uid_UNIQUE` (`firebase_uid`),
  KEY `fk_perfil_idx` (`id_perfil`),
  CONSTRAINT `fk_perfil` FOREIGN KEY (`id_perfil`) REFERENCES `perfil` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-28 12:54:30
