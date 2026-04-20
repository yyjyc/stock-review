import { reactive } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, register as registerApi, getMe } from '@/api/auth'

const state = reactive({
  token: getToken() || '',
  userInfo: null,
  isLoggedIn: !!getToken()
})

export function useUserStore() {
  function isAdmin() {
    return state.userInfo?.role === 'ADMIN'
  }

  async function login(username, password) {
    const res = await loginApi({ username, password })
    const { token, userInfo } = res.data
    state.token = token
    state.userInfo = userInfo
    state.isLoggedIn = true
    setToken(token)
  }

  async function register(username, password, nickname) {
    await registerApi({ username, password, nickname })
  }

  async function fetchUserInfo() {
    if (!state.token) return
    try {
      const res = await getMe()
      state.userInfo = res.data
      state.isLoggedIn = true
    } catch {
      logout()
    }
  }

  function logout() {
    state.token = ''
    state.userInfo = null
    state.isLoggedIn = false
    removeToken()
  }

  return {
    state,
    isAdmin,
    login,
    register,
    fetchUserInfo,
    logout
  }
}
