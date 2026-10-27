# AI Remote

用手机远程操控电脑上的 [ZCode](https://zcode.z.ai) 和 [Kimi Code](https://www.kimi.com/code) 编程助手。

启动后选择 ZCode 或 Kimi Code，粘贴电脑端生成的链接，即可通过内置 WebView 连接。两类链接分别保存在手机本地，远程控制不需要自动跳转浏览器。

## 功能

- **双入口**：首页同时提供 ZCode 与 Kimi Code 图标和独立连接列表
- **链接管理**：粘贴或从剪贴板导入远程链接，保存后下次一键直连，支持删除与撤销
- **深链唤起**：ZCode 远程链接及 Kimi Code Remote Control 链接可直接唤起 App
- **分享接收**：把包含链接的文本分享到本 App，按链接类型自动进入对应入口
- **Kimi Code 应用内连接**：Remote Control 页面及登录跳转留在 App 内，支持保存多台远程设备
- **会话体验**：屏幕常亮、网页回退优先、加载失败重试、链接复制，也可手动选择在浏览器中打开
- **隐私**：所有链接只保存在手机本地（SharedPreferences），不经过本 App 的第三方服务器

## 使用

### ZCode

1. 在电脑端 ZCode 里打开「远程」功能，生成 `zcode.z.ai/remote` 链接
2. 把链接发到手机，在首页选择 ZCode
3. 粘贴链接，点击「保存并连接」

### Kimi Code

1. 确认电脑端 Kimi Code 已登录具有 Remote Control 权限的 Kimi 账号，并保持电脑清醒、联网
2. 在电脑端运行 `kimi rc`（也可使用 `kimi remote` 或 CLI 内的 `/remote-control`）
3. 把终端生成的 `https://code-rc.kimi.com/devices/...` 链接完整发到手机；国际区账号使用 `code-rc.kimi.ai`
4. 在首页选择 Kimi Code，粘贴链接并连接，登录与电脑端相同的 Kimi 账号

Kimi Remote Control 通过 Kimi 官方中继连接，手机与电脑不需要处于同一网络，也不要把链接中的地址替换为局域网 IP。远程链接可控制电脑会话和文件，不要分享给他人。电脑端的 `kimi rc` 进程退出后，远程设备会离线。

## 构建

- JDK 17 + Android SDK（compileSdk 35，minSdk 24）
- Android Studio 直接打开即可；使用 Gradle 执行 `:app:assembleRelease`
- 产物：`app/build/outputs/apk/release/app-release.apk`（debug 签名，可直接侧载安装）

也可在 [Releases](../../releases) 页直接下载 APK。

## 技术要点

- Kotlin + XML View 体系 + Material 3（深色），无其他运行时依赖
- `HomeActivity` 双入口与深链路由 → `LinkListActivity` 分类链接管理 → `SessionActivity` WebView 会话页
- `LinkStore` SharedPreferences(JSON) 存储；ZCode 按 host:port 判重，Kimi 按远程设备 ID 判重；旧版数据继续兼容
- 适配 Android 15 边到边（edge-to-edge）与旧版系统的着色状态栏

## 声明

本项目为个人工具，非 Z.ai 或 Moonshot AI 官方产品，与 ZCode、Kimi Code 无隶属关系。
远程功能分别由对应服务提供，请遵守其服务条款。

## License

[MIT](./LICENSE)
