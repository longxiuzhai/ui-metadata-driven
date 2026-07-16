<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { apiRequest } from '../api'
import type { AdminMenu, AdminPermission, AdminRole, AdminUser } from '../types'

type Tab = 'users' | 'roles' | 'permissions' | 'menus'
const route = useRoute()
const validTabs: Tab[] = ['users', 'roles', 'permissions', 'menus']
const tab = computed<Tab>(() => {
  const section = String(route.query.section ?? 'users') as Tab
  return validTabs.includes(section) ? section : 'users'
})
const sectionMeta = computed(() => ({
  users: { title: '用户管理', description: '配置账号状态及所属角色。' },
  roles: { title: '角色管理', description: '维护角色，并关联权限与可见菜单。' },
  permissions: { title: '权限配置', description: '定义后端资源与操作权限编码。' },
  menus: { title: '菜单配置', description: '维护菜单入口、排序及访问权限。' }
})[tab.value])
const loading = ref(true)
const error = ref('')
const notice = ref('')
const users = ref<AdminUser[]>([])
const roles = ref<AdminRole[]>([])
const permissions = ref<AdminPermission[]>([])
const menus = ref<AdminMenu[]>([])

const roleForm = reactive({ id: 0, code: '', name: '', description: '', enabled: true,
  permissionIds: [] as number[], menuIds: [] as number[] })
const permissionForm = reactive({ id: 0, code: '', name: '', resource: '', action: '', description: '' })
const menuForm = reactive({ id: 0, parentId: '' as number | '', code: '', name: '', path: '', icon: '',
  sortOrder: 0, permissionCode: '', enabled: true })

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    ;[users.value, roles.value, permissions.value, menus.value] = await Promise.all([
      apiRequest<AdminUser[]>('/api/admin/users'),
      apiRequest<AdminRole[]>('/api/admin/roles'),
      apiRequest<AdminPermission[]>('/api/admin/permissions'),
      apiRequest<AdminMenu[]>('/api/admin/menus')
    ])
  } catch (reason) { error.value = (reason as Error).message }
  finally { loading.value = false }
}

function flash(message: string) { notice.value = message; setTimeout(() => (notice.value = ''), 2200) }
async function saveUser(user: AdminUser) {
  await apiRequest(`/api/admin/users/${user.id}/roles`, {
    method: 'PUT', body: JSON.stringify({ ids: user.roleIds })
  })
  await apiRequest(`/api/admin/users/${user.id}/status`, {
    method: 'PUT', body: JSON.stringify({ enabled: user.enabled })
  })
  flash(`已保存 ${user.username}`)
}

function newRole() { Object.assign(roleForm, { id: 0, code: '', name: '', description: '', enabled: true,
  permissionIds: [], menuIds: [] }) }
function editRole(role: AdminRole) { Object.assign(roleForm, { ...role,
  permissionIds: [...role.permissionIds], menuIds: [...role.menuIds] }) }
async function saveRole() {
  const body = { code: roleForm.code, name: roleForm.name, description: roleForm.description,
    enabled: roleForm.enabled }
  const saved = roleForm.id
    ? await apiRequest<AdminRole>(`/api/admin/roles/${roleForm.id}`, { method: 'PUT', body: JSON.stringify(body) })
    : await apiRequest<AdminRole>('/api/admin/roles', { method: 'POST', body: JSON.stringify(body) })
  await Promise.all([
    apiRequest(`/api/admin/roles/${saved.id}/permissions`, {
      method: 'PUT', body: JSON.stringify({ ids: roleForm.permissionIds })
    }),
    apiRequest(`/api/admin/roles/${saved.id}/menus`, {
      method: 'PUT', body: JSON.stringify({ ids: roleForm.menuIds })
    })
  ])
  await loadAll(); editRole(roles.value.find(role => role.id === saved.id)!); flash('角色已保存')
}
async function deleteRole(id: number) {
  if (!confirm('确定删除该角色？')) return
  await apiRequest(`/api/admin/roles/${id}`, { method: 'DELETE' }); newRole(); await loadAll()
}

function newPermission() { Object.assign(permissionForm,
  { id: 0, code: '', name: '', resource: '', action: '', description: '' }) }
function editPermission(value: AdminPermission) { Object.assign(permissionForm, { ...value, action: value.actionName }) }
async function savePermission() {
  const body = { code: permissionForm.code, name: permissionForm.name, resource: permissionForm.resource,
    action: permissionForm.action, description: permissionForm.description }
  if (permissionForm.id) await apiRequest(`/api/admin/permissions/${permissionForm.id}`,
    { method: 'PUT', body: JSON.stringify(body) })
  else await apiRequest('/api/admin/permissions', { method: 'POST', body: JSON.stringify(body) })
  newPermission(); await loadAll(); flash('权限已保存')
}
async function deletePermission(id: number) {
  if (!confirm('删除权限会同步解除角色关联，确定继续？')) return
  await apiRequest(`/api/admin/permissions/${id}`, { method: 'DELETE' }); newPermission(); await loadAll()
}

