import { resolveParameters } from './ParameterResolver';
import { routeRegistry } from './routeRegistry';
export function createActionRuntime(router, callbacks) {
    return async (action, context) => {
        try {
            const params = resolveParameters(action.params, {
                pageContext: context.pageContext,
                cardData: context.cardData,
                account: context.account ?? {}
            });
            if (action.type === 'refresh') {
                callbacks.refreshCards([context.cardCode]);
                return;
            }
            const target = typeof action.target === 'string' ? undefined : action.target;
            if (action.type === 'navigate') {
                const routeCode = target?.routeCode ?? (typeof action.target === 'string' ? action.target : '');
                const routeBuilder = routeRegistry[routeCode];
                if (!routeBuilder)
                    throw new Error(`未注册的路由：${routeCode}`);
                await router.push(routeBuilder(params));
                return;
            }
            if (action.type === 'open-form') {
                if (!target?.formCode || !target.actionCode)
                    throw new Error('编辑动作配置不完整');
                callbacks.openForm({
                    formCode: target.formCode,
                    actionCode: target.actionCode,
                    pageCode: context.pageCode,
                    cardCode: context.cardCode,
                    params
                });
            }
        }
        catch (reason) {
            callbacks.notify(reason.message);
        }
    };
}
