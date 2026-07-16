import type { ActionPreparation, ActionResult, CardPageDefinition } from './types'

async function getCardPage(url: string, pageCode: string): Promise<CardPageDefinition> {
  const response = await fetch(url)
  if (!response.ok) throw new Error(`元数据加载失败 (${response.status})`)
  const value = await response.json()
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
  const url = Object.entries(context).reduce(
    (value, [key, replacement]) => value.replaceAll(`{${key}}`, encodeURIComponent(replacement)),
    urlTemplate
  )
  if (/\{[^}]+}/.test(url)) {
    throw new Error('卡片数据源缺少页面上下文')
  }
  if (!url.startsWith('/api/')) {
    throw new Error('拒绝访问非站内数据源')
  }
  const response = await fetch(url, { signal })
  if (!response.ok) {
    throw new Error(`数据加载失败 (${response.status})`)
  }
  return response.json()
}

async function postAction<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}))
    throw new Error(errorBody.message ?? `动作执行失败 (${response.status})`)
  }
  return response.json()
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
