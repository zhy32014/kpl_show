# KPL 电竞中心 —— 后端服务 (Spring Boot)

## 技术栈

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.2.4 |
| Spring Security | 6.x |
| Spring Data JPA | 3.x |
| MySQL | 8.0+ |
| JWT (jjwt) | 0.12.5 |
| Lombok | latest |

---

## 快速启动

### 1. 准备数据库

```sql
-- 登录 MySQL，执行：
CREATE DATABASE kpl_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行建表脚本：
```bash
mysql -u root -p kpl_db < src/main/resources/schema.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库密码：
```yaml
spring:
  datasource:
    password: your_mysql_password   # ← 改这里
```

### 3. 编译运行

```bash
# 进入后端目录
cd springboot

# 方式一：Maven 直接运行
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/kpl-backend-1.0.0.jar
```

后端服务启动后访问：`http://localhost:8080/api`

---

## API 接口文档

### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/auth/login` | 登录，返回JWT token | 公开 |
| POST | `/auth/register` | 注册新用户 | 公开 |
| GET  | `/auth/me` | 获取当前登录用户信息 | 需登录 |

**登录示例：**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### 战队模块 `/api/teams`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/teams` | 所有战队（按排名） | 公开 |
| GET | `/teams/{id}` | 战队详情+选手 | 公开 |
| GET | `/teams/search?q=` | 搜索战队 | 公开 |
| GET | `/teams/champion` | 当前冠军战队 | 公开 |
| GET | `/teams/top` | 排名前6战队 | 公开 |
| POST | `/teams` | 新增战队 | ADMIN |
| PUT | `/teams/{id}` | 修改战队 | ADMIN |
| DELETE | `/teams/{id}` | 删除战队 | ADMIN |

### 赛事模块 `/api/matches`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/matches` | 所有比赛 | 公开 |
| GET | `/matches/{id}` | 比赛详情 | 公开 |
| GET | `/matches/status/{status}` | 按状态筛选 upcoming/finished | 公开 |
| GET | `/matches/team/{teamId}` | 某战队所有比赛 | 公开 |
| GET | `/matches/recent?size=10` | 最近比赛 | 公开 |
| GET | `/matches/finals` | 历届总决赛 | 公开 |
| POST | `/matches` | 新增比赛 | ADMIN |
| PUT | `/matches/{id}` | 修改比赛 | ADMIN |
| PATCH | `/admin/matches/{id}/score` | 更新比分 | ADMIN |

### 资讯模块 `/api/news`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/news?page=0&size=10` | 资讯列表（分页） | 公开 |
| GET | `/news/{id}` | 资讯详情（自动+1阅读量） | 公开 |
| GET | `/news/featured` | 头条新闻 | 公开 |
| GET | `/news/category/{category}` | 按分类筛选 | 公开 |
| GET | `/news/search?q=` | 搜索资讯 | 公开 |
| POST | `/news` | 发布资讯 | ADMIN |
| PUT | `/news/{id}` | 修改资讯 | ADMIN |
| DELETE | `/news/{id}` | 删除资讯 | ADMIN |

### 社区模块 `/api/community`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/community/posts` | 帖子列表 | 公开 |
| POST | `/community/posts` | 发帖 | 需登录 |
| GET | `/community/posts/{id}/comments` | 获取评论 | 公开 |
| POST | `/community/posts/{id}/comments` | 评论 | 需登录 |
| POST | `/community/posts/{id}/like` | 点赞 | 需登录 |
| DELETE | `/community/posts/{id}` | 删除帖子 | 本人或ADMIN |

### 管理后台 `/api/admin`（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/dashboard` | 数据统计概览 |
| GET | `/admin/users` | 用户列表 |
| PUT | `/admin/users/{id}/role` | 修改用户角色 |
| PUT | `/admin/users/{id}/enabled` | 启用/禁用账号 |
| DELETE | `/admin/users/{id}` | 删除用户 |
| GET | `/admin/posts` | 所有帖子（含未审核） |
| DELETE | `/admin/posts/{id}` | 删除帖子 |

---

## 默认账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | ADMIN |

---

## 统一响应格式

```json
{
  "success": true,
  "message": "success",
  "data": { ... }
}
```

---

## 项目结构

```
springboot/
├── src/main/java/com/kpl/backend/
│   ├── KplBackendApplication.java   # 启动入口
│   ├── config/
│   │   └── SecurityConfig.java      # Security + CORS 配置
│   ├── controller/
│   │   ├── AuthController.java      # 登录/注册
│   │   ├── TeamController.java      # 战队 CRUD
│   │   ├── MatchController.java     # 赛事 CRUD
│   │   ├── NewsController.java      # 资讯 CRUD
│   │   ├── CommunityController.java # 社区互动
│   │   └── AdminController.java     # 管理后台
│   ├── entity/                      # JPA 实体类
│   ├── repository/                  # JPA Repository
│   ├── dto/                         # 请求/响应 DTO
│   └── security/                    # JWT 组件
└── src/main/resources/
    ├── application.yml              # 配置文件
    └── schema.sql                   # 建表脚本
```
