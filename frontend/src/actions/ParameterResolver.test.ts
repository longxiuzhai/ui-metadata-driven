import { describe, expect, it } from 'vitest'
import { resolveParameters } from './ParameterResolver'

describe('resolveParameters', () => {
  it('resolves page context and literal parameters', () => {
    expect(
      resolveParameters(
        {
          customerId: { source: 'page-context', path: 'customer.id', required: true },
          status: { source: 'literal', value: 'OPEN', required: false }
        },
        {
          pageContext: { customer: { id: '1001' } },
          cardData: {},
          account: {}
        }
      )
    ).toEqual({ customerId: '1001', status: 'OPEN' })
  })

  it('rejects missing required values', () => {
    expect(() =>
      resolveParameters(
        { customerId: { source: 'page-context', path: 'customerId', required: true } },
        { pageContext: {}, cardData: {}, account: {} }
      )
    ).toThrow('动作参数缺失：customerId')
  })

  it('resolves navigation parameters from the selected table row', () => {
    expect(
      resolveParameters(
        { customerId: { source: 'card-data', path: 'customerId', required: true } },
        { pageContext: {}, cardData: { orderNo: 'O1001', customerId: '1001' }, account: {} }
      )
    ).toEqual({ customerId: '1001' })
  })

  it('does not traverse unsafe object paths', () => {
    expect(() =>
      resolveParameters(
        { value: { source: 'card-data', path: '__proto__.value', required: true } },
        { pageContext: {}, cardData: {}, account: {} }
      )
    ).toThrow('动作参数缺失：value')
  })
})
