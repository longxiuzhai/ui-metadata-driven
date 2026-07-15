import type { CardPageDefinition } from './types'

export async function getCustomerDetailCards(customerId: string): Promise<CardPageDefinition> {
  const response = await fetch(`/api/ui/customer-detail/cards?customerId=${encodeURIComponent(customerId)}`)
  if (!response.ok) throw new Error(`元数据加载失败 (${response.status})`)
  const value = await response.json()
  if (value.version !== '1.0' || !Array.isArray(value.cards)) throw new Error('不支持的元数据协议')
  return value
}

export async function loadCardData(urlTemplate: string, customerId: string, signal: AbortSignal) {
  const url = urlTemplate.replaceAll('{customerId}', encodeURIComponent(customerId))
  if (!url.startsWith('/api/')) throw new Error('拒绝访问非站内数据源')
  const response = await fetch(url, { signal })
  if (!response.ok) throw new Error(`数据加载失败 (${response.status})`)
  return response.json()
}

