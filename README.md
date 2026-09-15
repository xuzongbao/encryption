# encryption

一套面向学习的 Java 加解密示例：用 JDK 自带的 `javax.crypto` / `java.security` API，演示 Base64、对称密码（DES、3DES、AES）和非对称密码（DH、RSA）的基本调用方式。

源码集中在 `encryption/src/` 下，配套若干可直接运行的 `main` 演示类。仓库采用经典的 `src/` 包目录布局，**目前没有 Maven / Gradle 工程文件**。

> **请先读这一句：** 本仓库是 2016 年左右的教学 / 演示代码，**不是**生产级密码学库。不要把它拷进线上系统当安全组件使用。

## 这是什么，这不是什么

**这是什么**

- 帮助初学者把「课本上的算法名字」和「Java 里怎么 `Cipher.getInstance(...)`」对上号
- 每个算法都有：工具类（生成密钥 / 加密 / 解密）+ `elven.test` 下的演示入口
- 适合对照源码阅读、在 IDE 里单步调试、改一行参数看看输出会怎样变

**这不是什么**

- 不是经过安全评审的加密 SDK，没有版本化 API、没有单元测试框架、没有持续维护
- 不处理密钥存储、证书、随机数质量、协议设计、侧信道等问题
- Base64 **只是编码**，不是加密；放进来是为了演示「二进制怎么变成可打印字符串」

## 包含的能力

| 类别 | 类 | 实际用到的算法名 | 演示里的关键参数 |
| --- | --- | --- | --- |
| 编码 | `Base64Util` | 自带 JAR 里的 `Decoder.BASE64Encoder` / `BASE64Decoder` | 明文 → Base64 字符串 → 再解回字符串 |
| 对称 | `DESUtil` | `DES`（`KeyGenerator` + `Cipher`） | 密钥长度 56 bit |
| 对称 | `DESede` | `DESede`（即 3DES） | 密钥长度 168 bit |
| 对称 | `AESUtil` | `AES` | 密钥长度 128 bit |
| 密钥交换 | `DHUtil` | `DH` + `KeyAgreement` | DH 模数 1024 bit；协商出的本地密钥算法写死为 `DES` |
| 非对称 | `RSAUtil` | `RSA` | 密钥长度 1024 bit；公钥加密、私钥解密 |
| 辅助 | `BytesToHex` | — | 把 `byte[]` 打成十六进制，方便看密文 |

演示类都在 `elven.test` 包里，明文样例统一是 `"hi, welcome to my git area!"`：

- `testBase64` / `testDES` / `testDESede` / `testAES` / `testRSA` / `testDH`

它们都是带 `main` 的普通 Java 类，**不是** JUnit 测试。

## 仓库结构

```
.
├── README.md
├── .gitignore
└── encryption/
    ├── src/
    │   └── elven/
    │       ├── encryption/          # 工具类
    │       │   ├── Base64Util.java
    │       │   ├── DESUtil.java
    │       │   ├── DESede.java
    │       │   ├── AESUtil.java
    │       │   ├── DHUtil.java
    │       │   ├── RSAUtil.java
    │       │   └── BytesToHex.java
    │       └── test/                # 演示入口（main）
    │           ├── testBase64.java
    │           ├── testDES.java
    │           ├── testDESede.java
    │           ├── testAES.java
    │           ├── testRSA.java
    │           └── testDH.java
    └── sun.misc.BASE64Decoder.jar   # Base64 实现（包名是 Decoder，不是 sun.misc）
```

几点和「第一眼观感」可能不一致的地方：

- 目录名像 Eclipse 工程，但仓库里 **没有** `.project` / `.classpath` / `pom.xml` / `build.gradle`。
- 根目录的 `.gitignore` 来自 GitHub 初始化模板（Node / GitBook 一类规则），和这份 Java 示例关系不大。
- `DESede` 的类名没有 `Util` 后缀；`RSAUtil.getpublicKey` 的方法名是小写 `p`，调用时请按源码原样写。
- JAR 文件名叫 `sun.misc.BASE64Decoder.jar`，里面的类却在 `Decoder` 包下（`Decoder.BASE64Encoder` 等）。`Base64Util` 依赖的是这个 JAR，而不是 JDK 里已删除的 `sun.misc.BASE64Encoder`。

最近一次有实质代码提交是 **2016 年 6 月**（对称算法、非对称算法分两次提交）。之后仓库基本处于「示例代码原样保留」的状态。

## 如何打开、编译、运行

### 环境

- 需要 JDK（`javac` + `java`）。演示代码是较早期的 Java 写法，没有用到模块系统。
- **没有** Maven / Gradle，也就没有一键 `mvn test`。请用命令行或 IDE 自己把源码和那个 JAR 放进 classpath。
- 本文档中的命令在 **OpenJDK 21** 上核对过：Base64、DES、3DES、AES、RSA 可以跑通；DH 在生成共享密钥一步会失败（原因见下方「安全与已知限制」）。

Windows 下把下面命令里的 classpath 分隔符 `:` 换成 `;` 即可。

### 用 javac / java（不依赖 IDE）

