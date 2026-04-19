#!/bin/bash

set -e

echo "=========================================="
echo "  股票复盘系统 - 部署脚本"
echo "=========================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$SCRIPT_DIR"

if [ ! -f .env ]; then
    echo "警告: .env 文件不存在，正在从 .env.example 创建..."
    cp .env.example .env
    echo "请编辑 .env 文件设置数据库密码后重新运行部署脚本"
    exit 1
fi

echo "[1/4] 检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

echo "[2/4] 停止现有容器..."
docker-compose down 2>/dev/null || true

echo "[3/4] 构建并启动服务..."
docker-compose up -d --build

echo "[4/4] 等待服务启动..."
sleep 10

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "访问地址: http://localhost"
echo "后端API:  http://localhost:8080"
echo ""
echo "常用命令:"
echo "  查看日志:   docker-compose logs -f"
echo "  停止服务:   docker-compose down"
echo "  重启服务:   docker-compose restart"
echo ""
