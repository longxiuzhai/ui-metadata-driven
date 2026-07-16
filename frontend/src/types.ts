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

export interface AccountIdentity {
  tenantId: string
  userId: string
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
  features: string[]
}

export interface AuthResult {
  token: string
  account: AccountIdentity
}

export interface MenuItem {
  id: number
  parentId?: number
  code: string
  name: string
  path: string
  icon?: string
  sortOrder: number
  permissionCode?: string
  enabled: boolean
}

export interface AdminUser {
  id: number
  tenantId: string
  username: string
  displayName: string
  email?: string
  enabled: boolean
  roleIds: number[]
}

export interface AdminRole {
  id: number
  code: string
  name: string
  description?: string
  enabled: boolean
  permissionIds: number[]
  menuIds: number[]
}

export interface AdminPermission {
  id: number
  code: string
  name: string
  resource: string
  actionName: string
  description?: string
}

export interface AdminMenu extends MenuItem {}
