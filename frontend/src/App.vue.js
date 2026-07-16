import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { createActionRuntime } from './actions/ActionRuntime';
import { authState, initializeAuth, logout } from './auth';
import { getCustomerDetailCards, getHomeCards } from './api';
import AuthPage from './components/AuthPage.vue';
import DynamicCard from './components/DynamicCard.vue';
import FormDrawer from './components/FormDrawer.vue';
import SecurityAdmin from './components/SecurityAdmin.vue';
const securitySections = [
    { key: 'users', label: '用户管理' },
    { key: 'roles', label: '角色管理' },
    { key: 'permissions', label: '权限配置' },
    { key: 'menus', label: '菜单配置' }
];
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
const activeSecuritySection = computed(() => {
    const section = String(route.query.section ?? 'users');
    return securitySections.some(item => item.key === section) ? section : 'users';
});
const activeSecurityLabel = computed(() => securitySections.find(item => item.key === activeSecuritySection.value)?.label ?? '用户管理');
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
    return '首页';
});
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
function navigateSecuritySection(section) {
    router.push({ name: 'security-admin', query: { section } });
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
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "app-shell" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({
        ...{ class: "topbar" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "brand-block" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "brand-mark" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.b, __VLS_intrinsicElements.b)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "topbar-actions" },
    });
    if (!__VLS_ctx.isSecurityAdmin) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (__VLS_ctx.loadPage) },
            ...{ class: "icon-button" },
            title: "刷新当前页面",
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "top-account" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
        ...{ class: "mini-avatar" },
    });
    (__VLS_ctx.authState.account.displayName.slice(0, 1));
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.b, __VLS_intrinsicElements.b)({});
    (__VLS_ctx.authState.account.displayName);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
    (__VLS_ctx.authState.account.username);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.signOut) },
        ...{ class: "logout-button" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "workspace" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.aside, __VLS_intrinsicElements.aside)({
        ...{ class: "left-sidebar" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "sidebar-heading" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
    (__VLS_ctx.authState.menus.length);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.nav, __VLS_intrinsicElements.nav)({
        ...{ class: "side-menu" },
        'aria-label': "主菜单",
    });
    for (const [menu] of __VLS_getVForSourceType((__VLS_ctx.authState.menus))) {
        (menu.id);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(!__VLS_ctx.authState.initialized))
                        return;
                    if (!!(!__VLS_ctx.authState.account || __VLS_ctx.isAuthRoute))
                        return;
                    __VLS_ctx.navigateMenu(menu.path);
                } },
            ...{ class: "primary-menu" },
            ...{ class: ({ active: __VLS_ctx.route.path === menu.path.split('?')[0] }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "menu-icon" },
        });
        (menu.name.slice(0, 1));
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "menu-copy" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.b, __VLS_intrinsicElements.b)({});
        (menu.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (menu.code);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
            ...{ class: "menu-arrow" },
        });
        if (__VLS_ctx.isSecurityAdmin && menu.path.split('?')[0] === '/admin/security') {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
                ...{ class: "side-submenu" },
                'aria-label': "权限管理二级菜单",
            });
            for (const [item] of __VLS_getVForSourceType((__VLS_ctx.securitySections))) {
                __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
                    ...{ onClick: (...[$event]) => {
                            if (!!(!__VLS_ctx.authState.initialized))
                                return;
                            if (!!(!__VLS_ctx.authState.account || __VLS_ctx.isAuthRoute))
                                return;
                            if (!(__VLS_ctx.isSecurityAdmin && menu.path.split('?')[0] === '/admin/security'))
                                return;
                            __VLS_ctx.navigateSecuritySection(item.key);
                        } },
                    key: (item.key),
                    ...{ class: ({ active: __VLS_ctx.activeSecuritySection === item.key }) },
                });
                __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
                    ...{ class: "submenu-dot" },
                });
                (item.label);
            }
        }
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "sidebar-foot" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span)({
        ...{ class: "status-dot" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.b, __VLS_intrinsicElements.b)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
        ...{ class: "center-stage" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.nav, __VLS_intrinsicElements.nav)({
        ...{ class: "page-directory" },
        'aria-label': "页面目录",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
    (__VLS_ctx.isSecurityAdmin ? '权限管理' : __VLS_ctx.heading);
    if (__VLS_ctx.isSecurityAdmin) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.i, __VLS_intrinsicElements.i)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (__VLS_ctx.activeSecurityLabel);
    }
    if (__VLS_ctx.isSecurityAdmin) {
        /** @type {[typeof SecurityAdmin, ]} */ ;
        // @ts-ignore
        const __VLS_3 = __VLS_asFunctionalComponent(SecurityAdmin, new SecurityAdmin({}));
        const __VLS_4 = __VLS_3({}, ...__VLS_functionalComponentArgsRest(__VLS_3));
    }
    else {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({
            ...{ class: "business-main" },
        });
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
    __VLS_asFunctionalElement(__VLS_intrinsicElements.footer, __VLS_intrinsicElements.footer)({
        ...{ class: "app-footer" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
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
/** @type {__VLS_StyleScopedClasses['app-shell']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-block']} */ ;
/** @type {__VLS_StyleScopedClasses['brand-mark']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['icon-button']} */ ;
/** @type {__VLS_StyleScopedClasses['top-account']} */ ;
/** @type {__VLS_StyleScopedClasses['mini-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['logout-button']} */ ;
/** @type {__VLS_StyleScopedClasses['workspace']} */ ;
/** @type {__VLS_StyleScopedClasses['left-sidebar']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-heading']} */ ;
/** @type {__VLS_StyleScopedClasses['side-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['primary-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-copy']} */ ;
/** @type {__VLS_StyleScopedClasses['menu-arrow']} */ ;
/** @type {__VLS_StyleScopedClasses['side-submenu']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['submenu-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['sidebar-foot']} */ ;
/** @type {__VLS_StyleScopedClasses['status-dot']} */ ;
/** @type {__VLS_StyleScopedClasses['center-stage']} */ ;
/** @type {__VLS_StyleScopedClasses['page-directory']} */ ;
/** @type {__VLS_StyleScopedClasses['business-main']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['grid']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['empty']} */ ;
/** @type {__VLS_StyleScopedClasses['orders-placeholder']} */ ;
/** @type {__VLS_StyleScopedClasses['app-footer']} */ ;
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
            securitySections: securitySections,
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
            activeSecuritySection: activeSecuritySection,
            activeSecurityLabel: activeSecurityLabel,
            pageContext: pageContext,
            heading: heading,
            loadPage: loadPage,
            switchPage: switchPage,
            navigateMenu: navigateMenu,
            navigateSecuritySection: navigateSecuritySection,
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
