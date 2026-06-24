import { ref } from 'vue'

/**
 * 逐词流式显示 composable
 * @param {number} wordDelay - 每词间隔 ms，默认 55
 */
export function useStreamingText(wordDelay = 55) {
  const displayedContent = ref('')
  const isStreaming = ref(false)

  let wordQueue = []
  let lastWordTime = 0
  let displayText = ''

  function processQueue(timestamp) {
    if (wordQueue.length === 0) {
      isStreaming.value = false
      return
    }
    isStreaming.value = true
    if (!lastWordTime) lastWordTime = timestamp
    if (timestamp - lastWordTime >= wordDelay) {
      displayText += wordQueue.shift()
      displayedContent.value = displayText
      lastWordTime = timestamp
    }
    requestAnimationFrame(processQueue)
  }

  function appendText(text) {
    wordQueue.push(...text.split(/(?<=\s)/))
    if (!isStreaming.value) {
      lastWordTime = 0
      requestAnimationFrame(processQueue)
    }
  }

  function flush(fullText) {
    wordQueue = []
    isStreaming.value = false
    displayText = fullText || ''
    displayedContent.value = displayText
  }

  function resetStreaming() {
    wordQueue = []
    displayText = ''
    lastWordTime = 0
    isStreaming.value = false
    displayedContent.value = ''
  }

  return { displayedContent, isStreaming, appendText, flush, resetStreaming }
}
