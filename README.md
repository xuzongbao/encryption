# encryption

一套面向学习的 Java 加解密示例：用 JDK 自带的 `javax.crypto` / `java.security` / `java.util.Base64` API，演示 Base64、对称密码（DES、3DES、AES）、口令派生密钥（PBKDF2）、密钥交换（经典 DH、椭圆曲线 ECDH）、非对称密码（RSA 加密 / PSS 签名，以及 ECDSA）、SHA-256 摘要和 HMAC-SHA-256 的基本调用方式。

源码在 `encryption/src/` 下，配套若干带 `main` 的演示类。仓库仍是经典 `src/` 包目录布局（没有把教学代码搬进 `src/main/java`）。根目录有一份 `pom.xml`，用来编译这些类并跑 JUnit 5；也可以继续只用 `javac` / `java`，不必装 Maven。

> **请先读这一句：** 这是教学 / 演示代码，**不是**生产级密码学库，也不是 FIPS 认证组件。不要把它拷进线上系统当安全模块。

## 这是什么，这不是什么

**这是什么**

- 帮助初学者把「课本上的算法名字」和「Java 里怎么 `Cipher.getInstance(算法/模式/填充)` / `MessageDigest` / `Mac` / `Signature`」对上号
- 每个算法都有：工具类（生成密钥 / 加密 / 解密，或哈希 / HMAC / 签名）+ `elven.test` 下的演示入口
- 适合对照源码阅读、在 IDE 里单步调试、改一行参数看输出怎么变

**这不是什么**

- 不是经过安全评审的加密 SDK：没有版本化 API、没有密钥存储 / 证书 / 协议设计。仓库里的 JUnit 只覆盖教学 round-trip / 篡改失败，不是安全评审。
- 不处理侧信道、随机数审计、密钥轮换、多接收方等问题
- Base64 **只是编码**，不是加密；SHA-256 **只是哈希**，也不是加密

## 包含的能力（当前实现）

| 类别 | 类 | 实际变换 / 算法 | 演示参数 | 定位 |
| --- | --- | --- | --- | --- |
| 编码 | `Base64Util` | `java.util.Base64`（标准 RFC 4648） | 明文 → Base64 字符串 → UTF-8 文本 | 已现代化 |
| 对称 | `AESUtil` | `AES/GCM/NoPadding` | AES-128；随机 12 字节 IV 前置 | **推荐对照这条学** |
| 口令派生 | `PBKDF2Util` | `PBKDF2WithHmacSHA256` | 16 字节盐；100_000 次迭代；输出 128 bit AES 密钥 | **口令 → 密钥，再交给 AES** |
| 密钥交换（遗留对照） | `DHUtil` | 有限域 DH 2048 + SHA-256 截断派生 AES-128 | 乙方必须复用甲方 p、g；协商 → AES-GCM | **经典对照，新系统优先 ECDH** |
| 密钥交换 | `ECDHUtil` | ECDH `secp256r1`（P-256）+ SHA-256 截断派生 AES-128；附加 `SHA256withECDSA` | 双方独立 `initKey()` → AES-GCM；错公钥得到不同秘密 | **现代默认路径（教学）** |
| 非对称 | `RSAUtil` | 加密：`RSA/ECB/OAEPWithSHA-256AndMGF1Padding`；签名：`RSASSA-PSS`（SHA-256 / MGF1-SHA256） | 2048 bit；公钥加密 / 私钥解密；私钥签名 / 公钥验签 | 加密与签名是两套变换 |
| 摘要 | `SHA256Util` | `SHA-256`（`MessageDigest`） | 32 字节摘要；可转十六进制 | **哈希不是加密** |
| 消息认证 | `HMACUtil` | `HmacSHA256` | 256 bit 密钥；`MessageDigest.isEqual` 校验 | 能发现篡改，**不保密** |
| 对称（遗留） | `DESUtil` | `DES/ECB/PKCS5Padding` | 56 bit；无 IV | **仅教材对照，不安全** |
| 对称（遗留） | `DESede` | `DESede/CBC/PKCS5Padding` | 168 bit；随机 8 字节 IV 前置 | **遗留算法，不推荐新系统** |
| 辅助 | `BytesToHex` | — | `byte[]` → 小写十六进制 | 打印用 |

