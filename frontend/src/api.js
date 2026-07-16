export async function apiRequest(url, init = {}) {
    const headers = new Headers(init.headers);
    const token = localStorage.getItem('metadata-ui-token');
    if (token)
        headers.set('Authorization', `Bearer ${token}`);
    if (init.body && !headers.has('Content-Type'))
        headers.set('Content-Type', 'application/json');
    const response = await fetch(url, { ...init, headers });
    if (response.status === 401) {
        localStorage.removeItem('metadata-ui-token');
        if (!url.startsWith('/api/auth/'))
            window.location.assign('/login');
    }
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.message ?? `请求失败 (${response.status})`);
    }
    if (response.status === 204)
        return undefined;
    return response.json();
}
async function getCardPage(url, pageCode) {
    const value = await apiRequest(url);
    if (value.version !== '1.0' || value.pageCode !== pageCode || !Array.isArray(value.cards)) {
        throw new Error('不支持的元数据协议');
    }
    return value;
}
export async function getCustomerDetailCards(customerId) {
    return getCardPage(`/api/ui/pages/customer_detail/cards?customerId=${encodeURIComponent(customerId)}`, 'customer_detail');
}
export async function getHomeCards() {
    return getCardPage('/api/ui/pages/home/cards', 'home');
}
export async function loadCardData(urlTemplate, context, signal) {
    const url = Object.entries(context).reduce((value, [key, replacement]) => value.replaceAll(`{${key}}`, encodeURIComponent(replacement)), urlTemplate);
    if (/\{[^}]+}/.test(url)) {
        throw new Error('卡片数据源缺少页面上下文');
    }
    if (!url.startsWith('/api/')) {
        throw new Error('拒绝访问非站内数据源');
    }
    return apiRequest(url, { signal });
}
async function postAction(url, body) {
    return apiRequest(url, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}
export function prepareAction(actionCode, pageCode, cardCode, params) {
    return postAction(`/api/ui/actions/${encodeURIComponent(actionCode)}/prepare`, {
        pageCode,
        cardCode,
        params
    });
}
export function executeAction(actionCode, request) {
    return postAction(`/api/ui/actions/${encodeURIComponent(actionCode)}/execute`, request);
}
