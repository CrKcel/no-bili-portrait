# 禁止竖屏 / No Bilibili Portrait

> 强制哔哩哔哩 App 用传统播放器播放竖屏视频，拒绝"看一看"竖屏信息流。

Forked from [WankkoRee/Portrait2Landscape](https://github.com/WankkoRee/Portrait2Landscape)，适配 Bilibili 8.x（原版在 7.x 后失效）。

## 原理

哔哩哔哩把竖屏视频路由到 `bilibili://story/...`（"看一看"播放器），横屏路由到 `bilibili://video/...`（传统播放器）。8.x 在点击时用 avid 现场拼出 story URL 再交给统一路由 BLRouter，绕过了原版 hook 的卡片字段。本模块 hook `RouteRequest.Builder` 的构造函数，在路由入口把 `story` 改写成 `video`，覆盖所有经 BLRouter 的路径。构造函数签名跨版本稳定未混淆，所以在 8.x 各版本通用。

## 支持版本

- 标准版 `tv.danmaku.bili`：已验证 8.60.0、8.97.0
- 国际版 `com.bilibili.app.in`：未测试

## 安装

LSPosed/Xposed 模块，需 root + LSPosed（Android 15 用社区 fork [LSPosed_mod](https://github.com/mywalkb/LSPosed_mod)）。

1. 从 [Releases](https://github.com/zombie12138/no-bili-portrait/releases) 下载 APK 安装
2. LSPosed 管理器启用模块，作用域勾选哔哩哔哩
3. 重启哔哩哔哩

## 致谢

原作者 [WankkoRee](https://github.com/WankkoRee) 的 [Portrait2Landscape](https://github.com/WankkoRee/Portrait2Landscape)。
