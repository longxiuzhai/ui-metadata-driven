import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { createActionRuntime } from './actions/ActionRuntime';
import { authState, initializeAuth, logout } from './auth';
import { getCustomerDetailCards, getHomeCards } from './api';
import AuthPage from './components/AuthPage.vue';
import DynamicCard from './components/DynamicCard.vue';
import FormDrawer from './components/FormDrawer.vue';
import SecurityAdmin from './components/SecurityAdmin.vue';
const route = useRoute();
const router = useRouter();
const userId = computed(() => authState.account?.userId ?? '');
const page = ref(null);
const error = ref('');
const loading = ref(true);
const refreshTokens = ref({});
const formOpen = ref(false);
const formRequest = ref(null);
const toast = ref('');
let toastTimer;
const activePage = computed(() => {
    if (route.name === 'customer-detail')
        return 'customer';
    if (route.name === 'order-list')
        return 'orders';
    return 'home';
});
const isAuthRoute = computed(() => route.name === 'login' || route.name === 'register');
const isSecurityAdmin = computed(() => route.name === 'security-admin');
const customerId = computed(() => String(route.query.customerId ?? '1001'));
const pageCode = computed(() => (activePage.value === 'customer' ? 'customer_detail' : 'home'));
const pageContext = computed(() => activePage.value === 'customer'
    ? Object.fromEntries([['customerId', customerId.value]])
    : activePage.value === 'orders'
        ? Object.fromEntries(Object.entries(route.query).map(([key, value]) => [key, String(value ?? '')]))
        : Object.fromEntries([['userId', userId.value]]));
const heading = computed(() => {
    if (activePage.value === 'customer')
        return '客户详情';
    if (activePage.value === 'orders')
        return '客户订单';
    return '工作首页';
});
const subtitle = computed(() => {
    if (activePage.value === 'customer') {
        return `客户编号 ${customerId.value} · 卡片由后端能力、租户与权限动态决定`;
    }
    if (activePage.value === 'orders') {
        return `客户 ${pageContext.value.customerId ?? '-'} · 状态 ${pageContext.value.status ?? '全部'}`;
    }
    return '面向当前用户动态装配工作摘要与业务插件卡片';
});
const avatar = computed(() => activePage.value === 'customer'
    ? customerId.value.slice(-2)
    : activePage.value === 'orders'
        ? 'OR'
        : 'HI');
