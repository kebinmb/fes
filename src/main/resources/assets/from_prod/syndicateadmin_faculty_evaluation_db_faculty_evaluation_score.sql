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
-- Table structure for table `faculty_evaluation_score`
--

DROP TABLE IF EXISTS `faculty_evaluation_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `faculty_evaluation_score` (
  `faculty_evaluation_score_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `faculty_id` varchar(60) NOT NULL,
  `evaluator_id` varchar(15) NOT NULL,
  `class_code` varchar(20) NOT NULL,
  `semester` varchar(10) NOT NULL,
  `school_year` int(11) NOT NULL,
  `subject_code` varchar(255) NOT NULL,
  `year_level` varchar(255) NOT NULL,
  `comments_or_feedbacks` varchar(255) DEFAULT NULL,
  `management_of_teaching_and_learning_id` bigint(20) DEFAULT NULL,
  `content_knowledge_pedagogy_and_technology_id` bigint(20) DEFAULT NULL,
  `commitment_and_transparency_id` bigint(20) DEFAULT NULL,
  `overall_average_score` double DEFAULT NULL,
  `overall_interpretation` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `student_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`faculty_evaluation_score_id`),
  UNIQUE KEY `uq_evaluation` (`faculty_id`,`evaluator_id`,`class_code`,`semester`,`school_year`),
  UNIQUE KEY `UKe1opfm7d2je0i2l8eqripy0dx` (`faculty_id`,`student_id`,`class_code`,`semester`,`school_year`),
  KEY `fk_eval_mtl` (`management_of_teaching_and_learning_id`),
  KEY `fk_eval_ckpt` (`content_knowledge_pedagogy_and_technology_id`),
  KEY `fk_eval_ct` (`commitment_and_transparency_id`),
  KEY `idx_eval_faculty` (`faculty_id`),
  KEY `idx_eval_evaluator` (`evaluator_id`),
  KEY `idx_eval_class` (`class_code`),
  KEY `idx_eval_subject` (`subject_code`),
  KEY `idx_eval_school_year` (`school_year`),
  KEY `idx_eval_lookup` (`faculty_id`,`class_code`,`semester`,`school_year`),
  KEY `FKkhu9m35rorsjx8exiaxhv8m0r` (`student_id`),
  CONSTRAINT `FK5om4oe36dwe7k4tqxtdn2iha6` FOREIGN KEY (`faculty_id`) REFERENCES `primary_faculty` (`faculty_id`),
  CONSTRAINT `FKkhu9m35rorsjx8exiaxhv8m0r` FOREIGN KEY (`student_id`) REFERENCES `primary_student` (`student_id`),
  CONSTRAINT `fk_eval_ckpt` FOREIGN KEY (`content_knowledge_pedagogy_and_technology_id`) REFERENCES `content_knowledge_pedagogy_and_technology` (`content_knowledge_pedagogy_and_technology_id`),
  CONSTRAINT `fk_eval_ct` FOREIGN KEY (`commitment_and_transparency_id`) REFERENCES `commitment_and_transparency` (`commitment_and_transparency_id`),
  CONSTRAINT `fk_eval_mtl` FOREIGN KEY (`management_of_teaching_and_learning_id`) REFERENCES `management_of_teaching_and_learning` (`management_of_teaching_and_learning_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `faculty_evaluation_score`
--

LOCK TABLES `faculty_evaluation_score` WRITE;
/*!40000 ALTER TABLE `faculty_evaluation_score` DISABLE KEYS */;
/*!40000 ALTER TABLE `faculty_evaluation_score` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-18 17:57:41
