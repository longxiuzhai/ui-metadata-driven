export const formatters: Record<string, (value: unknown) => string> = {
  'mobile-mask': value => String(value ?? '').replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2'),
  'customer-status': value =>
    ({ ACTIVE: '正常', INACTIVE: '停用' })[String(value)] ?? String(value ?? ''),
  'order-status': value =>
    ({ OPEN: '待处理', PAID: '已支付', CLOSED: '已关闭', CANCELLED: '已取消' })[String(value)]
      ?? String(value ?? ''),
  'member-level': value =>
    ({ GOLD: '金卡', SILVER: '银卡', NORMAL: '普通' })[String(value)] ?? String(value ?? ''),
  'money-cny': value => {
    const amount = Number(value)
    return Number.isFinite(amount)
      ? new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(amount)
      : '-'
  }
}
