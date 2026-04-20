# WeatherApp

一个基于 Android 原生技术栈的天气应用课程项目。仓库当前仍处于工程初始化阶段，根目录的 [DEV.md](/d:/Desktop/JSUT/MobileApplicationDevelopment/Weather/WeatherApp/DEV.md) 提供了较完整的开发规划，涵盖功能设计、分层结构、数据库方案和后续开发步骤。

## 项目目标

根据开发文档，本项目计划实现一个面向课程作业/实验答辩的天气应用，核心方向包括：

- 用户注册、登录与重置密码
- 城市搜索、添加、切换与管理
- 当前天气展示与未来 4 天天气预报
- 天气详情页展示
- 本地 SQLite 缓存，支持断网兜底显示

## 当前状态

当前仓库是一个可运行的 Android Studio 工程，但业务功能尚未开始落地：

- 已完成基础 Android 工程初始化
- 已配置 `app` 模块和 Material 依赖
- 当前只有 `MainActivity` 与一个简单的 `Hello World` 页面
- 开发文档已经给出了后续完整实现方案

如果你准备继续开发，这个仓库现在更适合作为“项目骨架 + 设计文档”的起点。

## 少量技术细节

- 开发语言：Java
- 构建方式：Gradle Kotlin DSL
- 包名：`com.dengyy.weatherapp`
- UI 基础：AppCompat + Material Design + ConstraintLayout
- 当前编译配置：
  - `minSdk = 30`
  - `targetSdk = 36`
  - Java 11
- 已启用 `EdgeToEdge` 页面适配
- `settings.gradle.kts` 中配置了多个国内 Maven 镜像源，便于依赖解析

开发文档中的目标技术方案还包括：

- SQLite / `SQLiteOpenHelper` 做本地数据持久化
- Activity + Adapter 的轻量分层页面组织
- Repository 协调网络数据与本地缓存
- 预留天气 API 配置，后续接入真实接口

## 计划中的目录与模块

`DEV.md` 规划了较清晰的分层结构，后续预计围绕这些模块展开：

- `model`：用户、城市、天气数据实体
- `db` / `dao`：SQLite 表结构与数据访问
- `network`：天气接口请求与 JSON 解析
- `repository`：统一对外提供数据
- `ui` / `adapter`：登录、主页、城市管理、天气详情等页面
- `utils` / `constants` / `config`：常量、工具类、API 配置

## 运行方式

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. 选择模拟器或真机运行 `app` 模块

目前运行后会进入一个基础首页界面，后续功能需要按照 `DEV.md` 继续实现。
