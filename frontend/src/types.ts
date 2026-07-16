export type LoadStrategy = 'eager' | 'on-visible'

export interface CardAction {
  code: string
  label: string
  type: 'refresh' | 'navigate' | 'open-form'
  target?: string
}

export interface ResponsiveSpan {
  xs: number
  md: number
  xl: number
}

export interface CardDefinition {
  code: string
  title: string
  component: string
  order: number
  span: ResponsiveSpan
  dataApi: string
  loadStrategy: LoadStrategy
  props: Record<string, unknown>
  actions: CardAction[]
}

export interface CardPageDefinition {
  version: string
  pageCode: string
  layout: {
    type: string
    columns: number
    gap: number
  }
  cards: CardDefinition[]
}
