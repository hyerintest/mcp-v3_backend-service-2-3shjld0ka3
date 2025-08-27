CREATE TABLE IF NOT EXISTS `sample` (
  `id` varchar(32) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `post` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
