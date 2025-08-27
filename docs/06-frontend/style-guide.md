# 🎨 프론트엔드 스타일 가이드

## 📅 문서 정보
- **최종 업데이트**: 2025-08-27
- **작성자**: Design & Frontend Team
- **목적**: STUDYMATE 프론트엔드 디자인 시스템 및 스타일 가이드
- **참조**: [STUDYMATE-CLIENT 프로젝트](../../STYDYMATE-CLIENT/)

---

## 🎯 디자인 원칙

### 1. 사용자 중심 디자인 (User-Centered Design)
- **직관성**: 사용자가 쉽게 이해할 수 있는 인터페이스
- **접근성**: 모든 사용자가 사용할 수 있는 포용적 디자인
- **일관성**: 전체 애플리케이션에서 일관된 경험 제공

### 2. 언어 학습 특화 디자인
- **명확성**: 학습에 방해되지 않는 깔끔한 인터페이스
- **집중력**: 학습에 집중할 수 있도록 돕는 시각적 계층
- **동기부여**: 학습 진도를 시각화하여 동기 부여

### 3. 글로벌 사용성
- **다국어 지원**: 텍스트 길이 변화에 대응하는 레이아웃
- **문화적 고려**: 다양한 문화권 사용자를 고려한 색상과 아이콘
- **시간대 대응**: 전 세계 사용자를 위한 시간 표시

---

## 🎨 컬러 시스템

### Primary Colors (브랜드 컬러)
```css
:root {
  /* Blue - 신뢰성과 전문성 */
  --color-primary-50: #eff6ff;
  --color-primary-100: #dbeafe;
  --color-primary-200: #bfdbfe;
  --color-primary-300: #93c5fd;
  --color-primary-400: #60a5fa;
  --color-primary-500: #3b82f6;  /* Main Primary */
  --color-primary-600: #2563eb;
  --color-primary-700: #1d4ed8;
  --color-primary-800: #1e40af;
  --color-primary-900: #1e3a8a;
}
```

### Secondary Colors (보조 컬러)
```css
:root {
  /* Green - 성공, 성장, 학습 */
  --color-secondary-50: #ecfdf5;
  --color-secondary-100: #d1fae5;
  --color-secondary-200: #a7f3d0;
  --color-secondary-300: #6ee7b7;
  --color-secondary-400: #34d399;
  --color-secondary-500: #10b981;  /* Main Secondary */
  --color-secondary-600: #059669;
  --color-secondary-700: #047857;
  --color-secondary-800: #065f46;
  --color-secondary-900: #064e3b;
}
```

### Neutral Colors (중성 컬러)
```css
:root {
  /* Gray Scale */
  --color-gray-50: #f9fafb;
  --color-gray-100: #f3f4f6;
  --color-gray-200: #e5e7eb;
  --color-gray-300: #d1d5db;
  --color-gray-400: #9ca3af;
  --color-gray-500: #6b7280;
  --color-gray-600: #4b5563;
  --color-gray-700: #374151;
  --color-gray-800: #1f2937;
  --color-gray-900: #111827;
  
  /* Black & White */
  --color-white: #ffffff;
  --color-black: #000000;
}
```

### Semantic Colors (의미론적 컬러)
```css
:root {
  /* Success */
  --color-success-50: #ecfdf5;
  --color-success-500: #10b981;
  --color-success-700: #047857;
  
  /* Warning */
  --color-warning-50: #fffbeb;
  --color-warning-500: #f59e0b;
  --color-warning-700: #b45309;
  
  /* Error */
  --color-error-50: #fef2f2;
  --color-error-500: #ef4444;
  --color-error-700: #b91c1c;
  
  /* Info */
  --color-info-50: #eff6ff;
  --color-info-500: #3b82f6;
  --color-info-700: #1d4ed8;
}
```

### 컬러 사용 가이드
```jsx
// Primary: 주요 액션, 링크, 브랜드 요소
<button className="bg-primary-500 text-white">로그인</button>

// Secondary: 보조 액션, 성공 상태
<div className="bg-secondary-100 text-secondary-700">완료됨</div>

// Semantic: 상태 표시
<div className="bg-error-50 text-error-700 border border-error-200">
  오류가 발생했습니다
</div>
```

---

## 📝 타이포그래피

