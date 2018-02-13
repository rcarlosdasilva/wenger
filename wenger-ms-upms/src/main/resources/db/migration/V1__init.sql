SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for upms_account
-- ----------------------------
CREATE TABLE `upms_account` (
  `id` bigint(20) NOT NULL,
  `contact_id` bigint(20) NOT NULL COMMENT '账号通过联系人表关联到机构',
  `role_id` bigint(20) DEFAULT NULL COMMENT '当前处于的角色（用户归属多个分组时使用）',
  `code` char(8) NOT NULL COMMENT '用户的唯一资源标识码',
  `type` char(3) NOT NULL DEFAULT 'USR' COMMENT '账号类型，取值：ADM 超管用户，CLT 客户，USR 普通用户',
  `username` char(20) DEFAULT NULL COMMENT '账号',
  `password` char(64) DEFAULT NULL COMMENT '密码（BCrypt）',
  `signet_series` char(64) DEFAULT NULL COMMENT 'Remember Me功能，序列标识',
  `signet_token` char(64) DEFAULT NULL COMMENT 'Remember Me功能，自动认证token',
  `time_of_scar` datetime DEFAULT NULL COMMENT 'Remember Me时间',
  `nickname` char(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `time_of_lastlogin` datetime NOT NULL DEFAULT '2000-01-01 00:00:00' COMMENT '最后一次登录时间',
  `count_of_login` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '登录次数',
  `flag_online` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '是否在线',
  `flag_abnormal` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '账号是否异常被锁定，默认否，一般在有非法行为或欠费之类的情况下导致账号异常，不可用',
  `flag_expired` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '用户账号是否已过期，默认否',
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `flag_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `time_create` datetime NOT NULL,
  `time_update` datetime NOT NULL,
  `who_create` bigint(20) NOT NULL,
  `who_update` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `contact_id` (`contact_id`),
  CONSTRAINT `upms_account_ibfk_1` FOREIGN KEY (`contact_id`) REFERENCES `upms_contact` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统账号（包含超管、客户、用户）';

-- ----------------------------
-- Table structure for upms_authority
-- ----------------------------
CREATE TABLE `upms_authority` (
  `id` bigint(20) NOT NULL,
  `type` char(3) NOT NULL COMMENT '权限类型，取值：MNU菜单展示权限，CPN页内组件展示权限，FET功能操作权限',
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='权限表';

-- ----------------------------
-- Table structure for upms_cluster
-- ----------------------------
CREATE TABLE `upms_cluster` (
  `id` bigint(20) NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `name` varchar(32) NOT NULL,
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `flag_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `time_create` datetime NOT NULL,
  `time_update` datetime NOT NULL,
  `who_create` bigint(20) NOT NULL,
  `who_update` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cluster_id_parent` (`parent_id`) USING BTREE,
  CONSTRAINT `upms_cluster_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `upms_cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户分组表';

-- ----------------------------
-- Table structure for upms_component
-- ----------------------------
CREATE TABLE `upms_component` (
  `id` bigint(11) NOT NULL,
  `feature_id` bigint(11) NOT NULL,
  `authority_id` bigint(20) NOT NULL,
  `digest` varchar(32) NOT NULL COMMENT '摘要，用于做权限判断',
  `description` varchar(255) DEFAULT NULL,
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `feature_id` (`feature_id`),
  KEY `authority_id` (`authority_id`),
  CONSTRAINT `upms_component_ibfk_1` FOREIGN KEY (`feature_id`) REFERENCES `upms_feature` (`id`),
  CONSTRAINT `upms_component_ibfk_2` FOREIGN KEY (`authority_id`) REFERENCES `upms_authority` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='页面组件表，描述页面中的主要组成部分';

-- ----------------------------
-- Table structure for upms_contact
-- ----------------------------
CREATE TABLE `upms_contact` (
  `id` bigint(20) NOT NULL,
  `name` char(6) DEFAULT NULL COMMENT '姓名',
  `name_en` varchar(32) DEFAULT NULL COMMENT '英文名（可兼容非中文名）',
  `title` char(3) NOT NULL DEFAULT 'NON' COMMENT '称谓，取值：NON 无， MRG 先生， MSG 女士， MIS 小姐',
  `mobile` char(11) DEFAULT NULL COMMENT '手机',
  `telephone` char(20) DEFAULT NULL COMMENT '座机',
  `mail` char(50) DEFAULT NULL COMMENT '邮件地址',
  `address` char(64) DEFAULT NULL COMMENT '联系地址',
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `flag_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `time_create` datetime NOT NULL,
  `time_update` datetime NOT NULL,
  `who_create` bigint(20) NOT NULL,
  `who_update` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='联系人信息表';

-- ----------------------------
-- Table structure for upms_feature
-- ----------------------------
CREATE TABLE `upms_feature` (
  `id` bigint(11) NOT NULL,
  `menu_id` bigint(20) NOT NULL,
  `authority_id` bigint(20) NOT NULL,
  `digest` varchar(32) NOT NULL COMMENT '摘要，用于做权限判断',
  `description` varchar(255) DEFAULT NULL,
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `authority_id` (`authority_id`),
  KEY `menu_id` (`menu_id`),
  CONSTRAINT `upms_feature_ibfk_1` FOREIGN KEY (`authority_id`) REFERENCES `upms_authority` (`id`),
  CONSTRAINT `upms_feature_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `upms_menu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='功能操作表';

-- ----------------------------
-- Table structure for upms_menu
-- ----------------------------
CREATE TABLE `upms_menu` (
  `id` bigint(11) NOT NULL,
  `parent_id` bigint(11) DEFAULT NULL,
  `authority_id` bigint(20) NOT NULL,
  `digest` varchar(32) NOT NULL COMMENT '摘要，用于做权限判断',
  `name` varchar(32) NOT NULL,
  `sort` tinyint(2) unsigned NOT NULL COMMENT '排序，从0开始，0最往前',
  `icon` varchar(128) NOT NULL,
  `url` varchar(128) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `digest` (`digest`),
  KEY `parent_id` (`parent_id`),
  KEY `authority_id` (`authority_id`),
  CONSTRAINT `upms_menu_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `upms_menu` (`id`),
  CONSTRAINT `upms_menu_ibfk_2` FOREIGN KEY (`authority_id`) REFERENCES `upms_authority` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='菜单表';

-- ----------------------------
-- Table structure for upms_privilege
-- ----------------------------
CREATE TABLE `upms_privilege` (
  `id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `cluster_id` bigint(20) NOT NULL,
  `authority_id` bigint(11) NOT NULL,
  `type` char(3) NOT NULL COMMENT '特权类型，取值：AFX附加额外权限，CUT削减已有权限',
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `flag_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `time_create` datetime NOT NULL,
  `time_update` datetime NOT NULL,
  `who_create` bigint(20) NOT NULL,
  `who_update` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `authority_id` (`authority_id`),
  KEY `account_id` (`account_id`),
  KEY `cluster_id` (`cluster_id`),
  CONSTRAINT `upms_privilege_ibfk_1` FOREIGN KEY (`authority_id`) REFERENCES `upms_authority` (`id`),
  CONSTRAINT `upms_privilege_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `upms_account` (`id`),
  CONSTRAINT `upms_privilege_ibfk_3` FOREIGN KEY (`cluster_id`) REFERENCES `upms_cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='特权表，标识除角色之外的额外附加或削减权限';

-- ----------------------------
-- Table structure for upms_role
-- ----------------------------
CREATE TABLE `upms_role` (
  `id` bigint(20) NOT NULL,
  `cluster_id` bigint(20) NOT NULL,
  `name` varchar(32) NOT NULL,
  `flag_disabled` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `flag_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `time_create` datetime NOT NULL,
  `time_update` datetime NOT NULL,
  `who_create` bigint(20) NOT NULL,
  `who_update` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cluster_id` (`cluster_id`),
  CONSTRAINT `upms_role_ibfk_1` FOREIGN KEY (`cluster_id`) REFERENCES `upms_cluster` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色表，角色只隶属于一个用户组下';

-- ----------------------------
-- Table structure for mid_account_cluster
-- ----------------------------
CREATE TABLE `mid_account_cluster` (
  `id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `cluster_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cluster_id` (`cluster_id`),
  KEY `account_id` (`account_id`),
  CONSTRAINT `mid_account_cluster_ibfk_1` FOREIGN KEY (`cluster_id`) REFERENCES `upms_cluster` (`id`),
  CONSTRAINT `mid_account_cluster_ibfk_2` FOREIGN KEY (`account_id`) REFERENCES `upms_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for mid_account_role
-- ----------------------------
CREATE TABLE `mid_account_role` (
  `id` bigint(20) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `account_id` (`account_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `mid_account_role_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `upms_account` (`id`),
  CONSTRAINT `mid_account_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `upms_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户在特定组下的角色（一个用户在一个组下只关联一个角色）';

-- ----------------------------
-- Table structure for mid_role_authority
-- ----------------------------
CREATE TABLE `mid_role_authority` (
  `id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `authority_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `authority_id` (`authority_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `mid_role_authority_ibfk_2` FOREIGN KEY (`authority_id`) REFERENCES `upms_authority` (`id`),
  CONSTRAINT `mid_role_authority_ibfk_3` FOREIGN KEY (`role_id`) REFERENCES `upms_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS=1;