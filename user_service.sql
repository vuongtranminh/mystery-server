-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: user_service
-- ------------------------------------------------------
-- Server version	8.1.0

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
-- Table structure for table `tbl_refresh_token`
--

DROP TABLE IF EXISTS `tbl_refresh_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_refresh_token` (
  `id` varchar(250) NOT NULL,
  `refresh_token` varchar(250) NOT NULL,
  `expires_at` varchar(250) NOT NULL,
  `user_id` varchar(250) NOT NULL,
  `status` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_refresh_token`
--

LOCK TABLES `tbl_refresh_token` WRITE;
/*!40000 ALTER TABLE `tbl_refresh_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `tbl_refresh_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_user`
--

DROP TABLE IF EXISTS `tbl_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user` (
  `id` varchar(250) NOT NULL,
  `name` varchar(250) NOT NULL,
  `avt_url` varchar(250) DEFAULT NULL,
  `bio` varchar(250) DEFAULT NULL,
  `email` varchar(250) NOT NULL,
  `password` varchar(250) DEFAULT NULL,
  `verified` tinyint NOT NULL,
  `provider` int NOT NULL,
  `provider_id` varchar(250) DEFAULT NULL,
  `created_at` varchar(250) NOT NULL,
  `updated_at` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_user`
--

LOCK TABLES `tbl_user` WRITE;
/*!40000 ALTER TABLE `tbl_user` DISABLE KEYS */;
INSERT INTO `tbl_user` VALUES ('03248087-3549-440e-a72d-e5d3ffbff855','Tran Minh Vuong',NULL,NULL,'user1@gmail.com','$2a$10$VzcGDBdxeswH7IcPfY9iMuukdYV9caRH9qxVnjPqmU1T3vbVvo0Im',0,1,NULL,'2023-11-25T09:35:15.621175100Z','2023-11-25T09:35:15.621175100Z'),('1e005db4-ae17-4d16-8f79-7bf86d7a3ce0','Tran Minh Vuong',NULL,NULL,'user@gmail.com','$2a$10$.NcT9ykn/0Gkfoo2X/qsPOVs9vyAochz2LQrmVhiITJpY7AZ5GAF2',0,1,NULL,'2023-11-24T18:14:35.596085100Z','2023-11-24T18:14:35.596085100Z'),('8fe552d5-aca2-4258-8bf2-258c0d5a9aad','Vương Trần Minh','https://lh3.googleusercontent.com/a/ACg8ocKnRSL5AnBetx3kKYy6sXvgm6V0Xvmc-gjVjj9EbvrwSw=s96-c','','tranminhvuong.visedu@gmail.com',NULL,1,3,'103348227149081346395','2023-11-17T15:15:22.448197400Z','2023-11-17T15:15:22.448197400Z'),('c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','Vương Trần Minh','https://lh3.googleusercontent.com/a/ACg8ocKFNod4UJ1bDZuYGjCYEF3g3vMfCWdw5g6N-vyG6VkQkA=s96-c','','vuongtranminh.hn@gmail.com',NULL,1,3,'107154026591532393327','2023-11-19T13:24:36.048277Z','2023-11-19T13:24:36.048277Z');
/*!40000 ALTER TABLE `tbl_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_verification_credential`
--

DROP TABLE IF EXISTS `tbl_verification_credential`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_verification_credential` (
  `id` varchar(250) NOT NULL,
  `verification_token` varchar(250) NOT NULL,
  `verification_otp` varchar(250) NOT NULL,
  `expire_date` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_verification_credential`
--

LOCK TABLES `tbl_verification_credential` WRITE;
/*!40000 ALTER TABLE `tbl_verification_credential` DISABLE KEYS */;
INSERT INTO `tbl_verification_credential` VALUES ('03248087-3549-440e-a72d-e5d3ffbff855','c920ba27-1dbc-4bc7-a27c-75abf7e95462','891302','2023-11-26T09:35:15.720684400Z'),('1e005db4-ae17-4d16-8f79-7bf86d7a3ce0','cda7560d-cb74-42a6-90ae-91952748b421','626414','2023-11-25T18:14:44.564902Z');
/*!40000 ALTER TABLE `tbl_verification_credential` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-11-27  0:43:08