### 폰트 패밀리
```css
/* 기본 폰트 */
body {
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, 
               'Segoe UI', Roboto, 'Helvetica Neue', Arial, 
               'Noto Sans', sans-serif;
}

/* 모노스페이스 폰트 (코드용) */
.font-mono {
  font-family: 'Fira Code', 'SF Mono', Monaco, 'Cascadia Code',
               'Roboto Mono', Consolas, 'Courier New', monospace;
}
```

### 폰트 크기 스케일
```css
/* Font Scale */
.text-xs    { font-size: 0.75rem;  line-height: 1rem;    } /* 12px */
.text-sm    { font-size: 0.875rem; line-height: 1.25rem; } /* 14px */
.text-base  { font-size: 1rem;     line-height: 1.5rem;  } /* 16px */
.text-lg    { font-size: 1.125rem; line-height: 1.75rem; } /* 18px */
.text-xl    { font-size: 1.25rem;  line-height: 1.75rem; } /* 20px */
.text-2xl   { font-size: 1.5rem;   line-height: 2rem;    } /* 24px */
.text-3xl   { font-size: 1.875rem; line-height: 2.25rem; } /* 30px */
.text-4xl   { font-size: 2.25rem;  line-height: 2.5rem;  } /* 36px */
.text-5xl   { font-size: 3rem;     line-height: 1;       } /* 48px */
```

### 폰트 웨이트
```css
.font-thin       { font-weight: 100; }
.font-extralight { font-weight: 200; }
.font-light      { font-weight: 300; }
.font-normal     { font-weight: 400; }
.font-medium     { font-weight: 500; }
.font-semibold   { font-weight: 600; }
.font-bold       { font-weight: 700; }
.font-extrabold  { font-weight: 800; }
.font-black      { font-weight: 900; }
```

### 텍스트 스타일 사용법
```jsx
// 헤딩
<h1 className="text-4xl font-bold text-gray-900">메인 제목</h1>
<h2 className="text-3xl font-semibold text-gray-800">섹션 제목</h2>
<h3 className="text-2xl font-medium text-gray-700">서브 제목</h3>

// 본문
<p className="text-base text-gray-600 leading-relaxed">본문 텍스트</p>

// 캡션
<span className="text-sm text-gray-500">부가 정보</span>

// 강조
<strong className="font-semibold text-gray-900">중요한 내용</strong>
```

---

## 📐 스페이싱 시스템

### Spacing Scale (Tailwind 기반)
```css
/* Spacing Values */
.spacing-0   { /* 0px    */ }
.spacing-1   { /* 4px    */ }
.spacing-2   { /* 8px    */ }
.spacing-3   { /* 12px   */ }
.spacing-4   { /* 16px   */ }
.spacing-5   { /* 20px   */ }
.spacing-6   { /* 24px   */ }
.spacing-8   { /* 32px   */ }
.spacing-10  { /* 40px   */ }
.spacing-12  { /* 48px   */ }
.spacing-16  { /* 64px   */ }
.spacing-20  { /* 80px   */ }
.spacing-24  { /* 96px   */ }
.spacing-32  { /* 128px  */ }
```

### 레이아웃 가이드라인
```jsx
// 컨테이너 패딩
<div className="px-4 sm:px-6 lg:px-8">
  
// 섹션 간 여백
<section className="py-12 sm:py-16 lg:py-20">

// 카드 내부 패딩
<div className="p-6 sm:p-8">

// 컴포넌트 간 간격
<div className="space-y-4"> {/* 수직 간격 */}
<div className="space-x-3"> {/* 수평 간격 */}
```

---

## 🧩 컴포넌트 스타일

### 1. 버튼 (Button)
```jsx
// Primary Button
<button className="
  inline-flex items-center justify-center
  px-4 py-2 text-sm font-medium
  text-white bg-primary-600 
  border border-transparent rounded-md
  shadow-sm hover:bg-primary-700
  focus:outline-none focus:ring-2 focus:ring-primary-500
  disabled:opacity-50 disabled:cursor-not-allowed
  transition-colors duration-200
">
  Primary Button
</button>

// Secondary Button
<button className="
  inline-flex items-center justify-center
  px-4 py-2 text-sm font-medium
  text-gray-700 bg-white
  border border-gray-300 rounded-md
  shadow-sm hover:bg-gray-50
  focus:outline-none focus:ring-2 focus:ring-primary-500
">
  Secondary Button
</button>

// Outline Button
<button className="
  inline-flex items-center justify-center
  px-4 py-2 text-sm font-medium
  text-primary-700 bg-transparent
  border border-primary-300 rounded-md
  hover:bg-primary-50
  focus:outline-none focus:ring-2 focus:ring-primary-500
">
  Outline Button
</button>
```

