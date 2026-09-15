# encryption

一套面向学习的 Java 加解密示例：用 JDK 自带的 `javax.crypto` / `java.security` / `java.util.Base64` API，演示 Base64、对称密码（DES、3DES、AES）、非对称密码（DH、RSA 加密 / PSS 签名）、SHA-256 摘要和 HMAC-SHA-256 的基本调用方式。

源码在 `encryption/src/` 下，配套若干带 `main` 的演示类。仓库是经典 `src/` 包目录布局，**没有 Maven / Gradle**。

> **请先读这一句：** 这是教学 / 演示代码，**不是**生产级密码学库，也不是 FIPS 认证组件。不要把它拷进线上系统当安全模块。

## 这是什么，这不是什么

**这是什么**

- 帮助初学者把「课本上的算法名字」和「Java 里怎么 `Cipher.getInstance(算法/模式/填充)` / `MessageDigest` / `Mac` / `Signature`」对上号
- 每个算法都有：工具类（生成密钥 / 加密 / 解密，或哈希 / HMAC / 签名）+ `elven.test` 下的演示入口
- 适合对照源码阅读、在 IDE 里单步调试、改一行参数看输出怎么变

**这不是什么**

- 不是经过安全评审的加密 SDK：没有版本化 API、没有 JUnit、没有密钥存储 / 证书 / 协议设计
- 不处理侧信道、随机数审计、密钥轮换、多接收方等问题
- Base64 **只是编码**，不是加密；SHA-256 **只是哈希**，也不是加密

## 包含的能力（当前实现）

| 类别 | 类 | 实际变换 / 算法 | 演示参数 | 定位 |
| --- | --- | --- | --- | --- |
| 编码 | `Base64Util` | `java.util.Base64`（标准 RFC 4648） | 明文 → Base64 字符串 → UTF-8 文本 | 已现代化 |
| 对称 | `AESUtil` | `AES/GCM/NoPadding` | AES-128；随机 12 字节 IV 前置 | **推荐对照这条学** |
| 密钥交换 | `DHUtil` | DH 2048 + SHA-256 截断派生 AES-128 | 协商 → AES-GCM 加密示例明文 | 已修好现代 JDK 路径 |
| 非对称 | `RSAUtil` | 加密：`RSA/ECB/OAEPWithSHA-256AndMGF1Padding`；签名：`SHA256withRSA/PSS` | 2048 bit；公钥加密 / 私钥解密；私钥签名 / 公钥验签 | 加密与签名是两套变换 |
| 摘要 | `SHA256Util` | `SHA-256`（`MessageDigest`） | 32 字节摘要；可转十六进制 | **哈希不是加密** |
| 消息认证 | `HMACUtil` | `HmacSHA256` | 256 bit 密钥；`MessageDigest.isEqual` 校验 | 能发现篡改，**不保密** |
| 对称（遗留） | `DESUtil` | `DES/ECB/PKCS5Padding` | 56 bit；无 IV | **仅教材对照，不安全** |
| 对称（遗留） | `DESede` | `DESede/CBC/PKCS5Padding` | 168 bit；随机 8 字节 IV 前置 | **遗留算法，不推荐新系统** |
| 辅助 | `BytesToHex` | — | `byte[]` → 小写十六进制 | 打印用 |

演示类都在 `elven.test` 包，明文样例统一是 `"hi, welcome to my git area!"`：

- `testBase64` / `testDES` / `testDESede` / `testAES` / `testRSA` / `testRSASign` / `testDH` / `testSHA256` / `testHMAC`

它们都是带 `main` 的普通 Java 类，**不是** JUnit。

## 线格式（密文怎么拼）

对称加密的返回值是「能直接交给对应 `decrypt*` 的一整段字节」，方便演示 round-trip。

### AES-GCM（`AESUtil`）

```
[12 字节 IV] + [密文 || 16 字节 GCM tag]
```

- 每次 `encryptAES` 都会 `SecureRandom` 生成新 IV，**不要重用 IV**。
- Java `Cipher.doFinal` 已经把 GCM tag 接在密文后面，调用方不用单独处理 tag。
- `decryptAES` 先拆出前 12 字节当 IV，其余当密文+tag。

### 3DES-CBC（`DESede`，遗留）

```
[8 字节 IV] + [PKCS5 填充后的 CBC 密文]
```

### DES-ECB（`DESUtil`，遗留）

没有 IV。密文就是 `Cipher.doFinal` 的输出。ECB 下相同明文块会得到相同密文块——这是故意展示的反面教材。

### RSA / Base64 / DH

