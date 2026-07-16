package com.example.metadataui.customization.demo;

import com.example.metadataui.action.exception.ActionConflictException;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DemoCustomerRepository {
    private final Map<String, CustomerSnapshot> customers = new ConcurrentHashMap<>();

    public CustomerSnapshot get(String customerId) {
        return customers.computeIfAbsent(customerId,
                id -> new CustomerSnapshot("示例客户 " + id, "13800138000", "ACTIVE", 1));
    }

    public synchronized CustomerSnapshot update(String customerId, long expectedVersion,
                                                String name, String mobile, String status) {
        CustomerSnapshot current = get(customerId);
        if (current.getVersion() != expectedVersion) {
            throw new ActionConflictException("客户信息已被其他操作修改，请重新加载");
        }
        CustomerSnapshot updated = new CustomerSnapshot(
                name, mobile, status, current.getVersion() + 1);
        customers.put(customerId, updated);
        return updated;
    }

    public static final class CustomerSnapshot {
        private final String name;
        private final String mobile;
        private final String status;
        private final long version;

        public CustomerSnapshot(String name, String mobile, String status, long version) {
            this.name = name;
            this.mobile = mobile;
            this.status = status;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getMobile() {
            return mobile;
        }

        public String getStatus() {
            return status;
        }

        public long getVersion() {
            return version;
        }

        public Map<String, Object> values() {
            return Map.of("name", name, "mobile", mobile, "status", status);
        }
    }
}
