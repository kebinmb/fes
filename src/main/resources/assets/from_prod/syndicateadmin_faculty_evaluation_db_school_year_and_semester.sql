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
-- Table structure for table `school_year_and_semester`
--

DROP TABLE IF EXISTS `school_year_and_semester`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school_year_and_semester` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `school_year` int(11) NOT NULL,
  `semester` varchar(255) NOT NULL,
  `status` varchar(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_year_semester` (`school_year`,`semester`),
  UNIQUE KEY `UKtphwmqade3nn386cstfn5nett` (`school_year`,`semester`),
  KEY `idx_school_year_semester_status` (`status`),
  KEY `idx_school_year_semester` (`school_year`,`semester`),
  CONSTRAINT `chk_semester` CHECK (`semester` in ('1st','2nd','summer')),
  CONSTRAINT `chk_status` CHECK (`status` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school_year_and_semester`
--

LOCK TABLES `school_year_and_semester` WRITE;
/*!40000 ALTER TABLE `school_year_and_semester` DISABLE KEYS */;
INSERT INTO `school_year_and_semester` VALUES (1,2023,'1st','INACTIVE','2026-05-18 07:31:06'),(2,2023,'2nd','INACTIVE','2026-05-18 07:31:06'),(3,2025,'2nd','ACTIVE','2026-05-17 23:32:28');
/*!40000 ALTER TABLE `school_year_and_semester` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-18 17:57:43
