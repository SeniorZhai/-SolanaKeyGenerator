# Solana Key Generator

这是一个使用 Kotlin 编写的库，用于根据 BIP-39 助记词生成 Solana 私钥和地址。它遵循 Solana 的派生路径（m/44'/501'/0'/0'）和 SLIP-0010 标准。

## 功能

*   从 BIP-39 助记词派生 Ed25519 私钥。
*   生成 Solana 公钥和 Base58 编码的 Solana 地址。

## 构建和运行

本项目使用 Gradle 构建。

### 构建项目

```bash
./gradlew build
```

### 运行示例

`SolanaKeyGenerator.kt` 文件包含一个 `main` 函数，用于演示如何使用该库生成私钥和地址。

```bash
./gradlew run
```

运行后，您将在控制台看到生成的私钥和 Solana 地址。

## 依赖

本项目依赖于：
*   Bouncy Castle (用于 Ed25519 加密)
*   Kotlin BIP-39 (用于助记词处理)
*   Kethereum BIP32 (用于 BIP32/SLIP-0010 派生)
*   Mixin Tink Eddsa (用于 EdDSA 签名)