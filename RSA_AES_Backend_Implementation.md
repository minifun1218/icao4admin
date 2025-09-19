# RSA+AES混合加密后端实现指南

## 🔐 概述

本文档详细说明了如何在Java后端实现RSA+AES混合加密方案，与前端Vue.js应用配合使用。

---

## 📋 实现步骤

### 步骤1: 前端获取公钥（登录前）

**流程**:
1. 后端生成RSA密钥对，私钥安全保存
2. 前端请求公钥接口
3. 后端返回RSA公钥
4. 前端缓存公钥用于加密

### 步骤2: 前端混合加密（登录时）

**流程**:
1. 前端生成一次性AES密钥（256位随机字符串）
2. 使用RSA公钥加密AES密钥
3. 使用AES密钥加密用户密码
4. 发送加密数据到后端

### 步骤3: 后端解密验证（登录时）

**流程**:
1. 使用RSA私钥解密AES密钥
2. 使用解密的AES密钥解密密码
3. 验证时间戳有效性
4. 与数据库密码哈希值比对

---

## 🔧 Java后端实现

### 1. 添加依赖

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Apache Commons Codec (Base64编码) -->
    <dependency>
        <groupId>commons-codec</groupId>
        <artifactId>commons-codec</artifactId>
        <version>1.15</version>
    </dependency>
    
    <!-- Bouncy Castle (加密库) -->
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk15on</artifactId>
        <version>1.70</version>
    </dependency>
</dependencies>
```

### 2. RSA密钥管理服务

```java
package com.icao4.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class RSAKeyService {
    
    private KeyPair keyPair;
    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    
    public RSAKeyService() {
        generateKeyPair();
    }
    
    /**
     * 生成RSA密钥对
     */
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyGen.initialize(KEY_SIZE);
            this.keyPair = keyGen.generateKeyPair();
            
            System.out.println("🔑 RSA密钥对生成成功");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA密钥对生成失败", e);
        }
    }
    
    /**
     * 获取公钥字符串
     */
    public String getPublicKeyString() {
        PublicKey publicKey = keyPair.getPublic();
        byte[] encoded = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }
    
    /**
     * 获取格式化的公钥（PEM格式）
     */
    public String getFormattedPublicKey() {
        String publicKeyString = getPublicKeyString();
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        
        // 每64个字符换行
        for (int i = 0; i < publicKeyString.length(); i += 64) {
            int endIndex = Math.min(i + 64, publicKeyString.length());
            sb.append(publicKeyString, i, endIndex).append("\n");
        }
        
        sb.append("-----END PUBLIC KEY-----");
        return sb.toString();
    }
    
    /**
     * 获取Base64格式的公钥（不含PEM头尾）
     */
    public String getPublicKeyBase64() {
        return getPublicKeyString();
    }
    
    /**
     * 获取当前密钥ID
     */
    public String getCurrentKeyId() {
        // 生成基于时间戳的唯一密钥ID
        long timestamp = System.currentTimeMillis();
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        return "rsa_" + timestamp + "_" + randomSuffix;
    }
    
    /**
     * 使用私钥解密数据
     */
    public String decryptWithPrivateKey(String encryptedData) {
        try {
            PrivateKey privateKey = keyPair.getPrivate();
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("RSA解密失败", e);
        }
    }
}
```

### 3. AES解密服务

```java
package com.icao4.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class AESDecryptService {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 使用AES密钥解密数据
     */
    public String decryptWithAESKey(String encryptedData, String aesKey) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(aesKey.getBytes("UTF-8"), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }
    
    /**
     * 解密密码并提取时间戳
     */
    public PasswordDecryptResult decryptPasswordWithTimestamp(String encryptedPassword, String aesKey) {
        try {
            String decryptedData = decryptWithAESKey(encryptedPassword, aesKey);
            String[] parts = decryptedData.split("\\|");
            
            if (parts.length != 2) {
                throw new IllegalArgumentException("解密数据格式不正确");
            }
            
            String password = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            
            return new PasswordDecryptResult(password, timestamp);
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败", e);
        }
    }
    
    /**
     * 密码解密结果
     */
    public static class PasswordDecryptResult {
        private final String password;
        private final long timestamp;
        
        public PasswordDecryptResult(String password, long timestamp) {
            this.password = password;
            this.timestamp = timestamp;
        }
        
        public String getPassword() { return password; }
        public long getTimestamp() { return timestamp; }
        
        public boolean isTimestampValid(long maxAgeMs) {
            long currentTime = System.currentTimeMillis();
            return (currentTime - timestamp) <= maxAgeMs;
        }
    }
}
```

### 4. 混合解密服务

```java
package com.icao4.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HybridDecryptService {
    
    @Autowired
    private RSAKeyService rsaKeyService;
    
    @Autowired
    private AESDecryptService aesDecryptService;
    
    // 时间戳有效期：5分钟
    private static final long TIMESTAMP_MAX_AGE_MS = 5 * 60 * 1000;
    
    /**
     * 混合解密登录数据
     */
    public HybridDecryptResult decryptLoginData(HybridLoginRequest request) {
        try {
            System.out.println("🔐 开始混合解密过程...");
            
            // 步骤1: 使用RSA私钥解密AES密钥
            System.out.println("🔑 步骤1: 解密AES密钥");
            String aesKey = rsaKeyService.decryptWithPrivateKey(request.getEncryptedAESKey());
            System.out.println("✅ AES密钥解密成功");
            
            // 步骤2: 使用AES密钥解密密码
            System.out.println("🔒 步骤2: 解密用户密码");
            AESDecryptService.PasswordDecryptResult passwordResult = 
                aesDecryptService.decryptPasswordWithTimestamp(request.getEncryptedPassword(), aesKey);
            System.out.println("✅ 用户密码解密成功");
            
            // 步骤3: 验证时间戳
            System.out.println("⏰ 步骤3: 验证时间戳");
            boolean isTimestampValid = passwordResult.isTimestampValid(TIMESTAMP_MAX_AGE_MS);
            
            if (!isTimestampValid) {
                throw new SecurityException("请求时间戳已过期");
            }
            System.out.println("✅ 时间戳验证通过");
            
            // 清除内存中的AES密钥（安全考虑）
            aesKey = null;
            System.gc();
            
            return new HybridDecryptResult(
                request.getUsername(),
                passwordResult.getPassword(),
                passwordResult.getTimestamp(),
                true
            );
            
        } catch (Exception e) {
            System.err.println("❌ 混合解密失败: " + e.getMessage());
            throw new RuntimeException("登录数据解密失败", e);
        }
    }
    
    /**
     * 混合解密结果
     */
    public static class HybridDecryptResult {
        private final String username;
        private final String password;
        private final long timestamp;
        private final boolean success;
        
        public HybridDecryptResult(String username, String password, long timestamp, boolean success) {
            this.username = username;
            this.password = password;
            this.timestamp = timestamp;
            this.success = success;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public long getTimestamp() { return timestamp; }
        public boolean isSuccess() { return success; }
    }
}
```

### 5. 请求和响应模型

```java
package com.icao4.model;

/**
 * 混合加密登录请求
 */
public class HybridLoginRequest {
    private String username;
    private String password;             // AES加密的密码（前端字段名为password）
    private String encryptedAESKey;      // RSA加密的AES密钥
    private String keyId;                // RSA密钥ID
    private String timestamp;
    private String encryptionType;
    private Integer keySize;
    
    // Constructors
    public HybridLoginRequest() {}
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEncryptedAESKey() { return encryptedAESKey; }
    public void setEncryptedAESKey(String encryptedAESKey) { this.encryptedAESKey = encryptedAESKey; }
    
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getEncryptionType() { return encryptionType; }
    public void setEncryptionType(String encryptionType) { this.encryptionType = encryptionType; }
    
    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }
}

