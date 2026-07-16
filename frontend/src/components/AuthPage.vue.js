import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { login, register } from '../auth';
const route = useRoute();
const router = useRouter();
const username = ref('admin');
const password = ref('Admin123!');
const displayName = ref('');
const email = ref('');
const submitting = ref(false);
const error = ref('');
const isRegister = computed(() => route.name === 'register');
async function submit() {
    submitting.value = true;
    error.value = '';
    try {
        if (isRegister.value) {
            await register(username.value, password.value, displayName.value, email.value);
        }
        else {
            await login(username.value, password.value);
        }
        await router.replace('/');
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
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
    ...{ class: "auth-page" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
    ...{ onSubmit: (__VLS_ctx.submit) },
    ...{ class: "auth-card" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({
    ...{ class: "brand" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
(__VLS_ctx.isRegister ? '创建账号' : '欢迎回来');
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.isRegister ? '注册后自动获得普通用户角色' : '登录后按角色加载菜单和卡片权限');
if (__VLS_ctx.isRegister) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
        maxlength: "100",
        autocomplete: "name",
    });
    (__VLS_ctx.displayName);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    required: true,
    maxlength: "32",
    autocomplete: "username",
});
(__VLS_ctx.username);
if (__VLS_ctx.isRegister) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        type: "email",
        maxlength: "150",
        autocomplete: "email",
    });
    (__VLS_ctx.email);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
    type: "password",
    required: true,
    minlength: "8",
    autocomplete: "current-password",
});
(__VLS_ctx.password);
if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "auth-error" },
    });
    (__VLS_ctx.error);
}
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ class: "primary" },
    disabled: (__VLS_ctx.submitting),
});
(__VLS_ctx.submitting ? '处理中…' : __VLS_ctx.isRegister ? '注册并登录' : '登录');
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (...[$event]) => {
            __VLS_ctx.router.push(__VLS_ctx.isRegister ? '/login' : '/register');
        } },
    type: "button",
    ...{ class: "link" },
});
(__VLS_ctx.isRegister ? '已有账号？去登录' : '没有账号？立即注册');
if (!__VLS_ctx.isRegister) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
}
/** @type {__VLS_StyleScopedClasses['auth-page']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-card']} */ ;
/** @type {__VLS_StyleScopedClasses['brand']} */ ;
/** @type {__VLS_StyleScopedClasses['auth-error']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['link']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            router: router,
            username: username,
            password: password,
            displayName: displayName,
            email: email,
            submitting: submitting,
            error: error,
            isRegister: isRegister,
            submit: submit,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