### 2. 인풋 (Input)
```jsx
// Text Input
<input className="
  block w-full px-3 py-2
  text-gray-900 placeholder-gray-500
  border border-gray-300 rounded-md
  shadow-sm focus:ring-primary-500 focus:border-primary-500
  disabled:bg-gray-50 disabled:text-gray-500
" />

// Input with Label
<div>
  <label className="block text-sm font-medium text-gray-700 mb-1">
    이메일
  </label>
  <input type="email" className="..." />
</div>

// Error State
<input className="
  border-error-300 text-error-900
  placeholder-error-400 focus:ring-error-500 focus:border-error-500
" />
<p className="mt-1 text-sm text-error-600">오류 메시지</p>
```

### 3. 카드 (Card)
```jsx
<div className="
  bg-white border border-gray-200 rounded-lg
  shadow-sm hover:shadow-md
  transition-shadow duration-200
">
  <div className="p-6">
    <h3 className="text-lg font-medium text-gray-900">카드 제목</h3>
    <p className="mt-2 text-gray-600">카드 내용</p>
  </div>
</div>
```

### 4. 모달 (Modal)
```jsx
// Overlay
<div className="fixed inset-0 bg-black bg-opacity-50 z-50">
  
  // Modal Container  
  <div className="
    fixed inset-0 z-50 overflow-y-auto
    flex items-center justify-center p-4
  ">
    
    // Modal Content
    <div className="
      relative bg-white rounded-lg
      max-w-md w-full p-6
      shadow-xl transform transition-all
    ">
      <h2 className="text-xl font-semibold text-gray-900 mb-4">
        모달 제목
      </h2>
      <p className="text-gray-600">모달 내용</p>
    </div>
  </div>
</div>
```

---

## 📱 반응형 디자인

### Breakpoint 시스템
```css
/* Tailwind CSS Breakpoints */
/* sm: 640px  - Small tablets and large phones */
/* md: 768px  - Tablets */
/* lg: 1024px - Small laptops */
/* xl: 1280px - Desktops */
/* 2xl: 1536px - Large desktops */
```

### 반응형 패턴
```jsx
// 모바일 우선 설계
<div className="
  w-full p-4                    // Mobile: full width, 16px padding
  sm:max-w-sm sm:p-6           // Small: max 384px width, 24px padding  
  md:max-w-md md:p-8           // Medium: max 448px width, 32px padding
  lg:max-w-lg lg:p-10          // Large: max 512px width, 40px padding
">

// 그리드 시스템
<div className="
  grid grid-cols-1             // Mobile: 1 column
  sm:grid-cols-2               // Small: 2 columns
  lg:grid-cols-3               // Large: 3 columns
  gap-4 sm:gap-6 lg:gap-8      // Responsive gap
">

// 텍스트 크기
<h1 className="
  text-2xl sm:text-3xl lg:text-4xl
  font-bold
">

// 숨김/표시
<div className="hidden md:block">데스크톱에서만 표시</div>
<div className="block md:hidden">모바일에서만 표시</div>
```

---

## 🎭 애니메이션 및 트랜지션

### 트랜지션 설정
```css
/* 기본 트랜지션 */
.transition-basic {
  transition: all 0.2s ease-in-out;
}

/* 색상 트랜지션 */
.transition-colors {
  transition: color 0.2s ease-in-out, 
              background-color 0.2s ease-in-out, 
              border-color 0.2s ease-in-out;
}

/* 그림자 트랜지션 */
.transition-shadow {
  transition: box-shadow 0.2s ease-in-out;
}

/* 변형 트랜지션 */
.transition-transform {
  transition: transform 0.2s ease-in-out;
}
```

### 호버 효과
```jsx
// 버튼 호버
<button className="
  bg-primary-600 hover:bg-primary-700
  transform hover:scale-105
  transition-all duration-200
">

// 카드 호버
<div className="
  shadow-sm hover:shadow-md
  transition-shadow duration-300
">

// 링크 호버
<a className="
  text-primary-600 hover:text-primary-800
  underline hover:no-underline
  transition-colors duration-200
">
```

### 애니메이션 예시
```css
/* 페이드 인 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

/* 슬라이드 업 */
@keyframes slideUp {
  from { 
    transform: translateY(20px); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0); 
    opacity: 1; 
  }
}

.animate-slide-up {
  animation: slideUp 0.4s ease-out;
}

/* 스핀 (로딩) */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
```

