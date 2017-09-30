CREATE TABLE `push_app_credential` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) DEFAULT NULL,
  `ios_private_key` blob,
  `ios_team_id` varchar(255) DEFAULT NULL,
  `ios_key_id` varchar(255) DEFAULT NULL,
  `ios_bundle` varchar(255) DEFAULT NULL,
  `android_server_key` text,
  `android_bundle` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `app_id_index` (`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `push_device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activation_id` varchar(37) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `app_id` bigint(20) DEFAULT NULL,
  `platform` varchar(20) DEFAULT NULL,
  `push_token` varchar(255) DEFAULT NULL,
  `timestamp_last_registered` TIMESTAMP NULL,
  `is_active` bigint(20) DEFAULT NULL,
  `encryption_key` text,
  `encryption_key_index` text,
  PRIMARY KEY (`id`),
  KEY `activation_id_index` (`activation_id`),
  KEY `user_id_index` (`user_id`),
  KEY `app_id_index` (`app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `push_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_id` bigint(20) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `activation_id` varchar(37) DEFAULT NULL,
  `silent` bigint(20) DEFAULT NULL,
  `personal` bigint(20) DEFAULT NULL,
  `encrypted` bigint(20) DEFAULT NULL,
  `message_body` text,
  `timestamp_created` TIMESTAMP NULL,
  `status` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `USER_ID_INDEX` (`user_id`,`activation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE push_campaign (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL,
  `message` TEXT NULL,
  `sent` INT(1) NULL,
  `timestamp_created` TIMESTAMP NULL,
  `timestamp_sent` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE push_campaign_user (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_id` bigint(20) NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
  `timestamp_created` TIMESTAMP NULL ,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id` ASC))
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

