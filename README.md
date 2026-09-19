# ZCode Remote

用手机远程操控电脑上的 [ZCode](https://zcode.z.ai) 编程助手。

电脑端 ZCode 的「远程」功能会生成一个 `zcode.z.ai/remote` 链接，本 App 让你在手机上保存、管理这些链接，
并以内置 WebView 打开远程控制页 —— 深色主题、图标风格与电脑端一致，连接走 z.ai 官方中转，
手机不需要与电脑同一网络，也无需代理。

## 功能

- **链接管理**：粘贴或从剪贴板一键导入远程链接，保存后下次一键直连，支持删除与撤销
- **深链唤起**：在手机浏览器/微信里点开 `zcode.z.ai/remote/...` 链接，直接唤起 App 自动填入并连接
- **分享接收**：把包含链接的文本「分享」到本 App 即可导入
- **会话体验**：屏幕常亮、多返回层级（网页回退优先）、加载失败重试、链接复制、在浏览器中打开
- **隐私**：所有链接只保存在手机本地（SharedPreferences），不经过任何第三方服务器

## 使用

1. 在电脑端 ZCode 里打开「远程」功能，生成远程链接
2. 把链接发到手机（微信/二维码/任意方式）
3. 手机上安装本 App，粘贴链接 → 保存并连接

## 构建

- JDK 17 + Android SDK（compileSdk 35，minSdk 24）
- Android Studio 直接打开即可；命令行：

```bash
./gradlew :app:assembleRelease
# 产物 app/build/outputs/apk/release/app-release.apk（debug 签名，可直接侧载安装）
```

也可在 [Releases](../../releases) 页直接下载 APK。

## 技术要点

- Kotlin + XML View 体系 + Material 3（深色），无其他运行时依赖
- `HomeActivity` 入口与深链路由 → `LinkListActivity` 链接管理 → `SessionActivity` WebView 会话页
- `LinkStore` SharedPreferences(JSON) 存储，按 host:port 判重去重
- 适配 Android 15 边到边（edge-to-edge）与旧版系统的着色状态栏

## 声明

本项目为个人工具，非 Z.ai 官方产品，与 ZCode/OpenAI 无隶属关系。
「远程」功能本身由 zcode.z.ai 提供，请遵守其服务条款。

## License

[MIT](./LICENSE)
