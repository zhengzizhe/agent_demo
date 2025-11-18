@echo off
REM ========================================
REM Agent Demo 项目启动脚本 (Windows)
REM ========================================

setlocal enabledelayedexpansion

set PROJECT_NAME=agent_demo
set DOCKER_COMPOSE_FILE=src\docs\dev-ops\docker-compose.yml
set DB_CONTAINER=vector_db
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=agent
set DB_USER=postgres
set APP_PORT=8080
set LOG_DIR=logs

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

set APP_LOG=%LOG_DIR%\app.log
set DB_LOG=%LOG_DIR%\db.log

REM 检查Docker是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker 未运行，请先启动 Docker Desktop
    exit /b 1
)

REM 等待数据库就绪
:wait_db
echo [INFO] 等待数据库启动...
docker exec %DB_CONTAINER% pg_isready -U %DB_USER% -d %DB_NAME% >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto wait_db
)
echo [SUCCESS] 数据库已就绪

REM 启动数据库
echo [INFO] 启动 PostgreSQL 数据库...
docker ps -a --format "{{.Names}}" | findstr /C:"%DB_CONTAINER%" >nul
if errorlevel 1 (
    echo [INFO] 创建并启动数据库容器...
    docker-compose -f "%DOCKER_COMPOSE_FILE%" up -d >> "%DB_LOG%" 2>&1
    goto wait_db
) else (
    docker ps --format "{{.Names}}" | findstr /C:"%DB_CONTAINER%" >nul
    if errorlevel 1 (
        echo [INFO] 启动已存在的数据库容器...
        docker start %DB_CONTAINER% >> "%DB_LOG%" 2>&1
        goto wait_db
    ) else (
        echo [WARNING] 数据库容器已在运行
    )
)

REM 检查应用是否运行
netstat -ano | findstr ":%APP_PORT%" >nul
if not errorlevel 1 (
    echo [WARNING] 应用已在运行 (端口 %APP_PORT%)
    goto :status
)

REM 启动应用
echo [INFO] 启动 Java 应用...
call mvn mn:run >> "%APP_LOG%" 2>&1

:status
echo.
echo [INFO] ========== 服务状态 ==========
docker ps --format "{{.Names}}" | findstr /C:"%DB_CONTAINER%" >nul
if not errorlevel 1 (
    echo [SUCCESS] 数据库: 运行中 (容器: %DB_CONTAINER%)
) else (
    echo [ERROR] 数据库: 未运行
)

netstat -ano | findstr ":%APP_PORT%" >nul
if not errorlevel 1 (
    echo [SUCCESS] 应用: 运行中 (端口: %APP_PORT%)
) else (
    echo [ERROR] 应用: 未运行
)

echo.
echo [INFO] 访问地址:
echo   - 应用: http://localhost:%APP_PORT%
echo   - 数据库: %DB_HOST%:%DB_PORT%
echo.

endlocal