- RSA 加密：单块密文，长度等于模数（2048 bit → 256 字节）。OAEP(SHA-256) 单块明文大约最多 190 字节。
- RSA 签名：`sign` 返回的也是模数长的一串字节（PSS + SHA-256）；验签用原文 + 签名 + 公钥，不是「解密签名」。
- SHA-256 / HMAC-SHA-256：输出都是 32 字节。哈希没有密钥；HMAC 有密钥，但仍不是密文。
- Base64：标准编码器，**不按 76 字符折行**（旧 `sun.misc` / 捆绑 JAR 会折行）。
- DH：`getSecretKey` 返回 16 字节 AES 密钥，不是「DH 原始共享秘密」本身。

## 仓库结构

```
.
├── README.md
├── .gitignore
└── encryption/
    └── src/
        └── elven/
            ├── encryption/          # 工具类
            │   ├── Base64Util.java
            │   ├── DESUtil.java
            │   ├── DESede.java
            │   ├── AESUtil.java
            │   ├── DHUtil.java
            │   ├── RSAUtil.java
            │   ├── SHA256Util.java
            │   ├── HMACUtil.java
            │   └── BytesToHex.java
            └── test/                # 演示入口（main）
                ├── testBase64.java
                ├── testDES.java
                ├── testDESede.java
                ├── testAES.java
                ├── testRSA.java
                ├── testRSASign.java
                ├── testDH.java
                ├── testSHA256.java
                └── testHMAC.java
```

- 目录名像 Eclipse 工程，但仓库里 **没有** `.project` / `.classpath` / `pom.xml` / `build.gradle`。
- 曾经捆绑的 `encryption/sun.misc.BASE64Decoder.jar`（包名其实是 `Decoder`）**已删除**，Base64 改走 JDK 标准库。
- `DESede` 的类名没有 `Util` 后缀；`RSAUtil.getpublicKey` 的方法名仍是小写 `p`，调用时请按源码原样写。

## 如何打开、编译、运行

### 环境

- 需要 JDK 8+（`javac` + `java`）。`SHA256withRSA/PSS` 签名演示需要 **JDK 11+**。文档中的命令在 **OpenJDK 21** 上核对过。
- **没有** Maven / Gradle。把 `encryption/src` 当源码根目录即可。
- 不再需要任何第三方 JAR。

Windows 下把 classpath 分隔符 `:` 换成 `;`。

### 用 javac / java

在仓库根目录：

```bash
mkdir -p encryption/bin

javac -encoding UTF-8 \
  -d encryption/bin \
  encryption/src/elven/encryption/*.java \
  encryption/src/elven/test/*.java
```

运行某一个演示（以 AES 为例）：

```bash
java -cp encryption/bin elven.test.testAES
```

把类名换成 `elven.test.testBase64`、`elven.test.testDES`、`elven.test.testDESede`、`elven.test.testRSA`、`elven.test.testRSASign`、`elven.test.testDH`、`elven.test.testSHA256` 或 `elven.test.testHMAC` 即可。

成功时大致会看到：打印密钥 → 打印密文（十六进制）→ 再打印解密后的原文。`testRSA` 现在直接打印解密字符串（不再把明文打成 hex）。`testRSASign` 会打印 PSS 签名，并演示改一个字节后面验签失败；同一对密钥仍可做 OAEP 加解密。`testHMAC` 同样会翻转一个字节，展示 `verify` 从 `true` 变成 `false`。`testDH` 会先确认双方派生密钥相同，再用 AES-GCM 加解密那句示例明文。DH 第一次生成 2048 bit 参数可能要几秒。

### 用 IDE

1. 用「从现有源码创建项目」或新建空 Java 项目。
2. 把 `encryption/src` 标成 Source Root，这样 `elven.encryption` / `elven.test` 才会被识别成包名。
3. **不必**再添加 Base64 JAR。
4. 打开某个 `test*.java`，运行它的 `main`。

## 最小用法示例

下面都直接对应源码里的 public 方法。完整流程对照 `encryption/src/elven/test/`。

### Base64（编码，不是加密）

```java
String data = "hi, welcome to my git area!";
String encoded = Base64Util.base64Encrypt(data.getBytes(StandardCharsets.UTF_8));
String decoded = Base64Util.base64Decrypt(encoded);
```

### AES-GCM（推荐对照）

```java
byte[] key = AESUtil.initKey();                 // 128 bit
byte[] packed = AESUtil.encryptAES(data.getBytes(StandardCharsets.UTF_8), key);
byte[] plain = AESUtil.decryptAES(packed, key); // packed = IV || 密文+tag
```

方法名仍是 `encryptAES` / `decryptAES`，但返回值已经带 IV，**不能**再把旧 ECB 密文丢进来解。

### DES / 3DES（遗留）