function newMenu() { Object.assign(menuForm, { id: 0, parentId: '', code: '', name: '', path: '', icon: '',
  sortOrder: 0, permissionCode: '', enabled: true }) }
function editMenu(value: AdminMenu) { Object.assign(menuForm, { ...value, parentId: value.parentId ?? '' }) }
async function saveMenu() {
  const body = { ...menuForm, parentId: menuForm.parentId === '' ? null : menuForm.parentId }
  if (menuForm.id) await apiRequest(`/api/admin/menus/${menuForm.id}`,
    { method: 'PUT', body: JSON.stringify(body) })
  else await apiRequest('/api/admin/menus', { method: 'POST', body: JSON.stringify(body) })
  newMenu(); await loadAll(); flash('菜单已保存')
}
async function deleteMenu(id: number) {
  if (!confirm('确定删除该菜单？')) return
  await apiRequest(`/api/admin/menus/${id}`, { method: 'DELETE' }); newMenu(); await loadAll()
}

onMounted(loadAll)
</script>

<template>
  <section class="security-admin">
    <header class="admin-toolbar">
      <div><h1>{{ sectionMeta.title }}</h1><p>{{ sectionMeta.description }}</p></div>
      <button class="secondary" @click="loadAll">刷新数据</button>
    </header>
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="notice" class="message ok">{{ notice }}</div>
    <div v-if="loading" class="loading">正在加载权限配置…</div>

    <div v-else-if="tab === 'users'" class="panel">
      <table><thead><tr><th>账号</th><th>名称</th><th>角色</th><th>状态</th><th></th></tr></thead>
        <tbody><tr v-for="user in users" :key="user.id">
          <td data-label="账号"><div class="account-cell"><strong>{{ user.username }}</strong>
            <small>{{ user.email || '未设置邮箱' }}</small></div></td>
          <td data-label="名称">{{ user.displayName }}</td>
          <td data-label="角色"><div class="role-checks"><label v-for="role in roles" :key="role.id" class="check">
            <input v-model="user.roleIds" type="checkbox" :value="role.id" />{{ role.name }}</label></div></td>
          <td data-label="状态"><label class="switch"><input v-model="user.enabled" type="checkbox" />启用</label></td>
          <td data-label="操作"><button @click="saveUser(user)">保存</button></td>
        </tr></tbody></table>
    </div>

    <div v-else-if="tab === 'roles'" class="split">
      <div class="list"><button class="new" @click="newRole">＋ 新建角色</button>
        <button v-for="role in roles" :key="role.id" :class="{ selected: roleForm.id === role.id }"
                @click="editRole(role)"><strong>{{ role.name }}</strong><small>{{ role.code }}</small></button></div>
      <form class="editor" @submit.prevent="saveRole">
        <h2>{{ roleForm.id ? '编辑角色' : '新建角色' }}</h2>
        <div class="form-row"><label>编码<input v-model="roleForm.code" required /></label>
          <label>名称<input v-model="roleForm.name" required /></label></div>
        <label>说明<input v-model="roleForm.description" /></label>
        <label class="switch"><input v-model="roleForm.enabled" type="checkbox" />启用角色</label>
        <fieldset><legend>权限</legend><label v-for="item in permissions" :key="item.id" class="check">
          <input v-model="roleForm.permissionIds" type="checkbox" :value="item.id" />
          <span><strong>{{ item.name }}</strong><small>{{ item.code }}</small></span></label></fieldset>
        <fieldset><legend>菜单</legend><label v-for="item in menus" :key="item.id" class="check">
          <input v-model="roleForm.menuIds" type="checkbox" :value="item.id" />{{ item.name }}</label></fieldset>
        <div class="actions"><button>保存角色</button>
          <button v-if="roleForm.id" type="button" class="danger" @click="deleteRole(roleForm.id)">删除</button></div>
      </form>
    </div>

    <div v-else-if="tab === 'permissions'" class="split">
      <div class="list"><button class="new" @click="newPermission">＋ 新建权限</button>
        <button v-for="item in permissions" :key="item.id" :class="{ selected: permissionForm.id === item.id }"
                @click="editPermission(item)"><strong>{{ item.name }}</strong><small>{{ item.code }}</small></button></div>
      <form class="editor" @submit.prevent="savePermission"><h2>{{ permissionForm.id ? '编辑权限' : '新建权限' }}</h2>
        <label>权限编码<input v-model="permissionForm.code" required placeholder="order:approve" /></label>
        <label>权限名称<input v-model="permissionForm.name" required /></label>
        <div class="form-row"><label>资源<input v-model="permissionForm.resource" required /></label>
          <label>动作<input v-model="permissionForm.action" required /></label></div>
        <label>说明<input v-model="permissionForm.description" /></label>
        <div class="actions"><button>保存权限</button><button v-if="permissionForm.id" type="button" class="danger"
          @click="deletePermission(permissionForm.id)">删除</button></div></form>
    </div>

    <div v-else class="split">
      <div class="list"><button class="new" @click="newMenu">＋ 新建菜单</button>
        <button v-for="item in menus" :key="item.id" :class="{ selected: menuForm.id === item.id }"
                @click="editMenu(item)"><strong>{{ item.name }}</strong><small>{{ item.path }}</small></button></div>
      <form class="editor" @submit.prevent="saveMenu"><h2>{{ menuForm.id ? '编辑菜单' : '新建菜单' }}</h2>
        <div class="form-row"><label>编码<input v-model="menuForm.code" required /></label>
          <label>名称<input v-model="menuForm.name" required /></label></div>
        <label>路径<input v-model="menuForm.path" required placeholder="/orders" /></label>
        <div class="form-row"><label>图标<input v-model="menuForm.icon" /></label>
          <label>排序<input v-model.number="menuForm.sortOrder" type="number" /></label></div>
        <label>所需权限<input v-model="menuForm.permissionCode" list="permission-codes" /></label>
        <datalist id="permission-codes"><option v-for="item in permissions" :key="item.id" :value="item.code" /></datalist>
        <label>父菜单<select v-model="menuForm.parentId"><option value="">无</option>
          <option v-for="item in menus.filter(value => value.id !== menuForm.id)" :key="item.id" :value="item.id">{{ item.name }}</option>
        </select></label>
        <label class="switch"><input v-model="menuForm.enabled" type="checkbox" />启用菜单</label>
        <div class="actions"><button>保存菜单</button><button v-if="menuForm.id" type="button" class="danger"
          @click="deleteMenu(menuForm.id)">删除</button></div></form>
    </div>
  </section>
