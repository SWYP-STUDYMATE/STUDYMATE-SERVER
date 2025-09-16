#\!/bin/bash
# Cloudflare API 토큰 테스트 스크립트

TOKEN="your-api-token-here"
ACCOUNT_ID="69abd904cab5ffd103e569e7e050a884"

echo "=== Cloudflare API 토큰 권한 테스트 ==="

# 1. 토큰 유효성 검증
echo "1. 토큰 유효성 검증..."
curl -s -X GET "https://api.cloudflare.com/client/v4/user/tokens/verify" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .

# 2. Workers 목록 조회
echo "2. Workers 목록 조회..."
curl -s -X GET "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/workers/scripts" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .

# 3. KV 네임스페이스 목록
echo "3. KV 네임스페이스 목록..."
curl -s -X GET "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/storage/kv/namespaces" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .

# 4. R2 버킷 목록
echo "4. R2 버킷 목록..."
curl -s -X GET "https://api.cloudflare.com/client/v4/accounts/$ACCOUNT_ID/r2/buckets" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .
