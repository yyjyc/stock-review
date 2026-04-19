@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo   股票复盘系统 - 部署脚本 (Windows)
echo ==========================================

cd /d "%~dp0"

if not exist .env (
    echo 警告: .env 文件不存在，正在从 .env.example 创建...
    copy .env.example .env >nul
    echo 请编辑 .env 文件设置数据库密码后重新运行部署脚本
    pause
    exit /b 1
)

echo [1/4] 检查 Docker 环境...
where docker >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker 未安装，请先安装 Docker Desktop
    pause
    exit /b 1
)

docker --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker 未运行，请先启动 Docker Desktop
    pause
    exit /b 1
)

echo [2/4] 停止现有容器...
docker-compose down 2>nul

echo [3/4] 构建并启动服务...
docker-compose up -d --build
if errorlevel 1 (
    echo 错误: 部署失败
    pause
    exit /b 1
)

echo [4/4] 等待服务启动...
timeout /t 10 /nobreak >nul

echo.
echo ==========================================
echo   部署完成！
echo ==========================================
echo.
echo 访问地址: http://localhost
echo 后端API:  http://localhost:8080
echo.
echo 常用命令:
echo   查看日志:   docker-compose logs -f
echo   停止服务:   docker-compose down
echo   重启服务:   docker-compose restart
echo.
pause
