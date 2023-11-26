-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: discord_service
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
-- Table structure for table `tbl_channel`
--

DROP TABLE IF EXISTS `tbl_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_channel` (
  `id` varchar(250) NOT NULL,
  `name` varchar(250) NOT NULL,
  `type` int NOT NULL,
  `server_id` varchar(250) NOT NULL,
  `created_at` varchar(250) NOT NULL,
  `updated_at` varchar(250) NOT NULL,
  `updated_by` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_channel`
--

LOCK TABLES `tbl_channel` WRITE;
/*!40000 ALTER TABLE `tbl_channel` DISABLE KEYS */;
INSERT INTO `tbl_channel` VALUES ('07e04071-c689-41db-aeff-ca59a73e8b4f','general',1,'1719ca52-2166-4787-b120-2e8ec76c3b5d','2023-11-25T14:22:30.107989300Z','2023-11-25T14:22:30.107989300Z','1e005db4-ae17-4d16-8f79-7bf86d7a3ce0'),('584f174f-59f9-4c58-84d1-d0e57d8e8b07','general',1,'2a3f1196-794b-46a3-9c1c-eea71fe782ff','2023-11-17T15:16:08.855465500Z','2023-11-17T15:16:08.855465500Z','8fe552d5-aca2-4258-8bf2-258c0d5a9aad'),('947ecd86-534c-4a9a-9824-e9fffd6660d3','general',1,'955e3ec0-a5f0-4594-9a62-f480eb138dbd','2023-11-18T12:53:54.516554200Z','2023-11-18T12:53:54.516554200Z','8fe552d5-aca2-4258-8bf2-258c0d5a9aad');
/*!40000 ALTER TABLE `tbl_channel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_member`
--

DROP TABLE IF EXISTS `tbl_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_member` (
  `id` varchar(250) NOT NULL,
  `role` int NOT NULL,
  `profile_id` varchar(250) NOT NULL,
  `server_id` varchar(250) NOT NULL,
  `join_at` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_member`
--

LOCK TABLES `tbl_member` WRITE;
/*!40000 ALTER TABLE `tbl_member` DISABLE KEYS */;
INSERT INTO `tbl_member` VALUES ('607f17e2-7b4b-4fe1-8221-b379b659f331',3,'c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2a3f1196-794b-46a3-9c1c-eea71fe782ff','2023-11-26T16:36:40.378941500Z'),('7b89fd71-a217-4bf7-a7af-0279db0e39c8',1,'1e005db4-ae17-4d16-8f79-7bf86d7a3ce0','1719ca52-2166-4787-b120-2e8ec76c3b5d','2023-11-25T14:22:30.107989300Z'),('c3355cb1-f3f5-4995-b78d-a5e3317bf1d0',1,'8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2a3f1196-794b-46a3-9c1c-eea71fe782ff','2023-11-17T15:16:08.856466Z'),('f62eeda5-879a-4824-892c-c62a3e4d0817',1,'8fe552d5-aca2-4258-8bf2-258c0d5a9aad','955e3ec0-a5f0-4594-9a62-f480eb138dbd','2023-11-18T12:53:54.519118Z');
/*!40000 ALTER TABLE `tbl_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_message`
--

DROP TABLE IF EXISTS `tbl_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_message` (
  `id` varchar(250) NOT NULL,
  `content` varchar(250) DEFAULT NULL,
  `file_url` varchar(250) DEFAULT NULL,
  `channel_id` varchar(250) NOT NULL,
  `created_by` varchar(250) NOT NULL,
  `created_at` varchar(250) NOT NULL,
  `updated_at` varchar(250) NOT NULL,
  `deleted_at` varchar(250) DEFAULT NULL,
  `deleted_by` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_message`
--

LOCK TABLES `tbl_message` WRITE;
/*!40000 ALTER TABLE `tbl_message` DISABLE KEYS */;
INSERT INTO `tbl_message` VALUES ('0b8090a7-363e-49a7-883b-0f3421a6fa09','tôi tên là',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:47:00.429014900Z','2023-11-19T14:47:00.429014900Z',NULL,NULL),('0c05662f-8bdd-430a-8b89-d53f755682de','hi',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:26:25.106926100Z','2023-11-18T18:26:25.106926100Z',NULL,NULL),('0d5b1e8b-9f14-41de-b8d3-421a1e62375e','hello',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:46:12.112610800Z','2023-11-19T14:46:12.112610800Z',NULL,NULL),('317ed1d9-8060-438b-86ef-aeeeb464346d','hhhhh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:13:54.372732200Z','2023-11-18T19:13:54.372732200Z',NULL,NULL),('35dbf791-cb8c-4988-8750-257a5f2d77ec','hhh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:28:06.510908900Z','2023-11-18T18:28:06.510908900Z',NULL,NULL),('44912236-0c9e-4a0d-807e-b4ffbdef9939','fgh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:44:29.834575800Z','2023-11-18T18:44:29.834575800Z',NULL,NULL),('48c6bc12-254b-4bba-ac5a-a8afec2b0040','hhgfdfgh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:10:13.912923Z','2023-11-18T19:10:13.912923Z',NULL,NULL),('49f53834-1054-43bb-9564-72845e3e6254','hhh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:05:30.701276300Z','2023-11-18T19:05:30.701276300Z',NULL,NULL),('50982c05-6ffa-4a74-a315-a4f2c0e1104e','không tôi đang vui',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:47:14.820523800Z','2023-11-19T14:47:14.820523800Z',NULL,NULL),('5e357c69-c102-4d9f-8e80-a3699bb44238','cảm ơn bạn',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:47:26.732212300Z','2023-11-19T14:47:26.732212300Z',NULL,NULL),('692ad814-51c5-448b-b307-c0cb68fc9524','hehe',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:32:10.575801800Z','2023-11-18T18:32:10.575801800Z',NULL,NULL),('6a35bbbf-6fd3-452b-9584-ffcdc48c9252','xin chào bạn',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:47:22.223845200Z','2023-11-19T14:47:22.223845200Z',NULL,NULL),('7a67398b-32ff-4c2b-b45d-6760d8858bc8','ghfdjjfgh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:06:24.069998700Z','2023-11-18T19:06:24.069998700Z',NULL,NULL),('7e54749a-bc01-4ce2-b638-cf70c0e30f02','oke ban',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:46:06.544145700Z','2023-11-19T14:46:06.544145700Z',NULL,NULL),('81f4ea7b-ce2f-4f4e-930a-202ab32d1f85','bạn tên gì',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:46:54.726146900Z','2023-11-19T14:46:54.726146900Z',NULL,NULL),('84421506-1f2c-40ff-9001-b6880a267a90','hello\\',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-25T16:48:35.524674100Z','2023-11-25T16:48:35.524674100Z',NULL,NULL),('8d3f1f18-aac3-4dbd-814d-9147872569b8','fghjdjfhgjfg',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:44:52.515675Z','2023-11-18T18:44:52.515675Z',NULL,NULL),('99ed6d3e-586a-464f-9072-0c3563eee4b8','alo',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-25T16:48:47.856832500Z','2023-11-25T16:48:47.856832500Z',NULL,NULL),('9da75f4e-6900-4ff5-8a72-7b9fdb0a551c','hi',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T13:26:04.865674Z','2023-11-19T13:26:04.865674Z',NULL,NULL),('a1e17bc7-f765-4335-84c8-e139557d2e6e','ok',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T13:29:05.529246800Z','2023-11-19T13:29:05.529246800Z',NULL,NULL),('a441df6e-8178-4c61-bf8a-8bd486ee25fc','chào bạn nhé',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:46:41.619670700Z','2023-11-19T14:46:41.619670700Z',NULL,NULL),('a52e3706-7ee1-4404-a451-bf200cfd7e06','chào các bạn',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:15:33.947511900Z','2023-11-18T19:15:33.947511900Z',NULL,NULL),('ade053c3-9001-44ef-86ab-ab8baa3e72d3','hihi',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-25T16:48:42.189812100Z','2023-11-25T16:48:42.189812100Z',NULL,NULL),('b21604ae-3593-4c81-b503-266b759ba7ec','hi',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T13:26:24.609236400Z','2023-11-19T13:26:24.609236400Z',NULL,NULL),('b4f976cb-8218-4dbe-a437-9cb7fdab7c63','chao ban',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T14:46:01.494852100Z','2023-11-19T14:46:01.494852100Z',NULL,NULL),('d2d44d03-5396-4a17-adae-38a25f6d1fcd','hi',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T13:29:53.305766400Z','2023-11-19T13:29:53.305766400Z',NULL,NULL),('d9bc3a04-c112-4660-9218-428b3640b572','hello',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T16:35:24.221080500Z','2023-11-18T16:35:24.221080500Z',NULL,NULL),('dc7b4a0f-8e4e-403e-91b7-d214f099dda1','xin chao',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:45:41.265183200Z','2023-11-19T14:45:41.265183200Z',NULL,NULL),('e3af9aa5-fbe9-4fb4-b9a5-8ab64bc11573','ok bạn nhé',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:46:46.942354600Z','2023-11-19T14:46:46.942354600Z',NULL,NULL),('e8827911-8a3a-49f9-a354-a1ddfcc550f5','hh',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T13:32:42.856682200Z','2023-11-19T13:32:42.856682200Z',NULL,NULL),('e99b9fe9-dd9b-45ce-8486-baa629ca6e3a','gsdfsdgsdfg',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T18:47:24.529374900Z','2023-11-18T18:47:24.529374900Z',NULL,NULL),('e9dacac1-f73a-4b02-8e87-aba9cc7d4092','xin chào',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:15:28.908084300Z','2023-11-18T19:15:28.908084300Z',NULL,NULL),('f7fb6ebb-3a80-4dde-bbbe-81017b629b68','bạn đang buồn à',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','2023-11-19T14:47:08.957307900Z','2023-11-19T14:47:08.957307900Z',NULL,NULL),('fbfb990d-20b9-428a-920d-77d9ae9b0748','xin chaof',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-19T13:27:59.510475400Z','2023-11-19T13:27:59.510475400Z',NULL,NULL),('fd11cef0-ba2d-4ad8-a68c-9147048d42d2','hello các bạn',NULL,'584f174f-59f9-4c58-84d1-d0e57d8e8b07','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T19:15:40.378113200Z','2023-11-18T19:15:40.378113200Z',NULL,NULL);
/*!40000 ALTER TABLE `tbl_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_profile`
--

DROP TABLE IF EXISTS `tbl_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_profile` (
  `id` varchar(250) NOT NULL,
  `name` varchar(250) NOT NULL,
  `avt_url` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_profile`
--

LOCK TABLES `tbl_profile` WRITE;
/*!40000 ALTER TABLE `tbl_profile` DISABLE KEYS */;
INSERT INTO `tbl_profile` VALUES ('1e005db4-ae17-4d16-8f79-7bf86d7a3ce0','Tran Minh Vuong',NULL),('8fe552d5-aca2-4258-8bf2-258c0d5a9aad','Vương Trần Minh','https://lh3.googleusercontent.com/a/ACg8ocKnRSL5AnBetx3kKYy6sXvgm6V0Xvmc-gjVjj9EbvrwSw=s96-c'),('c2f308f9-3adb-4aa4-a0bb-e2a310e91a4a','Vương Trần Minh','https://lh3.googleusercontent.com/a/ACg8ocKFNod4UJ1bDZuYGjCYEF3g3vMfCWdw5g6N-vyG6VkQkA=s96-c');
/*!40000 ALTER TABLE `tbl_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbl_server`
--

DROP TABLE IF EXISTS `tbl_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_server` (
  `id` varchar(250) NOT NULL,
  `name` varchar(250) NOT NULL,
  `img_url` varchar(250) NOT NULL,
  `invite_code` varchar(250) NOT NULL,
  `created_by` varchar(250) NOT NULL,
  `created_at` varchar(250) NOT NULL,
  `updated_at` varchar(250) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `invite_code_UNIQUE` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_server`
--

LOCK TABLES `tbl_server` WRITE;
/*!40000 ALTER TABLE `tbl_server` DISABLE KEYS */;
INSERT INTO `tbl_server` VALUES ('1719ca52-2166-4787-b120-2e8ec76c3b5d','LOL','https://uploadthing.com/f/14f36964-beac-477f-b410-0adcc1f9528e-wvzcmn.jpg','1719ca52-2166-4787-b120-2e8ec76c3b5d','1e005db4-ae17-4d16-8f79-7bf86d7a3ce0','2023-11-25T14:22:30.103989800Z','2023-11-25T14:22:30.104993500Z'),('2a3f1196-794b-46a3-9c1c-eea71fe782ff','Picolo','https://uploadthing.com/f/21ed5765-963b-45a3-bf7c-e06080b6a35e-ly124v.jpg','2a3f1196-794b-46a3-9c1c-eea71fe782ff','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-17T15:16:08.853464200Z','2023-11-17T15:16:08.853464200Z'),('955e3ec0-a5f0-4594-9a62-f480eb138dbd','Dragon Ball','https://uploadthing.com/f/57fb3f46-4227-4715-861f-005486d591d6-9sumk6.jpg','955e3ec0-a5f0-4594-9a62-f480eb138dbd','8fe552d5-aca2-4258-8bf2-258c0d5a9aad','2023-11-18T12:53:54.516554200Z','2023-11-18T12:53:54.516554200Z');
/*!40000 ALTER TABLE `tbl_server` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-11-27  0:43:35
