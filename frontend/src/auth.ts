import { reactive } from 'vue'
import { apiRequest } from './api'
import type { AccountIdentity, AuthResult, MenuItem } from './types'

export const authState = reactive<{
  account: AccountIdentity | null
  menus: MenuItem[]
  initialized: boolean
}>({ account: null, menus: [], initialized: false })

async function accept(result: AuthResult) {
  localStorage.setItem('metadata-ui-token', result.token)
  authState.account = result.account
  authState.menus = await apiRequest<MenuItem[]>('/api/menus/me')
}

export async function initializeAuth() {
  const token = localStorage.getItem('metadata-ui-token')
  if (!token) {
    authState.initialized = true
    return
  }
  try {
    authState.account = await apiRequest<AccountIdentity>('/api/accounts/me')
    authState.menus = await apiRequest<MenuItem[]>('/api/menus/me')
  } catch {
    localStorage.removeItem('metadata-ui-token')
    authState.account = null
    authState.menus = []
  } finally {
    authState.initialized = true
  }
}

export async function login(username: string, password: string, tenantId = 'demo') {
  const result = await apiRequest<AuthResult>('/api/auth/login', {
    method: 'POST', body: JSON.stringify({ tenantId, username, password })
  })
  await accept(result)
}

export async function register(username: string, password: string, displayName: string, email: string) {
  const result = await apiRequest<AuthResult>('/api/auth/register', {
    method: 'POST', body: JSON.stringify({ username, password, displayName, email })
  })
  await accept(result)
}

export async function logout() {
  try { await apiRequest('/api/auth/logout', { method: 'POST' }) } catch { /* local logout still succeeds */ }
  localStorage.removeItem('metadata-ui-token')
  authState.account = null
  authState.menus = []
}

export function hasPermission(code: string) {
  return authState.account?.permissions.includes(code) ?? false
}
