CREATE TABLE `push_app_credentials` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` varchar(255) NOT NULL,
  `ios_private_key` blob DEFAULT NULL,
  `ios_team_id` varchar(255) DEFAULT NULL,
  `ios_key_id` varchar(255) DEFAULT NULL,
  `ios_bundle` varchar(255) DEFAULT NULL,
  `ios_environment` varchar(32) DEFAULT NULL,
  `android_private_key` blob DEFAULT NULL,
  `android_project_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `push_device_registration` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activation_id` varchar(37) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `app_id` bigint(20) NOT NULL,
  `platform` varchar(20) NOT NULL,
  `push_token` varchar(255) NOT NULL,
  `timestamp_last_registered` DATETIME NOT NULL,
  `is_active` int(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `push_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_registration_id` bigint(20) NOT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `activation_id` varchar(37) DEFAULT NULL,
  `is_silent` int(1) DEFAULT 0,
  `is_personal` int(1) DEFAULT 0,
  `message_body` text NOT NULL,
  `timestamp_created` DATETIME NOT NULL,
  `status` int(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `push_campaign` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_id` bigint(20) NOT NULL,
  `message` text NOT NULL,
  `is_sent` int(1) DEFAULT 0,
  `timestamp_created` DATETIME NOT NULL,
  `timestamp_sent` DATETIME DEFAULT NULL,
  `timestamp_completed` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `push_campaign_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_id` bigint(20) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `timestamp_created` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE `push_inbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inbox_id` VARCHAR(37),
  `user_id` VARCHAR(255) NOT NULL,
  `app_id` VARCHAR(255) NOT NULL,
  `subject` TEXT NOT NULL,
  `body` TEXT NOT NULL,
  `read` BOOLEAN DEFAULT false NOT NULL,
  `timestamp_created` TIMESTAMP NOT NULL,
  `timestamp_read` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

--
-- Indexes for better performance.
--

CREATE UNIQUE INDEX `push_app_cred_app` ON `push_app_credentials`(`app_id`);

CREATE INDEX `push_device_app_token` ON `push_device_registration`(`app_id`, `push_token`);
CREATE INDEX `push_device_user_app` ON `push_device_registration`(`user_id`, `app_id`);
CREATE UNIQUE INDEX `push_device_activation` ON `push_device_registration`(`activation_id`);
CREATE UNIQUE INDEX `push_device_activation_token` ON `push_device_registration`(`activation_id`, `push_token`);

CREATE INDEX `push_message_status` ON `push_message`(`status`);

CREATE INDEX `push_campaign_sent` ON `push_campaign`(`is_sent`);

CREATE INDEX `push_campaign_user_campaign` ON `push_campaign_user`(`campaign_id`, `user_id`);
CREATE INDEX `push_campaign_user_detail` ON `push_campaign_user`(`user_id`);

CREATE INDEX `push_inbox_id` ON `push_inbox` (`inbox_id`);
CREATE INDEX `push_inbox_user_app` ON `push_inbox` (`user_id`, `app_id`);
CREATE INDEX `push_inbox_user_read` ON `push_inbox` (`user_id`, `read`);