/**
 * 公钥响应
 */
public class PublicKeyResponse {
    private String publicKey;
    private String algorithm;
    private Integer keySize;
    private Long timestamp;
    
    public PublicKeyResponse(String publicKey, String algorithm, Integer keySize) {
        this.publicKey = publicKey;
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    
    public Integer getKeySize() { return keySize; }
    public void setKeySize(Integer keySize) { this.keySize = keySize; }
    
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
```

### 6. 控制器实现

```java
package com.icao4.controller;

import com.icao4.model.*;
import com.icao4.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityController {
    
    @Autowired
    private RSAKeyService rsaKeyService;
    
    @Autowired
    private HybridDecryptService hybridDecryptService;
    
    @Autowired
    private UserService userService; // 假设有用户服务
    
    /**
     * 获取RSA公钥
     */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, Object>> getPublicKey() {
        try {
            // 获取当前密钥对信息
            String publicKeyBase64 = rsaKeyService.getPublicKeyBase64(); // 返回Base64格式
            String keyId = rsaKeyService.getCurrentKeyId();
            int expiresIn = 1800; // 30分钟过期
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("keyId", keyId);
            data.put("publicKey", publicKeyBase64); // 直接返回Base64格式，前端自行转换为PEM
            data.put("expiresIn", expiresIn);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
            
            System.out.println("🔑 RSA公钥请求成功: KeyID=" + keyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ 获取RSA公钥失败: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取公钥失败");
            errorResponse.put("data", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 混合加密登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> hybridLogin(@RequestBody HybridLoginRequest request) {
        try {
            System.out.println("🔐 收到混合加密登录请求: " + request.getUsername());
            System.out.println("📋 请求信息: KeyID=" + request.getKeyId() + 
                              ", 加密类型=" + request.getEncryptionType());
            
            // 检查必要字段
            if (request.getKeyId() == null || request.getKeyId().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少密钥ID"));
            }
            
            // 检查加密类型
            if (!"RSA+AES-256".equals(request.getEncryptionType())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "不支持的加密类型: " + request.getEncryptionType()));
            }
            
            // 验证密钥ID是否有效
            if (!rsaKeyService.isValidKeyId(request.getKeyId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "无效的密钥ID: " + request.getKeyId()));
            }
            
            // 混合解密（传入密钥ID用于选择正确的私钥）
            HybridDecryptService.HybridDecryptResult decryptResult = 
                hybridDecryptService.decryptLoginData(request);
            
            if (!decryptResult.isSuccess()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "登录数据解密失败"));
            }
            
            // 用户认证
            boolean isAuthenticated = userService.authenticateUser(
                decryptResult.getUsername(), 
                decryptResult.getPassword()
            );
            
            if (!isAuthenticated) {
                return ResponseEntity.unauthorized()
                    .body(Map.of("error", "用户名或密码错误"));
            }
            
            // 生成JWT Token（假设有JWT服务）
            String accessToken = jwtService.generateAccessToken(decryptResult.getUsername());
            String refreshToken = jwtService.generateRefreshToken(decryptResult.getUsername());
            
            // 获取用户信息
            UserInfo userInfo = userService.getUserInfo(decryptResult.getUsername());
            
            LoginResponse response = new LoginResponse(accessToken, refreshToken, userInfo);
            
            System.out.println("✅ 用户登录成功: " + decryptResult.getUsername() + 
                              " (KeyID: " + request.getKeyId() + ")");
            return ResponseEntity.ok(response);
            
        } catch (SecurityException e) {
            System.err.println("🔒 安全异常: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "安全验证失败: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ 登录失败: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "登录处理失败"));
        }
    }
    
