// 极简消息弹窗实现：复用项目原有的 .modal-mask/.modal-box 风格，
// 避免引入第三方 UI 库，保持与现有视觉一致。
import { createApp, h, ref } from 'vue'

const ICONS = {
  success:
    'M16 8A8 8 0 11.001 8 8 8 0 0116 8zm-3.97-3.03a.75.75 0 00-1.08.022L7.477 9.417 5.384 7.323a.75.75 0 00-1.06 1.06L6.97 11.03a.75.75 0 001.079-.02l3.992-4.99a.75.75 0 00-.01-1.05z',
  error:
    'M16 8A8 8 0 11.001 8 8 8 0 0116 8zM4.646 4.646a.5.5 0 01.708 0L8 7.293l2.646-2.647a.5.5 0 01.708.708L8.707 8l2.647 2.646a.5.5 0 01-.708.708L8 8.707l-2.646 2.647a.5.5 0 01-.708-.708L7.293 8 4.646 5.354a.5.5 0 010-.708z',
  warning:
    'M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z',
  info:
    'M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z',
}

const COLORS = {
  success: { bg: 'var(--color-green-light)', border: 'var(--color-green-border)', color: 'var(--color-green)' },
  error: { bg: 'var(--color-red-light)', border: 'var(--color-red-border)', color: 'var(--color-red)' },
  warning: { bg: 'var(--color-orange-light)', border: 'var(--color-orange-border)', color: 'var(--color-orange)' },
  info: { bg: 'var(--color-blue-light)', border: 'var(--color-blue-border)', color: 'var(--color-blue)' },
}

function MessageComponent(props) {
  const visible = ref(true)
  const close = () => {
    visible.value = false
    setTimeout(() => props.onClose && props.onClose(), 200)
  }
  // 错误消息停留更久，便于阅读
  const duration = props.duration ?? (props.type === 'error' ? 3500 : 2500)
  setTimeout(close, duration)
  const c = COLORS[props.type] || COLORS.info
  return h(
    'div',
    {
      class: 'app-message',
      style: {
        position: 'fixed',
        top: '80px',
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 9999,
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        padding: '10px 16px',
        minWidth: '240px',
        maxWidth: '480px',
        background: c.bg,
        border: `1px solid ${c.border}`,
        color: c.color,
        borderRadius: 'var(--radius)',
        boxShadow: 'var(--shadow-md)',
        fontSize: '13px',
        fontWeight: '500',
        opacity: visible.value ? '1' : '0',
        transition: 'opacity 0.2s',
      },
    },
    [
      h(
        'svg',
        {
          width: 16,
          height: 16,
          viewBox: '0 0 16 16',
          fill: 'currentColor',
          style: { flexShrink: '0' },
        },
        [h('path', { d: ICONS[props.type] || ICONS.info })],
      ),
      h('span', { style: { flex: 1, color: 'var(--color-text-primary)' } }, props.message),
    ],
  )
}

export function ElMessage(options) {
  const { message, type = 'info', duration } = options
  const div = document.createElement('div')
  document.body.appendChild(div)
  const app = createApp({
    setup() {
      return () => h(MessageComponent, { message, type, duration, onClose: () => unmount() })
    },
  })
  app.mount(div)
  function unmount() {
    app.unmount()
    if (div.parentNode) div.parentNode.removeChild(div)
  }
}