演示类都在 `elven.test` 包，明文样例统一是 `"hi, welcome to my git area!"`：

- `testBase64` / `testDES` / `testDESede` / `testAES` / `testPBKDF2` / `testRSA` / `testRSASign` / `testDH` / `testECDH` / `testSHA256` / `testHMAC`

它们都是带 `main` 的普通 Java 类，**不是** JUnit。自动化测试在 `src/test/java/`，用 `mvn test` 跑（见下面「自动化测试」）。

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

### RSA / Base64 / DH / ECDH / PBKDF2

- RSA 加密：单块密文，长度等于模数（2048 bit → 256 字节）。OAEP(SHA-256) 单块明文大约最多 190 字节。
- RSA 签名：`sign` 返回的也是模数长的一串字节（PSS + SHA-256）；验签用原文 + 签名 + 公钥，不是「解密签名」。
- SHA-256 / HMAC-SHA-256：输出都是 32 字节。哈希没有密钥；HMAC 有密钥，但仍不是密文。
- Base64：标准编码器，**不按 76 字符折行**（旧 `sun.misc` / 捆绑 JAR 会折行）。
- DH / ECDH：`getSecretKey` 返回 16 字节 AES 密钥，不是「原始共享秘密」本身。ECDH 公钥是 X.509 编码（P-256 大约 91 字节），比 DH-2048 公钥小很多。
- ECDSA：`ECDHUtil.sign` 返回 DER 编码的签名（长度不固定，常见 70 字节上下）；验签用原文 + 签名 + 公钥。
- PBKDF2：本类只输出密钥字节，**不**改 `AESUtil` 的线格式。盐要调用方自己和密文一起存（例如另存一列，或自己拼 `盐 || IV || 密文+tag`）。丢掉盐就无法再派生出同一把密钥。

## 仓库结构

```
.
├── README.md
├── .gitignore
├── pom.xml                          # Maven：编译 encryption/src + 跑 JUnit 5
├── .github/workflows/ci.yml         # push / PR 到 master 时 mvn test
├── src/test/java/elven/encryption/  # JUnit 5（不是 main 演示）
└── encryption/
    └── src/
        └── elven/
            ├── encryption/          # 工具类
            │   ├── Base64Util.java
            │   ├── DESUtil.java
            │   ├── DESede.java
            │   ├── AESUtil.java
            │   ├── PBKDF2Util.java
            │   ├── DHUtil.java
            │   ├── ECDHUtil.java
            │   ├── RSAUtil.java
            │   ├── SHA256Util.java
            │   ├── HMACUtil.java
            │   └── BytesToHex.java
            └── test/                # 演示入口（main）
                ├── testBase64.java
                ├── testDES.java
                ├── testDESede.java
                ├── testAES.java
                ├── testPBKDF2.java
                ├── testRSA.java
                ├── testRSASign.java
                ├── testDH.java
                ├── testECDH.java
                ├── testSHA256.java
                └── testHMAC.java
```

- 目录名像 Eclipse 工程。根目录 `pom.xml` 把 `encryption/src` 配成 Maven 主源码根（所以包名仍是 `elven.encryption` / `elven.test`），JUnit 放在标准的 `src/test/java`。没有把教学文件搬进 `src/main/java`。
- 没有 `.project` / `.classpath` / `build.gradle`。`mvn` 产出在 `target/`，已写入 `.gitignore`。
- 曾经捆绑的 `encryption/sun.misc.BASE64Decoder.jar`（包名其实是 `Decoder`）**已删除**，Base64 改走 JDK 标准库。
- `DESede` 的类名没有 `Util` 后缀；`RSAUtil.getpublicKey` 的方法名仍是小写 `p`，调用时请按源码原样写。