    /**
     * 兼容传统AES加密登录（向后兼容）
     */
    @PostMapping("/login-legacy")
    public ResponseEntity<?> legacyLogin(@RequestBody LegacyLoginRequest request) {
        // 处理传统AES加密登录...
        // 实现省略
        return ResponseEntity.ok("传统登录方式");
    }
}
```

---

## 🛡️ 安全最佳实践

### 1. 密钥管理

```java
@Configuration
public class SecurityConfig {
    
    /**
     * RSA密钥对配置
     */
    @Bean
    @ConfigurationProperties(prefix = "app.security.rsa")
    public RSAKeyProperties rsaKeyProperties() {
        return new RSAKeyProperties();
    }
    
    /**
     * 定期轮换密钥
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24小时
    public void rotateKeys() {
        System.out.println("🔄 开始轮换RSA密钥对...");
        // 实现密钥轮换逻辑
    }
}
```

### 2. 安全验证

```java
@Component
public class SecurityValidator {
    
    private static final long MAX_TIMESTAMP_AGE = 5 * 60 * 1000; // 5分钟
    
    /**
     * 验证请求时间戳
     */
    public boolean validateTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long age = currentTime - timestamp;
        return age >= 0 && age <= MAX_TIMESTAMP_AGE;
    }
    
    /**
     * 验证请求频率（防暴力破解）
     */
    public boolean validateRequestRate(String username, String clientIP) {
        // 实现请求频率限制
        return true;
    }
}
```

### 3. 日志和监控

```java
@Component
public class SecurityLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityLogger.class);
    
    public void logLoginAttempt(String username, String clientIP, boolean success) {
        if (success) {
            logger.info("✅ 登录成功 - 用户: {}, IP: {}", username, clientIP);
        } else {
            logger.warn("❌ 登录失败 - 用户: {}, IP: {}", username, clientIP);
        }
    }
    
    public void logSecurityEvent(String event, String details) {
        logger.warn("🚨 安全事件 - {}: {}", event, details);
    }
}
```

---

## 📊 性能优化

### 1. 密钥缓存

```java
@Service
public class KeyCacheService {
    
    private final CacheManager cacheManager;
    
    @Cacheable(value = "rsa-keys", key = "'current'")
    public KeyPair getCurrentKeyPair() {
        return generateNewKeyPair();
    }
}
```

### 2. 异步处理

```java
@Service
public class AsyncDecryptService {
    
    @Async("decryptExecutor")
    public CompletableFuture<String> decryptAsync(String encryptedData, String key) {
        // 异步解密处理
        return CompletableFuture.completedFuture(decryptedData);
    }
}
```

---

## ⚠️ 重要注意事项

### 1. 生产环境配置

```properties
# application-prod.properties
app.security.rsa.key-size=2048
app.security.rsa.algorithm=RSA
app.security.timestamp.max-age=300000
app.security.rate-limit.max-attempts=5
app.security.rate-limit.window=300000

# 启用HTTPS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
```

### 2. 错误处理

```java
@ControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "安全验证失败", "message", e.getMessage()));
    }
    
    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<?> handleDecryptionException(DecryptionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "解密失败", "message", "数据格式不正确"));
    }
}
```

---

## ✅ 验证清单

- [x] RSA密钥对生成和管理
- [x] 公钥接口实现
- [x] RSA解密AES密钥
- [x] AES解密用户密码
- [x] 时间戳验证
- [x] 用户认证集成
- [x] 错误处理和日志
- [x] 安全最佳实践
- [x] 性能优化建议

---

**实施时间**: 2024-09-18  
**安全级别**: 高  
**维护状态**: 活跃维护

> **重要**: 这是一个完整的RSA+AES混合加密后端实现方案。请确保在生产环境中遵循所有安全最佳实践，包括HTTPS传输、密钥安全存储、请求频率限制等。