```java
byte[] desKey = DESUtil.initKey();              // 56 bit，不安全
byte[] desCipher = DESUtil.encryptDES(data.getBytes(StandardCharsets.UTF_8), desKey);
byte[] desPlain = DESUtil.decryptDES(desCipher, desKey);

byte[] tdesKey = DESede.initKey();              // 168 bit，过时
byte[] tdesPacked = DESede.encrypt3DES(data.getBytes(StandardCharsets.UTF_8), tdesKey);
byte[] tdesPlain = DESede.decrypt3DES(tdesPacked, tdesKey); // 含 8 字节 IV
```

看密文时可以用 `BytesToHex.fromBytesToHex(...)`。

### RSA（公钥加密，私钥解密）

注意方法名仍是 `getpublicKey`（`p` 小写）：

```java
Map<String, Object> keyMap = RSAUtil.initKey(); // 2048 bit
RSAPublicKey publicKey = RSAUtil.getpublicKey(keyMap);
RSAPrivateKey privateKey = RSAUtil.getPrivateKey(keyMap);

byte[] cipher = RSAUtil.encrypt(data.getBytes(StandardCharsets.UTF_8), publicKey);
byte[] plain = RSAUtil.decrypt(cipher, privateKey);
```

Map 的键仍是 `RSAUtil.PUBLIC_KEY` / `RSAUtil.PRIVATE_KEY`。

### RSA 签名（私钥签名，公钥验签）

和上面共用 `initKey()` / `getpublicKey` / `getPrivateKey`。签名变换是 `SHA256withRSA/PSS`（需要 JDK 11+）：

```java
byte[] signature = RSAUtil.sign(data.getBytes(StandardCharsets.UTF_8), privateKey);
boolean ok = RSAUtil.verify(data.getBytes(StandardCharsets.UTF_8), signature, publicKey);
```

签名通过只说明「私钥持有者签过这份数据」；任何人都能用公钥验签，所以签名**不保密**。

### SHA-256（哈希，不是加密）

```java
byte[] digest = SHA256Util.hash(data.getBytes(StandardCharsets.UTF_8)); // 32 字节
String hex = SHA256Util.hashHex(data.getBytes(StandardCharsets.UTF_8));
```

同样输入永远得到同样摘要，但不能从摘要还原原文。

### HMAC-SHA-256（带密钥的认证码，也不加密）

```java
byte[] hmacKey = HMACUtil.initKey();              // 256 bit
byte[] mac = HMACUtil.hmac(data.getBytes(StandardCharsets.UTF_8), hmacKey);
boolean ok = HMACUtil.verify(data.getBytes(StandardCharsets.UTF_8), hmacKey, mac);
```

改一个字节后 `verify` 会返回 `false`。HMAC 只证明完整性和「持有同一把密钥」，原文仍是明文。需要同时保密时请看 AES-GCM，而不是自己拼「先加密再 HMAC」。

### DH 密钥交换 → AES 加密

```java
Map<String, Object> keyMap1 = DHUtil.initKey();           // 甲方，2048 bit
byte[] publicKey1 = DHUtil.getPublicKey(keyMap1);
byte[] privateKey1 = DHUtil.getPrivateKey(keyMap1);

Map<String, Object> keyMap2 = DHUtil.initKey(publicKey1); // 乙方按甲方参数生成密钥对
byte[] publicKey2 = DHUtil.getPublicKey(keyMap2);
byte[] privateKey2 = DHUtil.getPrivateKey(keyMap2);

byte[] secret1 = DHUtil.getSecretKey(publicKey2, privateKey1); // 16 字节 AES 密钥
byte[] secret2 = DHUtil.getSecretKey(publicKey1, privateKey2);
// secret1 与 secret2 应相同

byte[] packed = AESUtil.encryptAES(data.getBytes(StandardCharsets.UTF_8), secret1);
byte[] plain = AESUtil.decryptAES(packed, secret2);
```

## 从旧 API 迁移

如果你拷贝过 2016 年版本的调用方式，注意这些**故意不兼容**的变化（类名大多没改，线格式和默认参数改了）：

