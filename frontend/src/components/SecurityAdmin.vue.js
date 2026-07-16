import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { apiRequest } from '../api';
const route = useRoute();
const validTabs = ['users', 'roles', 'permissions', 'menus'];
const tab = computed(() => {
    const section = String(route.query.section ?? 'users');
    return validTabs.includes(section) ? section : 'users';
});
const sectionMeta = computed(() => ({
    users: { title: '用户管理', description: '配置账号状态及所属角色。' },
    roles: { title: '角色管理', description: '维护角色，并关联权限与可见菜单。' },
    permissions: { title: '权限配置', description: '定义后端资源与操作权限编码。' },
    menus: { title: '菜单配置', description: '维护菜单入口、排序及访问权限。' }
})[tab.value]);
const loading = ref(true);
const error = ref('');
const notice = ref('');
const users = ref([]);
const roles = ref([]);
const permissions = ref([]);
const menus = ref([]);
const roleForm = reactive({ id: 0, code: '', name: '', description: '', enabled: true,
    permissionIds: [], menuIds: [] });
const permissionForm = reactive({ id: 0, code: '', name: '', resource: '', action: '', description: '' });
const menuForm = reactive({ id: 0, parentId: '', code: '', name: '', path: '', icon: '',
    sortOrder: 0, permissionCode: '', enabled: true });
