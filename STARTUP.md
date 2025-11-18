# Agent Demo 启动脚本使用说明

## 快速开始

### Linux/macOS

```bash
# 启动所有服务（数据库 + 应用）
./start.sh start

# 查看服务状态
./start.sh status

# 查看日志
./start.sh logs app      # 应用日志
./start.sh logs db       # 数据库日志
./start.sh logs          # 所有日志

# 停止所有服务
./start.sh stop

# 重启服务
./start.sh restart

# 清理所有数据（包括数据库数据卷）
./start.sh clean

# 查看帮助
./start.sh help
```

### Windows

```cmd
REM 启动所有服务
start.bat

REM 注意：Windows版本功能较简单，主要用于启动服务
REM 停止服务请使用 Ctrl+C 或关闭命令行窗口
```

## 功能说明

### start.sh (Linux/macOS)

完整的项目启动脚本，包含以下功能：

1. **启动数据库**
   - 自动检查Docker是否运行
   - 启动PostgreSQL容器（如果不存在则创建）
   - 等待数据库就绪（最多60秒）
   - 自动执行SQL初始化脚本

2. **启动应用**
   - 检查Java和Maven是否安装
   - 自动编译项目（如果需要）
   - 后台启动Micronaut应用
   - 等待应用就绪（最多60秒）

3. **服务管理**
   - `start` - 启动所有服务
   - `stop` - 停止所有服务
   - `restart` - 重启所有服务
   - `status` - 查看服务状态
   - `logs` - 查看日志
   - `clean` - 清理所有数据

### start.bat (Windows)

简化版启动脚本，主要用于快速启动服务。

## 前置要求

- Docker Desktop（已安装并运行）
- Java 21+
- Maven 3.6+

## 服务端口

- **应用**: http://localhost:8080
- **数据库**: localhost:5432

## 日志文件

日志文件保存在 `logs/` 目录：
- `app.log` - 应用日志
- `db.log` - 数据库日志
- `app.pid` - 应用进程ID（Linux/macOS）

## 故障排查

### 数据库无法启动

1. 检查Docker是否运行：`docker info`
2. 检查端口是否被占用：`lsof -i :5432` (Linux/macOS) 或 `netstat -ano | findstr :5432` (Windows)
3. 查看数据库日志：`./start.sh logs db`

### 应用无法启动

1. 检查Java版本：`java -version`（需要Java 21+）
2. 检查Maven是否安装：`mvn -version`
3. 检查端口是否被占用：`lsof -i :8080` (Linux/macOS) 或 `netstat -ano | findstr :8080` (Windows)
4. 查看应用日志：`./start.sh logs app`
5. 手动编译：`mvn clean compile`

### 数据库连接失败

1. 确认数据库已启动：`./start.sh status`
2. 检查数据库配置：`src/main/resources/application.yml`
3. 确认数据库容器健康：`docker ps`

## 重新初始化数据库

如果需要重新初始化数据库（删除所有数据并重新执行SQL脚本）：

```bash
./start.sh clean
./start.sh start
```

**警告**: `clean` 命令会删除所有数据库数据，请谨慎使用！

## 开发模式

在开发时，你可能希望应用在前台运行以查看实时日志：

```bash
# 只启动数据库
docker-compose -f src/docs/dev-ops/docker-compose.yml up -d

# 在前台运行应用（可以看到实时日志）
mvn mn:run
```























