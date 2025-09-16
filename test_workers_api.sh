#\!/bin/bash
# API 키 테스트 스크립트

API_KEY="your-generated-token-here"
BASE_URL="https://workers.languagemate.kr"

echo "=== Workers AI API 키 테스트 ==="

# 1. Health Check (인증 불필요)
echo "1. Health Check..."
curl -s "$BASE_URL/health" | jq .

# 2. 인증이 필요한 엔드포인트 테스트
echo "2. Transcribe API 테스트..."
curl -s -X POST "$BASE_URL/api/v1/transcribe" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"test\": \"auth\"}" | jq .

# 3. Level Test API 테스트
echo "3. Level Test API 테스트..."
curl -s -X POST "$BASE_URL/api/v1/level-test" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"test\": \"auth\"}" | jq .