## 如何打开、编译、运行

### 环境

- **跑 `elven.test` 演示（`javac` / `java`）**：JDK 11+（`RSASSA-PSS` 需要 11）。文档中的命令在 **OpenJDK 21** 上核对过。
- **跑 `mvn test` / GitHub Actions**：需要 **JDK 17+**（`pom.xml` 的 `maven.compiler.release` 是 17）。CI 使用 **JDK 21**。
- 把 `encryption/src` 当源码根目录即可。教学代码没有搬到 `src/main/java`。
- 编译工具类本身仍是纯 JDK，不需要 BouncyCastle。JUnit 5 只作为 Maven **test** 依赖出现，不会打进演示 classpath。

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

把类名换成 `elven.test.testBase64`、`elven.test.testDES`、`elven.test.testDESede`、`elven.test.testRSA`、`elven.test.testRSASign`、`elven.test.testDH`、`elven.test.testECDH`、`elven.test.testSHA256`、`elven.test.testHMAC` 或 `elven.test.testPBKDF2` 即可。

成功时大致会看到：打印密钥 → 打印密文（十六进制）→ 再打印解密后的原文。`testRSA` 现在直接打印解密字符串（不再把明文打成 hex）。`testRSASign` 会打印 PSS 签名，并演示改一个字节后面验签失败；同一对密钥仍可做 OAEP 加解密。`testHMAC` 同样会翻转一个字节，展示 `verify` 从 `true` 变成 `false`。`testDH` 会先确认双方派生密钥相同，再用 AES-GCM 加解密那句示例明文。DH 第一次生成 2048 bit 参数可能要几秒。`testECDH` 同样确认双方 AES 密钥相同并 round-trip，再演示「拿错对方公钥会得到不同秘密、AES-GCM 解密失败」，以及一对很小的 ECDSA 验签。ECDH 比 DH 快得多，因为不用生成 2048 bit 素数。`testPBKDF2` 会打印盐、派生耗时、正确口令 round-trip，以及错误口令 / 错误盐时 AES-GCM 解密失败。

### 用 Maven 跑 JUnit（自动化测试）

`elven.test.test*` 是给人看输出的 `main` 演示；`src/test/java` 里的 `*Test` 才是断言。两者都覆盖同一套工具类，**改算法教学默认值之前请两边都看一眼**。

在仓库根目录（有 `pom.xml` 的地方）：

```bash
mvn test
```

CI（`.github/workflows/ci.yml`）在 push / PR 到 `master` 时用 JDK 21 执行同样的 `mvn -B test`。

常见结果：Base64 / SHA-256 / HMAC / AES-GCM / PBKDF2 / RSA / ECDH 都应很快结束；`DHUtilTest` 第一次生成 2048 bit DH 参数可能要几秒，这是预期行为，不是卡住。

不需要把 `elven.test` 演示改成 JUnit，也不要用 `mvn test` 去「运行」那些 `main`。演示仍然用上一节的 `java -cp encryption/bin elven.test.testAES`。

### 用 IDE

1. 最省事：用 IDE **打开根目录的 Maven 项目**（识别 `pom.xml`）。主源码根已指向 `encryption/src`，测试根是 `src/test/java`。
2. 也可以「从现有源码创建项目」：把 `encryption/src` 标成 Source Root，这样 `elven.encryption` / `elven.test` 才会被识别成包名。
3. **不必**再添加 Base64 JAR。
4. 打开某个 `encryption/src/elven/test/test*.java`，运行它的 `main`。
5. 若用 Maven / IDE 的 JUnit 运行器：打开 `src/test/java/elven/encryption/*Test.java`，不要和上面的 `main` 演示搞混。

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

### PBKDF2（口令 → AES 密钥）

人记住的口令不能直接当 AES 密钥。先用 PBKDF2 派生 16 字节密钥，再交给 `AESUtil`。**盐必须和密文一起保存**，解密时用同一口令 + 同一份盐重新派生：

