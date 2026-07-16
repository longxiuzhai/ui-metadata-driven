import type { ActionPreparation, ActionResult, CardPageDefinition } from './types'

export async function apiRequest<T>(url: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  const token = localStorage.getItem('metadata-ui-token')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (init.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  const response = await fetch(url, { ...init, headers })
  if (response.status === 401) {
    localStorage.removeItem('metadata-ui-token')
    if (!url.startsWith('/api/auth/')) window.location.assign('/login')
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message ?? `请求失败 (${response.status})`)
  }
  if (response.status === 204) return undefined as T
  return response.json()
}

async function getCardPage(url: string, pageCode: string): Promise<CardPageDefinition> {
  const value = await apiRequest<CardPageDefinition>(url)
  // 页面元数据是动态渲染的输入，先做最小协议校验，避免错误配置进入组件层。
  if (value.version !== '1.0' || value.pageCode !== pageCode || !Array.isArray(value.cards)) {
    throw new Error('不支持的元数据协议')
  }
  return value
}

export async function getCustomerDetailCards(customerId: string): Promise<CardPageDefinition> {
  return getCardPage(
    `/api/ui/pages/customer_detail/cards?customerId=${encodeURIComponent(customerId)}`,
    'customer_detail'
  )
}

export async function getHomeCards(): Promise<CardPageDefinition> {
  return getCardPage('/api/ui/pages/home/cards', 'home')
}

export async function loadCardData(urlTemplate: string, context: Record<string, string>, signal: AbortSignal) {
  // dataApi 由后端元数据提供，例如 /api/customers/{customerId}/summary。
  // 页面上下文只负责替换占位符；卡片组件不会感知路由或业务查询参数。
  const url = Object.entries(context).reduce(
    (value, [key, replacement]) => value.replaceAll(`{${key}}`, encodeURIComponent(replacement)),
    urlTemplate
  )
  if (/\{[^}]+}/.test(url)) {
    throw new Error('卡片数据源缺少页面上下文')
  }
  if (!url.startsWith('/api/')) {
    // 元数据不能把浏览器引向任意外部地址，数据请求统一限制在本站 API 下。
    throw new Error('拒绝访问非站内数据源')
  }
  return apiRequest(url, { signal })
}

async function postAction<T>(url: string, body: unknown): Promise<T> {
  return apiRequest<T>(url, {
    method: 'POST',
    body: JSON.stringify(body)
  })
}

export function prepareAction(
  actionCode: string,
  pageCode: string,
  cardCode: string,
  params: Record<string, string>
): Promise<ActionPreparation> {
  return postAction(`/api/ui/actions/${encodeURIComponent(actionCode)}/prepare`, {
    pageCode,
    cardCode,
    params
  })
}

export function executeAction(
  actionCode: string,
  request: {
    pageCode: string
    cardCode: string
    params: Record<string, string>
    values: Record<string, unknown>
    version: number
    requestId: string
  }
): Promise<ActionResult> {
  return postAction(`/api/ui/actions/${encodeURIComponent(actionCode)}/execute`, request)
}
