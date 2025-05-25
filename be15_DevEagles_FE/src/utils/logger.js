/**
 * 환경변수 기반 로그 유틸리티
 * DEV 모드일 때만 일반 로그 출력, 에러는 항상 출력
 */

const isDev = import.meta.env.DEV || import.meta.env.VITE_DEV_MODE === 'true';
const logLevel = import.meta.env.VITE_LOG_LEVEL || 'info';

// 로그 레벨 우선순위
const LOG_LEVELS = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3,
};

const currentLogLevel = LOG_LEVELS[logLevel] || LOG_LEVELS.info;

class Logger {
  constructor(prefix = '') {
    this.prefix = prefix;
  }

  debug(...args) {
    if (isDev && currentLogLevel <= LOG_LEVELS.debug) {
      console.log(`[DEBUG]${this.prefix}`, ...args);
    }
  }

  info(...args) {
    if (isDev && currentLogLevel <= LOG_LEVELS.info) {
      console.log(`[INFO]${this.prefix}`, ...args);
    }
  }

  warn(...args) {
    if (isDev && currentLogLevel <= LOG_LEVELS.warn) {
      console.warn(`[WARN]${this.prefix}`, ...args);
    }
  }

  error(...args) {
    // 에러는 항상 출력 (심각한 문제)
    console.error(`[ERROR]${this.prefix}`, ...args);
  }

  // 조건부 로그 (개발 모드에서만)
  devLog(...args) {
    if (isDev) {
      console.log(`[DEV]${this.prefix}`, ...args);
    }
  }

  // 성능 측정용 로그
  time(label) {
    if (isDev) {
      console.time(`${this.prefix} ${label}`);
    }
  }

  timeEnd(label) {
    if (isDev) {
      console.timeEnd(`${this.prefix} ${label}`);
    }
  }
}

// 기본 로거
export const logger = new Logger();

// 모듈별 로거 생성 함수
export function createLogger(moduleName) {
  return new Logger(` [${moduleName}]`);
}

// 개발 모드 확인 함수
export function isDevelopment() {
  return isDev;
}

// 로그 레벨 확인 함수
export function shouldLog(level) {
  return isDev && currentLogLevel <= LOG_LEVELS[level];
}
