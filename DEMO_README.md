# KPL AI Demo 项目打包说明

## 项目概述
这是一个基于Vue前端、Spring Boot后端和Python AI服务的KPL赛事分析平台。

## 完整Docker部署（推荐）
包含所有服务：前端、后端、AI服务。

### 1. 构建和运行
```bash
# 构建所有服务
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 2. 访问Demo
- 前端界面：http://localhost
- 后端API：http://localhost/api
- AI服务：http://localhost/ai

### 3. 环境变量
创建 `.env` 文件：
```
DEEPSEEK_API_KEY=your_api_key_here
```

### 4. 停止和清理
```bash
# 停止服务
docker-compose down

# 清理镜像
docker-compose down --rmi all
```

## 单体Docker部署（简化版）
只包含前端和后端，AI功能通过外部API调用。

### 1. 构建和运行
```bash
# 构建镜像
docker build -t kpl-demo .

# 启动容器
docker run -p 8080:8080 kpl-demo
```

### 2. 访问Demo
- 前端界面：http://localhost:8080

## 注意事项
- 首次构建可能需要较长时间
- 确保端口80、8080、8000未被占用
- 需要有效的DeepSeek API密钥
- 数据库默认使用H2

## 分享方式
- 将整个项目文件夹压缩分享
- 接收方只需安装Docker即可运行