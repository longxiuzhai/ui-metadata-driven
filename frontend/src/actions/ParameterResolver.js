const unsafeSegments = new Set(['__proto__', 'prototype', 'constructor']);
function readPath(source, path) {
    if (!path) {
        return undefined;
    }
    return path.split('.').reduce((current, segment) => {
        if (unsafeSegments.has(segment) || current == null || typeof current !== 'object') {
            return undefined;
        }
        return Object.prototype.hasOwnProperty.call(current, segment)
            ? current[segment]
            : undefined;
    }, source);
}
export function resolveParameters(bindings = {}, sources) {
    return Object.fromEntries(Object.entries(bindings).map(([name, binding]) => {
        let value;
        if (binding.source === 'literal')
            value = binding.value;
        if (binding.source === 'page-context')
            value = readPath(sources.pageContext, binding.path);
        if (binding.source === 'card-data')
            value = readPath(sources.cardData, binding.path);
        if (binding.source === 'account')
            value = readPath(sources.account, binding.path);
        if (binding.required && (value == null || value === '')) {
            throw new Error(`动作参数缺失：${name}`);
        }
        return [name, value == null ? '' : String(value)];
    }));
}
