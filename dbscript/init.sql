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

DROP TABLE block_info;
CREATE TABLE `block_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `block_hash` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `block_height` int(8) DEFAULT NULL,
  `block_size` int(8) DEFAULT NULL,
  `block_weight` int(8) DEFAULT NULL,
  `block_time` datetime DEFAULT NULL,
  `block_award` decimal(16,8) DEFAULT NULL,
  `block_merkle` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `block_miner` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `block_txcount` int(8) DEFAULT NULL,
  `block_preblockhash` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique` (`block_hash`,`block_preblockhash`),
  KEY `index_block_hash` (`block_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE tx_info;
CREATE TABLE `tx_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `tx_id` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
    `tx_vincount` int(8) DEFAULT NULL,
    `tx_vin` longtext COLLATE utf8mb4_unicode_ci,
    `tx_voutcount` int(8) DEFAULT NULL,
    `tx_vout` longtext COLLATE utf8mb4_unicode_ci,
  `time` datetime DEFAULT NULL,
  `block_hash` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `block_height` int(8) DEFAULT NULL,
  `size` int(8) DEFAULT NULL,
  `tx_fee` decimal(16,8) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `tx_id_UNIQUE` (`tx_id`),
  KEY `index_time` (`time`),
  KEY `index_block_height` (`block_height`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE address_info;
CREATE TABLE `address_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `block_height` int(8) DEFAULT NULL,
  `tx_hash` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `balance_change` decimal(24,8) DEFAULT NULL,
    `token_symbol` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    `token_address` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_tx_hash` (`tx_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


DROP TABLE token_info;
CREATE TABLE `token_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `symbol` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `contract_address` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `decimal` int(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

