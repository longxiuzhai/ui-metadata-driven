async function getCardPage(url, pageCode) {
    const response = await fetch(url);
    if (!response.ok)
        throw new Error(`元数据加载失败 (${response.status})`);
    const value = await response.json();
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
    const response = await fetch(url, { signal });
    if (!response.ok) {
        throw new Error(`数据加载失败 (${response.status})`);
    }
    return response.json();
}