```java
byte[] salt = PBKDF2Util.generateSalt();                          // 16 字节随机盐
byte[] key = PBKDF2Util.deriveKey(password, salt, 128);           // AES-128
byte[] packed = AESUtil.encryptAES(data.getBytes(StandardCharsets.UTF_8), key);

byte[] key2 = PBKDF2Util.deriveKey(password, salt);               // 解密方没有「密钥文件」
byte[] plain = AESUtil.decryptAES(packed, key2);
```

演示参数（写在 `PBKDF2Util` 常量里，改一处即可）：

| 参数 | 演示取值 | 说明 |
| --- | --- | --- |
| 算法 | `PBKDF2WithHmacSHA256` | JDK `SecretKeyFactory` 标准名 |
| 盐 | 16 字节 `SecureRandom` | 不保密，但必须保存；每次加密 / 每个用户一份新盐 |
| 迭代次数 | 100_000 | 教学用，云虚拟机上通常不到 1 秒；**生产请按 [OWASP 口令存储建议](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) 再提高** |
| 输出长度 | 128 bit | 正好喂给本仓库的 `AESUtil` |

**不要**把口令做一次 SHA-256 就当密钥或当「已哈希的口令」存库：没有盐、没有迭代次数，彩虹表和 GPU 暴力破解都太容易。PBKDF2 也**不是** AES 的替代品——它只负责从口令算出密钥。本示例也不是完整的「用户登录口令存储」库：若真要存口令哈希，至少还要一起存算法名、迭代次数和盐，而不是「只存一行哈希」。

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

和上面共用 `initKey()` / `getpublicKey` / `getPrivateKey`。签名算法是 `RSASSA-PSS`，参数里写明 SHA-256（需要 JDK 11+）：

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

DH 仍保留，用来对照教材上的「大素数模幂」。新系统请优先看下面的 ECDH。

### ECDH 密钥交换 → AES 加密（现代默认路径）

双方都在命名曲线 `secp256r1`（NIST P-256）上各自 `initKey()`，**不必**像 DH 那样把甲方的 p、g 传给乙方：

```java
Map<String, Object> keyMap1 = ECDHUtil.initKey();          // 甲方
byte[] publicKey1 = ECDHUtil.getPublicKey(keyMap1);
byte[] privateKey1 = ECDHUtil.getPrivateKey(keyMap1);

Map<String, Object> keyMap2 = ECDHUtil.initKey();          // 乙方，独立生成
byte[] publicKey2 = ECDHUtil.getPublicKey(keyMap2);
byte[] privateKey2 = ECDHUtil.getPrivateKey(keyMap2);

byte[] secret1 = ECDHUtil.getSecretKey(publicKey2, privateKey1); // 16 字节 AES 密钥
byte[] secret2 = ECDHUtil.getSecretKey(publicKey1, privateKey2);
// secret1 与 secret2 应相同

byte[] packed = AESUtil.encryptAES(data.getBytes(StandardCharsets.UTF_8), secret1);
byte[] plain = AESUtil.decryptAES(packed, secret2);
```

可选的很小附加：同一对 P-256 密钥还能做 ECDSA（教学捷径；生产应把协商钥和签名钥分开）：

```java
byte[] signature = ECDHUtil.sign(data.getBytes(StandardCharsets.UTF_8), privateKey1);
boolean ok = ECDHUtil.verify(data.getBytes(StandardCharsets.UTF_8), signature, publicKey1);
```

### 经典 DH 和 ECDH 差在哪