</template>

<style scoped>
.security-admin { max-width: 1160px; margin: 0 auto; }
.admin-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-bottom: 14px; }
.admin-toolbar h1 { margin: 0 0 4px; color: #24324a; font-size: 20px; }.admin-toolbar p { margin: 0; color: #8491a5; font-size: 12px; }
.panel,.split { border: 1px solid #e2e8f0; border-radius: 15px; background: white; overflow: hidden; }
.split { display: grid; grid-template-columns: 240px 1fr; min-height: 560px; }.list { padding: 12px; background: #f8fafc; border-right: 1px solid #e2e8f0; }
.list button { width: 100%; display: grid; gap: 3px; margin-bottom: 5px; padding: 11px; background: transparent; color: #334155; text-align: left; }
.list button.selected { background: #dbeafe; color: #1d4ed8; }.list .new { margin-bottom: 12px; color: #2563eb; }
small { display: block; color: #94a3b8; }.editor { display: grid; align-content: start; gap: 15px; padding: 26px; }.editor h2 { margin: 0 0 4px; }
label { display: grid; gap: 7px; color: #475569; font-size: 13px; }.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
input,select { padding: 9px 10px; border: 1px solid #cbd5e1; border-radius: 8px; font: inherit; }.switch,.check { display: inline-flex; align-items: center; gap: 7px; margin: 4px 12px 4px 0; }
.switch input,.check input { width: auto; }.check span { display: inline-block; } fieldset { border: 1px solid #e2e8f0; border-radius: 10px; } legend { color: #334155; font-weight: 700; }
button { padding: 8px 12px; border: 0; border-radius: 8px; background: #2563eb; color: white; cursor: pointer; }.secondary { background: #e2e8f0; color: #475569; }
.actions { display: flex; gap: 9px; }.danger { background: #fee2e2; color: #b42318; }.message,.loading { margin: 12px 0; padding: 12px; border-radius: 8px; }.error { background: #fee2e2; color: #b42318; }.ok { background: #dcfce7; color: #15803d; }
table { width: 100%; border-collapse: collapse; } th,td { padding: 14px; border-bottom: 1px solid #e2e8f0; text-align: left; } th { background: #f8fafc; color: #64748b; font-size: 12px; }
@media (max-width: 760px) {
  .admin-toolbar { align-items: flex-start; }
  .admin-toolbar h1 { font-size: 18px; }
  .split { grid-template-columns: 1fr; }
  .list { border-right: 0; border-bottom: 1px solid #e2e8f0; }
  .form-row { grid-template-columns: 1fr; }
  .panel { border: 0; background: transparent; overflow: visible; }
  table,tbody { display: block; }
  thead { display: none; }
  tbody { display: grid; gap: 12px; }
  tr { display: grid; grid-template-columns: 1fr; padding: 10px 12px; border: 1px solid #e2e8f0;
    border-radius: 14px; background: white; box-shadow: 0 6px 20px #1e3a5f0a; }
  td { display: grid; grid-template-columns: 58px minmax(0,1fr); align-items: center; gap: 10px;
    padding: 8px 4px; border-bottom: 1px dashed #e2e8f0; overflow-wrap: anywhere; }
  td::before { content: attr(data-label); color: #94a3b8; font-size: 12px; font-weight: 700; }
  td:last-child { border-bottom: 0; }
  td:last-child button { width: 100%; }
  .account-cell,.role-checks { min-width: 0; }
  .role-checks { display: flex; flex-wrap: wrap; gap: 4px 10px; }
  .role-checks .check { margin: 0; white-space: nowrap; }
  .switch { margin: 0; }
}
</style>
