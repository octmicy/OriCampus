<div align="center">

# 原点助手 · OriCampus

校园服务安卓客户端 · 原生 Kotlin + Jetpack Compose

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

课表 · 成绩 · 综测 · 应用聚合 · 桌面小组件 · 今天吃什么

</div>

---

## 这是什么

一个面向高校场景的校园服务客户端：直连学校各业务系统的公开前端接口，把课表、成绩、综测、门户应用收在一个 App 里，无自建服务端，凭证全在本机加密保存。

- **不需要注册**：用学校账号登录即可
- **没有服务端**：数据直连学校系统，项目方不落任何数据
- **开源**：界面、协议适配、缓存策略全部可查

## 功能

- **首页**：问候与学期信息、今日课程速览、天气、可配置快捷入口、每日摘录
- **课表**：一屏式周课表（7 列 × 12 节自适应任意分辨率）、跟手翻页切周、学期切换、点击课程卡片查看详情（教师 / 教室 / 时间区间 / 周次 / 学分）
- **学期课表**：整学期全部课程的网格总表，同一时段多门课堆叠显示
- **自定义课程**：课表上没有的课（社团 / 讲座 / 自习）手动添加，支持按节次或按具体时间、单双周重复
- **成绩**：学年 + 学期两级切换、总学分 / 绩点汇总卡片、彩色分数徽标、**平均学分绩计算**（按学年合并、可勾选科目，创新创业默认排除、跨学期重复课程取第一学期）
- **应用**：校园门户应用聚合，带 ticket 免密跳转；含**成绩查询**与综测计算原生入口；**长按拖拽排序**、右上角可隐藏应用，顺序与显隐本地保存
- **综测计算**：按学校综合素质测评办法计算总分，结果页展示计算公式与带入分数，数据本机保存（德育 / 智育 / 体育 / 美育 / 劳动逐模块拆解）
- **今天吃什么**：28 个品类的转盘抽签，按组筛选、最近吃过的降权，浅色暖砂 / 深色深瓷两套皮肤
- **课程小组件**：2×2 / 2×3 / 3×3 / 3×4 / 4×4 五种规格，可翻看任意日期，自由拖拽缩放；已下课的课程自动消失，剩余课程即时补位
- **深色模式**：跟随系统，也可在「我的」里手动指定；小组件与状态栏同步跟随；支持 zip 主题包整体换肤
- **导出课表**：周课表一键导出图片，分享或存相册，配色随主题包
- **我的**：学籍卡、自定义头像、问题反馈

## 下载

到 [Releases](https://github.com/octmicy/OriCampus/releases) 下载最新 APK（需 Android 8.0 / API 26 以上）。

> 从 v0.5.1 起更换了签名证书，**从 v0.5.0 及更早版本升级需要先卸载旧版**。

## 构建

Android Studio 直接打开即可。命令行需要 **JDK 17** + **Android SDK 34**：

```bash
./gradlew :app:assembleDebug      # 产物 app/build/outputs/apk/debug/
./gradlew :app:assembleRelease    # 需要 release 签名，见下
```

依赖仓库已配置阿里云镜像优先（见 `settings.gradle.kts`），国内网络可直接拉依赖。

### Release 签名（可选）

项目根目录放 `keystore.properties`（已被 `.gitignore` 排除，不会入库）：

```properties
storeFile=oricampus-release.jks
storePassword=你的密码
keyAlias=你的别名
keyPassword=你的密码
```

缺少该文件时 release 构建不签名，debug 构建不受影响。

## 项目结构

```
app/src/main/java/cn/edu/xyc/campus/
├── data/
│   ├── remote/      # 门户 / 教务 / 学工 / 图书馆 接口适配与加解密
│   ├── local/       # SharedPreferences、课表缓存、主题、凭证存储
│   ├── model/       # 数据模型
│   └── ...
├── ui/
│   ├── screens/     # 各个页面（首页 / 课表 / 成绩 / 应用 / 我的 …）
│   ├── components/  # 复用组件（转盘、对话框、图片加载 …）
│   └── theme/       # 配色、深浅色、主题包
└── widget/          # Glance 桌面小组件
```

## 隐私

- 凭证用 Android Keystore + EncryptedSharedPreferences 加密，只存在本机
- 课表、成绩、综测数据只写本地缓存，不做任何云端同步
- **本仓库代码不包含任何统计 / 埋点 / 第三方分析 SDK**

## 常见问题

**登录提示账号或密码错误？**
这里要的是**移动门户（统一身份认证）密码**，不是教务系统密码。首次登录建议先在浏览器里登录一次门户确认密码可用。

**小组件一直是空的？**
先在 App 里打开一次课表，让数据落到本地缓存，小组件才有东西渲染。

**换手机后要重新登录吗？**
要。凭证不跟随账号迁移，这是有意为之。

## 主题包

App 支持导入 zip 主题包（「我的」→「主题外观」→「导入主题包」），可替换全局配色、底部导航图标、登录页形象、默认头像。

```
我的主题.zip
├── theme.json          # 必需，主题描述
├── nav_schedule.png    # 图标文件（可选，png/jpg/webp）
├── nav_home.png
├── ...
└── login_logo.png
```

`theme.json` 示例（`version` 必须为 1；`colors` / `icons` 都可以只写要覆盖的项）：

```json
{
  "name": "我的主题",
  "author": "你的名字",
  "version": 1,
  "colors": {
    "primary": "#1E5AA8",
    "onPrimary": "#FFFFFF",
    "primaryContainer": "#D6E3FF",
    "onPrimaryContainer": "#0F3866",
    "background": "#FFFFFF",
    "widgetBg": "#E8F1FF",
    "widgetCard": "#FFFFFF",
    "widgetText": "#22304D",
    "widgetPrimary": "#1D3F8C",
    "widgetSecondary": "#6B7B99"
  },
  "icons": {
    "nav_schedule": "nav_schedule.png",
    "nav_home": "nav_home.png",
    "nav_apps": "nav_apps.png",
    "nav_profile": "nav_profile.png",
    "login_logo": "login_logo.png"
  }
}
```

说明：

- `icons` 的 key 固定为 `nav_home` / `nav_schedule` / `nav_apps` / `nav_profile`（底部导航）、`login_logo`（登录页与引导页主视觉）、`avatar_default`（默认头像）
- 单文件不超过 6MB，建议图标用 256×256 PNG
- 「恢复默认主题」随时可回退

## 反馈

- 到 [Issues](https://github.com/octmicy/OriCampus/issues) 提交（有模板，照着填即可）
- 或在 App「我的」页面点「复制模板」，把运行环境与版本一键带走

## 免责声明

本项目为课程学习与技术交流用途，与学校官方无关；接口协议来自对公开前端页面的分析；请仅使用本人账号登录，勿作商业用途。使用本项目产生的一切后果由使用者自行承担。

## License

[MIT](LICENSE) © 2026 octmicy