---

## 🔍 상태 시각화

### 로딩 상태
```jsx
// 스피너
<div className="flex items-center justify-center">
  <div className="
    w-6 h-6 border-2 border-gray-300 
    border-t-primary-600 rounded-full 
    animate-spin
  "></div>
</div>

// 스켈레톤 로딩
<div className="animate-pulse">
  <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
</div>

// 프로그레스 바
<div className="w-full bg-gray-200 rounded-full h-2">
  <div 
    className="bg-primary-600 h-2 rounded-full transition-all duration-300"
    style={{ width: `${progress}%` }}
  ></div>
</div>
```

### 에러 상태
```jsx
<div className="
  bg-error-50 border border-error-200 rounded-md p-4
  text-error-800
">
  <div className="flex">
    <ExclamationTriangleIcon className="w-5 h-5 text-error-400" />
    <div className="ml-3">
      <h3 className="font-medium">오류가 발생했습니다</h3>
      <p className="mt-1 text-sm">다시 시도해주세요.</p>
    </div>
  </div>
</div>
```

### 성공 상태
```jsx
<div className="
  bg-success-50 border border-success-200 rounded-md p-4
  text-success-800
">
  <div className="flex">
    <CheckCircleIcon className="w-5 h-5 text-success-400" />
    <div className="ml-3">
      <h3 className="font-medium">성공적으로 완료되었습니다</h3>
    </div>
  </div>
</div>
```

---

## 🌐 접근성 (Accessibility)

### 컬러 대비
```css
/* WCAG AA 기준 충족 */
.text-high-contrast {
  color: #1f2937; /* 4.5:1 이상 대비 */
}

.text-medium-contrast {
  color: #4b5563; /* 3:1 이상 대비 */
}
```

### 키보드 네비게이션
```jsx
// Focus 상태 표시
<button className="
  focus:outline-none 
  focus:ring-2 focus:ring-primary-500 
  focus:ring-offset-2
">

// Skip Link
<a href="#main-content" className="
  sr-only focus:not-sr-only
  focus:absolute focus:top-4 focus:left-4
  bg-primary-600 text-white px-4 py-2 rounded
">
  메인 컨텐츠로 건너뛰기
</a>
```

### 스크린 리더 지원
```jsx
// ARIA 레이블
<button aria-label="채팅방 나가기">
  <XIcon className="w-5 h-5" />
</button>

// 상태 알림
<div role="status" aria-live="polite">
  {statusMessage}
</div>

// 숨은 텍스트
<span className="sr-only">현재 페이지: 홈</span>
```

---

## 🎨 다크 모드 (계획)

### CSS 변수를 이용한 다크 모드
```css
:root {
  --bg-primary: #ffffff;
  --text-primary: #1f2937;
  --border-primary: #e5e7eb;
}

[data-theme="dark"] {
  --bg-primary: #1f2937;
  --text-primary: #f9fafb;
  --border-primary: #374151;
}

.bg-primary {
  background-color: var(--bg-primary);
}

.text-primary {
  color: var(--text-primary);
}
```

---

## 🛠️ 개발 도구

### 스타일 린팅 (Stylelint)
```json
{
  "extends": ["stylelint-config-standard"],
  "rules": {
    "color-hex-case": "lower",
    "color-hex-length": "short",
    "declaration-no-important": true
  }
}
```

### 디자인 토큰 관리
```javascript
// tokens.js
export const tokens = {
  colors: {
    primary: {
      50: '#eff6ff',
      500: '#3b82f6',
      900: '#1e3a8a'
    }
  },
  spacing: {
    xs: '0.5rem',
    sm: '0.75rem',
    md: '1rem',
    lg: '1.5rem'
  }
};
```

---

## 📚 참고 자료

### 디자인 시스템 참고
- [Tailwind CSS](https://tailwindcss.com/)
- [Headless UI](https://headlessui.com/)
- [Radix UI](https://www.radix-ui.com/)
- [Material Design](https://material.io/)

### 접근성 가이드
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [WebAIM](https://webaim.org/)

### 컬러 도구
- [Coolors](https://coolors.co/)
- [Color Hunt](https://colorhunt.co/)
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)

---

## 📝 관련 문서

- [컴포넌트 가이드](./component-guide.md)
- [시스템 아키텍처](../03-architecture/system-architecture.md)
- [프론트엔드 상세 문서](../../STYDYMATE-CLIENT/docs/ARCHITECTURE.md)