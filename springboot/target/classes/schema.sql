-- KPL电竞中心数据库初始化脚本
-- 执行前请先创建数据库：CREATE DATABASE kpl_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE kpl_db;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `email`       VARCHAR(100) UNIQUE COMMENT '邮箱',
    `nickname`    VARCHAR(50)  COMMENT '昵称',
    `avatar`      VARCHAR(500) COMMENT '头像URL',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER/ADMIN',
    `enabled`     TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 战队表
-- =============================================
CREATE TABLE IF NOT EXISTS `team` (
    `id`               INT AUTO_INCREMENT PRIMARY KEY,
    `name`             VARCHAR(50)  NOT NULL COMMENT '战队名',
    `full_name`        VARCHAR(100) COMMENT '英文全称',
    `short_name`       VARCHAR(20)  COMMENT '简称',
    `city`             VARCHAR(30)  COMMENT '所在城市',
    `stadium`          VARCHAR(100) COMMENT '主场名称',
    `logo`             VARCHAR(500) COMMENT 'Logo URL',
    `color`            VARCHAR(20)  COMMENT '主色调',
    `secondary_color`  VARCHAR(20)  COMMENT '副色调',
    `founded_year`     INT          COMMENT '成立年份',
    `coach`            VARCHAR(50)  COMMENT '教练',
    `is_champion`      TINYINT(1)   DEFAULT 0 COMMENT '是否当前冠军',
    `championship`     VARCHAR(200) COMMENT '冠军描述',
    `group_name`       VARCHAR(50)  COMMENT '所在组别',
    `spring_rank`      INT          COMMENT '春季赛排名',
    `wins`             INT          DEFAULT 0,
    `losses`           INT          DEFAULT 0,
    `win_rate`         DECIMAL(5,2) DEFAULT 0.00,
    `points`           INT          DEFAULT 0,
    `description`      TEXT         COMMENT '战队简介',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战队表';

-- =============================================
-- 3. 选手表
-- =============================================
CREATE TABLE IF NOT EXISTS `player` (
    `id`              INT AUTO_INCREMENT PRIMARY KEY,
    `team_id`         INT          NOT NULL COMMENT '所属战队ID',
    `name`            VARCHAR(50)  NOT NULL COMMENT '游戏ID',
    `real_name`       VARCHAR(50)  COMMENT '真实姓名',
    `position`        VARCHAR(20)  COMMENT '位置（对抗路/打野/中路/发育路/游走）',
    `role`            VARCHAR(20)  COMMENT '英文位置',
    `avatar`          VARCHAR(500) COMMENT '头像',
    `birth_date`      VARCHAR(20)  COMMENT '生日',
    `native_place`    VARCHAR(50)  COMMENT '籍贯',
    `boarding_time`   VARCHAR(20)  COMMENT '上场时间',
    `position_code`   INT          COMMENT '位置编码',
    `win_cr`          DECIMAL(5,2) DEFAULT 0.00 COMMENT '胜率(0-1)',
    `kda_rank`        INT          COMMENT 'KDA排名',
    `dmg_min`         INT          COMMENT '分均输出',
    `dmg_min_rank`    INT          COMMENT '分均输出排名',
    `team_dmg_min`    INT          COMMENT '团队分均输出',
    `team_dmg_min_rank` INT        COMMENT '团队分均输出排名',
    `signature_hero`  VARCHAR(50)  COMMENT '招牌英雄',
    `kd`              DECIMAL(4,2) DEFAULT 0.00 COMMENT 'KDA',
    `mvp_count`       INT          DEFAULT 0 COMMENT 'MVP次数',
    `games_played`    INT          DEFAULT 0 COMMENT '出场场次',
    `is_starter`      TINYINT(1)   DEFAULT 1 COMMENT '1=主力 0=替补',
    `is_fmvp`         TINYINT(1)   DEFAULT 0,
    `fmvp_tournament` VARCHAR(100) COMMENT 'FMVP赛事',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选手表';

-- =============================================
-- 4. 选手英雄池表
-- =============================================
CREATE TABLE IF NOT EXISTS `player_hero` (
    `id`        INT AUTO_INCREMENT PRIMARY KEY,
    `player_id` INT         NOT NULL,
    `hero_name` VARCHAR(50) NOT NULL COMMENT '英雄名',
    FOREIGN KEY (`player_id`) REFERENCES `player`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选手英雄池';

-- =============================================
-- 5. 成就表（战队）
-- =============================================
CREATE TABLE IF NOT EXISTS `team_achievement` (
    `id`          INT AUTO_INCREMENT PRIMARY KEY,
    `team_id`     INT          NOT NULL,
    `achievement` VARCHAR(200) NOT NULL COMMENT '成就描述',
    FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='战队成就';

-- =============================================
-- 6. 比赛表
-- =============================================
CREATE TABLE IF NOT EXISTS `match_record` (
    `id`          VARCHAR(50)  PRIMARY KEY COMMENT '比赛ID如m2026_final',
    `tournament`  VARCHAR(100) NOT NULL COMMENT '赛事名称',
    `stage`       VARCHAR(50)  COMMENT '赛事阶段',
    `match_date`  DATE         COMMENT '比赛日期',
    `match_time`  VARCHAR(10)  COMMENT '比赛时间',
    `venue`       VARCHAR(100) COMMENT '场馆',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'upcoming' COMMENT 'upcoming/live/finished',
    `team_a_id`   VARCHAR(100) NOT NULL,
    `team_a_name` VARCHAR(50)  COMMENT '冗余战队名',
    `team_a_score` INT         DEFAULT 0,
    `team_a_logo` VARCHAR(500) COMMENT '战队A Logo URL',
    `team_b_id`   VARCHAR(100) NOT NULL,
    `team_b_name` VARCHAR(50)  COMMENT '冗余战队名',
    `team_b_score` INT         DEFAULT 0,
    `team_b_logo` VARCHAR(500) COMMENT '战队B Logo URL',
    `replay_url`  VARCHAR(500) COMMENT '比赛回放链接',
    `result`      VARCHAR(50)  COMMENT '比赛结果描述',
    `format`      VARCHAR(20)  COMMENT 'BO3/BO5/BO7',
    `note`        VARCHAR(200) COMMENT '备注',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='比赛记录表';

-- =============================================
-- 7. 视频表
-- =============================================
CREATE TABLE IF NOT EXISTS `video` (
    `id`          VARCHAR(20)  PRIMARY KEY,
    `title`       VARCHAR(200) NOT NULL,
    `tournament`  VARCHAR(100) COMMENT '所属赛事',
    `video_date`  DATE,
    `duration`    VARCHAR(20)  COMMENT '时长如45:32',
    `views`       BIGINT       DEFAULT 0 COMMENT '播放量',
    `platform`    VARCHAR(30)  COMMENT '平台bilibili/youtube',
    `url`         VARCHAR(500) COMMENT '视频链接',
    `thumbnail`   VARCHAR(500) COMMENT '封面图',
    `description` TEXT,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频表';

-- =============================================
-- 8. 直播流表
-- =============================================
CREATE TABLE IF NOT EXISTS `live_stream` (
    `id`           VARCHAR(20)  PRIMARY KEY,
    `title`        VARCHAR(200) NOT NULL,
    `platform`     VARCHAR(30)  COMMENT '平台',
    `stream_url`   VARCHAR(500) COMMENT '直播间链接',
    `thumbnail`    VARCHAR(500) COMMENT '封面',
    `viewers`      INT          DEFAULT 0 COMMENT '当前观看人数',
    `is_live`      TINYINT(1)   DEFAULT 0,
    `match_id`     VARCHAR(50)  COMMENT '关联比赛ID',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播流';

-- =============================================
-- 9. 资讯表
-- =============================================
CREATE TABLE IF NOT EXISTS `news` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title`       VARCHAR(300) NOT NULL COMMENT '标题',
    `content`     LONGTEXT     COMMENT '正文',
    `summary`     VARCHAR(500) COMMENT '摘要',
    `author`      VARCHAR(50)  COMMENT '作者',
    `category`    VARCHAR(30)  COMMENT '分类：赛事/战队/选手/游戏',
    `tags`        VARCHAR(200) COMMENT 'JSON数组',
    `cover`       VARCHAR(500) COMMENT '封面图',
    `has_video`   TINYINT(1)   DEFAULT 0,
    `views`       INT          DEFAULT 0,
    `is_featured` TINYINT(1)   DEFAULT 0 COMMENT '是否头条',
    `status`      VARCHAR(20)  DEFAULT 'published' COMMENT 'draft/published',
    `gradient`    VARCHAR(200) COMMENT '封面渐变色',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯表';

-- =============================================
-- 10. 社区帖子表
-- =============================================
CREATE TABLE IF NOT EXISTS `post` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT       NOT NULL COMMENT '发帖用户',
    `content`    TEXT         NOT NULL COMMENT '帖子内容',
    `images`     TEXT         COMMENT '图片JSON数组',
    `likes`      INT          DEFAULT 0,
    `comments`   INT          DEFAULT 0,
    `topic`      VARCHAR(100) COMMENT '话题标签',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

-- =============================================
-- 11. 评论表
-- =============================================
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id`    BIGINT NOT NULL,
    `user_id`    BIGINT NOT NULL,
    `content`    TEXT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =============================================
-- 12. 点赞表（帖子）
-- =============================================
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id`    BIGINT   NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    FOREIGN KEY (`post_id`) REFERENCES `post`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞';

-- =============================================
-- 初始管理员账号（密码：admin123）
-- =============================================
INSERT IGNORE INTO `user` (`username`, `password`, `email`, `nickname`, `role`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6aNVK', 'admin@kpl.com', 'KPL管理员', 'ADMIN');
