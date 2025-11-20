package com.example.ddd.common.utils;

import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 统一ID生成工具
 * 支持多种ID生成策略：
 * 1. Snowflake ID - 分布式唯一ID（推荐用于Long类型）
 * 2. ULID - 可排序的UUID（推荐用于String类型）
 * 3. NanoID - 短小精悍的ID（推荐用于URL友好场景）
 * 4. UUID - 标准UUID
 * 5. 时间戳ID - 简单时间戳ID
 */
@Slf4j
@Singleton
public class IdGenerator {

    // ==================== Snowflake ID ====================
    
    /**
     * Snowflake ID 生成器
     * 64位ID结构：1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
     */
    private static class SnowflakeIdGenerator {
        // 起始时间戳（2024-01-01 00:00:00）
        private static final long START_TIMESTAMP = 1704067200000L;
        
        // 机器ID占用的位数
        private static final long WORKER_ID_BITS = 5L;
        // 数据中心ID占用的位数
        private static final long DATACENTER_ID_BITS = 5L;
        // 序列号占用的位数
        private static final long SEQUENCE_BITS = 12L;
        
        // 机器ID的最大值
        private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
        // 数据中心ID的最大值
        private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
        // 序列号的最大值
        private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
        
        // 机器ID向左移12位
        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        // 数据中心ID向左移17位（12+5）
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        // 时间戳向左移22位（12+5+5）
        private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
        
        private final long workerId;
        private final long datacenterId;
        private long sequence = 0L;
        private long lastTimestamp = -1L;
        
