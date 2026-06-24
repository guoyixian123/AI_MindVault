/**
 * WebSocket 聊天客户端
 * 比 fetch + ReadableStream 更低延迟、更顺滑
 */

export class ChatWebSocket {
  constructor() {
    this.ws = null
    this.callbacks = {}
    this.reconnectTimer = null
    this.isConnected = false
  }

  connect() {
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    this.ws = new WebSocket(`${protocol}//localhost:8080/ws/chat`)

    this.ws.onopen = () => {
      this.isConnected = true
      this.callbacks.onOpen?.()
    }

    this.ws.onmessage = (event) => {
      const data = event.data
      if (data === '[DONE]') {
        this.callbacks.onDone?.()
      } else {
        this.callbacks.onMessage?.(data)
      }
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      this.callbacks.onError?.(error)
    }

    this.ws.onclose = () => {
      this.isConnected = false
      this.callbacks.onClose?.()
      // 自动重连
      this.reconnectTimer = setTimeout(() => this.connect(), 3000)
    }
  }

  send(message, options = {}) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      this.connect()
      // 等待连接建立后发送
      const waitSend = setInterval(() => {
        if (this.ws?.readyState === WebSocket.OPEN) {
          clearInterval(waitSend)
          this._doSend(message, options)
        }
      }, 100)
      setTimeout(() => clearInterval(waitSend), 5000)
      return
    }
    this._doSend(message, options)
  }

  _doSend(message, options) {
    const payload = {
      memoryId: options.memoryId || 'default',
      message: message,
      scenario: options.scenario || 'chat',
      ...options
    }
    console.log('WebSocket 发送:', payload)
    this.ws.send(JSON.stringify(payload))
  }

  on(event, callback) {
    this.callbacks[`on${event.charAt(0).toUpperCase()}${event.slice(1)}`] = callback
  }

  removeCallback(name) {
    delete this.callbacks[name]
  }

  close() {
    clearTimeout(this.reconnectTimer)
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}

// 单例
let instance = null

export function useChatWebSocket() {
  if (!instance) {
    instance = new ChatWebSocket()
  }
  return instance
}
