import { onMounted, ref } from 'vue';
import { getCustomerDetailCards } from './api';
import DynamicCard from './components/DynamicCard.vue';
const customerId = ref(new URLSearchParams(location.search).get('customerId') ?? '1001');
const page = ref(null);
const error = ref('');
const loading = ref(true);
async function loadPage() {
    loading.value = true;
    error.value = '';
    try {
        page.value = await getCustomerDetailCards(customerId.value);
    }
    catch (e) {
        error.value = e.message;
    }
    finally {
        loading.value = false;
    }
}
onMounted(loadPage);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "hero" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "eyebrow" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.customerId);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "avatar" },
});
(__VLS_ctx.customerId.slice(-2));
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
            customerId: (__VLS_ctx.customerId),
            ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
        }));
        const __VLS_1 = __VLS_0({
            key: (card.code),
            definition: (card),
            customerId: (__VLS_ctx.customerId),
            ...{ style: ({ '--xs': card.span.xs, '--md': card.span.md, '--xl': card.span.xl }) },
        }, ...__VLS_functionalComponentArgsRest(__VLS_0));
    }
}
/** @type {__VLS_StyleScopedClasses['hero']} */ ;
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['page-state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['grid']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            DynamicCard: DynamicCard,
            customerId: customerId,
            page: page,
            error: error,
            loading: loading,
            loadPage: loadPage,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
