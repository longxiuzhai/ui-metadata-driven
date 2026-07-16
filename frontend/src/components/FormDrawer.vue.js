import { computed, ref, watch } from 'vue';
import { executeAction, prepareAction } from '../api';
import { formRegistry } from '../actions/formRegistry';
const props = defineProps();
const emit = defineEmits();
const values = ref({});
const version = ref(0);
const formCode = ref('');
const loading = ref(false);
const submitting = ref(false);
const error = ref('');
const formComponent = computed(() => formRegistry[formCode.value]);
watch(() => [props.modelValue, props.request], async ([open, request]) => {
    if (!open || !request)
        return;
    loading.value = true;
    error.value = '';
    try {
        const preparation = await prepareAction(request.actionCode, request.pageCode, request.cardCode, request.params);
        if (!formRegistry[preparation.formCode]) {
            throw new Error(`未注册的表单：${preparation.formCode}`);
        }
        formCode.value = preparation.formCode;
        values.value = { ...preparation.initialValues };
        version.value = preparation.version;
    }
    catch (reason) {
        error.value = reason.message;
    }
    finally {
        loading.value = false;
    }
}, { deep: true });
function close() {
    if (!submitting.value)
        emit('update:modelValue', false);
}
async function submit() {
    const request = props.request;
    if (!request || !formComponent.value)
        return;
    submitting.value = true;
    error.value = '';
    try {
        const result = await executeAction(request.actionCode, {
            pageCode: request.pageCode,
            cardCode: request.cardCode,
            params: request.params,
            values: values.value,
            version: version.value,
            requestId: globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
        });
        emit('saved', result);
        emit('update:modelValue', false);
    }
    catch (reason) {
        error.value = reason.message;
    }
    finally {
        submitting.value = false;
    }
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['close']} */ ;
// CSS variable injection 
// CSS variable injection end 
const __VLS_0 = {}.Teleport;
/** @type {[typeof __VLS_components.Teleport, typeof __VLS_components.Teleport, ]} */ ;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent(__VLS_0, new __VLS_0({
    to: "body",
}));
const __VLS_2 = __VLS_1({
    to: "body",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_3.slots.default;
if (__VLS_ctx.modelValue) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ onClick: (__VLS_ctx.close) },
        ...{ class: "overlay" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.aside, __VLS_intrinsicElements.aside)({
        ...{ class: "drawer" },
        role: "dialog",
        'aria-modal': "true",
        'aria-label': "编辑表单",
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.close) },
        ...{ class: "close" },
        disabled: (__VLS_ctx.submitting),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.main, __VLS_intrinsicElements.main)({});
    if (__VLS_ctx.loading) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "state" },
        });
    }
    else if (__VLS_ctx.error) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "state error" },
        });
        (__VLS_ctx.error);
    }
    else if (__VLS_ctx.formComponent) {
        const __VLS_4 = ((__VLS_ctx.formComponent));
        // @ts-ignore
        const __VLS_5 = __VLS_asFunctionalComponent(__VLS_4, new __VLS_4({
            modelValue: (__VLS_ctx.values),
        }));
        const __VLS_6 = __VLS_5({
            modelValue: (__VLS_ctx.values),
        }, ...__VLS_functionalComponentArgsRest(__VLS_5));
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.footer, __VLS_intrinsicElements.footer)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.close) },
        ...{ class: "secondary" },
        disabled: (__VLS_ctx.submitting),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.submit) },
        disabled: (__VLS_ctx.loading || __VLS_ctx.submitting || !__VLS_ctx.formComponent),
    });
    (__VLS_ctx.submitting ? '保存中…' : '保存');
}
var __VLS_3;
/** @type {__VLS_StyleScopedClasses['overlay']} */ ;
/** @type {__VLS_StyleScopedClasses['drawer']} */ ;
/** @type {__VLS_StyleScopedClasses['close']} */ ;
/** @type {__VLS_StyleScopedClasses['state']} */ ;
/** @type {__VLS_StyleScopedClasses['state']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            values: values,
            loading: loading,
            submitting: submitting,
            error: error,
            formComponent: formComponent,
            close: close,
            submit: submit,
        };
    },
    __typeEmits: {},
    __typeProps: {},
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
    __typeEmits: {},
    __typeProps: {},
});
; /* PartiallyEnd: #4569/main.vue */
