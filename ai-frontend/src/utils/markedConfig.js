import { marked } from 'marked'

marked.use({ breaks: true, gfm: true })

/**
 * 渲染 Markdown 文本为 HTML
 * @param {string} text
 * @returns {string}
 */
export function renderMarkdown(text) {
  if (!text) return ''
  return marked.parse(text)
}
