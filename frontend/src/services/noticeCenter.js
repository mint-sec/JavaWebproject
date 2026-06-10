export const APP_NOTICE_EVENT = "coldchain-app-notice";

export function emitAppNotice({ type = "error", message, duration = 4200 } = {}) {
  if (!message) {
    return;
  }

  window.dispatchEvent(
    new CustomEvent(APP_NOTICE_EVENT, {
      detail: {
        type,
        message,
        duration,
      },
    }),
  );
}

export function onAppNotice(listener) {
  const handler = (event) => {
    listener(event.detail || {});
  };

  window.addEventListener(APP_NOTICE_EVENT, handler);
  return () => window.removeEventListener(APP_NOTICE_EVENT, handler);
}
