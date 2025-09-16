# Whisper API 다국어 지원 가이드

## 개요
Cloudflare Workers AI의 Whisper 모델을 사용한 음성 인식 API는 99개 언어를 지원합니다.

## API 사용법

### 요청 형식
```bash
POST /api/v1/internal/transcribe
Headers:
  - Content-Type: application/json
  - X-Internal-Secret: {내부 시크릿}
```

### 요청 본문
```json
{
  "audio_base64": "Base64 인코딩된 오디오 데이터",
  "language": "ko",  // 언어 코드 (선택사항)
  "user_context": "추가 컨텍스트 정보"
}
```

## 언어별 사용 예시

### 한국어 음성 인식
```json
{
  "audio_base64": "오디오 데이터...",
  "language": "ko"
}
```

### 일본어 음성 인식
```json
{
  "audio_base64": "オーディオデータ...",
  "language": "ja"
}
```

### 중국어 음성 인식
```json
{
  "audio_base64": "音频数据...",
  "language": "zh"
}
```

## 지원 언어 코드

### 주요 아시아 언어
| 언어 | 코드 | 모델 |
|------|------|------|
| 한국어 | ko | @cf/openai/whisper |
| 일본어 | ja | @cf/openai/whisper |
| 중국어 | zh | @cf/openai/whisper |
| 베트남어 | vi | @cf/openai/whisper |
| 태국어 | th | @cf/openai/whisper |
| 인도네시아어 | id | @cf/openai/whisper |

### 유럽 언어
| 언어 | 코드 | 모델 |
|------|------|------|
| 영어 | en | @cf/openai/whisper-tiny-en |
| 스페인어 | es | @cf/openai/whisper |
| 프랑스어 | fr | @cf/openai/whisper |
| 독일어 | de | @cf/openai/whisper |
| 이탈리아어 | it | @cf/openai/whisper |
| 포르투갈어 | pt | @cf/openai/whisper |
| 러시아어 | ru | @cf/openai/whisper |

### 기타 언어
| 언어 | 코드 | 모델 |
|------|------|------|
| 아랍어 | ar | @cf/openai/whisper |
| 터키어 | tr | @cf/openai/whisper |
| 힌디어 | hi | @cf/openai/whisper |
| 스웨덴어 | sv | @cf/openai/whisper |
| 네덜란드어 | nl | @cf/openai/whisper |
| 폴란드어 | pl | @cf/openai/whisper |

## 모델 선택 로직

### 자동 모델 선택
API는 언어 코드에 따라 자동으로 최적의 모델을 선택합니다:

```typescript
const modelName = language === 'en'
  ? '@cf/openai/whisper-tiny-en'  // 영어 전용 (빠름, 가벼움)
  : '@cf/openai/whisper';           // 다국어 지원
```

### 모델별 특징
- **whisper-tiny-en**:
  - 영어 전용
  - 빠른 처리 속도
  - 작은 모델 크기
  - 낮은 메모리 사용량

- **whisper**:
  - 99개 언어 지원
  - 자동 언어 감지
  - 높은 정확도
  - 상대적으로 느린 처리 속도

## 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": {
    "transcript": "안녕하세요, 저는 한국어를 말하고 있습니다.",
    "language": "ko",
    "confidence": 1.0,
    "word_count": 6,
    "processing_time": 1250,
    "vtt": "WEBVTT\n\n00:00:00.000 --> 00:00:02.500\n안녕하세요, 저는 한국어를 말하고 있습니다.",
    "words": [{
      "word": "안녕하세요",
      "start": 0.0,
      "end": 1.0
    }],
    "user_context": "테스트"
  },
  "meta": {
    "timestamp": "2025-09-16T10:30:00.000Z",
    "requestId": "unique-request-id"
  }
}
```

## 제한사항

### 파일 크기
- **최대**: 25MB
- **권장**: 4MB 이하
- **최적**: 30초 분량

### 오디오 형식
- 지원 형식: WAV, MP3, M4A, WebM
- 샘플링 레이트: 16kHz 이상 권장
- 채널: 모노/스테레오 모두 지원

### 처리 시간
- 영어 (tiny-en): ~1초
- 다국어: ~2-3초
- 긴 오디오 (>1분): 5-10초

## 최적화 팁

### 1. 언어 사전 지정
언어를 알고 있다면 `language` 파라미터로 지정하면 더 빠르고 정확한 결과를 얻을 수 있습니다.

### 2. 오디오 압축
대용량 파일은 Base64 인코딩 전에 압축하여 전송 시간을 단축할 수 있습니다.

### 3. 청킹 처리
긴 오디오는 30초 단위로 나누어 처리하면 더 안정적입니다.

## 예제 코드

### JavaScript/TypeScript
```typescript
async function transcribeAudio(audioBlob: Blob, language: string = 'auto') {
  const arrayBuffer = await audioBlob.arrayBuffer();
  const base64 = btoa(String.fromCharCode(...new Uint8Array(arrayBuffer)));

  const response = await fetch('/api/v1/internal/transcribe', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-Internal-Secret': 'your-secret'
    },
    body: JSON.stringify({
      audio_base64: base64,
      language: language
    })
  });

  return response.json();
}

// 사용 예시
const audioFile = new File(['...'], 'speech.wav');
const result = await transcribeAudio(audioFile, 'ko');
console.log('인식된 텍스트:', result.data.transcript);
```

### Python
```python
import base64
import requests

def transcribe_audio(audio_path, language='auto'):
    with open(audio_path, 'rb') as audio_file:
        audio_base64 = base64.b64encode(audio_file.read()).decode('utf-8')

    response = requests.post(
        'https://workers.languagemate.kr/api/v1/internal/transcribe',
        headers={
            'Content-Type': 'application/json',
            'X-Internal-Secret': 'your-secret'
        },
        json={
            'audio_base64': audio_base64,
            'language': language
        }
    )

    return response.json()

# 사용 예시
result = transcribe_audio('speech.wav', 'ko')
print('인식된 텍스트:', result['data']['transcript'])
```

## 문제 해결

### 언어 감지 실패
- 명확한 음성 녹음 확인
- `language` 파라미터 명시적 지정
- 노이즈 제거 후 재시도

### 정확도 향상
- 고품질 마이크 사용
- 조용한 환경에서 녹음
- 명확한 발음
- 적절한 음량 유지

### 성능 최적화
- 영어는 `whisper-tiny-en` 모델 사용
- 긴 오디오는 분할 처리
- 불필요한 무음 구간 제거

## 업데이트 기록
- 2025-09-16: 다국어 지원 구현
- 2025-09-16: 모델 자동 선택 로직 추가
- 2025-09-16: 한국어 지원 확인