async function loadAll() {
    loading.value = true;
    error.value = '';
    try {
        ;
        [users.value, roles.value, permissions.value, menus.value] = await Promise.all([
            apiRequest('/api/admin/users'),
            apiRequest('/api/admin/roles'),
            apiRequest('/api/admin/permissions'),
            apiRequest('/api/admin/menus')
        ]);
    }
    catch (reason) {
        error.value = reason.message;
    }
    finally {
        loading.value = false;
    }
}
function flash(message) { notice.value = message; setTimeout(() => (notice.value = ''), 2200); }
async function saveUser(user) {
    await apiRequest(`/api/admin/users/${user.id}/roles`, {
        method: 'PUT', body: JSON.stringify({ ids: user.roleIds })
    });
    await apiRequest(`/api/admin/users/${user.id}/status`, {
        method: 'PUT', body: JSON.stringify({ enabled: user.enabled })
    });
    flash(`已保存 ${user.username}`);
}
function newRole() {
    Object.assign(roleForm, { id: 0, code: '', name: '', description: '', enabled: true,
        permissionIds: [], menuIds: [] });
}
function editRole(role) {
    Object.assign(roleForm, { ...role,
        permissionIds: [...role.permissionIds], menuIds: [...role.menuIds] });
}
async function saveRole() {
    const body = { code: roleForm.code, name: roleForm.name, description: roleForm.description,
        enabled: roleForm.enabled };
    const saved = roleForm.id
        ? await apiRequest(`/api/admin/roles/${roleForm.id}`, { method: 'PUT', body: JSON.stringify(body) })
        : await apiRequest('/api/admin/roles', { method: 'POST', body: JSON.stringify(body) });
    await Promise.all([
        apiRequest(`/api/admin/roles/${saved.id}/permissions`, {
            method: 'PUT', body: JSON.stringify({ ids: roleForm.permissionIds })
        }),
        apiRequest(`/api/admin/roles/${saved.id}/menus`, {
            method: 'PUT', body: JSON.stringify({ ids: roleForm.menuIds })
        })
    ]);
    await loadAll();
    editRole(roles.value.find(role => role.id === saved.id));
    flash('角色已保存');
}
async function deleteRole(id) {
    if (!confirm('确定删除该角色？'))
        return;
    await apiRequest(`/api/admin/roles/${id}`, { method: 'DELETE' });
    newRole();
    await loadAll();
}
function newPermission() {
    Object.assign(permissionForm, { id: 0, code: '', name: '', resource: '', action: '', description: '' });
}
function editPermission(value) { Object.assign(permissionForm, { ...value, action: value.actionName }); }
async function savePermission() {
    const body = { code: permissionForm.code, name: permissionForm.name, resource: permissionForm.resource,
        action: permissionForm.action, description: permissionForm.description };
    if (permissionForm.id)
        await apiRequest(`/api/admin/permissions/${permissionForm.id}`, { method: 'PUT', body: JSON.stringify(body) });
    else
        await apiRequest('/api/admin/permissions', { method: 'POST', body: JSON.stringify(body) });
    newPermission();
    await loadAll();
    flash('权限已保存');
}
async function deletePermission(id) {
    if (!confirm('删除权限会同步解除角色关联，确定继续？'))
        return;
    await apiRequest(`/api/admin/permissions/${id}`, { method: 'DELETE' });
    newPermission();
    await loadAll();
}
function newMenu() {
    Object.assign(menuForm, { id: 0, parentId: '', code: '', name: '', path: '', icon: '',
        sortOrder: 0, permissionCode: '', enabled: true });
}
function editMenu(value) { Object.assign(menuForm, { ...value, parentId: value.parentId ?? '' }); }
async function saveMenu() {
    const body = { ...menuForm, parentId: menuForm.parentId === '' ? null : menuForm.parentId };
    if (menuForm.id)
        await apiRequest(`/api/admin/menus/${menuForm.id}`, { method: 'PUT', body: JSON.stringify(body) });
    else
        await apiRequest('/api/admin/menus', { method: 'POST', body: JSON.stringify(body) });
    newMenu();
    await loadAll();
    flash('菜单已保存');
}
async function deleteMenu(id) {
    if (!confirm('确定删除该菜单？'))
        return;
    await apiRequest(`/api/admin/menus/${id}`, { method: 'DELETE' });
    newMenu();
    await loadAll();
}
onMounted(loadAll);
debugger; /* PartiallyEnd: #3632/scriptSetup.vue */
const __VLS_ctx = {};
let __VLS_components;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['admin-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['admin-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['split']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['editor']} */ ;
/** @type {__VLS_StyleScopedClasses['switch']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['admin-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['admin-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['split']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['form-row']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['role-checks']} */ ;
/** @type {__VLS_StyleScopedClasses['role-checks']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['switch']} */ ;
// CSS variable injection 
// CSS variable injection end 
__VLS_asFunctionalElement(__VLS_intrinsicElements.section, __VLS_intrinsicElements.section)({
    ...{ class: "security-admin" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.header, __VLS_intrinsicElements.header)({
    ...{ class: "admin-toolbar" },
});
__VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({});
__VLS_asFunctionalElement(__VLS_intrinsicElements.h1, __VLS_intrinsicElements.h1)({});
(__VLS_ctx.sectionMeta.title);
__VLS_asFunctionalElement(__VLS_intrinsicElements.p, __VLS_intrinsicElements.p)({});
(__VLS_ctx.sectionMeta.description);
__VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
    ...{ onClick: (__VLS_ctx.loadAll) },
    ...{ class: "secondary" },
});
if (__VLS_ctx.error) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "message error" },
    });
    (__VLS_ctx.error);
}
if (__VLS_ctx.notice) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "message ok" },
    });
    (__VLS_ctx.notice);
}
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "loading" },
    });
}
else if (__VLS_ctx.tab === 'users') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "panel" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.table, __VLS_intrinsicElements.table)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.thead, __VLS_intrinsicElements.thead)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.th, __VLS_intrinsicElements.th)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.tbody, __VLS_intrinsicElements.tbody)({});
    for (const [user] of __VLS_getVForSourceType((__VLS_ctx.users))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.tr, __VLS_intrinsicElements.tr)({
            key: (user.id),
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            'data-label': "账号",
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "account-cell" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (user.username);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (user.email || '未设置邮箱');
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            'data-label': "名称",
        });
        (user.displayName);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            'data-label': "角色",
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
            ...{ class: "role-checks" },
        });
        for (const [role] of __VLS_getVForSourceType((__VLS_ctx.roles))) {
            __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
                key: (role.id),
                ...{ class: "check" },
            });
            __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
                type: "checkbox",
                value: (role.id),
            });
            (user.roleIds);
            (role.name);
        }
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            'data-label': "状态",
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
            ...{ class: "switch" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
            type: "checkbox",
        });
        (user.enabled);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.td, __VLS_intrinsicElements.td)({
            'data-label': "操作",
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!(__VLS_ctx.tab === 'users'))
                        return;
                    __VLS_ctx.saveUser(user);
                } },
        });
    }
}
else if (__VLS_ctx.tab === 'roles') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "split" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "list" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.newRole) },
        ...{ class: "new" },
    });
    for (const [role] of __VLS_getVForSourceType((__VLS_ctx.roles))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!(__VLS_ctx.tab === 'roles'))
                        return;
                    __VLS_ctx.editRole(role);
                } },
            key: (role.id),
            ...{ class: ({ selected: __VLS_ctx.roleForm.id === role.id }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (role.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (role.code);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
        ...{ onSubmit: (__VLS_ctx.saveRole) },
        ...{ class: "editor" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    (__VLS_ctx.roleForm.id ? '编辑角色' : '新建角色');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "form-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.roleForm.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.roleForm.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({});
    (__VLS_ctx.roleForm.description);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
        ...{ class: "switch" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        type: "checkbox",
    });
    (__VLS_ctx.roleForm.enabled);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.fieldset, __VLS_intrinsicElements.fieldset)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.legend, __VLS_intrinsicElements.legend)({});
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.permissions))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
            key: (item.id),
            ...{ class: "check" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
            type: "checkbox",
            value: (item.id),
        });
        (__VLS_ctx.roleForm.permissionIds);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.span, __VLS_intrinsicElements.span)({});
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (item.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (item.code);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.fieldset, __VLS_intrinsicElements.fieldset)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.legend, __VLS_intrinsicElements.legend)({});
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.menus))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
            key: (item.id),
            ...{ class: "check" },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
            type: "checkbox",
            value: (item.id),
        });
        (__VLS_ctx.roleForm.menuIds);
        (item.name);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "actions" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({});
    if (__VLS_ctx.roleForm.id) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!(__VLS_ctx.tab === 'roles'))
                        return;
                    if (!(__VLS_ctx.roleForm.id))
                        return;
                    __VLS_ctx.deleteRole(__VLS_ctx.roleForm.id);
                } },
            type: "button",
            ...{ class: "danger" },
        });
    }
}
else if (__VLS_ctx.tab === 'permissions') {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "split" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "list" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.newPermission) },
        ...{ class: "new" },
    });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.permissions))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!!(__VLS_ctx.tab === 'roles'))
                        return;
                    if (!(__VLS_ctx.tab === 'permissions'))
                        return;
                    __VLS_ctx.editPermission(item);
                } },
            key: (item.id),
            ...{ class: ({ selected: __VLS_ctx.permissionForm.id === item.id }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (item.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (item.code);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
        ...{ onSubmit: (__VLS_ctx.savePermission) },
        ...{ class: "editor" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    (__VLS_ctx.permissionForm.id ? '编辑权限' : '新建权限');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
        placeholder: "order:approve",
    });
    (__VLS_ctx.permissionForm.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.permissionForm.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "form-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.permissionForm.resource);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.permissionForm.action);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({});
    (__VLS_ctx.permissionForm.description);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "actions" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({});
    if (__VLS_ctx.permissionForm.id) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!!(__VLS_ctx.tab === 'roles'))
                        return;
                    if (!(__VLS_ctx.tab === 'permissions'))
                        return;
                    if (!(__VLS_ctx.permissionForm.id))
                        return;
                    __VLS_ctx.deletePermission(__VLS_ctx.permissionForm.id);
                } },
            type: "button",
            ...{ class: "danger" },
        });
    }
}
else {
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "split" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "list" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
        ...{ onClick: (__VLS_ctx.newMenu) },
        ...{ class: "new" },
    });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.menus))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!!(__VLS_ctx.tab === 'roles'))
                        return;
                    if (!!(__VLS_ctx.tab === 'permissions'))
                        return;
                    __VLS_ctx.editMenu(item);
                } },
            key: (item.id),
            ...{ class: ({ selected: __VLS_ctx.menuForm.id === item.id }) },
        });
        __VLS_asFunctionalElement(__VLS_intrinsicElements.strong, __VLS_intrinsicElements.strong)({});
        (item.name);
        __VLS_asFunctionalElement(__VLS_intrinsicElements.small, __VLS_intrinsicElements.small)({});
        (item.path);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.form, __VLS_intrinsicElements.form)({
        ...{ onSubmit: (__VLS_ctx.saveMenu) },
        ...{ class: "editor" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.h2, __VLS_intrinsicElements.h2)({});
    (__VLS_ctx.menuForm.id ? '编辑菜单' : '新建菜单');
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "form-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.menuForm.code);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
    });
    (__VLS_ctx.menuForm.name);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        required: true,
        placeholder: "/orders",
    });
    (__VLS_ctx.menuForm.path);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "form-row" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({});
    (__VLS_ctx.menuForm.icon);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        type: "number",
    });
    (__VLS_ctx.menuForm.sortOrder);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        list: "permission-codes",
    });
    (__VLS_ctx.menuForm.permissionCode);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.datalist, __VLS_intrinsicElements.datalist)({
        id: "permission-codes",
    });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.permissions))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.option)({
            key: (item.id),
            value: (item.code),
        });
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({});
    __VLS_asFunctionalElement(__VLS_intrinsicElements.select, __VLS_intrinsicElements.select)({
        value: (__VLS_ctx.menuForm.parentId),
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
        value: "",
    });
    for (const [item] of __VLS_getVForSourceType((__VLS_ctx.menus.filter(value => value.id !== __VLS_ctx.menuForm.id)))) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.option, __VLS_intrinsicElements.option)({
            key: (item.id),
            value: (item.id),
        });
        (item.name);
    }
    __VLS_asFunctionalElement(__VLS_intrinsicElements.label, __VLS_intrinsicElements.label)({
        ...{ class: "switch" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.input)({
        type: "checkbox",
    });
    (__VLS_ctx.menuForm.enabled);
    __VLS_asFunctionalElement(__VLS_intrinsicElements.div, __VLS_intrinsicElements.div)({
        ...{ class: "actions" },
    });
    __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({});
    if (__VLS_ctx.menuForm.id) {
        __VLS_asFunctionalElement(__VLS_intrinsicElements.button, __VLS_intrinsicElements.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(__VLS_ctx.tab === 'users'))
                        return;
                    if (!!(__VLS_ctx.tab === 'roles'))
                        return;
                    if (!!(__VLS_ctx.tab === 'permissions'))
                        return;
                    if (!(__VLS_ctx.menuForm.id))
                        return;
                    __VLS_ctx.deleteMenu(__VLS_ctx.menuForm.id);
                } },
            type: "button",
            ...{ class: "danger" },
        });
    }
}
/** @type {__VLS_StyleScopedClasses['security-admin']} */ ;
/** @type {__VLS_StyleScopedClasses['admin-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
/** @type {__VLS_StyleScopedClasses['message']} */ ;
/** @type {__VLS_StyleScopedClasses['error']} */ ;
/** @type {__VLS_StyleScopedClasses['message']} */ ;
/** @type {__VLS_StyleScopedClasses['ok']} */ ;
/** @type {__VLS_StyleScopedClasses['loading']} */ ;
/** @type {__VLS_StyleScopedClasses['panel']} */ ;
/** @type {__VLS_StyleScopedClasses['account-cell']} */ ;
/** @type {__VLS_StyleScopedClasses['role-checks']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['switch']} */ ;
/** @type {__VLS_StyleScopedClasses['split']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['new']} */ ;
/** @type {__VLS_StyleScopedClasses['selected']} */ ;
/** @type {__VLS_StyleScopedClasses['editor']} */ ;
/** @type {__VLS_StyleScopedClasses['form-row']} */ ;
/** @type {__VLS_StyleScopedClasses['switch']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['check']} */ ;
/** @type {__VLS_StyleScopedClasses['actions']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
/** @type {__VLS_StyleScopedClasses['split']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['new']} */ ;
/** @type {__VLS_StyleScopedClasses['selected']} */ ;
/** @type {__VLS_StyleScopedClasses['editor']} */ ;
/** @type {__VLS_StyleScopedClasses['form-row']} */ ;
/** @type {__VLS_StyleScopedClasses['actions']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
/** @type {__VLS_StyleScopedClasses['split']} */ ;
/** @type {__VLS_StyleScopedClasses['list']} */ ;
/** @type {__VLS_StyleScopedClasses['new']} */ ;
/** @type {__VLS_StyleScopedClasses['selected']} */ ;
/** @type {__VLS_StyleScopedClasses['editor']} */ ;
/** @type {__VLS_StyleScopedClasses['form-row']} */ ;
/** @type {__VLS_StyleScopedClasses['form-row']} */ ;
/** @type {__VLS_StyleScopedClasses['switch']} */ ;
/** @type {__VLS_StyleScopedClasses['actions']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
var __VLS_dollars;
const __VLS_self = (await import('vue')).defineComponent({
    setup() {
        return {
            tab: tab,
            sectionMeta: sectionMeta,
            loading: loading,
            error: error,
            notice: notice,
            users: users,
            roles: roles,
            permissions: permissions,
            menus: menus,
            roleForm: roleForm,
            permissionForm: permissionForm,
            menuForm: menuForm,
            loadAll: loadAll,
            saveUser: saveUser,
            newRole: newRole,
            editRole: editRole,
            saveRole: saveRole,
            deleteRole: deleteRole,
            newPermission: newPermission,
            editPermission: editPermission,
            savePermission: savePermission,
            deletePermission: deletePermission,
            newMenu: newMenu,
            editMenu: editMenu,
            saveMenu: saveMenu,
            deleteMenu: deleteMenu,
        };
    },
});
export default (await import('vue')).defineComponent({
    setup() {
        return {};
    },
});
; /* PartiallyEnd: #4569/main.vue */
