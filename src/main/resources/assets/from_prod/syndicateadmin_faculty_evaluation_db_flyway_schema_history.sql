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
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1','core schema','SQL','V1__core_schema.sql',-1193782189,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:04',35,1),(2,'2','academic schema','SQL','V2__academic_schema.sql',-631190883,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:04',43,1),(3,'3','evaluation schema','SQL','V3__evaluation_schema.sql',-609034234,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:04',36,1),(4,'4','evaluation score','SQL','V4__evaluation_score.sql',-975056802,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:04',21,1),(5,'5','auth schema','SQL','V5__auth_schema.sql',-521790940,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:04',63,1),(6,'6','add indexes','SQL','V6__add_indexes.sql',-1525986297,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:05',653,1),(7,'7','user acounts','SQL','V7__user_acounts.sql',-1071770155,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:05',4,1),(8,'8','student class load indexes','SQL','V8__student_class_load_indexes.sql',-964244818,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:05',211,1),(9,'9','school year semester data','SQL','V9__school_year_semester_data.sql',-562724377,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',84,1),(10,'10','multi campus migration support','SQL','V10__multi_campus_migration_support.sql',-1059608245,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',562,1),(11,'11','add new college enums','SQL','V11__add_new_college_enums.sql',529572203,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',27,1),(12,'12','add programs in user accounts','SQL','V12__add_programs_in_user_accounts.sql',1349218330,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',16,1),(13,'13','add firstname lastname in user accounts','SQL','V13__add_firstname_lastname_in_user_accounts.sql',2067401962,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',55,1),(14,'14','add datasource column','SQL','V14__add_datasource_column.sql',542693004,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',51,1),(15,'15','add major column','SQL','V15__add_major_column.sql',641074814,'syndicateadmin_faculty_evaluation_db','2026-05-18 07:31:06',38,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-18 17:57:55
