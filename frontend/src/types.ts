export type LoadStrategy = 'eager' | 'on-visible'

export interface CardAction {
  code: string
  label: string
  type: 'refresh' | 'navigate' | 'open-form'
  target?: ActionTarget | string
  params?: Record<string, ParameterBinding>
  success?: ActionSuccess
}

export interface ActionTarget {
  routeCode?: string
  formCode?: string
  actionCode?: string
  presentation?: 'modal' | 'drawer'
}

export interface ParameterBinding {
  source: 'page-context' | 'card-data' | 'account' | 'literal'
  path?: string
  value?: unknown
  required: boolean
}

export interface ActionSuccess {
  message?: string
  close: boolean
  refreshCards: string[]
}

export interface ActionPreparation {
  formCode: string
  initialValues: Record<string, unknown>
  version: number
}

export interface ActionResult {
  message: string
  refreshCards: string[]
}

export interface OpenFormRequest {
  formCode: string
  actionCode: string
  pageCode: string
  cardCode: string
  params: Record<string, string>
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
