// 轻量级 hash 路由：监听 location.hash，提供 push/replace/current
// 与 Vue Router 相比，没有嵌套路由 / 命名视图 / 路由参数，
// 但足够覆盖"登录页 + 主壳内页面切换"的诉求，避免引入 vue-router。
import { ref } from 'vue'

const currentRoute = ref(parseHash())

function parseHash() {
  const raw = location.hash || '#/'
  const path = raw.startsWith('#') ? raw.slice(1) : raw
  return path || '/'
}

window.addEventListener('hashchange', () => {
  currentRoute.value = parseHash()
})

export function useRouter() {
  function push(to) {
    const target = to.startsWith('#') ? to : '#' + to
    if (location.hash === target) {
      // 强制刷新（手动触发响应）
      currentRoute.value = parseHash()
    } else {
      location.hash = target
    }
  }
  function replace(to) {
    const target = to.startsWith('#') ? to : '#' + to
    const url = location.href.split('#')[0] + target
    history.replaceState(null, '', url)
    currentRoute.value = parseHash()
  }
  return { push, replace, currentRoute }
}
