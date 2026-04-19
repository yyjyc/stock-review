# 服务器部署指南

本文档提供了将股票复盘系统部署到服务器的完整指南。

## 目录

- [方式一：Docker 部署（推荐）](#方式一docker-部署推荐)
- [方式二：手动部署](#方式二手动部署)
- [常见问题](#常见问题)

***

## 方式一：Docker 部署（推荐）

### 1. 服务器要求

# 操作系统：Linux（推荐 Ubuntu 20.04+）或 Windows Server

- 内存：至少 2GB
- 磁盘：至少 10GB 可用空间
- 软件：Docker 和 Docker Compose

### 2. 安装 Docker

**Ubuntu/Debian:**

```bash
# 安装 Docker
curl -fsSL https://get.docker.com | sh

# 安装 Docker Compose
sudo apt install docker-compose-plugin

# 将当前用户加入 docker 组（可选）
sudo usermod -aG docker $USER
```

**CentOS/RHEL:**

```bash
sudo yum install -y docker docker-compose
sudo systemctl start docker
sudo systemctl enable docker
```

### 3. 部署步骤

```bash
# 1. 将项目上传到服务器（方式一：git clone）
git clone <your-repo-url> /opt/stock-review
cd /opt/stock-review/deploy

# 或方式二：使用 scp 上传
# scp -r ./stockBack0212 user@server:/opt/stock-review

# 2. 创建环境变量文件
cp .env.example .env

# 3. 编辑 .env 文件，设置数据库密码
vim .env
# 修改：MYSQL_ROOT_PASSWORD=你的安全密码

# 4. 运行部署脚本
chmod +x deploy.sh
./deploy.sh
```

### 4. 验证部署

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 测试后端 API
curl http://localhost:8080/api/health
```

### 5. 访问系统

- 前端界面：`http://服务器IP`
- 后端 API：`http://服务器IP:8080`

***

## 方式二：手动部署

### 1. 环境准备

**后端环境：**

- JDK 8
- MySQL 8.0
- Maven 3.6+

**前端环境：**

- Nginx
- Node.js 18+（仅构建时需要）

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE stock_review CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'stock_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON stock_review.* TO 'stock_user'@'%';
FLUSH PRIVILEGES;
```

```bash
# 导入数据库结构
mysql -u root -p stock_review < backend/src/main/resources/schema.sql
```

### 3. 后端部署

```bash
# 进入后端目录
cd backend

# 构建项目
mvn clean package -DskipTests

# 创建生产环境配置（可选：直接使用环境变量）
# 或创建 application-prod.yml

# 启动服务
java -jar target/stock-review-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="jdbc:mysql://localhost:3306/stock_review?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai" \
  --spring.datasource.username=root \
  --spring.datasource.password=your_password
```

**创建 Systemd 服务（Linux）：**

```bash
sudo vim /etc/systemd/system/stock-review.service
```

```ini
[Unit]
Description=Stock Review Backend
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/stock-review/backend
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/stock-review/backend/target/stock-review-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable stock-review
sudo systemctl start stock-review
```

### 4. 前端部署

```bash
# 构建前端
cd frontend
npm install
npm run build

# 将 dist 目录复制到 Nginx
sudo cp -r dist/* /var/www/stock-review/
```

**Nginx 配置：**

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或 IP
    
    root /var/www/stock-review;
    index index.html;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

```bash
# 测试配置
sudo nginx -t

# 重载配置
sudo nginx -s reload
```

***

## 安全配置

### 1. 防火墙设置

```bash
# Ubuntu UFW
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS Firewalld
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

### 2. HTTPS 配置（推荐）

使用 Let's Encrypt 免费证书：

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

***

## 常用运维命令

### Docker 部署

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f
docker-compose logs -f backend

# 重启服务
docker-compose restart

# 停止服务
docker-compose down

# 更新部署
git pull
docker-compose up -d --build

# 进入容器
docker exec -it stock-backend bash
docker exec -it stock-mysql mysql -u root -p
```

### 手动部署

```bash
# 查看后端状态
sudo systemctl status stock-review

# 重启后端
sudo systemctl restart stock-review

# 查看日志
journalctl -u stock-review -f

# 重载 Nginx
sudo nginx -s reload
```

***

## 常见问题

### 1. 容器启动失败

```bash
# 查看详细错误日志
docker-compose logs backend
docker-compose logs mysql
```

### 2. 数据库连接失败

- 检查 MySQL 容器是否正常启动
- 确认数据库密码配置正确
- 检查网络连接

### 3. 前端页面空白

- 检查 Nginx 配置是否正确
- 确认前端构建产物已正确部署
- 查看浏览器控制台错误

### 4. API 请求跨域

- 确认 Nginx 代理配置正确
- 检查后端 CORS 配置

***

## 文件结构

```
deploy/
├── docker-compose.yml    # Docker Compose 配置
├── Dockerfile.backend    # 后端 Docker 镜像配置
├── Dockerfile.frontend   # 前端 Docker 镜像配置
├── nginx.conf            # Nginx 配置
├── .env.example          # 环境变量示例
├── deploy.sh             # Linux 部署脚本
└── deploy.bat            # Windows 部署脚本
```