        public SnowflakeIdGenerator(long workerId, long datacenterId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException(
                    String.format("workerId can't be greater than %d or less than 0", MAX_WORKER_ID)
                );
            }
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException(
                    String.format("datacenterId can't be greater than %d or less than 0", MAX_DATACENTER_ID)
                );
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }
        
        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 
                        lastTimestamp - timestamp)
                );
            }
            
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            
            lastTimestamp = timestamp;
            
            return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
        }
        
        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
    
    // ==================== ULID ====================
    
    /**
     * ULID (Universally Unique Lexicographically Sortable Identifier)
     * 26个字符，可排序的UUID替代品
     */
    private static class UlidGenerator {
        private static final char[] ENCODING_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
            'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X',
            'Y', 'Z'
        };
        
        private static final SecureRandom RANDOM = new SecureRandom();
        
        public String generate() {
            long timestamp = System.currentTimeMillis();
            byte[] randomBytes = new byte[10];
            RANDOM.nextBytes(randomBytes);
            
            char[] chars = new char[26];
            
            // 编码时间戳（10个字符）
            encodeTimestamp(chars, timestamp);
            
            // 编码随机数（16个字符）
            encodeRandom(chars, randomBytes);
            
            return new String(chars);
        }
        
        private void encodeTimestamp(char[] chars, long timestamp) {
            chars[9] = ENCODING_CHARS[(int) (timestamp & 0x1F)];
            timestamp >>>= 5;
            for (int i = 8; i >= 0; i--) {
                chars[i] = ENCODING_CHARS[(int) (timestamp & 0x1F)];
                timestamp >>>= 5;
            }
        }
        
        private void encodeRandom(char[] chars, byte[] randomBytes) {
            int index = 10;
            for (int i = 0; i < 10; i += 2) {
                long value = ((randomBytes[i] & 0xFF) << 8) | (randomBytes[i + 1] & 0xFF);
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
                value >>>= 5;
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
                value >>>= 5;
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
                value >>>= 5;
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
                value >>>= 5;
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
                value >>>= 5;
                chars[index++] = ENCODING_CHARS[(int) (value & 0x1F)];
            }
        }
    }
    
    // ==================== NanoID ====================
    
    /**
     * NanoID 生成器
     * 短小精悍，URL友好
     */
    private static class NanoIdGenerator {
        private static final String DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        private static final int DEFAULT_SIZE = 21;
        private static final SecureRandom RANDOM = new SecureRandom();
        
        public String generate() {
            return generate(DEFAULT_SIZE);
        }
        
        public String generate(int size) {
            return generate(size, DEFAULT_ALPHABET);
        }
        
        public String generate(int size, String alphabet) {
            if (alphabet == null || alphabet.length() == 0) {
                throw new IllegalArgumentException("Alphabet must not be empty");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Size must be greater than 0");
            }
            
            StringBuilder id = new StringBuilder(size);
            byte[] bytes = new byte[size];
            RANDOM.nextBytes(bytes);
            
            for (int i = 0; i < size; i++) {
                id.append(alphabet.charAt(Math.abs(bytes[i]) % alphabet.length()));
            }
            
            return id.toString();
        }
    }
    
    // ==================== 实例变量 ====================
    
    private final SnowflakeIdGenerator snowflake;
    private final UlidGenerator ulidGenerator;
    private final NanoIdGenerator nanoIdGenerator;
    private final AtomicLong timestampIdCounter = new AtomicLong(0);
    
    public IdGenerator() {
        // 初始化 Snowflake（使用MAC地址生成workerId和datacenterId）
        long workerId = getWorkerId();
        long datacenterId = getDatacenterId();
        this.snowflake = new SnowflakeIdGenerator(workerId, datacenterId);
        this.ulidGenerator = new UlidGenerator();
        this.nanoIdGenerator = new NanoIdGenerator();
        
        log.info("IdGenerator initialized: workerId={}, datacenterId={}", workerId, datacenterId);
    }
    
    /**
     * 获取机器ID（基于MAC地址）
     */
    private long getWorkerId() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(mac);
                    return Math.abs(buffer.getLong()) % 32;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get MAC address, using random workerId", e);
        }
        return new SecureRandom().nextInt(32);
    }
    
    /**
     * 获取数据中心ID（基于主机名）
     */
    private long getDatacenterId() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            return Math.abs(hostname.hashCode()) % 32;
        } catch (Exception e) {
            log.warn("Failed to get hostname, using random datacenterId", e);
            return new SecureRandom().nextInt(32);
        }
    }
    
    // ==================== 公共API ====================
    
    /**
     * 生成 Snowflake ID（Long类型，推荐用于数据库主键）
     * 
     * @return 64位Long类型的唯一ID
     */
    public long nextSnowflakeId() {
        return snowflake.nextId();
    }
    
    /**
     * 生成 ULID（String类型，可排序，推荐用于需要排序的场景）
     * 
     * @return 26个字符的ULID字符串
     */
    public String nextUlid() {
        return ulidGenerator.generate();
    }
    
    /**
     * 生成 NanoID（String类型，短小精悍，URL友好）
     * 
     * @return 21个字符的NanoID字符串（默认）
     */
    public String nextNanoId() {
        return nanoIdGenerator.generate();
    }
    
    /**
     * 生成指定长度的NanoID
     * 
     * @param size ID长度
     * @return NanoID字符串
     */
    public String nextNanoId(int size) {
        return nanoIdGenerator.generate(size);
    }
    
    /**
     * 生成标准UUID（String类型）
     * 
     * @return UUID字符串（带连字符）
     */
    public String nextUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成不带连字符的UUID
     * 
     * @return UUID字符串（不带连字符）
     */
    public String nextUuidWithoutHyphens() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成时间戳ID（Long类型，简单但可能冲突）
     * 格式：时间戳 + 序列号（后4位）
     * 
     * @return 时间戳ID
     */
    public long nextTimestampId() {
        long timestamp = System.currentTimeMillis();
        long sequence = timestampIdCounter.incrementAndGet() % 10000;
        return timestamp * 10000 + sequence;
    }
    
    /**
     * 生成任务ID（String类型，格式：task_ + ULID）
     * 
     * @return 任务ID
     */
    public String nextTaskId() {
        return "task_" + nextUlid();
    }
    
    /**
     * 生成会话ID（String类型，格式：session_ + ULID）
     * 
     * @return 会话ID
     */
    public String nextSessionId() {
        return "session_" + nextUlid();
    }
    
    /**
     * 生成用户ID（String类型，格式：user_ + ULID）
     * 
     * @return 用户ID
     */
    public String nextUserId() {
        return "user_" + nextUlid();
    }
    
    /**
     * 生成文档ID（String类型，格式：doc_ + ULID）
     * 
     * @return 文档ID
     */
    public String nextDocId() {
        return "doc_" + nextUlid();
    }
    
    /**
     * 根据类型生成ID
     * 
     * @param idType ID类型
     * @return ID（Long或String）
     */
    public Object nextId(IdType idType) {
        return switch (idType) {
            case SNOWFLAKE -> nextSnowflakeId();
            case ULID -> nextUlid();
            case NANOID -> nextNanoId();
            case UUID -> nextUuid();
            case UUID_WITHOUT_HYPHENS -> nextUuidWithoutHyphens();
            case TIMESTAMP -> nextTimestampId();
            case TASK -> nextTaskId();
            case SESSION -> nextSessionId();
            case USER -> nextUserId();
            case DOC -> nextDocId();
        };
    }
    
    /**
     * ID类型枚举
     */
    public enum IdType {
        SNOWFLAKE,          // Snowflake ID (Long)
        ULID,               // ULID (String, 可排序)
        NANOID,             // NanoID (String, URL友好)
        UUID,               // UUID (String, 带连字符)
        UUID_WITHOUT_HYPHENS, // UUID (String, 不带连字符)
        TIMESTAMP,          // 时间戳ID (Long)
        TASK,               // 任务ID (String)
        SESSION,            // 会话ID (String)
        USER,               // 用户ID (String)
        DOC                 // 文档ID (String)
    }
}

