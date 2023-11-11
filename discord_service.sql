-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: discord
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_channel`
--

LOCK TABLES `tbl_channel` WRITE;
/*!40000 ALTER TABLE `tbl_channel` DISABLE KEYS */;
INSERT INTO `tbl_channel` VALUES ('2b21c09a-7bca-494f-895f-a2f77aebc2a4','general',1,'b8ae3f8e-3931-49f8-8982-df057c68eeab','2023-10-14T17:08:27.786781Z','2023-10-14T17:08:28.338326300Z','9fad9a7d-1a1b-47f2-9cea-66abb7719968'),('549a46aa-6cf8-47e7-a07c-7eaa837e4d23','Channel Text',1,'b8ae3f8e-3931-49f8-8982-df057c68eeab','2023-10-14T17:21:01.802014Z','2023-10-14T17:21:01.802014Z','9fad9a7d-1a1b-47f2-9cea-66abb7719968');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_member`
--

LOCK TABLES `tbl_member` WRITE;
/*!40000 ALTER TABLE `tbl_member` DISABLE KEYS */;
INSERT INTO `tbl_member` VALUES ('11a4279e-3acb-40c2-96b3-af026efa1d2c',1,'9fad9a7d-1a1b-47f2-9cea-66abb7719968','b8ae3f8e-3931-49f8-8982-df057c68eeab','2023-10-14T11:48:43.976648400Z');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_message`
--

LOCK TABLES `tbl_message` WRITE;
/*!40000 ALTER TABLE `tbl_message` DISABLE KEYS */;
INSERT INTO `tbl_message` VALUES ('01abc2e2-1372-4201-939a-6ff66fcaeeaa','sgh',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:45.358273200Z','2023-10-29T09:27:45.358273200Z',NULL,NULL),('052395ea-a043-43cd-ab48-199a9c60f328','ffsđf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:31.804291Z','2023-10-29T09:27:31.804291Z',NULL,NULL),('0696fb36-9f80-4cab-aabb-76c193030bf6','d',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:06.617701700Z','2023-10-29T09:28:06.617701700Z',NULL,NULL),('0ff33101-a972-4398-b545-71f7274dbae7','gf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:51.059850600Z','2023-10-29T09:29:51.059850600Z',NULL,NULL),('132a9f3e-0164-4396-8b64-d145bd3778d9','a',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:58.371313900Z','2023-10-29T09:27:58.371313900Z',NULL,NULL),('13611c95-6fbd-4d56-ab7f-1d2d8e847262','bdf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:04.788841400Z','2023-10-29T09:28:04.788841400Z',NULL,NULL),('1560ff4b-9888-4027-92e7-20cc014d157a','xin chao',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:26:44.870084500Z','2023-10-29T09:26:44.870084500Z',NULL,NULL),('16272ed1-62b6-4f77-8208-8483ac0a0222','df',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:11.025890800Z','2023-10-29T09:28:11.025890800Z',NULL,NULL),('1b9a92a3-7ba1-4280-aefc-bf48fd2b5cca','fdg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:49.548064100Z','2023-10-29T09:29:49.548064100Z',NULL,NULL),('1f200f64-48f3-46e6-ab52-de68c5d9d7f0','dshjgf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:52.161177500Z','2023-10-29T09:27:52.161177500Z',NULL,NULL),('21642bb2-d006-4951-8abd-d814e4f625e4','hds',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:41.536031400Z','2023-10-29T09:27:41.536031400Z',NULL,NULL),('220a7924-cc09-49a9-a018-6d0225163e1f','test edit',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:11.445107Z','2023-10-29T09:29:20.578574100Z',NULL,NULL),('2301e672-4d2d-4f2e-8509-e8c0f0ff7379','fd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:10.416720300Z','2023-10-29T09:28:10.416720300Z',NULL,NULL),('2655fb1d-eef9-40bd-a996-5c7b46029c75','gfdlghfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:37.692601600Z','2023-10-29T09:27:37.692601600Z',NULL,NULL),('29e62926-f0b0-406e-a0f7-67485e3f4f5e','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:16.232753400Z','2023-10-29T09:28:16.232753400Z',NULL,NULL),('2b7616be-b07f-4952-9c3f-dcc36244d717','jkhdfh',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:53.309966300Z','2023-10-29T09:27:53.309966300Z',NULL,NULL),('34c7d763-933b-4997-ab15-2bd6ff83ffcf','có chuyện gì khôn',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:29.558249200Z','2023-10-29T09:27:29.558249200Z',NULL,NULL),('3b5c24a2-512b-47a1-b086-dc41050f2bd7','ghgfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:40.764908700Z','2023-10-29T09:27:40.764908700Z',NULL,NULL),('4241af51-02de-4a7a-a16e-1f5f06778caf','g',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:13.430943Z','2023-10-29T09:28:13.430943Z',NULL,NULL),('45c95063-109b-47a6-bf76-e7c887dfb474',' ? ? ?',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:12:01.548018300Z','2023-10-29T09:12:01.548018300Z',NULL,NULL),('45d87408-d9cb-4832-8179-5b071161e5fa','gfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:48.135049900Z','2023-10-29T09:29:48.135049900Z',NULL,NULL),('46f02cb2-5df3-42a0-94cb-4e00b9a08cb4','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:12.517604600Z','2023-10-29T09:28:12.517604600Z',NULL,NULL),('474bc457-24af-45e4-9397-56f95d5b9205','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:15.994876900Z','2023-10-29T09:28:15.994876900Z',NULL,NULL),('475253b0-d78d-4652-b310-ae501dc58bce','v',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:00.892942900Z','2023-10-29T09:28:00.892942900Z',NULL,NULL),('47aac1e3-dd64-480f-8280-a8db2a11b157','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:16.485943400Z','2023-10-29T09:28:16.485943400Z',NULL,NULL),('4b21b9a9-9527-44ba-8395-b3046663f734','fdg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:46.715899800Z','2023-10-29T09:29:46.715899800Z',NULL,NULL),('4eb6ceda-efbe-41b8-98bc-2a01bcb44f70','gfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:48.564863200Z','2023-10-29T09:29:48.564863200Z',NULL,NULL),('51d9d2e5-72b4-42cf-b56b-c6a40b829ff6','fdg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:47.200880300Z','2023-10-29T09:29:47.200880300Z',NULL,NULL),('541b165b-8c21-4b1d-ab73-bd3e9bb020c1','chao các bạn',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:24.518059700Z','2023-10-29T09:27:24.518059700Z',NULL,NULL),('587eb19c-770d-46b8-8829-749b2128f072','fdgj',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:43.213950700Z','2023-10-29T09:27:43.213950700Z',NULL,NULL),('5e80ed98-bc03-47b8-9612-4f2d9f1489bb','fsfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:44.079553Z','2023-10-29T09:29:44.079553Z',NULL,NULL),('6559fe57-6846-4456-895e-9ff4be253cb9','fd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:05.273841300Z','2023-10-29T09:28:05.273841300Z',NULL,NULL),('6925358b-9922-4b30-801b-e8ab5d89cb21','g',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:39.858821700Z','2023-10-29T09:27:39.858821700Z',NULL,NULL),('69a5bbba-aed6-41b2-ab07-c9c1322f90df','a',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:58.925285Z','2023-10-29T09:27:58.925285Z',NULL,NULL),('6b6b0ab3-4d82-4942-b8dc-8c42d53f1507','jdfsjhfsgh',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:54.435175700Z','2023-10-29T09:27:54.435175700Z',NULL,NULL),('6e623dc5-70ef-4424-8c2d-13746ff3a0c1','dsfkjdhf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:33.764954400Z','2023-10-29T09:27:33.764954400Z',NULL,NULL),('72c3af75-a599-4aa4-a2c4-e57760626925','helloooooo',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-28T17:53:48.313712900Z','2023-10-28T18:10:18.013673600Z','2023-10-28T18:15:33.090316900Z','9fad9a7d-1a1b-47f2-9cea-66abb7719968'),('78477e63-5211-450e-b4c3-817c754efccf','ghjfdhgfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:34.655926100Z','2023-10-29T09:27:34.655926100Z',NULL,NULL),('7de36c7a-ff4d-4792-b4f7-302fd5937a38','ggbkb',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:02.406845500Z','2023-10-29T09:28:02.406845500Z',NULL,NULL),('7f88de3c-e8e2-4954-8109-b83f5a896c47','gg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:15.235510600Z','2023-10-29T09:28:15.235510600Z',NULL,NULL),('86f71ad4-9550-4dd0-9def-dd78dc0ecce3','g',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:13.987061800Z','2023-10-29T09:28:13.987061800Z',NULL,NULL),('950ed8f8-ddf0-4ed0-bdb2-a32de6db2135','gfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:57.796717600Z','2023-10-29T09:27:57.796717600Z',NULL,NULL),('97beca4c-ed5e-4173-9e24-e543748e99c3','g',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:00.242445700Z','2023-10-29T09:28:00.242445700Z',NULL,NULL),('9b5a9881-b261-462e-9e59-bd17aa58d8bd','hs',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:42.298998Z','2023-10-29T09:27:42.298998Z',NULL,NULL),('9f6d1594-5077-4b85-ae37-d898234f1af1','dfg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:46.219940800Z','2023-10-29T09:29:46.219940800Z',NULL,NULL),('9fe5ffda-a478-40bd-8aa5-1c5b552f6a00','g',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:14.599687200Z','2023-10-29T09:28:14.599687200Z',NULL,NULL),('a20cb16e-bec9-45f1-abba-23a81da320df','e',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:56.971148900Z','2023-10-29T09:27:56.971148900Z',NULL,NULL),('ab59359d-83ff-4380-a6cd-603cb71ddfa2','fdg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:49.003317500Z','2023-10-29T09:29:49.003317500Z',NULL,NULL),('ad68ac81-67e5-486b-9b13-b2f58ac5bf9f','df',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:09.690118500Z','2023-10-29T09:28:09.690118500Z',NULL,NULL),('adcb55f7-4a5e-40fb-9849-afe465c32e46','dfg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:44.690515200Z','2023-10-29T09:29:44.690515200Z',NULL,NULL),('afde4869-f29d-41f9-891c-748559dc333e','sfdhg;fdg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:35.610377600Z','2023-10-29T09:27:35.610377600Z',NULL,NULL),('b6d8a510-326c-49fb-a011-ecef13840bda','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:15.514851700Z','2023-10-29T09:28:15.514851700Z',NULL,NULL),('b72c28ec-a2bb-4aee-bf44-5288bcc10ecc','gjhgfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:38.652472200Z','2023-10-29T09:27:38.652472200Z',NULL,NULL),('bafc244e-8089-4da5-b142-c26f7c770c94','jsdfg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:50.838393200Z','2023-10-29T09:27:50.838393200Z',NULL,NULL),('bd685a34-17e7-4631-8caf-e7c2d6d509eb','vb',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:11.842874200Z','2023-10-29T09:28:11.842874200Z','2023-10-29T09:29:26.936760200Z','9fad9a7d-1a1b-47f2-9cea-66abb7719968'),('bfccd09e-f11e-49d2-bc25-4d0cbebcb288','sdjhf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:32.784922600Z','2023-10-29T09:27:32.784922600Z',NULL,NULL),('c20eb8bd-b669-491e-9c90-3fee4e00bff4','gn',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:03.215676200Z','2023-10-29T09:28:03.215676200Z',NULL,NULL),('c30ef6e8-da13-47c1-95b0-a2a19a9718c1','fdgj;dfs',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:48.404595800Z','2023-10-29T09:27:48.404595800Z',NULL,NULL),('c99f3bea-c761-4086-be6a-e7422e8b93dc','gfd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:29:45.778234400Z','2023-10-29T09:29:45.778234400Z',NULL,NULL),('ca479c34-6c96-4dc1-88c6-8ffc7b79fb6b','fdblgfdkjg',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:36.556033Z','2023-10-29T09:27:36.556033Z',NULL,NULL),('cacdd9da-eec5-406a-9635-fbc63acdc88d','n',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:03.586921600Z','2023-10-29T09:28:03.586921600Z',NULL,NULL),('cc2316e1-73f7-4d54-ab1f-cc0979c6bb50','b',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:01.436876Z','2023-10-29T09:28:01.436876Z',NULL,NULL),('d0e68065-6c57-43fd-aeac-7a315f96a509','jhgfjhd',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:55.574086500Z','2023-10-29T09:27:55.574086500Z',NULL,NULL),('d53061ed-7653-42fd-8178-50d329d482ed','sgdf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:39.599278300Z','2023-10-29T09:27:39.599278300Z',NULL,NULL),('dda723a1-c346-4435-be4b-457346a59099','egrth',NULL,'549a46aa-6cf8-47e7-a07c-7eaa837e4d23','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:12:21.298832200Z','2023-10-29T09:12:21.298832200Z',NULL,NULL),('e0e29622-eb97-400f-955f-8f20d3766229','d',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:06.112566200Z','2023-10-29T09:28:06.112566200Z',NULL,NULL),('e539f00a-70d5-44ee-ac50-256df7050e3c','f',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:15.756859900Z','2023-10-29T09:28:15.756859900Z',NULL,NULL),('e6b263ac-bab9-42fe-b667-1a230217ff3e','b',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:28:04.240701500Z','2023-10-29T09:28:04.240701500Z',NULL,NULL),('f17e26ff-d555-482b-a32a-a0b9dce6c5a0','fdkjgf',NULL,'2b21c09a-7bca-494f-895f-a2f77aebc2a4','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-29T09:27:49.584927700Z','2023-10-29T09:27:49.584927700Z',NULL,NULL);
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
  `avt_url` varchar(250) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_profile`
--

LOCK TABLES `tbl_profile` WRITE;
/*!40000 ALTER TABLE `tbl_profile` DISABLE KEYS */;
INSERT INTO `tbl_profile` VALUES ('9fad9a7d-1a1b-47f2-9cea-66abb7719968','Vuong Tran Minh','https://i.pinimg.com/564x/db/5f/8d/db5f8d6f7f28849f1325eaee7bad9bd2.jpg');
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbl_server`
--

LOCK TABLES `tbl_server` WRITE;
/*!40000 ALTER TABLE `tbl_server` DISABLE KEYS */;
INSERT INTO `tbl_server` VALUES ('b8ae3f8e-3931-49f8-8982-df057c68eeab','Dragon Ball','https://uploadthing.com/f/aca473ae-9ad3-48d2-b85a-fdfceaca8fec-9sumk6.jpg','b8ae3f8e-3931-49f8-8982-df057c68eeab','9fad9a7d-1a1b-47f2-9cea-66abb7719968','2023-10-14T11:48:43.971075600Z','2023-10-14T11:48:43.971075600Z');
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

-- Dump completed on 2023-10-30  0:24:27
