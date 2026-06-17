// 极简全屏 Loading：纯 DOM/CSS 实现，与项目现有风格保持一致
import { createApp, h, ref } from 'vue'

let loadingApp = null
let loadingDiv = null

function LoadingComponent() {
  const visible = ref(false)
  // 挂载后下一帧再显示，确保 CSS 过渡生效
  requestAnimationFrame(() => (visible.value = true))
  return h(
    'div',
    {
      class: 'app-loading-mask',
      style: {
        position: 'fixed',
        inset: '0',
        background: 'rgba(255,255,255,0.55)',
        backdropFilter: 'blur(1px)',
        zIndex: 9998,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        opacity: visible.value ? '1' : '0',
        transition: 'opacity 0.18s',
      },
    },
    [
      h(
        'div',
        {
          style: {
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px',
          },
        },
        [
          h('div', {
            class: 'app-loading-spinner',
            style: {
              width: '36px',
              height: '36px',
              border: '3px solid var(--color-gray-200)',
              borderTopColor: 'var(--color-blue)',
              borderRadius: '50%',
              animation: 'app-loading-rotate 0.8s linear infinite',
            },
          }),
          h(
            'div',
            {
              style: {
                fontSize: '13px',
                color: 'var(--color-text-secondary)',
                fontWeight: '500',
              },
            },
            '加载中...',
          ),
        ],
      ),
    ],
  )
}

function ensureStyle() {
  if (document.getElementById('app-loading-style')) return
  const style = document.createElement('style')
  style.id = 'app-loading-style'
  style.textContent = `@keyframes app-loading-rotate { to { transform: rotate(360deg); } }`
  document.head.appendChild(style)
}

export function showLoading() {
  if (loadingApp) return
  ensureStyle()
  loadingDiv = document.createElement('div')
  document.body.appendChild(loadingDiv)
  loadingApp = createApp(LoadingComponent)
  loadingApp.mount(loadingDiv)
}

export function hideLoading() {
  if (!loadingApp) return
  loadingApp.unmount()
  if (loadingDiv && loadingDiv.parentNode) loadingDiv.parentNode.removeChild(loadingDiv)
  loadingApp = null
  loadingDiv = null
}
