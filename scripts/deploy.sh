#!/bin/bash

# STUDYMATE Production Deployment Script
# This script handles the deployment and recovery of the production environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

# Functions
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
}

# Check if environment file exists
check_env_file() {
    if [ ! -f "$PROJECT_DIR/$ENV_FILE" ]; then
        error "Environment file $ENV_FILE not found!"
        echo "Please create $ENV_FILE based on .env.prod.example"
        exit 1
    fi
    log "Environment file found: $ENV_FILE"
}

# Check container status
check_containers() {
    log "Checking container status..."
    
    cd "$PROJECT_DIR"
    
    echo "=== Container Status ==="
    docker-compose -f "$COMPOSE_FILE" ps
    
    echo -e "\n=== Container Health ==="
    docker-compose -f "$COMPOSE_FILE" ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"
}

# Check container logs
check_logs() {
    log "Checking container logs..."
    
    cd "$PROJECT_DIR"
    
    echo "=== Application Logs (last 50 lines) ==="
    docker-compose -f "$COMPOSE_FILE" logs --tail=50 app || true
    
    echo -e "\n=== Database Logs (last 20 lines) ==="
    docker-compose -f "$COMPOSE_FILE" logs --tail=20 db || true
    
    echo -e "\n=== Redis Logs (last 20 lines) ==="
    docker-compose -f "$COMPOSE_FILE" logs --tail=20 redis || true
}

# Test database connection
test_db_connection() {
    log "Testing database connection..."
    
    cd "$PROJECT_DIR"
    
    if docker-compose -f "$COMPOSE_FILE" exec -T db mysql -u root -p"${DB_ROOT_PASSWORD}" -e "SELECT 1;" > /dev/null 2>&1; then
        log "Database connection: SUCCESS"
        return 0
    else
        error "Database connection: FAILED"
        return 1
    fi
}

# Test Redis connection
test_redis_connection() {
    log "Testing Redis connection..."
    
    cd "$PROJECT_DIR"
    
    if docker-compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping | grep -q "PONG"; then
        log "Redis connection: SUCCESS"
        return 0
    else
        error "Redis connection: FAILED"
        return 1
    fi
}

# Test application health
test_app_health() {
    log "Testing application health endpoint..."
    
    # Wait for app to be ready
    sleep 10
    
    for i in {1..30}; do
        if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
            log "Application health check: SUCCESS"
            return 0
        fi
        echo "Waiting for application to start... (attempt $i/30)"
        sleep 2
    done
    
    error "Application health check: FAILED"
    return 1
}

# Stop all containers
stop_containers() {
    log "Stopping all containers..."
    
    cd "$PROJECT_DIR"
    docker-compose -f "$COMPOSE_FILE" down || true
    
    log "Containers stopped"
}

# Start containers
start_containers() {
    log "Starting containers..."
    
    cd "$PROJECT_DIR"
    
    # Pull latest images
    docker-compose -f "$COMPOSE_FILE" pull || warn "Failed to pull latest images"
    
    # Start containers
    docker-compose -f "$COMPOSE_FILE" up -d
    
    log "Containers started"
}

# Full restart
restart_containers() {
    log "Performing full container restart..."
    
    stop_containers
    start_containers
}

# Diagnose issues
diagnose() {
    log "Starting comprehensive diagnosis..."
    
    check_env_file
    check_containers
    
    echo -e "\n=== Testing Connections ==="
    test_db_connection || true
    test_redis_connection || true
    test_app_health || true
    
    echo -e "\n=== Recent Logs ==="
    check_logs
}

# Show usage
usage() {
    echo "Usage: $0 {start|stop|restart|status|logs|diagnose|health}"
    echo ""
    echo "Commands:"
    echo "  start     - Start all containers"
    echo "  stop      - Stop all containers"
    echo "  restart   - Stop and start all containers"
    echo "  status    - Show container status"
    echo "  logs      - Show container logs"
    echo "  diagnose  - Run comprehensive diagnosis"
    echo "  health    - Test application health"
}

# Main script
cd "$PROJECT_DIR"

case "$1" in
    start)
        check_env_file
        start_containers
        ;;
    stop)
        stop_containers
        ;;
    restart)
        check_env_file
        restart_containers
        ;;
    status)
        check_containers
        ;;
    logs)
        check_logs
        ;;
    diagnose)
        diagnose
        ;;
    health)
        test_app_health
        ;;
    *)
        usage
        exit 1
        ;;
esac