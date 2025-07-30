#!/bin/bash

# NCP 배포 스크립트
# 사용법: ./scripts/deploy.sh [dev|prod]

set -e  # 에러 발생시 스크립트 중단

ENVIRONMENT=${1:-dev}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 NCP 배포 시작: $ENVIRONMENT 환경"

# 환경별 설정 파일 확인
ENV_FILE="$PROJECT_ROOT/.env.$ENVIRONMENT"
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ 환경 설정 파일이 없습니다: $ENV_FILE"
    echo "💡 .env.example을 참고하여 .env.$ENVIRONMENT 파일을 생성해주세요."
    exit 1
fi

# 환경 변수 로드
source "$ENV_FILE"

echo "📝 환경 설정:"
echo "  - Environment: $ENVIRONMENT"
echo "  - Registry: $DOCKER_REGISTRY"
echo "  - Image: $APP_IMAGE_NAME:$APP_IMAGE_VERSION"
echo "  - Database: $DB_HOST:$DB_PORT"
echo "  - Redis: $REDIS_HOST:$REDIS_PORT"

# Docker Compose 파일 선택
if [ "$ENVIRONMENT" = "prod" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
else
    COMPOSE_FILE="docker-compose.dev.yml"
fi

echo "📦 Docker Compose 파일: $COMPOSE_FILE"

# 개발 환경인 경우 빌드 수행
if [ "$ENVIRONMENT" = "dev" ]; then
    echo "🔨 애플리케이션 빌드 중..."
    cd "$PROJECT_ROOT"
    ./gradlew build -x test
    
    echo "🐳 Docker 이미지 빌드 중..."
    docker build -t studymate-app:dev .
fi

# 운영 환경인 경우 레지스트리에서 이미지 Pull
if [ "$ENVIRONMENT" = "prod" ]; then
    echo "🔐 Container Registry 로그인 중..."
    echo "$NCP_SECRET_KEY" | docker login "$DOCKER_REGISTRY" -u "$NCP_ACCESS_KEY" --password-stdin
    
    echo "📥 최신 이미지 Pull 중..."
    docker pull "$DOCKER_REGISTRY/$APP_IMAGE_NAME:$APP_IMAGE_VERSION"
fi

# 기존 컨테이너 중지
echo "🛑 기존 컨테이너 중지 중..."
docker-compose -f "$PROJECT_ROOT/$COMPOSE_FILE" down || true

# 새 컨테이너 시작
echo "🎯 새 컨테이너 시작 중..."
cd "$PROJECT_ROOT"
docker-compose -f "$COMPOSE_FILE" up -d

# 헬스체크 대기
echo "🔍 애플리케이션 시작 대기 중..."
sleep 30

# 헬스체크 수행
HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
echo "❤️ 헬스체크 수행: $HEALTH_CHECK_URL"

for i in {1..10}; do
    if curl -f "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
        echo "✅ 애플리케이션이 정상적으로 시작되었습니다!"
        break
    else
        echo "⏳ 헬스체크 시도 $i/10..."
        sleep 10
    fi
    
    if [ $i -eq 10 ]; then
        echo "❌ 애플리케이션 시작에 실패했습니다."
        echo "📋 로그 확인:"
        docker-compose -f "$COMPOSE_FILE" logs --tail=50
        exit 1
    fi
done

# 정리 작업
echo "🧹 사용하지 않는 Docker 이미지 정리 중..."
docker system prune -f

echo "🎉 배포가 성공적으로 완료되었습니다!"
echo ""
echo "📊 서비스 상태:"
docker-compose -f "$COMPOSE_FILE" ps

echo ""
echo "📝 유용한 명령어:"
echo "  - 로그 확인: docker-compose -f $COMPOSE_FILE logs -f"
echo "  - 서비스 중지: docker-compose -f $COMPOSE_FILE stop"
echo "  - 서비스 재시작: docker-compose -f $COMPOSE_FILE restart"