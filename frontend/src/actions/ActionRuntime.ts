import type { Router } from 'vue-router'
import type { CardAction, OpenFormRequest } from '../types'
import { resolveParameters } from './ParameterResolver'
import { routeRegistry } from './routeRegistry'

interface ActionExecutionContext {
  pageCode: string
  cardCode: string
  pageContext: Record<string, string>
  cardData: unknown
  account?: Record<string, unknown>
}

interface ActionRuntimeCallbacks {
  refreshCards(cardCodes: string[]): void
  openForm(request: OpenFormRequest): void
  notify(message: string): void
}

export function createActionRuntime(router: Router, callbacks: ActionRuntimeCallbacks) {
  return async (action: CardAction, context: ActionExecutionContext) => {
    try {
      // 参数绑定来自元数据，可从页面上下文、卡片数据、账号或字面量中取值。
      // 解析完成后，下面的分支只处理动作类型，不再关心参数来源。
      const params = resolveParameters(action.params, {
        pageContext: context.pageContext,
        cardData: context.cardData,
        account: context.account ?? {}
      })

      if (action.type === 'refresh') {
        // 通过回调通知页面递增 refreshToken，保持运行时与 Vue 组件解耦。
        callbacks.refreshCards([context.cardCode])
        return
      }

      const target = typeof action.target === 'string' ? undefined : action.target
      if (action.type === 'navigate') {
        const routeCode = target?.routeCode ?? (typeof action.target === 'string' ? action.target : '')
        const routeBuilder = routeRegistry[routeCode]
        if (!routeBuilder) throw new Error(`未注册的路由：${routeCode}`)
        await router.push(routeBuilder(params))
        return
      }

      if (action.type === 'open-form') {
        if (!target?.formCode || !target.actionCode) throw new Error('编辑动作配置不完整')
        callbacks.openForm({
          formCode: target.formCode,
          actionCode: target.actionCode,
          pageCode: context.pageCode,
          cardCode: context.cardCode,
          params
        })
      }
    } catch (reason) {
      callbacks.notify((reason as Error).message)
    }
  }
}