const executeCardAction = createActionRuntime(router, {
    refreshCards,
    openForm(request) {
        formRequest.value = request;
        formOpen.value = true;
    },
    notify
});
async function loadPage() {
    if (!authState.account || isAuthRoute.value || isSecurityAdmin.value) {
        page.value = null;
        loading.value = false;
        return;
    }
    if (activePage.value === 'orders') {
        page.value = null;
        error.value = '';
        loading.value = false;
        return;
    }
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
    router.push(kind === 'customer'
        ? { name: 'customer-detail', query: { customerId: customerId.value } }
        : { name: 'home' });
}
function navigateMenu(path) {
    router.push(path);
}
async function signOut() {
    await logout();
    await router.replace('/login');
}
function refreshCards(cardCodes) {
    for (const cardCode of cardCodes) {
        refreshTokens.value[cardCode] = (refreshTokens.value[cardCode] ?? 0) + 1;
    }
}
function notify(message) {
    toast.value = message;
    if (toastTimer)
        clearTimeout(toastTimer);
    toastTimer = setTimeout(() => (toast.value = ''), 3000);
}
function handleCardAction(action, cardData, cardCode) {
    executeCardAction(action, {
        pageCode: pageCode.value,
        cardCode,
        pageContext: pageContext.value,
        cardData
    });
}
function handleFormSaved(result) {
    refreshCards(result.refreshCards);
    notify(result.message);
}
onMounted(async () => {
    await initializeAuth();
    if (!authState.account && !isAuthRoute.value)
        await router.replace('/login');
    if (authState.account && isAuthRoute.value)
        await router.replace('/');
    await loadPage();
});
watch(() => route.fullPath, async () => {
    if (authState.initialized && !authState.account && !isAuthRoute.value) {
        await router.replace('/login');
        return;
    }
    await loadPage();
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
if (!__VLS_ctx.authState.initialized) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "boot-state" },
    });
}
else if (!__VLS_ctx.authState.account || __VLS_ctx.isAuthRoute) {
    /** @type {[typeof AuthPage, ]} */ ;
    // @ts-ignore
    const __VLS_0 = __VLS_asFunctionalComponent(AuthPage, new AuthPage({}));
    const __VLS_1 = __VLS_0({}, ...__VLS_functionalComponentArgsRest(__VLS_0));
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.nav, __VLS_intrinsicElements.nav)({
        ...{ class: "app-nav" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "menu-list" },
    });
    for (const [menu] of __VLS_getVForSourceType((__VLS_ctx.authState.menus))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(!__VLS_ctx.authState.initialized))
                        return;
                    if (!!(!__VLS_ctx.authState.account || __VLS_ctx.isAuthRoute))
                        return;
                    __VLS_ctx.navigateMenu(menu.path);
                } },
            key: (menu.id),
            ...{ class: ({ active: __VLS_ctx.route.path === menu.path.split('?')[0] }) },
        });
        (menu.name);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "account-box" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.b, __VLS_intrinsicElements.b)({});
    (__VLS_ctx.authState.account.displayName);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
    (__VLS_ctx.authState.account.username);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.signOut) },
    });
    if (__VLS_ctx.isSecurityAdmin) {
        /** @type {[typeof SecurityAdmin, ]} */ ;
        // @ts-ignore
        const __VLS_3 = __VLS_asFunctionalComponent(SecurityAdmin, new SecurityAdmin({}));
        const __VLS_4 = __VLS_3({}, ...__VLS_functionalComponentArgsRest(__VLS_3));
    }
    else {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({});
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
                const __VLS_6 = __VLS_asFunctionalComponent(DynamicCard, new DynamicCard({
                    ...{ 'onAction': {} },
                    key: (card.code),
                    definition: (card),
                    context: (__VLS_ctx.pageContext),
                    refreshToken: (__VLS_ctx.refreshTokens[card.code] ?? 0),
                    ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
                }));
                const __VLS_7 = __VLS_6({
                    ...{ 'onAction': {} },
                    key: (card.code),
                    definition: (card),
                    context: (__VLS_ctx.pageContext),
                    refreshToken: (__VLS_ctx.refreshTokens[card.code] ?? 0),
                    ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
                }, ...__VLS_functionalComponentArgsRest(__VLS_6));
                let __VLS_9;
                let __VLS_10;
                let __VLS_11;
                const __VLS_12 = {
                    onAction: ((action, data) => __VLS_ctx.handleCardAction(action, data, card.code))
                };
                var __VLS_8;
            }
            if (__VLS_ctx.page.cards.length === 0) {
                __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                    ...{ class: "page-state empty" },
                });
            }
        }
        else if (__VLS_ctx.activePage === 'orders') {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
                ...{ class: "orders-placeholder" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
            __VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
            __VLS_asFunctionalElement(__VLS_intrinsicElements.pre, __VLS_intrinsicElements.pre)({});
            (JSON.stringify(__VLS_ctx.pageContext, null, 2));
            __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
                ...{ onClick: (...[$event]) => {
                        if (!!(!__VLS_ctx.authState.initialized))
                            return;
                        if (!!(!__VLS_ctx.authState.account || __VLS_ctx.isAuthRoute))
                            return;
                        if (!!(__VLS_ctx.isSecurityAdmin))
                            return;
                        if (!!(__VLS_ctx.loading))
                            return;
                        if (!!(__VLS_ctx.error))
                            return;
                        if (!!(__VLS_ctx.page))
                            return;
                        if (!(__VLS_ctx.activePage === 'orders'))
                            return;
                        __VLS_ctx.switchPage('customer');
                    } },
            });
        }
    }
}
/** @type {[typeof FormDrawer, ]} */ ;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent(FormDrawer, new FormDrawer({
    ...{ 'onSaved': {} },
    modelValue: (__VLS_ctx.formOpen),
    request: (__VLS_ctx.formRequest),
}));
const __VLS_14 = __VLS_13({
    ...{ 'onSaved': {} },
    modelValue: (__VLS_ctx.formOpen),
    request: (__VLS_ctx.formRequest),
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
let __VLS_16;
let __VLS_17;
let __VLS_18;
const __VLS_19 = {
    onSaved: (__VLS_ctx.handleFormSaved)
};
var __VLS_15;
if (__VLS_ctx.toast) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "toast" },
    });
    (__VLS_ctx.toast);
}
/** @type {__VLS_StyleScopedClasses['boot-state']} */ ;
/** @type {__VLS_StyleScopedClasses['app-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-list']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['account-box']} */ ;
/** @type {__VLS_StyleScopedClasses['hero']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['grid']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['empty']} */ ;
/** @type {__VLS_StyleScopedClasses['orders-placeholder']} */ ;
/** @type {__VLS_StyleScopedClasses['toast']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            authState: authState,
            AuthPage: AuthPage,
            DynamicCard: DynamicCard,
            FormDrawer: FormDrawer,
            SecurityAdmin: SecurityAdmin,
            route: route,
            page: page,
            error: error,
            loading: loading,
            refreshTokens: refreshTokens,
            formOpen: formOpen,
            formRequest: formRequest,
            toast: toast,
            activePage: activePage,
            isAuthRoute: isAuthRoute,
            isSecurityAdmin: isSecurityAdmin,
            pageContext: pageContext,
            heading: heading,
            subtitle: subtitle,
            avatar: avatar,
            loadPage: loadPage,
            switchPage: switchPage,
            navigateMenu: navigateMenu,
            signOut: signOut,
            handleCardAction: handleCardAction,
            handleFormSaved: handleFormSaved,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
