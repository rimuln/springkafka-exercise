const isDev = import.meta.env.DEV;

export const logger = {
  info: (message, ...args) => {
    if (isDev) console.log(`%c[INFO] ${message}`, 'color: #2196F3; font-weight: bold', ...args);
  },
  warn: (message, ...args) => {
    if (isDev) console.warn(`%c[WARN] ${message}`, 'color: #FF9800; font-weight: bold', ...args);
  },
  error: (message, ...args) => {
    console.error(`%c[ERROR] ${message}`, 'color: #F44336; font-weight: bold', ...args);
  },
  debug: (message, ...args) => {
    if (isDev) console.debug(`%c[DEBUG] ${message}`, 'color: #9C27B0', ...args);
  }
};