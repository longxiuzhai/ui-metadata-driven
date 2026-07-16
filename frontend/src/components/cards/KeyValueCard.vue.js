import { formatters } from '../../formatters';
const props = defineProps();
function show(field) {
    const formatter = field.formatter ? formatters[field.formatter] : undefined;
    return formatter ? formatter(props.data[field.key]) : String(props.data[field.key] ?? '-');
}
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['kv']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.dl, __VLS_intrinsicElements.dl)({
    ...{ class: "kv" },
    ...{ style: ({ '--columns': __VLS_ctx.columns ?? 2 }) },
});
for (const [field] of __VLS_getVForSourceType((__VLS_ctx.fields))) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        key: (field.key),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.dt, __VLS_intrinsicElements.dt)({});
    (field.label);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.dd, __VLS_intrinsicElements.dd)({});
    (__VLS_ctx.show(field));
}
/** @type {__VLS_StyleScopedClasses['kv']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            show: show,
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
