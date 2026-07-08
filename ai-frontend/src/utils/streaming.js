/**
 * AI聊天流式客户端（基于 fetch + ReadableStream）
 * 通过 HTTP 流式接收AI回复
 */

/**
 * 发起流式聊天请求
 * @param {Object} payload - 请求体（memoryId, scenario, message 等）
 * @param {Object} callbacks - { onMessage(chunk), onDone(), onError(err) }
 * @returns {AbortController} 可用于取消请求
 */
export function streamChat(payload, { onMessage, onDone, onError } = {}) {
  const controller = new AbortController()

  const token = localStorage.getItem('token')
  fetch('http://localhost:8080/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload),
    signal: controller.signal
  })
    .then(response => {
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) { onDone?.(); return }
          const text = decoder.decode(value, { stream: true })
          if (text) onMessage?.(text)
          read()
        }).catch(err => {
          if (err.name !== 'AbortError') onError?.(err)
        })
      }
      read()
    })
    .catch(err => {
      if (err.name !== 'AbortError') onError?.(err)
    })

  return controller
}

// 兼容旧接口：保留 ChatWebSocket 类，内部改用 fetch
export class ChatWebSocket {
  constructor() {
    this.callbacks = {}
    this._controller = null
  }

  connect() { /* fetch 无需预连接 */ }

  send(message, options = {}) {
    const payload = {
      memoryId: options.memoryId || 'default',
      message: message,
      scenario: options.scenario || 'chat',
      ...options
    }
    this._controller = streamChat(payload, {
      onMessage: this.callbacks.onMessage,
      onDone: this.callbacks.onDone,
      onError: this.callbacks.onError
    })
  }

  on(event, callback) {
    this.callbacks[`on${event.charAt(0).toUpperCase()}${event.slice(1)}`] = callback
  }

  removeCallback(name) {
    delete this.callbacks[name]
  }

  close() {
    this._controller?.abort()
  }
}

let instance = null

export function useChatWebSocket() {
  if (!instance) instance = new ChatWebSocket()
  return instance
}