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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
        return customerMapper.selectList(new LambdaQueryWrapper<BizCustomer>()
                        .eq(BizCustomer::getTenantId, tenantProvider.currentTenantId())
                        .orderByDesc(BizCustomer::getUpdatedAt)
                        .orderByDesc(BizCustomer::getId)).stream()
                .map(this::customerValues)
                .collect(Collectors.toList());
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
        return orders(customerId, null);
    }

    public List<Map<String, Object>> orders(String customerId, String status) {
        LambdaQueryWrapper<BizOrder> query = new LambdaQueryWrapper<BizOrder>()
                        .eq(BizOrder::getTenantId, tenantProvider.currentTenantId())
                        .orderByDesc(BizOrder::getPlacedAt)
                        .orderByDesc(BizOrder::getId);
        if (customerId != null && !customerId.isBlank()) {
            get(customerId);
            query.eq(BizOrder::getCustomerId, customerId);
        }
        if (status != null && !status.isBlank()) query.eq(BizOrder::getOrderStatus, status.trim().toUpperCase());
        return orderMapper.selectList(query).stream()
                .map(this::orderValues).collect(Collectors.toList());
    }

    public Map<String, Object> order(String orderNo) {
        BizOrder order = orderMapper.selectOne(new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getTenantId, tenantProvider.currentTenantId()).eq(BizOrder::getOrderNo, orderNo));
        if (order == null) throw new BusinessDataNotFoundException("订单不存在: " + orderNo);
        return orderValues(order);
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
