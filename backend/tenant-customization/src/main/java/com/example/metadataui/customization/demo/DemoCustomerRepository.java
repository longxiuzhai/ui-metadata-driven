package com.example.metadataui.customization.demo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.metadataui.action.exception.ActionConflictException;
import com.example.metadataui.card.context.CurrentTenantProvider;
import com.example.metadataui.customization.demo.entity.BizCustomer;
import com.example.metadataui.customization.demo.entity.BizCustomerBehaviorTrace;
import com.example.metadataui.customization.demo.entity.BizCustomerFriendTag;
import com.example.metadataui.customization.demo.entity.BizMember;
import com.example.metadataui.customization.demo.entity.BizOrder;
import com.example.metadataui.customization.demo.mapper.BizCustomerBehaviorTraceMapper;
import com.example.metadataui.customization.demo.mapper.BizCustomerFriendTagMapper;
import com.example.metadataui.customization.demo.mapper.BizCustomerMapper;
import com.example.metadataui.customization.demo.mapper.BizMemberMapper;
import com.example.metadataui.customization.demo.mapper.BizOrderMapper;
import com.example.metadataui.customization.demo.query.CustomerListQuery;
import com.example.metadataui.customization.demo.query.OrderListQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class DemoCustomerRepository {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CurrentTenantProvider tenantProvider;
    private final BizCustomerMapper customerMapper;
    private final BizCustomerFriendTagMapper friendTagMapper;
    private final BizCustomerBehaviorTraceMapper behaviorTraceMapper;
    private final BizOrderMapper orderMapper;
    private final BizMemberMapper memberMapper;

    public DemoCustomerRepository(CurrentTenantProvider tenantProvider,
                                  BizCustomerMapper customerMapper,
                                  BizCustomerFriendTagMapper friendTagMapper,
                                  BizCustomerBehaviorTraceMapper behaviorTraceMapper,
                                  BizOrderMapper orderMapper,
                                  BizMemberMapper memberMapper) {
        this.tenantProvider = tenantProvider;
        this.customerMapper = customerMapper;
        this.friendTagMapper = friendTagMapper;
        this.behaviorTraceMapper = behaviorTraceMapper;
        this.orderMapper = orderMapper;
        this.memberMapper = memberMapper;
    }

    public CustomerSnapshot get(String customerId) {
        BizCustomer customer = customerMapper.selectOne(new LambdaQueryWrapper<BizCustomer>()
                .eq(BizCustomer::getTenantId, tenantProvider.currentTenantId())
                .eq(BizCustomer::getCustomerId, customerId));
        if (customer == null) throw new BusinessDataNotFoundException("客户不存在: " + customerId);
        return new CustomerSnapshot(customer);
    }

    public List<Map<String, Object>> customers() {
        return customers(new CustomerListQuery());
    }

    public List<Map<String, Object>> customers(CustomerListQuery criteria) {
        LambdaQueryWrapper<BizCustomer> query = new LambdaQueryWrapper<BizCustomer>()
                .eq(BizCustomer::getTenantId, tenantProvider.currentTenantId());
        if (hasText(criteria.getCustomerId())) {
            query.eq(BizCustomer::getCustomerId, criteria.getCustomerId().trim());
        }
        if (hasText(criteria.getName())) query.like(BizCustomer::getName, criteria.getName().trim());
        if (hasText(criteria.getMobile())) query.eq(BizCustomer::getMobile, criteria.getMobile().trim());
        String status = normalizedStatus(criteria.getStatus(), List.of("ACTIVE", "INACTIVE"));
        if (status != null) query.eq(BizCustomer::getStatus, status);
        return customerMapper.selectList(query
                        .orderByDesc(BizCustomer::getUpdatedAt)
                        .orderByDesc(BizCustomer::getId)).stream()
                .map(this::customerValues)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerSnapshot createCustomer(String customerId, String name, String unionId,
                                           String mobile, String status) {
        LocalDateTime now = LocalDateTime.now();
        BizCustomer customer = new BizCustomer();
        customer.setTenantId(tenantProvider.currentTenantId());
        customer.setCustomerId(customerId);
        customer.setName(name);
        customer.setUnionId(unionId == null || unionId.isBlank() ? null : unionId);
        customer.setMobile(mobile);
        customer.setStatus(status);
        customer.setVersion(1L);
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        customerMapper.insert(customer);
        return new CustomerSnapshot(customer);
    }

    @Transactional
    public CustomerSnapshot update(String customerId, long expectedVersion,
                                   String name, String mobile, String status) {
        BizCustomer current = get(customerId).customer;
        int changed = customerMapper.update(null, new LambdaUpdateWrapper<BizCustomer>()
                .eq(BizCustomer::getId, current.getId())
                .eq(BizCustomer::getVersion, expectedVersion)
                .set(BizCustomer::getName, name)
                .set(BizCustomer::getMobile, mobile)
                .set(BizCustomer::getStatus, status)
                .set(BizCustomer::getVersion, expectedVersion + 1)
                .set(BizCustomer::getUpdatedAt, LocalDateTime.now()));
        if (changed == 0) throw new ActionConflictException("客户信息已被其他操作修改，请重新加载");
        return get(customerId);
    }

    @Transactional
    public void deleteCustomer(String customerId) {
        BizCustomer customer = get(customerId).customer;
        long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getTenantId, tenantProvider.currentTenantId())
                .eq(BizOrder::getCustomerId, customerId));
        if (orderCount > 0) {
            throw new ActionConflictException("客户存在关联订单，请先删除订单");
        }
        customerMapper.deleteById(customer.getId());
    }

    public List<Map<String, String>> tags(String customerId) {
        Long customerPkId = get(customerId).customer.getId();
        return friendTagMapper.selectList(new LambdaQueryWrapper<BizCustomerFriendTag>()
                        .eq(BizCustomerFriendTag::getCustomerPkId, customerPkId)
                        .orderByAsc(BizCustomerFriendTag::getSortOrder)
                        .orderByAsc(BizCustomerFriendTag::getId)).stream()
                .map(tag -> Map.of("name", tag.getTagName(), "color", tag.getColor()))
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> traces(String customerId) {
        Long customerPkId = get(customerId).customer.getId();
        return behaviorTraceMapper.selectList(new LambdaQueryWrapper<BizCustomerBehaviorTrace>()
                        .eq(BizCustomerBehaviorTrace::getCustomerPkId, customerPkId)
                        .orderByDesc(BizCustomerBehaviorTrace::getOccurredAt)
                        .orderByDesc(BizCustomerBehaviorTrace::getId)).stream()
                .map(trace -> {
                    Map<String, String> value = new LinkedHashMap<>();
                    value.put("timestamp", DATE_TIME.format(trace.getOccurredAt()));
                    value.put("eventName", trace.getEventName());
                    value.put("description", trace.getDescription());
                    return value;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> orders(String customerId) {
        OrderListQuery query = new OrderListQuery();
        query.setCustomerId(customerId);
        return orders(query);
    }

    public List<Map<String, Object>> orders(String customerId, String status) {
        OrderListQuery query = new OrderListQuery();
        query.setCustomerId(customerId);
        query.setStatus(status);
        return orders(query);
    }

    public List<Map<String, Object>> orders(OrderListQuery criteria) {
        LambdaQueryWrapper<BizOrder> query = new LambdaQueryWrapper<BizOrder>()
                        .eq(BizOrder::getTenantId, tenantProvider.currentTenantId())
                        .orderByDesc(BizOrder::getPlacedAt)
                        .orderByDesc(BizOrder::getId);
        if (hasText(criteria.getCustomerId())) {
            String customerId = criteria.getCustomerId().trim();
            get(customerId);
            query.eq(BizOrder::getCustomerId, customerId);
        }
        if (hasText(criteria.getOrderNo())) query.eq(BizOrder::getOrderNo, criteria.getOrderNo().trim());
        if (hasText(criteria.getMemberId())) query.eq(BizOrder::getMemberId, criteria.getMemberId().trim());
        String status = normalizedStatus(criteria.getStatus(), List.of("OPEN", "PAID", "CLOSED"));
        if (status != null) query.eq(BizOrder::getOrderStatus, status);
        return orderMapper.selectList(query).stream()
                .map(this::orderValues).collect(Collectors.toList());
    }

    public Map<String, Object> order(String orderNo) {
        BizOrder order = orderMapper.selectOne(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getTenantId, tenantProvider.currentTenantId()).eq(BizOrder::getOrderNo, orderNo));
        if (order == null) throw new BusinessDataNotFoundException("订单不存在: " + orderNo);
        return orderValues(order);
    }

    @Transactional
    public Map<String, Object> createOrder(String orderNo, String customerId, String memberId,
                                           BigDecimal amount, String status, LocalDateTime placedAt) {
        get(customerId);
        if (memberId != null && !memberId.isBlank()) member(memberId);
        LocalDateTime now = LocalDateTime.now();
        BizOrder order = new BizOrder();
        order.setTenantId(tenantProvider.currentTenantId());
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setMemberId(memberId == null || memberId.isBlank() ? null : memberId);
        order.setOrderAmount(amount);
        order.setOrderStatus(status);
        order.setPlacedAt(placedAt);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderMapper.insert(order);
        return orderValues(order);
    }

    @Transactional
    public Map<String, Object> updateOrder(String orderNo, String customerId, String memberId,
                                           BigDecimal amount, String status, LocalDateTime placedAt) {
        BizOrder order = orderMapper.selectOne(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getTenantId, tenantProvider.currentTenantId())
                .eq(BizOrder::getOrderNo, orderNo));
        if (order == null) throw new BusinessDataNotFoundException("订单不存在: " + orderNo);
        get(customerId);
        if (memberId != null && !memberId.isBlank()) member(memberId);
        order.setCustomerId(customerId);
        order.setMemberId(memberId == null || memberId.isBlank() ? null : memberId);
        order.setOrderAmount(amount);
        order.setOrderStatus(status);
        order.setPlacedAt(placedAt);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return orderValues(order);
    }

    @Transactional
    public void deleteOrder(String orderNo) {
        BizOrder order = orderMapper.selectOne(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getTenantId, tenantProvider.currentTenantId())
                .eq(BizOrder::getOrderNo, orderNo));
        if (order == null) throw new BusinessDataNotFoundException("订单不存在: " + orderNo);
        orderMapper.deleteById(order.getId());
    }

    public Map<String, Object> member(String memberId) {
        BizMember member = memberMapper.selectOne(new LambdaQueryWrapper<BizMember>()
                .eq(BizMember::getTenantId, tenantProvider.currentTenantId()).eq(BizMember::getMemberId, memberId));
        if (member == null) throw new BusinessDataNotFoundException("会员不存在: " + memberId);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("memberId", member.getMemberId());
        value.put("unionId", member.getUnionId());
        value.put("mobile", member.getMobile());
        value.put("memberLevel", member.getMemberLevel());
        value.put("status", member.getStatus());
        return value;
    }

    private Map<String, Object> orderValues(BizOrder order) {
        BizMember member = order.getMemberId() == null ? null : memberMapper.selectOne(
                new LambdaQueryWrapper<BizMember>()
                        .eq(BizMember::getTenantId, tenantProvider.currentTenantId())
                        .eq(BizMember::getMemberId, order.getMemberId()));
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderNo", order.getOrderNo());
        value.put("customerId", order.getCustomerId());
        value.put("memberId", order.getMemberId());
        value.put("memberUnionId", member == null ? null : member.getUnionId());
        value.put("memberMobile", member == null ? null : member.getMobile());
        value.put("memberLevel", member == null ? null : member.getMemberLevel());
        value.put("memberStatus", member == null ? null : member.getStatus());
        value.put("orderAmount", order.getOrderAmount());
        value.put("orderStatus", order.getOrderStatus());
        value.put("placedAt", DATE_TIME.format(order.getPlacedAt()));
        value.put("createdAt", DATE_TIME.format(order.getCreatedAt()));
        value.put("updatedAt", DATE_TIME.format(order.getUpdatedAt()));
        return value;
    }

    private Map<String, Object> customerValues(BizCustomer customer) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("customerId", customer.getCustomerId());
        value.put("name", customer.getName());
        value.put("unionId", customer.getUnionId());
        value.put("mobile", customer.getMobile());
        value.put("status", customer.getStatus());
        value.put("createdAt", DATE_TIME.format(customer.getCreatedAt()));
        value.put("updatedAt", DATE_TIME.format(customer.getUpdatedAt()));
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizedStatus(String value, List<String> allowed) {
        if (!hasText(value)) return null;
        String normalized = value.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("不支持的状态: " + value);
        }
        return normalized;
    }

    public static final class CustomerSnapshot {
        private final BizCustomer customer;

        private CustomerSnapshot(BizCustomer customer) { this.customer = customer; }

        public String getName() { return customer.getName(); }
        public String getMobile() { return customer.getMobile(); }
        public String getStatus() { return customer.getStatus(); }
        public long getVersion() { return customer.getVersion(); }

        public Map<String, Object> values() {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("customerId", customer.getCustomerId());
            value.put("name", customer.getName());
            value.put("unionId", customer.getUnionId());
            value.put("mobile", customer.getMobile());
            value.put("status", customer.getStatus());
            return value;
        }
    }
}
