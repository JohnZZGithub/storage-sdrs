CREATE TABLE `dm_queue` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `data_storage_name` varchar(256) NOT NULL,
  `status` varchar(256) NOT NULL,
  `priority` int(10) unsigned NOT NULL DEFAULT 0,
  `data_storage_root` varchar(256) NOT NULL,
  `retention_job_id` int(10) unsigned,
  `number_of_retry` int(10) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
