export const formatters: Record<string, (value: unknown) => string> = {
  'mobile-mask': value => String(value ?? '').replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2'),
  'customer-status': value => ({ ACTIVE: '正常', INACTIVE: '停用' }[String(value)] ?? String(value ?? ''))
}