在仓库根目录执行：

```bash
mkdir -p encryption/bin

javac -encoding UTF-8 \
  -cp encryption/sun.misc.BASE64Decoder.jar \
  -d encryption/bin \
  encryption/src/elven/encryption/*.java \
  encryption/src/elven/test/*.java
```

运行某一个演示（以 AES 为例）：

```bash
java -cp encryption/bin:encryption/sun.misc.BASE64Decoder.jar elven.test.testAES
```

把最后的类名换成 `elven.test.testBase64`、`elven.test.testDES`、`elven.test.testDESede`、`elven.test.testRSA` 或 `elven.test.testDH` 即可。

成功时大致会看到：打印密钥（或公钥）的十六进制 / 对象信息 → 打印密文 → 再打印解密结果。AES 解密后用 `new String(plain)` 还原原文；RSA 演示里解密结果是再用 `BytesToHex` 打成十六进制（例如原文开头的 `hi` 会显示成 `6869...`），这是演示代码的写法，不是解密失败。

### 用 IDE

任意能「按包目录打开 Java 源码」的 IDE 都可以，例如 IntelliJ IDEA 或 Eclipse：

1. 用「从现有源码创建项目」或新建一个空 Java 项目。
2. 把 `encryption/src` 标成 Source Root（源码根目录），这样 `elven.encryption` / `elven.test` 才会被识别成包名。
3. 把 `encryption/sun.misc.BASE64Decoder.jar` 加到模块依赖 / Build Path。没有这一步，`Base64Util` 会找不到 `Decoder.BASE64Encoder`。
4. 打开某个 `test*.java`，运行它的 `main`。

不需要先转换成 Maven 工程；如果以后有人补构建脚本，那是增强，不是当前仓库的一部分。

## 最小用法示例

下面都直接对应源码里的 **public 方法**，没有额外封装。完整流程可以对照 `encryption/src/elven/test/`。

### Base64

```java
String data = "hi, welcome to my git area!";
String encoded = Base64Util.base64Encrypt(data.getBytes());
String decoded = Base64Util.base64Decrypt(encoded);
```

`base64Encrypt` 入参是 `byte[]`，返回 `String`；`base64Decrypt` 入参是 Base64 字符串，内部 `decodeBuffer` 后再 `new String(bytes)`（平台默认字符集）。

### AES

```java
byte[] key = AESUtil.initKey();                 // 128 bit
byte[] cipher = AESUtil.encryptAES(data.getBytes(), key);
byte[] plain = AESUtil.decryptAES(cipher, key);
```

DES / 3DES 同一模式，只是类名和密钥长度不同：

```java
byte[] desKey = DESUtil.initKey();              // 56 bit
byte[] desCipher = DESUtil.encryptDES(data.getBytes(), desKey);
byte[] desPlain = DESUtil.decryptDES(desCipher, desKey);

byte[] tdesKey = DESede.initKey();              // 168 bit
byte[] tdesCipher = DESede.encrypt3DES(data.getBytes(), tdesKey);
byte[] tdesPlain = DESede.decrypt3DES(tdesCipher, tdesKey);
```

看密文时可以用 `BytesToHex.fromBytesToHex(...)`。

### RSA（公钥加密，私钥解密）

注意方法名是 `getpublicKey`（`p` 小写）：

```java
Map<String, Object> keyMap = RSAUtil.initKey(); // 1024 bit
RSAPublicKey publicKey = RSAUtil.getpublicKey(keyMap);
RSAPrivateKey privateKey = RSAUtil.getPrivateKey(keyMap);

byte[] cipher = RSAUtil.encrypt(data.getBytes(), publicKey);
byte[] plain = RSAUtil.decrypt(cipher, privateKey);
```

`initKey()` 返回的 Map 里，键名常量是 `RSAUtil.PUBLIC_KEY`（`"RSAPublicKey"`）和 `RSAUtil.PRIVATE_KEY`（`"RSAPrivateKey"`）。

### DH 密钥交换（甲方 / 乙方协商出同一把本地密钥）

```java
Map<String, Object> keyMap1 = DHUtil.initKey();           // 甲方，1024 bit
byte[] publicKey1 = DHUtil.getPublicKey(keyMap1);
byte[] privateKey1 = DHUtil.getPrivateKey(keyMap1);

Map<String, Object> keyMap2 = DHUtil.initKey(publicKey1); // 乙方按甲方公钥生成自己的密钥对
byte[] publicKey2 = DHUtil.getPublicKey(keyMap2);
byte[] privateKey2 = DHUtil.getPrivateKey(keyMap2);

byte[] secret1 = DHUtil.getSecretKey(publicKey2, privateKey1); // 甲方：对方公钥 + 自己私钥
byte[] secret2 = DHUtil.getSecretKey(publicKey1, privateKey2); // 乙方：对方公钥 + 自己私钥
// 理想情况下 secret1 与 secret2 相同，再拿去当对称密钥用
```

`testDH` 目前只演示到「打印双方密钥 / 尝试生成本地密钥」，**没有**继续调用 DES/AES 去加密那句示例明文。`getSecretKey` 内部是 `keyAgreement.generateSecret("DES")`。

