import { computed, onMounted, ref } from 'vue';
import { getCustomerDetailCards, getHomeCards } from './api';
import DynamicCard from './components/DynamicCard.vue';
const params = new URLSearchParams(location.search);
const initialPage = location.pathname.includes('customer-detail') || params.get('page') === 'customer'
    ? 'customer'
    : 'home';
const activePage = ref(initialPage);
const customerId = ref(params.get('customerId') ?? '1001');
const userId = ref('user-1');
const page = ref(null);
const error = ref('');
const loading = ref(true);
const pageContext = computed(() => activePage.value === 'customer'
    ? Object.fromEntries([['customerId', customerId.value]])
    : Object.fromEntries([['userId', userId.value]]));
const heading = computed(() => (activePage.value === 'customer' ? '客户详情' : '工作首页'));
const subtitle = computed(() => activePage.value === 'customer'
    ? `客户编号 ${customerId.value} · 卡片由后端能力、租户与权限动态决定`
    : '面向当前用户动态装配工作摘要与业务插件卡片');
const avatar = computed(() => activePage.value === 'customer' ? customerId.value.slice(-2) : 'HI');
async function loadPage() {
    loading.value = true;
    error.value = '';
    page.value = null;
    try {
        page.value =
            activePage.value === 'customer'
                ? await getCustomerDetailCards(customerId.value)
                : await getHomeCards();
    }
    catch (reason) {
        error.value = reason.message;
    }
    finally {
        loading.value = false;
    }
}
function switchPage(kind) {
    if (activePage.value === kind) {
        return;
    }
    activePage.value = kind;
    const url = kind === 'customer'
        ? `/customer-detail?customerId=${encodeURIComponent(customerId.value)}`
        : '/';
    history.pushState({}, '', url);
    loadPage();
}
onMounted(loadPage);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.nav, __VLS_intrinsicElements.nav)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.switchPage('home');
        } },
    ...{ class: ({ active: __VLS_ctx.activePage === 'home' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.switchPage('customer');
        } },
    ...{ class: ({ active: __VLS_ctx.activePage === 'customer' }) },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "hero" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
(__VLS_ctx.heading);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.subtitle);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "avatar" },
});
(__VLS_ctx.avatar);
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "page-state" },
    });
}
else if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "page-state error" },
    });
    (__VLS_ctx.error);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.loadPage) },
    });
}
else if (__VLS_ctx.page) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
        ...{ class: "grid" },
        ...{ style: ({ '--gap': `${__VLS_ctx.page.layout.gap}px` }) },
    });
    for (const [card] of __VLS_getVForSourceType((__VLS_ctx.page.cards))) {
        /** @type {[typeof DynamicCard, ]} */ ;
        // @ts-ignore
        const __VLS_0 = __VLS_asFunctionalComponent(DynamicCard, new DynamicCard({
            key: (card.code),
            definition: (card),
            context: (__VLS_ctx.pageContext),
            ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
        }));
        const __VLS_1 = __VLS_0({
            key: (card.code),
            definition: (card),
            context: (__VLS_ctx.pageContext),
            ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
        }, ...__VLS_functionalComponentArgsRest(__VLS_0));
    }
    if (__VLS_ctx.page.cards.length === 0) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "page-state empty" },
        });
    }
}
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['hero']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['grid']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['empty']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            DynamicCard: DynamicCard,
            activePage: activePage,
            page: page,
            error: error,
            loading: loading,
            pageContext: pageContext,
            heading: heading,
            subtitle: subtitle,
            avatar: avatar,
            loadPage: loadPage,
            switchPage: switchPage,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
