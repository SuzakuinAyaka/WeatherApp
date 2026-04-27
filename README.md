# WeatherApp

WeatherApp 是一个使用 Android 原生技术栈开发的天气应用课程项目，目前已经完成核心业务落地。

## 项目定位

这个项目面向移动应用开发课程作业和实验答辩，重点不是做成复杂的商业级产品，而是把 Android 开发中的核心知识点串成一个完整可讲解、可演示的天气应用。当前已落地的功能包括：

- 用户注册、登录与重置密码
- 城市搜索、添加、切换与管理
- 当前天气展示与未来 3 天天气预报
- 天气详情页展示
- 本地 SQLite 缓存，支持断网兜底显示

## 当前状态

当前仓库主要状态如下：

- 已完成 Android 基础工程初始化
- 已配置 `app` 模块、Material、RecyclerView、ViewPager2、OkHttp 等依赖
- 已声明登录、注册、重置密码、主页、城市管理、天气详情等 Activity 入口
- 已实现用户、城市、天气、缓存、网络请求和解析相关代码
- 已预留高德天气接口的 `BuildConfig` 配置项，支持通过 `amap.properties` 注入真实接口信息
- 业务基本功能已全部实现

## 技术细节

- 开发语言：Java
- 构建方式：Gradle Kotlin DSL
- 包名：`com.dengyy.weatherapp`
- UI 基础：AppCompat + Material Design + ConstraintLayout
- 当前编译配置：
  - `minSdk = 24`
  - `targetSdk = 36`
  - Java 11
- 已将 `LoginActivity` 作为应用启动入口
- 已通过 `BuildConfig` 预留高德天气接口基础地址、密钥和路径
- `settings.gradle.kts` 中配置了多个国内 Maven 镜像源，便于依赖解析

## 已实现的技术路径

- SQLite / `SQLiteOpenHelper` 做本地数据持久化
- Activity + Adapter 的轻量分层页面组织
- Repository 协调网络数据与本地缓存
- AMap 天气与城市搜索接口接入

## 运行方式

1. 使用 Android Studio 打开项目根目录
2. 复制项目根目录的 `amap.properties.template` 并将新文件重命名为 `amap.properties` ，填入高德开放平台提供的api
3. 等待 Gradle 同步完成
4. 选择模拟器或真机运行 `app` 模块

当前运行后会进入登录页，登录成功后可进入主界面体验已实现的天气与城市管理流程。