## 安全与已知限制

这些限制来自源码里真实的写法，方便你一边学习一边知道「演示和工程实践差在哪」。它们不是对原作者的批评——2016 年的入门示例本来就走最短路径。

1. **仅供学习。** 没有认证加密、没有密钥管理、没有安全随机数约定、异常也被直接 `throws Exception`。当作业或对照 JDK 文档很好；当产品加密模块不够。
2. **`Cipher.getInstance` 只用了算法名。** 例如 `"AES"`、`"DES"`、`"DESede"`、`"RSA"`，没有写全 `算法/模式/填充`。具体模式由 JCE 提供者默认值决定（常见实现里对称算法往往会落到 ECB）。ECB 相同明文块会得到相同密文，不适合大多数真实数据。
3. **DES / 3DES 已过时。** `DESUtil` 使用 56 bit DES；`DESede` 使用 168 bit 3DES。它们出现在仓库里，是因为当年教材和 JDK 示例经常从 DES 讲起，不代表今天还该选用它们。
4. **RSA / DH 密钥偏短。** `RSAUtil.initKey()` 和 `DHUtil.initKey()` 都是 **1024 bit**。现代建议通常从 2048 bit（RSA）或更安全的密钥交换方案起步。另外 RSA 示例只做了「公钥加密一小段数据」，没有签名，也没有分段加密大文件。
5. **AES 默认 128 bit。** `AESUtil.initKey()` 里写了 `keyGen.init(128)`，注释提到 192/256 需要对应的策略文件权限。即便升级到 256，只要还是 `Cipher.getInstance("AES")`，模式/填充问题仍然在。
6. **DH 协商密钥算法写死为 DES。** `DHUtil.getSecretKey` 注释里写了 `DES、3DES、AES`，代码实际调用的是 `generateSecret("DES")`。在较新的 JDK（已在 OpenJDK 21 上观察到）这一步会抛出 `NoSuchAlgorithmException: Unsupported secret key algorithm: DES`。若只是想看 DH 流程，建议用接近 2016 年的 JDK 8 环境，或自行阅读源码、不要预期它在所有现代 JDK 上都能跑通。
7. **Base64 来自捆绑 JAR，不是标准库。** Java 8 起已有 `java.util.Base64`；`sun.misc.BASE64Encoder` 在后来的 JDK 中被移除。本项目通过 `encryption/sun.misc.BASE64Decoder.jar` 提供 `Decoder` 包下的同类实现，因此 **编译、运行都必须带上这个 JAR**。不要把它理解成「还在用 JDK 内部 API」。
8. **编码与字符集。** 多处 `new String(bytes)` / `getBytes()` 没有指定 `UTF-8`。在 UTF-8 环境下跑英文示例没问题；换系统默认字符集或非 ASCII 明文时，编解码可能对不齐。
9. **演示代码本身不完整的地方。** `testDH` 声明了 `DATA` 却没有加密它；`testRSA` 把解密后的明文又转成了十六进制打印。对照工具类看 API 即可，不必把演示输出格式当成规范。

如果你把这些示例改造成作业或内部工具，更稳妥的方向通常是：明确写出 `AES/GCM/NoPadding`（或至少 `AES/CBC/PKCS5Padding` + 随机 IV）、使用 `java.util.Base64`、RSA/DH 加大密钥或换更现代的密钥交换、并引入真正的测试——那些改动超出本 README 的范围，也 **不应** 在未评估的情况下直接改本仓库里的算法实现。

## 许可证

本仓库 **没有** `LICENSE` 文件，也没有在 README 或源码头里声明许可证，因此 **许可证目前未指定**。

克隆下来自学、对照代码阅读一般没问题；若要再分发、修改后用于其他项目，请自行向仓库所有者确认授权，也欢迎所有者后续补上一份明确的开源许可证（例如 MIT、Apache-2.0 等）。本文档不会擅自添加 `LICENSE` 文件。

## 参与贡献

欢迎来。这个仓库很久没有代码活动了，因此：

- 文档勘误、用法说明、把「演示环境在哪个 JDK 上能跑」写清楚，都非常有价值。
- 若提交代码：请尽量保持「小而清晰的教学示例」定位；大规模重构、引入构建系统、或改写算法实现，建议先开 Issue 说明动机。
- 没有贡献者协议。普通的 GitHub Pull Request 即可。

感谢原作者把这些 JDK 调用例子公开出来——它们依然是理解 `KeyGenerator`、`Cipher`、`KeyPairGenerator`、`KeyAgreement` 关系的一份短小标本。

## English summary

Educational Java samples (circa 2016) for Base64, DES, 3DES, AES, Diffie–Hellman, and RSA, using the JDK crypto APIs plus a bundled `Decoder` Base64 JAR. Classic `src/` layout, no Maven/Gradle. **Not a production crypto library.** On OpenJDK 21 the AES/DES/3DES/RSA/Base64 demos run; DH key agreement fails because it requests a DES secret key. License is unspecified (no `LICENSE` file).