| 点 | `DHUtil` | `ECDHUtil` |
| --- | --- | --- |
| 数学 | 有限域模幂 | 椭圆曲线点乘 |
| 参数 | 甲方生成 2048 bit 的 p、g，乙方必须 `initKey(甲方公钥)` 复用 | 双方独立使用命名曲线 `secp256r1` |
| 公钥体积 | 较大（X.509 编码通常几百字节） | 小很多（P-256 的 X.509 编码大约 91 字节） |
| 速度 | 首次生成 DH 参数可能要几秒 | 通常立刻完成 |
| Java API | `KeyPairGenerator.getInstance("DH")` + `KeyAgreement.getInstance("DH")` | `ECGenParameterSpec` + `KeyPairGenerator.getInstance("EC")` + `KeyAgreement.getInstance("ECDH")` |
| 教学 KDF | `generateSecret()` 原始字节 → SHA-256 前 16 字节 → AES-128 | **同一套**教学 KDF，方便对照 |
| 定位 | 经典 / 遗留对照 | 现代默认路径（本仓库教学级别） |

两条路径都**没有**身份认证：只保证「算出同一把密钥」，不保证「对面真是你以为的那个人」。真实协议还要证书或签名（本仓库的 RSA-PSS / ECDSA 只演示签名 API，不是完整的 TLS）。

