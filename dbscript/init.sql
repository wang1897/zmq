CREATE TABLE `address_tx` (
  `address` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tx_hash` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` decimal(16,8) DEFAULT NULL,
  `in_out` tinyint(1) DEFAULT NULL,
  `is_token` tinyint(1) NOT NULL,
  `token_addr` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token_amount` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `spent_tx_id` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `spent_index` int(3) DEFAULT NULL,
  `spent_height` bigint(11) DEFAULT NULL,
  `tx_index` int(3) DEFAULT NULL,
  `asm` longtext COLLATE utf8mb4_unicode_ci,
  `hex` longtext COLLATE utf8mb4_unicode_ci,
  `address_txcol` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `token_from_addr` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token_logged` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique` (`address`,`tx_hash`,`in_out`,`tx_index`,`spent_tx_id`),
  KEY `index_token` (`is_token`,`token_addr`),
  KEY `index_tx_hash` (`tx_hash`),
  KEY `index_token_from_addr` (`is_token`,`token_logged`)
) ENGINE=InnoDB AUTO_INCREMENT=4995063 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `transaction` (
  `tx_id` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tx_raw` longtext COLLATE utf8mb4_unicode_ci,
  `time` datetime DEFAULT NULL,
  `block_hash` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `block_height` bigint(11) DEFAULT NULL,
  `size` int(5) DEFAULT NULL,
  `locktime` int(10) DEFAULT NULL,
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tx_id_UNIQUE` (`tx_id`),
  KEY `index_time` (`time`),
  KEY `index_block_height` (`block_height`)
) ENGINE=InnoDB AUTO_INCREMENT=897521 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
