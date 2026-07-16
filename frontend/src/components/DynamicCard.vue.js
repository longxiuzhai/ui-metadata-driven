import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef } from 'vue';
import { loadCardData } from '../api';
import { cardRegistry } from '../cardRegistry';
import UnknownCard from './cards/UnknownCard.vue';
const props = defineProps();
const root = useTemplateRef('root');
const data = ref(null);
const loading = ref(false);
const error = ref('');
let controller;
let observer;
const resolvedComponent = computed(() => cardRegistry[props.definition.component] ?? UnknownCard);
const isUnknown = computed(() => !cardRegistry[props.definition.component]);
const isEmpty = computed(() => data.value == null || (Array.isArray(data.value) && data.value.length === 0));
async function load() {
    if (loading.value) {
        return;
    }
    controller?.abort();
    controller = new AbortController();
    loading.value = true;
    error.value = '';
    const started = performance.now();
    try {
        data.value = await loadCardData(props.definition.dataApi, props.context, controller.signal);
    }
    catch (reason) {
        if (reason.name !== 'AbortError') {
            error.value = reason.message;
        }
    }
    finally {
        loading.value = false;
        console.info('card_load', {
            card: props.definition.code,
            durationMs: Math.round(performance.now() - started),
            ok: !error.value
        });
    }
}
function handleAction(action) {
    if (action.type === 'refresh') {
        return load();
    }
    if (action.type === 'navigate' && action.target) {
        window.location.assign(action.target);
    }
    if (action.type === 'open-form') {
        window.alert(`打开已登记表单：${action.target}`);
    }
}
onMounted(() => {
    if (props.definition.loadStrategy === 'eager') {
        return load();
    }
    observer = new IntersectionObserver(entries => {
        if (entries.some(entry => entry.isIntersecting)) {
            observer?.disconnect();
            load();
        }
    }, { rootMargin: '120px' });
    if (root.value) {
        observer.observe(root.value);
    }
});
onBeforeUnmount(() => {
    observer?.disconnect();
    controller?.abort();
});
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['skeleton']} */ ;
/** @type {__VLS_StyleScopedClasses['skeleton']} */ ;
/** @type {__VLS_StyleScopedClasses['skeleton']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.article, __VLS_intrinsicElements.article)({
    ref: "root",
    ...{ class: "card" },
});
/** @type {typeof __VLS_ctx.root} */ ;
__VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
(__VLS_ctx.definition.title);
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
for (const [action] of __VLS_getVForSourceType((__VLS_ctx.definition.actions))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.handleAction(action);
            } },
        key: (action.code),
    });
    (action.label);
}
if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "state error" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    (__VLS_ctx.error);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.load) },
    });
}
else if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "skeleton" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.i)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.i)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.i)({});
}
else if (__VLS_ctx.data === null) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "state" },
    });
}
else if (__VLS_ctx.isEmpty) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "state" },
    });
}
else {
    const __VLS_0 = ((__VLS_ctx.resolvedComponent));
    // @ts-ignore
    const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
        data: (__VLS_ctx.data),
        ...(__VLS_ctx.definition.props),
        componentName: (__VLS_ctx.isUnknown ? __VLS_ctx.definition.component : undefined),
    }));
    const __VLS_2 = __VLS_1({
        data: (__VLS_ctx.data),
        ...(__VLS_ctx.definition.props),
        componentName: (__VLS_ctx.isUnknown ? __VLS_ctx.definition.component : undefined),
    }, ...__VLS_functionalComponentArgsRest(__VLS_1));
}
/** @type {__VLS_StyleScopedClasses['card']} */ ;
/** @type {__VLS_StyleScopedClasses['state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['skeleton']} */ ;
/** @type {__VLS_StyleScopedClasses['state']} */ ;
/** @type {__VLS_StyleScopedClasses['state']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            root: root,
            data: data,
            loading: loading,
            error: error,
            resolvedComponent: resolvedComponent,
            isUnknown: isUnknown,
            isEmpty: isEmpty,
            load: load,
            handleAction: handleAction,
        };
    },
    __typeProps: {},
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
    __typeProps: {},
});
; /* PartiallyEnd: #4569/main.vue */