生产环境不要照抄这里的 SHA-256 截断，应使用 [HKDF](https://datatracker.ietf.org/doc/html/rfc5869)（HMAC 提取再扩展）。本仓库故意不实现完整 HKDF，以免和这条「一眼能看懂的截断」抢焦点。更现代的密钥交换还有 X25519（Java 11+ 的 `XDH`），本示例为了和常见 NIST 教材对齐，选用 OpenJDK 里最好找的 `secp256r1`。

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
| RSA 签名 | 无 | 新增 `sign` / `verify`，算法 `RSASSA-PSS` + SHA-256 参数；**不改变**原有 encrypt/decrypt |
| DH 密钥 | 1024 bit | 2048 bit |
| DH 共享密钥 | `generateSecret("DES")`，在 OpenJDK 21 上会失败 | `generateSecret()` + SHA-256 前 16 字节 → AES-128 |
| ECDH | 无 | 新增 `ECDHUtil`：`secp256r1` + 与 DH 相同的教学 KDF；可选 `SHA256withECDSA` |
| 字符集 | 多处 `getBytes()` / `new String(bytes)` 用平台默认 | 演示和 Base64 解码使用 UTF-8 |

旧密文 **不能** 用新 `decrypt*` 解开（AES/3DES 线格式变了，RSA 填充也变了）。这是教学仓库，没有提供兼容层。

## 安全边界：修了什么，故意留下什么

**已按教学标准修掉的问题**

1. 不再依赖已过时的 Base64 JAR / `sun.misc`。
2. AES 不再使用会落到 ECB 的短变换名；改用 GCM，并讲清 IV 线格式。
3. RSA 默认密钥改为 2048，填充改为 OAEP。
4. DH 在现代 JDK 上可以跑通；不再走 `generateSecret("DES")`。
5. 明文编解码指定 UTF-8。
6. 补了 PBKDF2 口令派生演示，避免把 `SHA-256(口令)` 直接当 AES 密钥。
7. 补了 ECDH（`secp256r1`）作为现代密钥交换教学路径；经典 DH 仍保留作对照。

**故意保留的「遗留学习」部分**

1. **DES**：仍提供，明确标成不安全（56 bit + ECB）。用来对照教材，不是推荐方案。
2. **3DES**：仍提供 CBC+IV 的稍好写法，但算法本身过时。新系统用 AES。
3. **DH / ECDH 的 KDF**：都用 SHA-256 截断，便于读懂；真实系统应使用 HKDF 等。经典有限域 DH 保留作对照，新代码请看 ECDH。
4. **没有**密钥管理、证书、大文件分段、AAD、scrypt / Argon2 / 完整 HKDF。仓库现已补充 SHA-256 / HMAC / RSA-PSS / ECDSA 签名、ECDH，以及 JDK 自带的 PBKDF2-HMAC-SHA256 教学示例，但仍不是完整协议。口令派生的迭代次数是为了 demo 能很快跑完，不是当年 OWASP 生产推荐值。ECDH 选用 `secp256r1` 是因为 OpenJDK 开箱即有，不是「唯一正确的曲线」。
5. 异常仍是 `throws Exception`，方便演示，不是产品级错误处理。
6. `test*` 类名保持小写开头的历史风格。

**仍然不要做的事**

- 不要声称本仓库可用于生产、通过 FIPS、或「已经安全到能保护用户数据」。
- 不要把 SHA-256 / HMAC 当成加密：哈希和认证码都不保密。
- 不要把 RSA / ECDSA 签名当成加密：验签用公钥，签名本身可被任何人看到。
- HMAC 防篡改 ≠ AEAD。要同时保密和认证，教学对照请看 AES-GCM，而不是自己组合「加密 + HMAC」。
- 不要复用 AES-GCM 的 IV。
- 不要用 RSA 直接加密大文件。
- 不要把 DES/3DES 示例里的密钥拿去保护真实数据。
- 不要省略 PBKDF2 的盐，也不要把 `SHA-256(口令)` 当成密钥派生或口令存储。
- 不要把演示里的 100_000 次迭代照抄进真实系统而不查当前 OWASP 建议。
- 不要把 ECDH 当成已经认证过身份：没有签名/证书时，中间人可以分别和甲、乙各做一次协商。
- 不要在生产里把同一对 EC 密钥既做 ECDH 又做 ECDSA；演示里合在一起只是为了少生成一对钥匙。
- 不要用 `KeyAgreement.generateSecret("AES")`：现代 JDK 会拒绝这种隐式截断。也不要把教学用的 SHA-256 截断当成 HKDF。
- 不要把 `secp256k1`（比特币常用）和本示例的 `secp256r1`（NIST P-256）搞混，它们不是同一条曲线。

## 许可证

本项目采用 **MIT License**。完整许可条款见仓库根目录的 [`LICENSE`](LICENSE) 文件。

## 参与贡献

欢迎来。请尽量保持「小而清晰的教学示例」：

- 修文档、核对某个 JDK 上能否跑通、补 JUnit 断言，都很有价值。
- 大规模框架化、引入 BouncyCastle 等完整密码学库，通常超出本仓库定位。
- 没有贡献者协议，普通 GitHub Pull Request 即可。

## English summary

Educational Java samples for Base64, DES, 3DES, AES, PBKDF2, classic Diffie–Hellman, ECDH (secp256r1), RSA (OAEP encrypt + PSS sign), ECDSA, SHA-256, and HMAC-SHA-256 using only the JDK. Teaching sources stay in classic `encryption/src/` (`elven.encryption` / `elven.test` mains). A root `pom.xml` compiles that tree and runs JUnit 5 from `src/test/java` (`mvn test`; CI uses JDK 21). Maven requires **JDK 17+**; `javac` demos need **JDK 11+**. **Not a production crypto library and not FIPS certified.** Hashing is not encryption; HMAC authenticates but does not conceal; RSA/ECDSA signatures are not encryption.

Current teaching defaults: `java.util.Base64`; AES-128-GCM with a 12-byte IV prepended; PBKDF2-HMAC-SHA256 with a 16-byte salt, 100_000 iterations, and a 128-bit AES key (demo speed, not OWASP production guidance); RSA-2048 OAEP(SHA-256) for encrypt/decrypt and `RSASSA-PSS` with SHA-256/MGF1-SHA256 parameters for sign/verify; HMAC-SHA-256 with `MessageDigest.isEqual`; DH-2048 kept as a classic contrast; ECDH on `secp256r1` (P-256) as the modern key-agreement path. Both DH and ECDH hash the raw `generateSecret()` bytes with SHA-256 and truncate to an AES-128 key (a teaching KDF, not HKDF; the old `generateSecret("DES")` / `generateSecret("AES")` paths are avoided). ECDHUtil also has a tiny `SHA256withECDSA` bonus on the same keys. DES remains as an explicit insecure ECB demo; 3DES remains as legacy CBC with an 8-byte IV prepended. The bundled Base64 JAR has been removed. Licensed under the MIT License; see the `LICENSE` file.
