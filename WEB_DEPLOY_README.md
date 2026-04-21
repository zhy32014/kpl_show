# KPL项目网站托管指南

## 概述
除了Docker打包，您还可以将项目直接部署到云平台，实现网站托管。

您的项目包含三个主要部分：
- **前端** (`vue2/`)：Vue.js应用
- **Spring Boot后端** (`springboot/`)：Java后端服务
- **Python AI服务** (`AIandPrompt/kpl_ai_backend_deepseek/`)：基于FastAPI的AI接口

可以全部部署到云端，实现完整的在线Demo。

## 推荐方案：Vercel + Railway（完整部署）

### 1. 前端部署到Vercel
Vercel 适合静态前端应用，支持Vue.js自动构建。

#### 步骤：
1. 注册Vercel账号：https://vercel.com
2. 连接GitHub仓库（或上传代码）
3. 选择 `vue2` 文件夹作为根目录
4. Vercel会自动检测Vue项目并构建
5. 部署完成后获得前端URL（如：https://your-app.vercel.app）

#### 前端环境变量配置：
在Vercel项目设置中添加环境变量：
```
VUE_APP_API_BASE_URL=https://your-backend.railway.app/api
VUE_APP_AI_BASE_URL=https://your-ai.railway.app
```

### 2. Spring Boot后端部署到Railway
Railway 支持Java/Spring Boot应用。

#### 步骤：
1. 注册Railway账号：https://railway.app
2. 创建新项目，选择 "Deploy from GitHub"
3. 选择 `springboot` 文件夹
4. 配置环境变量：
   ```
   DEEPSEEK_API_KEY=your_api_key
   JWT_SECRET=kpl-esports-center-secret-key-2026-spring-boot-jwt-very-long-key
   ```
5. Railway会自动检测Java项目并部署
6. 获得后端URL（如：https://your-backend.railway.app）

### 3. Python AI服务部署到Railway
`AIandPrompt/kpl_ai_backend_deepseek/` 是独立的FastAPI AI服务，提供战术分析、教练建议、解说词生成。

#### 步骤：
1. 在Railway中创建另一个新项目
2. 选择 "Deploy from GitHub"，选择 `AIandPrompt/kpl_ai_backend_deepseek` 文件夹
3. Railway会检测Python项目，自动安装requirements.txt依赖
4. 配置环境变量：
   ```
   DEEPSEEK_API_KEY=your_api_key
   HOST=0.0.0.0
   PORT=8000
   ```
5. 部署完成后获得AI服务URL（如：https://your-ai.railway.app）

### 4. 配置前后端连接
在前端代码中，将API调用地址改为Railway后端URL：

```javascript
// vue2/src/api/request.js
const BASE_URL = process.env.NODE_ENV === 'production'
  ? 'https://your-backend.railway.app/api'
  : '/api'
```

对于AI服务调用：
```javascript
// vue2/src/api/backend.js
const aiRequest = axios.create({
  baseURL: process.env.VUE_APP_AI_BASE_URL || 'http://localhost:8000',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' }
})
```

### 5. 数据库配置

### 5. 数据库配置
Railway提供PostgreSQL数据库：
- 在Railway中添加PostgreSQL插件
- 更新 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

## 其他托管选项

### Heroku（备选）
1. 前端：部署到Heroku（需要buildpack）
2. 后端：Heroku支持Java应用

### Netlify + Heroku
- 前端：Netlify
- 后端：Heroku

### AWS/GCP/Azure
- 更灵活但配置复杂
- 适合生产环境

## 优势
- 无需本地运行，24/7在线
- 自动扩展
- 免费额度足够演示使用

## 注意事项
- 需要配置API密钥和数据库
- 免费计划有使用限制
- 首次部署可能需要调试