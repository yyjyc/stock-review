@echo off
echo ========================================
echo    炒股复盘系统启动脚本
echo ========================================
echo.

echo [1/2] 启动后端服务...
cd backend
start cmd /k "mvn spring-boot:run"
cd ..

timeout /t 10 /nobreak > nul

echo [2/2] 启动前端服务...
cd frontend
start cmd /k "npm run dev"
cd ..

echo.
echo ========================================
echo 启动完成！
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:3000
echo ========================================
pause