| 点 | 旧行为 | 现在 |
| --- | --- | --- |
| Base64 | 捆绑 `sun.misc.BASE64Decoder.jar`，`Decoder.BASE64Encoder` | `java.util.Base64`；删除该 JAR；编译不用再把 JAR 放进 classpath |
| Base64 折行 | 旧编码器常按 76 字符换行 | 标准编码器不分行，长字符串会和旧结果不同 |
| AES | `Cipher.getInstance("AES")`（常见实现等于 ECB，无 IV） | `AES/GCM/NoPadding`；密文前 12 字节是 IV |
| 3DES | `Cipher.getInstance("DESede")`（常见等于 ECB） | `DESede/CBC/PKCS5Padding`；密文前 8 字节是 IV |
| DES | `Cipher.getInstance("DES")` | 仍是 ECB，但变换名写死为 `DES/ECB/PKCS5Padding`，并标明不安全 |
| RSA 密钥 | 1024 bit | 2048 bit |
| RSA 填充 | `Cipher.getInstance("RSA")`（常见 PKCS#1 v1.5） | OAEP + SHA-256（含显式 `OAEPParameterSpec`） |
| RSA 签名 | 无 | 新增 `sign` / `verify`，变换 `SHA256withRSA/PSS`；**不改变**原有 encrypt/decrypt |
| DH 密钥 | 1024 bit | 2048 bit |
| DH 共享密钥 | `generateSecret("DES")`，在 OpenJDK 21 上会失败 | `generateSecret()` + SHA-256 前 16 字节 → AES-128 |
| 字符集 | 多处 `getBytes()` / `new String(bytes)` 用平台默认 | 演示和 Base64 解码使用 UTF-8 |

旧密文 **不能** 用新 `decrypt*` 解开（AES/3DES 线格式变了，RSA 填充也变了）。这是教学仓库，没有提供兼容层。

## 安全边界：修了什么，故意留下什么

**已按教学标准修掉的问题**

1. 不再依赖已过时的 Base64 JAR / `sun.misc`。
2. AES 不再使用会落到 ECB 的短变换名；改用 GCM，并讲清 IV 线格式。
3. RSA 默认密钥改为 2048，填充改为 OAEP。
4. DH 在现代 JDK 上可以跑通；不再走 `generateSecret("DES")`。
5. 明文编解码指定 UTF-8。

**故意保留的「遗留学习」部分**

1. **DES**：仍提供，明确标成不安全（56 bit + ECB）。用来对照教材，不是推荐方案。
2. **3DES**：仍提供 CBC+IV 的稍好写法，但算法本身过时。新系统用 AES。
3. **DH 的 KDF**：用 SHA-256 截断，便于读懂；真实系统应使用 HKDF 等。经典有限域 DH 本身也逐渐让位给 ECDH。
4. **没有**密钥管理、证书、大文件分段、AAD、口令派生（PBKDF2/scrypt/Argon2）。仓库现已补充 SHA-256 / HMAC / RSA-PSS 签名教学示例，但仍不是完整协议。
5. 异常仍是 `throws Exception`，方便演示，不是产品级错误处理。
6. `test*` 类名保持小写开头的历史风格。

**仍然不要做的事**

- 不要声称本仓库可用于生产、通过 FIPS、或「已经安全到能保护用户数据」。
- 不要把 SHA-256 / HMAC 当成加密：哈希和认证码都不保密。
- 不要把 RSA 签名当成加密：验签用公钥，签名本身可被任何人看到。
- HMAC 防篡改 ≠ AEAD。要同时保密和认证，教学对照请看 AES-GCM，而不是自己组合「加密 + HMAC」。
- 不要复用 AES-GCM 的 IV。
- 不要用 RSA 直接加密大文件。
- 不要把 DES/3DES 示例里的密钥拿去保护真实数据。

## 许可证

本项目采用 **MIT License**。完整许可条款见仓库根目录的 [`LICENSE`](LICENSE) 文件。

## 参与贡献

欢迎来。请尽量保持「小而清晰的教学示例」：

- 修文档、核对某个 JDK 上能否跑通，都很有价值。
- 大规模框架化、引入完整密码学库，通常超出本仓库定位。
- 没有贡献者协议，普通 GitHub Pull Request 即可。

## English summary

Educational Java samples for Base64, DES, 3DES, AES, Diffie–Hellman, RSA (OAEP encrypt + PSS sign), SHA-256, and HMAC-SHA-256 using only the JDK. Classic `src/` layout, no Maven/Gradle. **Not a production crypto library and not FIPS certified.** Hashing is not encryption; HMAC authenticates but does not conceal; RSA signatures are not RSA encryption.

Current teaching defaults: `java.util.Base64`; AES-128-GCM with a 12-byte IV prepended; RSA-2048 OAEP(SHA-256) for encrypt/decrypt and `SHA256withRSA/PSS` for sign/verify; HMAC-SHA-256 with `MessageDigest.isEqual`; DH-2048 whose shared secret is hashed with SHA-256 and truncated to an AES-128 key (the old `generateSecret("DES")` path is gone). DES remains as an explicit insecure ECB demo; 3DES remains as legacy CBC with an 8-byte IV prepended. The bundled Base64 JAR has been removed. Licensed under the MIT License; see the `LICENSE` file.
