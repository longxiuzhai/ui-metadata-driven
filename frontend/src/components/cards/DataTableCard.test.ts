// @vitest-environment jsdom
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import type { CardAction } from '../../types'
import DataTableCard from './DataTableCard.vue'

const actions: CardAction[] = [
  { code: 'create', label: '新增', type: 'open-form', target: {}, params: {} },
  { code: 'view', label: '查看', type: 'navigate', target: {}, params: {} },
  { code: 'edit', label: '修改', type: 'open-form', target: {}, params: {} },
  { code: 'delete', label: '删除', type: 'execute', target: {}, params: {} }
]

describe('DataTableCard', () => {
  it('renders table and row actions with frozen columns', async () => {
    const wrapper = mount(DataTableCard, {
      props: {
        data: [{ id: '1', name: '测试客户' }],
        columns: [
          { key: 'id', label: 'ID', width: 100, fixed: 'left' as const },
          { key: 'name', label: '名称', width: 180 }
        ],
        actions,
        tableActionCodes: ['create'],
        rowActionCodes: ['view', 'edit', 'delete']
      },
      slots: {
        'toolbar-after': '<button class="query-from-page">查询</button>'
      }
    })

    expect(wrapper.find('.toolbar-action').text()).toBe('新增')
    expect(wrapper.find('.query-from-page').text()).toBe('查询')
    expect(wrapper.findAll('.table-actions button').map(button => button.text())).toEqual(['新增', '查询'])
    expect(wrapper.findAll('.row-action').map(button => button.text())).toEqual(['查看', '修改', '删除'])
    expect(wrapper.find('th.fixed-left').attributes('style')).toContain('left: 0px')
    expect(wrapper.find('th.operation-column').classes()).toContain('fixed-right')

    await wrapper.find('.toolbar-action').trigger('click')
    expect(wrapper.emitted('action')?.[0]).toEqual([actions[0], {}])
  })

  it('asks for confirmation before a destructive row action', async () => {
    const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false)
    const wrapper = mount(DataTableCard, {
      props: {
        data: [{ id: '1' }],
        columns: [{ key: 'id', label: 'ID' }],
        actions,
        rowActionCodes: ['delete'],
        rowActionConfirmations: { delete: '确认删除？' }
      }
    })

    await wrapper.find('.row-action').trigger('click')
    expect(confirm).toHaveBeenCalledWith('确认删除？')
    expect(wrapper.emitted('action')).toBeUndefined()
    confirm.mockRestore()
  })
})
