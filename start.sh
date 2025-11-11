#!/bin/bash

# ========================================
# Agent Demo 项目启动脚本
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目配置
PROJECT_NAME="agent_demo"
DOCKER_COMPOSE_FILE="src/docs/dev-ops/docker-compose.yml"
DB_CONTAINER="vector_db"
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="agent"
DB_USER="postgres"
DB_PASSWORD="postgres"
APP_PORT="8080"

# 日志文件
LOG_DIR="logs"
APP_LOG="$LOG_DIR/app.log"
DB_LOG="$LOG_DIR/db.log"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        print_error "$1 未安装，请先安装 $1"
        exit 1
    fi
}

# 检查Docker是否运行
check_docker() {
    if ! docker info &> /dev/null; then
        print_error "Docker 未运行，请先启动 Docker"
        exit 1
    fi
}

# 等待数据库就绪
wait_for_db() {
    print_info "等待数据库启动..."
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" &> /dev/null; then
            print_success "数据库已就绪"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    print_error "数据库启动超时"
    return 1
}

# 启动数据库
start_database() {
    print_info "启动 PostgreSQL 数据库..."
    
    if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
        if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
            print_warning "数据库容器已在运行"
        else
            print_info "启动已存在的数据库容器..."
            docker start "$DB_CONTAINER" > "$DB_LOG" 2>&1
        fi
    else
        print_info "创建并启动数据库容器..."
        docker-compose -f "$DOCKER_COMPOSE_FILE" up -d >> "$DB_LOG" 2>&1
    fi
    
    wait_for_db
}

# 停止数据库
stop_database() {
    print_info "停止 PostgreSQL 数据库..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down >> "$DB_LOG" 2>&1
    print_success "数据库已停止"
}

# 检查Java应用是否运行
is_app_running() {
    if lsof -Pi :$APP_PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0
    else
        return 1
    fi
}

# 启动Java应用
start_application() {
    if is_app_running; then
        print_warning "应用已在运行 (端口 $APP_PORT)"
        return 0
    fi
    
    print_info "启动 Java 应用..."
    
    # 检查Java和Maven
    check_command "java"
    check_command "mvn"
    
    # 编译项目（如果需要）
    if [ ! -d "target" ] || [ "src" -nt "target" ]; then
        print_info "编译项目..."
        mvn clean compile -q
    fi
    
    # 启动应用（后台运行）
    print_info "启动应用服务器..."
    nohup mvn mn:run >> "$APP_LOG" 2>&1 &
    local app_pid=$!
    
    # 等待应用启动
    print_info "等待应用启动..."
    local max_attempts=30
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if is_app_running; then
            print_success "应用已启动 (PID: $app_pid, 端口: $APP_PORT)"
            echo "$app_pid" > "$LOG_DIR/app.pid"
            return 0
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    print_error "应用启动超时，请查看日志: $APP_LOG"
    return 1
}

# 停止Java应用
stop_application() {
    if [ -f "$LOG_DIR/app.pid" ]; then
        local pid=$(cat "$LOG_DIR/app.pid")
        if ps -p "$pid" > /dev/null 2>&1; then
            print_info "停止应用 (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
            rm -f "$LOG_DIR/app.pid"
            print_success "应用已停止"
        else
            print_warning "应用进程不存在"
            rm -f "$LOG_DIR/app.pid"
        fi
    else
        # 尝试通过端口查找并杀死进程
        local pid=$(lsof -ti:$APP_PORT)
        if [ -n "$pid" ]; then
            print_info "停止应用 (PID: $pid)..."
            kill "$pid" 2>/dev/null || true
            print_success "应用已停止"
        else
            print_warning "应用未运行"
        fi
    fi
}

# 查看日志
show_logs() {
    case "$1" in
        app)
            print_info "应用日志 (最后50行):"
            tail -n 50 "$APP_LOG" 2>/dev/null || echo "日志文件不存在"
            ;;
        db)
            print_info "数据库日志 (最后50行):"
            tail -n 50 "$DB_LOG" 2>/dev/null || echo "日志文件不存在"
            ;;
        *)
            print_info "应用日志:"
            tail -n 20 "$APP_LOG" 2>/dev/null || echo "日志文件不存在"
            echo ""
            print_info "数据库日志:"
            tail -n 20 "$DB_LOG" 2>/dev/null || echo "日志文件不存在"
            ;;
    esac
}

# 查看状态
show_status() {
    echo ""
    print_info "========== 服务状态 =========="
    
    # 数据库状态
    if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
        print_success "数据库: 运行中 (容器: $DB_CONTAINER)"
    else
        print_error "数据库: 未运行"
    fi
    
    # 应用状态
    if is_app_running; then
        local pid=$(lsof -ti:$APP_PORT 2>/dev/null || cat "$LOG_DIR/app.pid" 2>/dev/null || echo "N/A")
        print_success "应用: 运行中 (端口: $APP_PORT, PID: $pid)"
    else
        print_error "应用: 未运行"
    fi
    
    echo ""
    print_info "访问地址:"
    echo "  - 应用: http://localhost:$APP_PORT"
    echo "  - 数据库: $DB_HOST:$DB_PORT"
    echo ""
}

# 重启服务
restart() {
    print_info "重启服务..."
    stop
    sleep 2
    start
}

# 完全停止
stop() {
    print_info "停止所有服务..."
    stop_application
    stop_database
    print_success "所有服务已停止"
}

# 启动所有服务
start() {
    print_info "========== 启动 $PROJECT_NAME =========="
    
    check_docker
    
    # 启动数据库
    start_database
    
    # 启动应用
    start_application
    
    echo ""
    show_status
}

# 清理（停止并删除数据卷）
clean() {
    print_warning "这将删除所有数据，包括数据库数据卷！"
    read -p "确认继续? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        stop
        print_info "删除数据卷..."
        docker-compose -f "$DOCKER_COMPOSE_FILE" down -v >> "$DB_LOG" 2>&1
        print_success "清理完成"
    else
        print_info "已取消"
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start     启动所有服务（数据库 + 应用）"
    echo "  stop      停止所有服务"
    echo "  restart   重启所有服务"
    echo "  status    查看服务状态"
    echo "  logs      查看日志 (app|db|all)"
    echo "  clean     清理所有数据（包括数据库数据卷）"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start          # 启动所有服务"
    echo "  $0 logs app       # 查看应用日志"
    echo "  $0 status         # 查看服务状态"
}

# 主函数
main() {
    case "${1:-start}" in
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "${2:-all}"
            ;;
        clean)
            clean
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"










