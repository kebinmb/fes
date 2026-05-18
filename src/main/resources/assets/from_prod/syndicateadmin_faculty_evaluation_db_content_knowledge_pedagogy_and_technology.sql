-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.161.177    Database: syndicateadmin_faculty_evaluation_db
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.17-MariaDB

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
-- Table structure for table `content_knowledge_pedagogy_and_technology`
--

DROP TABLE IF EXISTS `content_knowledge_pedagogy_and_technology`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content_knowledge_pedagogy_and_technology` (
  `content_knowledge_pedagogy_and_technology_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `faculty_id` varchar(60) DEFAULT NULL,
  `subject_knowledge` enum('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
  `content_simplification` enum('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
  `real_world_application` enum('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
  `technology_integration` enum('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
  `assessment_alignment` enum('ALWAYS_MANIFESTED','NEVER_OR_RARELY_MANIFESTED','OFTEN_MANIFESTED','SELDOM_MANIFESTED','SOMETIMES_MANIFESTED') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`content_knowledge_pedagogy_and_technology_id`),
  KEY `idx_ckpt_faculty` (`faculty_id`),
  CONSTRAINT `FK3vie8rqmffoq0a534hlfh4yuh` FOREIGN KEY (`faculty_id`) REFERENCES `primary_faculty` (`faculty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content_knowledge_pedagogy_and_technology`
--

LOCK TABLES `content_knowledge_pedagogy_and_technology` WRITE;
/*!40000 ALTER TABLE `content_knowledge_pedagogy_and_technology` DISABLE KEYS */;
/*!40000 ALTER TABLE `content_knowledge_pedagogy_and_technology` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-18 17:57:45
