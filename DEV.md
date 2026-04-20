# Android 天气应用项目开发文档

**项目代号**：WeatherApp
**文档版本**：V1.0（最终稿）
**适用场景**：移动应用开发课程期末大作业 / 课程答辩 / 实验报告
**技术栈**：Java + Android Studio + Material 3 + SQLite

---

## 第1章 项目概述

### 1.1 项目背景

随着移动互联网的普及，天气查询已成为用户使用频率最高的移动端服务之一。天气类应用涉及 Android 开发中几乎所有核心知识点：多页面 Activity 跳转、列表与卡片 UI、网络请求、JSON 解析、本地数据库持久化、本地缓存策略、用户登录体系。本项目选择"安卓天气应用"作为移动应用开发课程期末大作业的主题，能够在一个完整项目中串联课程所学的所有关键能力。

本项目不是纯演示型 Demo，也不是过度工程化的企业级项目，而是一个"贴近真实天气类产品形态、可接入真实天气 API、具备完整本地数据库能力"的学生级工程项目。

### 1.2 项目目标

1. 构建一款基于 Android 原生平台的天气查询应用。
2. 使用 Java 语言与 Android Studio 开发，UI 采用 Material 3 设计语言。
3. 实现完整的用户管理、天气展示、城市管理、天气详情四大功能模块。
4. 接入真实天气 API，具备联网查询实时天气与未来 4 天预报的能力。
5. 使用 SQLite 承担用户数据持久化、城市列表存储、天气缓存兜底三大职责。
6. 保留清晰的 API 配置预留位，最终只需填入 API Key、Base URL 与接口路径即可完成联调运行。
7. 可直接作为课程答辩演示项目与实验报告素材。

### 1.3 项目核心功能

1. **用户管理**：账号注册、登录、重置密码。
2. **天气展示**：主页展示当前城市实时天气、当日最高最低温、未来 4 天预报。
3. **城市管理**：搜索城市、添加城市、切换当前城市、管理已添加城市。
4. **天气详情**：查看当日完整天气信息，包括温度、湿度、风向、风力、天气现象等。
5. **本地缓存**：所有联网数据落库缓存，无网络时自动读取本地数据兜底展示。

### 1.4 项目亮点

1. **双数据源架构**：Repository 层统一协调"远程 API + 本地 SQLite 缓存"，具备真实工程思维。
2. **Material 3 视觉规范**：采用现代卡片式布局、柔和圆角、天气氛围渐变背景，摆脱传统学生作业模板感。
3. **MainActivity 信息密度控制**：主界面作为视觉核心，采用"天气主内容区 + 左侧抽屉式城市管理侧边栏"的双区结构，兼顾主信息聚焦与城市管理效率。
4. **API 预留位清晰**：配置类集中管理 API Key、Base URL、Path，无需改动业务代码即可切换接口提供方。
5. **完整断网兜底**：任何天气请求失败都会自动回落 SQLite 缓存，保证用户体验不中断。
6. **技术栈克制**：不引入 Kotlin、不引入 Room、不强行 MVVM，仅使用课程内可掌握技术完成，同时保留工程清晰度。

### 1.5 技术选型说明

| 技术项     | 选型                                              | 选型理由                                 |
| ---------- | ------------------------------------------------- | ---------------------------------------- |
| 开发语言   | Java                                              | 课程要求，语法成熟，调试友好             |
| IDE        | Android Studio                                    | 官方 IDE，完善的 Gradle 与模拟器支持     |
| UI 规范    | Material 3                                        | 官方最新设计语言，组件丰富、风格现代     |
| 本地数据库 | SQLite（SQLiteOpenHelper）                        | 轻量、零配置、课程可掌握、不引入额外依赖 |
| 页面组织   | Activity 为主                                     | 结构清晰、跳转直观、便于答辩讲解         |
| 网络请求   | OkHttp + 原生 JSON 解析（建议）                   | 轻量可控，学生可逐步理解每一步链路       |
| 架构风格   | 轻量分层（model/dao/repository/network/ui/utils） | 清晰、可落地，不过度设计                 |

---

## 第2章 需求分析

### 2.1 功能需求分析

#### 2.1.1 用户管理模块

1. 用户可通过用户名与密码注册账号，支持填写邮箱、手机号。
2. 用户可通过用户名与密码登录，登录成功后进入主界面。
3. 用户忘记密码时可通过"用户名 + 邮箱或手机号"进行身份校验后重置密码。
4. 所有用户数据存储在本地 SQLite `users` 表中，密码需以简单哈希（如 MD5 或 SHA-256）存储。

#### 2.1.2 天气展示模块

1. 主界面默认展示用户当前选中城市的实时天气。
2. 实时天气字段包括：温度、湿度、风向、风力、天气现象、最高温、最低温、数据发布时间。
3. 主界面下方展示未来 4 天天气预报，每条包含日期、白天/夜间天气现象、高低温、风向风力。
4. 点击当前天气卡片进入天气详情页，展示更完整信息。

#### 2.1.3 城市管理模块

1. 支持城市关键词搜索（本地城市表匹配或远程城市搜索接口）。
2. 可将搜索结果添加至已管理城市。
3. 已添加城市以可滚动列表形式展示在主页侧边栏中，并支持点击切换当前城市。
4. 可删除已添加城市（长按或侧滑）。

#### 2.1.4 天气详情模块

1. 展示当日完整天气信息。
2. 展示未来 4 天预报的详细横向或纵向列表。
3. 与主界面数据联动，通过 `adcode` 传参。

### 2.2 非功能需求分析

1. **性能**：主界面加载时间控制在 2 秒内，缓存读取应在 500ms 内返回。
2. **可用性**：无网络时 APP 不崩溃，应展示兜底缓存数据并提示"当前为离线数据"。
3. **兼容性**：最低支持 Android 7.0（API 24），目标版本 Android 14（API 34）。
4. **安全性**：用户密码不得明文存储，所有网络请求使用 HTTPS。
5. **可维护性**：API 配置、常量、字符串资源统一管理，便于后期替换。

### 2.3 UI/UX 设计目标

1. 简洁：去除一切不必要的信息，主界面聚焦当前温度与天气氛围，城市管理统一收纳进侧边栏。
2. 现代：Material 3 组件、柔和圆角、柔光阴影、渐变色背景。
3. 美观：留白充足、信息层级清晰、色彩克制。
4. 轻量高级感：字体层级分明、动效轻微、避免花哨堆砌。
5. 克制的炫技：主界面背景色与侧边栏色调可随天气现象变化（晴/阴/雨/雪对应不同渐变），温度数字使用大号细体字作为视觉锚点，保证整体视觉统一。

### 2.4 项目范围说明

**范围内**：用户本地登录体系、天气数据联网查询与缓存、城市管理、Material 3 视觉实现。

**范围外**：第三方登录（微信、QQ）、地图定位、推送通知、小组件 Widget、多语言国际化、后端服务器自建。

---

## 第3章 系统总体设计

### 3.1 系统整体架构

系统采用轻量分层架构，自上而下共五层：

```
┌─────────────────────────────────────────┐
│  UI 层（Activity + Adapter）             │
│  - 页面展示、用户交互、事件分发              │
└─────────────────────────────────────────┘
                   ↓ 调用
┌─────────────────────────────────────────┐
│  Repository 层                          │
│  - 协调 API 与 SQLite，统一对外暴露数据     │
└─────────────────────────────────────────┘
         ↓ 远程                ↓ 本地
┌──────────────────┐  ┌────────────────────┐
│  Network/API 层   │  │  DAO 层            │
│  - 请求天气接口    │  │  - SQLite 增删改查   │
└──────────────────┘  └────────────────────┘
         ↓                     ↓
┌─────────────────────────────────────────┐
│  Model 层（数据实体）                     │
└─────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────┐
│  Utils 层（工具类、常量、网络检测）          │
└─────────────────────────────────────────┘
```

### 3.2 模块划分

| 模块     | 职责                                                   | 关键类命名建议                                         |
| -------- | ------------------------------------------------------ | ------------------------------------------------------ |
| 用户模块 | 登录、注册、重置密码                                   | `UserDao`、`UserRepository`                            |
| 城市模块 | 城市搜索、添加、切换、管理                             | `CityDao`、`CityRepository`                            |
| 天气模块 | 实时天气、预报天气、缓存兜底                           | `WeatherDao`、`WeatherRepository`、`WeatherApiService` |
| UI 模块  | 7 个 Activity + 相关 Adapter                           | `MainActivity` 等                                      |
| 工具模块 | 网络检测、日期格式、加密、常量、SharedPreferences 封装 | `NetworkUtils`、`DateUtils`、`MD5Utils`、`Constants`   |

### 3.3 页面结构设计

```
启动页
  ↓
LoginActivity ──→ RegisterActivity
   │         ──→ ResetPasswordActivity
   ↓ 登录成功
MainActivity
   ├──→ 侧边栏（用户信息概要 / 设置 / 城市列表 / 添加城市）
   ├──→ AddCityActivity
   ├──→ SettingsActivity
   └──→ DetailsOfTodayActivity
```

### 3.4 用户使用流程

1. 启动 APP → 进入 `LoginActivity`。
2. 已注册用户输入账号密码登录 → 校验通过进入 `MainActivity`。
3. 未注册用户跳转至 `RegisterActivity` 完成注册。
4. 忘记密码跳转至 `ResetPasswordActivity` 验证后修改密码。
5. 进入 `MainActivity` 后自动加载当前城市实时天气与 4 天预报。
6. 用户点击主界面侧边栏按钮，展开抽屉查看用户信息概要、设置入口和已添加城市列表。
7. 用户在侧边栏城市列表中点击任一城市，切换当前城市并刷新主界面。
8. 用户点击侧边栏悬浮的添加城市按钮进入 `AddCityActivity` 搜索并添加。
9. 用户点击侧边栏设置按钮进入 `SettingsActivity`，可执行退出登录等操作。
10. 用户点击天气卡片进入 `DetailsOfTodayActivity` 查看完整信息。

### 3.5 推荐项目目录结构

```
com.example.weatherapp
├── WeatherApplication.java             // 自定义 Application
├── config
│   └── ApiConfig.java                  // API Key、Base URL、Path 预留位
├── constants
│   └── Constants.java                  // 全局常量（表名、字段名、SP Key）
├── model
│   ├── User.java
│   ├── City.java
│   ├── CurrentWeather.java
│   └── ForecastWeather.java
├── db
│   ├── DBHelper.java                   // SQLiteOpenHelper
│   └── dao
│       ├── UserDao.java
│       ├── CityDao.java
│       ├── CurrentWeatherDao.java
│       └── ForecastWeatherDao.java
├── network
│   ├── HttpClient.java                 // OkHttp 封装
│   ├── WeatherApiService.java          // 接口方法定义
│   └── parser
│       ├── CurrentWeatherParser.java
│       └── ForecastWeatherParser.java
├── repository
│   ├── UserRepository.java
│   ├── CityRepository.java
│   └── WeatherRepository.java
├── ui
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── ResetPasswordActivity.java
│   ├── MainActivity.java
│   ├── AddCityActivity.java
│   ├── SettingsActivity.java
│   └── DetailsOfTodayActivity.java
├── adapter
│   ├── ForecastAdapter.java
│   ├── CitySearchAdapter.java
│   └── SavedCityAdapter.java
└── utils
    ├── NetworkUtils.java
    ├── DateUtils.java
    ├── MD5Utils.java
    ├── SPUtils.java
    └── ToastUtils.java
```

### 3.6 当前技术方案为什么适合本项目

1. **Java + SQLiteOpenHelper + Activity**：课程核心知识点全覆盖，便于答辩讲解每一层。
2. **轻量分层不强制 MVVM**：学生可在有限开发周期内完成，架构清晰但不复杂。
3. **Repository 作为唯一数据出口**：UI 层不直接触碰数据库和网络，学生容易理解"谁找谁要数据"。
4. **API 配置集中预留**：后期只需在 `ApiConfig` 填写真实值即可联调，不影响已开发业务逻辑。

---

## 第4章 数据库设计

### 4.1 SQLite 设计思路

SQLite 在本项目中承担两类职责：

1. **业务数据存储**：`users` 表、`cities` 表，存储用户与城市信息，属于项目正式业务数据。
2. **天气数据缓存**：`current_weather_cache` 表、`forecast_weather_cache` 表，作为远程 API 返回数据的本地镜像，用于断网兜底与减少重复请求。

数据库通过 `DBHelper` 统一管理，初始化时一次性创建四张表，后续通过对应 DAO 访问。数据库文件名建议为 `weather_app.db`，初始版本号 1。

### 4.2 `users` 表设计

**用途**：存储所有本地注册用户的账号信息与当前选中的城市。
**对应页面**：`LoginActivity`、`RegisterActivity`、`ResetPasswordActivity`、`MainActivity`（读取 `current_city` 决定默认展示城市）。

| 字段名         | 类型    | 约束                      | 说明                                          |
| -------------- | ------- | ------------------------- | --------------------------------------------- |
| `id`           | INTEGER | PRIMARY KEY AUTOINCREMENT | 用户主键                                      |
| `username`     | TEXT    | NOT NULL UNIQUE           | 登录用户名                                    |
| `email`        | TEXT    |                           | 注册邮箱，用于重置密码身份校验                |
| `phone`        | TEXT    |                           | 手机号，用于重置密码身份校验                  |
| `password`     | TEXT    | NOT NULL                  | 密码（MD5 或 SHA-256 哈希后存储）             |
| `current_city` | TEXT    |                           | 当前选中城市的 `adcode`，用于主页加载默认城市 |

### 4.3 `cities` 表设计

**用途**：存储用户已添加的城市列表，支持切换与排序。
**对应页面**：`MainActivity`（读取当前城市）、`AddCityActivity`（添加城市入库）。

| 字段名        | 类型    | 约束                      | 说明                           |
| ------------- | ------- | ------------------------- | ------------------------------ |
| `id`          | INTEGER | PRIMARY KEY AUTOINCREMENT | 主键                           |
| `city_name`   | TEXT    | NOT NULL                  | 城市中文名，如"北京"           |
| `adcode`      | TEXT    | NOT NULL UNIQUE           | 城市行政编码，用于 API 查询    |
| `is_selected` | INTEGER | DEFAULT 0                 | 是否为当前选中城市（0/1）      |
| `sort_order`  | INTEGER | DEFAULT 0                 | 排序序号，用于管理列表展示顺序 |

### 4.4 `current_weather_cache` 表设计

**用途**：缓存每个城市最近一次的实时天气查询结果，支持无网兜底。
**对应页面**：`MainActivity`、`DetailsOfTodayActivity`。

| 字段名              | 类型    | 约束                      | 说明                                         |
| ------------------- | ------- | ------------------------- | -------------------------------------------- |
| `id`                | INTEGER | PRIMARY KEY AUTOINCREMENT | 主键                                         |
| `city_name`         | TEXT    |                           | 城市名                                       |
| `adcode`            | TEXT    | NOT NULL UNIQUE           | 城市 adcode，作为缓存命中依据                |
| `temperature`       | TEXT    |                           | 当前温度（℃）                                |
| `humidity`          | TEXT    |                           | 湿度（%）                                    |
| `weather_condition` | TEXT    |                           | 天气现象，如"晴"、"多云"                     |
| `wind_direction`    | TEXT    |                           | 风向                                         |
| `wind_power`        | TEXT    |                           | 风力等级                                     |
| `high_temperature`  | TEXT    |                           | 当日最高温                                   |
| `low_temperature`   | TEXT    |                           | 当日最低温                                   |
| `report_time`       | TEXT    |                           | 接口返回的数据发布时间                       |
| `update_time`       | INTEGER |                           | 本地写入时间戳（毫秒），用于判断缓存是否过期 |

### 4.5 `forecast_weather_cache` 表设计

**用途**：缓存每个城市未来 4 天的预报信息，支持无网兜底。
**对应页面**：`MainActivity`（预报列表）、`DetailsOfTodayActivity`（详情页预报）。

| 字段名                 | 类型    | 约束                      | 说明                     |
| ---------------------- | ------- | ------------------------- | ------------------------ |
| `id`                   | INTEGER | PRIMARY KEY AUTOINCREMENT | 主键                     |
| `city_name`            | TEXT    |                           | 城市名                   |
| `adcode`               | TEXT    | NOT NULL                  | 城市 adcode              |
| `forecast_date`        | TEXT    | NOT NULL                  | 预报日期，如"2026-04-21" |
| `day_weather`          | TEXT    |                           | 白天天气现象             |
| `night_weather`        | TEXT    |                           | 夜间天气现象             |
| `day_temp`             | TEXT    |                           | 白天温度（最高温）       |
| `night_temp`           | TEXT    |                           | 夜间温度（最低温）       |
| `day_wind_direction`   | TEXT    |                           | 白天风向                 |
| `night_wind_direction` | TEXT    |                           | 夜间风向                 |
| `day_wind_power`       | TEXT    |                           | 白天风力                 |
| `night_wind_power`     | TEXT    |                           | 夜间风力                 |
| `update_time`          | INTEGER |                           | 本地写入时间戳           |

**联合唯一约束建议**：`(adcode, forecast_date)` 作为业务唯一键，写入时采用"先按此条件删除旧记录再插入新记录"或 `INSERT OR REPLACE` 策略。

### 4.6 表之间关系

1. `users.current_city` → `cities.adcode`（逻辑外键，代码中校验）。
2. `cities.adcode` → `current_weather_cache.adcode`、`forecast_weather_cache.adcode`（逻辑外键）。
3. 实际不建立 SQL 外键约束，由 Repository 层保证一致性，便于课程实现与讲解。

### 4.7 缓存数据职责说明

1. **写入时机**：每次成功调用天气接口后，Repository 层同步写入对应缓存表。
2. **读取时机**：
   - 优先请求网络 → 成功后更新缓存 → 返回数据给 UI。
   - 网络失败或无网络 → 读取对应 `adcode` 的缓存记录 → 返回给 UI，并提示"当前为离线数据"。
3. **缓存过期策略**：建议以 `update_time` 为判断依据，2 小时内视为有效缓存可直接使用（可选优化，初版可每次都请求网络）。
4. **后续填入 API 后如何直接联调**：业务代码已按"请求→落库→回读"链路写完，只需在 `ApiConfig` 中填入 Key、Base URL、Path，并在 Parser 中确认字段映射即可运行。

---

## 第5章 API 接入与数据流设计

### 5.1 天气 API 能力拆分

项目需要接入以下三类能力（兼容主流天气服务商如和风天气、高德天气、OpenWeather 等）：

1. **当前实时天气接口**：返回温度、湿度、风向、风力、天气现象等。
2. **未来多日天气预报接口**：返回至少 4 天每日白天/夜间的天气、温度、风向风力。
3. **城市搜索/识别接口（可选）**：根据关键词返回城市列表与 `adcode`；若服务商不提供，可改为在本地预置主要城市字典。

### 5.2 当前天气接口设计说明

**调用页面**：`MainActivity`、`DetailsOfTodayActivity`。

**接口约定（预留）**：
- Base URL：在 `ApiConfig.BASE_URL` 配置。
- Path：在 `ApiConfig.PATH_CURRENT_WEATHER` 配置。
- 请求方式：GET。
- 请求参数：`key`（API Key）、`city` 或 `adcode`、`extensions=base`（视服务商而定）。
- 返回数据：JSON 格式，包含城市信息、温度、湿度、风向、风力、天气现象、发布时间。

**关键字段映射**（以常见返回结构为基准，实际以所选服务商为准）：

| API 返回字段                | 映射到 `CurrentWeather` 实体 | 映射到 SQLite 字段  |
| --------------------------- | ---------------------------- | ------------------- |
| `city` / `name`             | `cityName`                   | `city_name`         |
| `adcode` / `id`             | `adcode`                     | `adcode`            |
| `temperature` / `temp`      | `temperature`                | `temperature`       |
| `humidity`                  | `humidity`                   | `humidity`          |
| `weather` / `text`          | `weatherCondition`           | `weather_condition` |
| `winddirection` / `windDir` | `windDirection`              | `wind_direction`    |
| `windpower` / `windScale`   | `windPower`                  | `wind_power`        |
| `reporttime` / `obsTime`    | `reportTime`                 | `report_time`       |

最高温与最低温若当前接口不返回，可从预报接口当日数据中提取合并。

### 5.3 未来 4 天预报接口设计说明

**调用页面**：`MainActivity`（预报横向列表）、`DetailsOfTodayActivity`（完整预报）。

**接口约定（预留）**：
- Path：在 `ApiConfig.PATH_FORECAST_WEATHER` 配置。
- 请求参数：`key`、`city` 或 `adcode`、`extensions=all`（视服务商而定）。
- 返回数据：包含多日的预报数组。

**关键字段映射**：

| API 返回字段                    | 映射到 `ForecastWeather` 实体 | 映射到 SQLite 字段     |
| ------------------------------- | ----------------------------- | ---------------------- |
| `date` / `fxDate`               | `forecastDate`                | `forecast_date`        |
| `dayweather` / `textDay`        | `dayWeather`                  | `day_weather`          |
| `nightweather` / `textNight`    | `nightWeather`                | `night_weather`        |
| `daytemp` / `tempMax`           | `dayTemp`                     | `day_temp`             |
| `nighttemp` / `tempMin`         | `nightTemp`                   | `night_temp`           |
| `daywind` / `windDirDay`        | `dayWindDirection`            | `day_wind_direction`   |
| `nightwind` / `windDirNight`    | `nightWindDirection`          | `night_wind_direction` |
| `daypower` / `windScaleDay`     | `dayWindPower`                | `day_wind_power`       |
| `nightpower` / `windScaleNight` | `nightWindPower`              | `night_wind_power`     |

若返回多于 4 天，仅取前 4 天（课程要求）；若少于 4 天，取全部并在 UI 层兼容展示。

### 5.4 城市搜索接口设计说明

**调用页面**：`AddCityActivity`。

**两种实现方式任选其一**：

1. **远程搜索**（推荐，若服务商支持）：
   - Path：在 `ApiConfig.PATH_CITY_SEARCH` 配置。
   - 请求参数：`key`、`keywords`。
   - 返回：城市列表（`city_name`、`adcode`）。

2. **本地词典**（兜底方案）：
   - 在 `assets/cities.json` 中预置全国主要城市与 `adcode` 映射。
   - 输入关键词时本地模糊匹配。
   - 优点：无额外 API 费用、零延迟、演示稳定。

### 5.5 API 调用链路设计

以"主界面加载当前天气"为例：

```
MainActivity.onCreate
    ↓
WeatherRepository.getCurrentWeather(adcode)
    ↓
判断 NetworkUtils.isConnected()
    ├── 有网 → WeatherApiService.requestCurrentWeather(adcode)
    │         ↓ HttpClient.get(url)
    │         ↓ CurrentWeatherParser.parse(json) → CurrentWeather
    │         ↓ CurrentWeatherDao.insertOrReplace(weather)
    │         ↓ 返回实体给 UI
    │
    └── 无网/请求失败 → CurrentWeatherDao.queryByAdcode(adcode)
                      ↓ 返回缓存实体给 UI
                      ↓ UI 顶部提示"当前为离线数据"
```

### 5.6 API 返回数据到本地模型的映射关系

1. 所有 API 请求的原始 JSON 字符串由 `HttpClient` 返回。
2. 各 `Parser` 类负责将 JSON 转换为 `CurrentWeather` / `ForecastWeather` / `City` 实体。
3. Parser 是唯一与具体 API 返回结构耦合的位置，**更换服务商时只需修改 Parser**。
4. 实体类字段与 SQLite 字段一一对应，DAO 层直接读写实体。

### 5.7 API 数据写入 SQLite 缓存的流程

1. API 请求成功 → Parser 解析为实体。
2. Repository 调用对应 DAO 的 `insertOrReplace`（基于 `adcode` 或 `(adcode, forecast_date)` 覆盖）。
3. 记录 `update_time = System.currentTimeMillis()`。
4. 数据写入后立即回调给 UI 层刷新。

### 5.8 网络失败时的本地兜底读取流程

1. UI 层调用 Repository 获取数据。
2. Repository 首先判断网络状态：
   - 无网：直接走缓存路径。
   - 有网：发起请求，请求失败（超时、非 200、解析异常）也走缓存路径。
3. 缓存路径：DAO 根据 `adcode` 查询本地记录。
4. 若缓存也无数据（首次进入且无网），UI 层展示空态提示"暂无天气数据，请联网后重试"。

### 5.9 API Key / Base URL / Path / 参数预留位置说明

所有 API 配置统一集中在 `config/ApiConfig.java` 中，字段建议：

| 配置项                  | 说明                                  |
| ----------------------- | ------------------------------------- |
| `BASE_URL`              | 天气服务商根地址                      |
| `API_KEY`               | 申请到的 Key                          |
| `PATH_CURRENT_WEATHER`  | 当前天气接口路径                      |
| `PATH_FORECAST_WEATHER` | 预报接口路径                          |
| `PATH_CITY_SEARCH`      | 城市搜索接口路径（可选）              |
| `PARAM_KEY_NAME`        | Key 参数名（如 `key`）                |
| `PARAM_CITY_NAME`       | 城市参数名（如 `city` 或 `location`） |
| `EXTENSIONS_BASE`       | 实时天气扩展参数（如和风的 `base`）   |
| `EXTENSIONS_ALL`        | 预报扩展参数（如 `all`）              |

**最终联调步骤**：

1. 注册目标 API 服务商账号，获取 `API_KEY`。
2. 在 `ApiConfig` 中填入 Key、Base URL、各 Path。
3. 核对所选服务商的实际 JSON 字段名，调整 `Parser` 类的字段读取。
4. 编译运行，即可进入真实联调。

---

## 第6章 页面与功能详细设计

### 6.1 `LoginActivity`

**本轮 UI / 交互修订补充**：
- 登录页字段命名统一使用“账号 / 密码”，不再显示“用户名”文案。
- 字段名与输入提示语分离：字段名放在输入框上方，hint 仅保留在输入框内部。
- 输入框获得焦点后隐藏 hint，失焦且内容为空时恢复 hint。
- 页面结构简化为“标题区 + 单表单面板 + 底部跳转入口”，避免多层卡片堆叠。

**页面目标**：用户输入账号密码完成登录，作为 APP 入口。

**界面组成**：
- 顶部应用 Logo 与欢迎文案（大标题 + 副标题）。
- 用户名输入框（`TextInputLayout` + `TextInputEditText`，填充样式 Outlined）。
- 密码输入框（带密码可见性切换图标）。
- 主登录按钮（`FilledButton`，圆角 16dp）。
- 次级操作区：左下"忘记密码"、右下"立即注册"，均为 `TextButton`。

**Material 3 组件**：`TopAppBar`（可选）、`TextInputLayout`、`MaterialButton`、`TextView` 带 Material Typography。

**功能逻辑**：
1. 校验输入非空。
2. 通过 `UserRepository.login(username, passwordHash)` 查询 SQLite。
3. 成功：写入 `SPUtils` 登录态（当前用户 id），跳转 `MainActivity` 并 `finish`。
4. 失败：Snackbar 提示"账号或密码错误"。

**数据来源**：SQLite `users` 表。

**涉及 API**：否。
**涉及 SQLite**：是（`users` 表查询）。

**跳转关系**：
- → `RegisterActivity`（注册入口）
- → `ResetPasswordActivity`（忘记密码）
- → `MainActivity`（登录成功）

**开发注意事项**：
1. 密码哈希方式应与注册页保持一致。
2. 登录态存储建议使用 `SharedPreferences` 键 `KEY_CURRENT_USER_ID`。
3. 若 APP 已登录，启动时直接跳转主界面，跳过登录页。

---

### 6.2 `RegisterActivity`

**本轮 UI / 交互修订补充**：
- 注册页上半区优先展示“账号 / 密码 / 确认密码”，邮箱与手机号移动至下方。
- 邮箱与手机号字段必须明确标注“可选”。
- 注册页继续沿用“字段名外置 + hint 内置 + 焦点时隐藏 hint”的全局输入规范。
- 页面保持单表单面板结构，不拆分为多个独立卡片。

**页面目标**：完成新用户注册并写入本地数据库。

**界面组成**：
- `TopAppBar`（带返回箭头）。
- 标题"创建账号"。
- 输入字段：用户名、邮箱、手机号、密码、确认密码。
- 主按钮"注册"。

**Material 3 组件**：`TopAppBar`、`TextInputLayout`、`MaterialButton`。

**功能逻辑**：
1. 校验用户名非空且长度合法、邮箱格式、手机号格式、两次密码一致。
2. 通过 `UserRepository.isUsernameExists` 判重。
3. 密码哈希后调用 `UserRepository.register` 写入 `users` 表。
4. 注册成功：Snackbar 提示，自动回跳登录页并回填用户名。

**数据来源**：用户输入 + SQLite 判重查询。

**涉及 API**：否。
**涉及 SQLite**：是（`users` 表插入与查询）。

**跳转关系**：← 从 `LoginActivity` 进入，完成后返回。

**开发注意事项**：
1. 用户名唯一约束由 SQL 层与业务层双重保证。
2. 输入校验建议封装在 `ValidateUtils`。

---

### 6.3 `ResetPasswordActivity`

**本轮 UI / 交互修订补充**：
- 找回密码页字段命名统一使用“账号 / 邮箱或手机号 / 密码 / 确认密码”。
- 页面结构控制为单表单面板，减少视觉装饰与重复分组。
- 输入框交互继续遵循“字段名外置 + hint 内置 + 焦点时隐藏 hint”的统一规则。

**页面目标**：通过已知信息校验身份后重置密码。

**界面组成**：
- `TopAppBar`（带返回）。
- 输入字段：用户名、邮箱或手机号（任一匹配即可）、新密码、确认新密码。
- 主按钮"重置密码"。

**Material 3 组件**：`TopAppBar`、`TextInputLayout`、`MaterialButton`。

**功能逻辑**：
1. 校验用户名存在。
2. 校验输入的邮箱或手机号与 `users` 表中该用户记录匹配。
3. 校验两次新密码一致。
4. 哈希后通过 `UserRepository.updatePassword` 更新。
5. 成功后回跳登录页。

**数据来源**：SQLite `users` 表。

**涉及 API**：否。
**涉及 SQLite**：是（`users` 表查询与更新）。

**跳转关系**：← 从 `LoginActivity` 进入，完成后返回。

**开发注意事项**：
1. 身份校验字段应与注册时填写一致。
2. 提示语应明确区分"用户不存在"与"邮箱/手机号不匹配"。

---

### 6.4 `MainActivity`（视觉与交互核心）

**页面目标**：作为 APP 主视觉入口，展示当前城市实时天气与未来 4 天预报；同时通过统一风格的侧边栏完成用户概要、设置入口与城市管理。

**界面组成（自上而下）**：

1. **整体容器**：采用 `DrawerLayout` 或等价抽屉式布局，主内容区与左侧边栏共用同一套主题色、圆角和阴影语言。
2. **顶部状态栏沉浸式**：状态栏透明，内容延伸至状态栏下。
3. **顶部操作栏**：
   - 左侧：侧边栏按钮（汉堡按钮或头像按钮），点击展开侧边栏。
   - 中间：当前城市名与简短天气概览。
   - 右侧：刷新或详情辅助操作按钮，避免与侧边栏功能重复。
4. **主天气卡片**（当前温度核心区，占屏幕约 35%）：
   - 超大温度数字（如 `24°`，字号约 96sp，Light 字重）。
   - 天气现象文字（如"多云"）。
   - 副信息行：最高温 / 最低温、数据发布时间。
   - 背景：根据天气现象渐变（晴=暖黄蓝、多云=灰蓝、雨=深蓝、雪=浅灰白），`ShapeableImageView` 或渐变 Drawable。
5. **天气详情卡片**（卡片化 3 栏）：湿度、风向、风力。使用 `MaterialCardView`，柔和阴影，圆角 20dp。
6. **未来 4 天预报卡片**：
   - 标题"未来 4 天"。
   - `RecyclerView` 横向或纵向 4 条，每条显示日期（周几 + 月日）、天气图标、高/低温、风向风力。
7. **底部"查看详情"按钮或整卡可点击**：点击进入 `DetailsOfTodayActivity`。
8. **侧边栏内容**：
   - 顶部用户信息概要区：头像、用户名、当前默认城市，可附带一句轻量状态文案。
   - 右上或顶部尾部设置按钮：进入设置页或弹出设置菜单。
   - 中部到底部：使用 `RecyclerView` 展示已添加城市列表，列表可滚动并占满剩余空间。
   - 底部右下角或列表区域悬浮 `FloatingActionButton`：用于添加城市，点击进入 `AddCityActivity`。
9. **下拉刷新**：`SwipeRefreshLayout` 包裹主内容区，触发重新请求 API。

**Material 3 组件**：`DrawerLayout`、`MaterialToolbar`、`MaterialCardView`、`NavigationView` 或自定义侧边栏容器、`FloatingActionButton`、`FilledTonalButton`、`SwipeRefreshLayout`、`RecyclerView` + `ForecastAdapter` / `SavedCityAdapter`、`Snackbar`、`CircularProgressIndicator`。

**功能逻辑**：
1. `onCreate` 读取 `SPUtils` 当前用户 id → 查询 `users.current_city` → 获取 `adcode`。
2. 若无当前城市（首次登录），自动写入默认城市（如"北京" `110100`）。
3. 调用 `WeatherRepository.getCurrentWeather(adcode)` 与 `getForecast(adcode)`。
4. 加载 `users` 表中的当前用户信息和 `cities` 表中的已添加城市列表，填充侧边栏顶部概要区与滚动列表。
5. 展示天气返回数据。若来自缓存，顶部以 Snackbar 提示"离线数据"。
6. 下拉刷新触发强制刷新（不走缓存优先）。
7. 点击侧边栏城市项 → 更新 `is_selected` 与 `users.current_city` → 关闭侧边栏并重新加载主界面。
8. 点击侧边栏悬浮添加按钮 → 跳转 `AddCityActivity`。
9. 点击设置按钮 → 进入 `SettingsActivity`，可进行退出登录、主题跟随系统等操作。

**数据来源**：
- 城市信息：SQLite `cities` 表、`users` 表。
- 天气数据：Repository（API + 缓存）。

**涉及 API**：是（当前天气接口 + 预报接口）。
**涉及 SQLite**：是（`users`、`cities`、`current_weather_cache`、`forecast_weather_cache`）。

**跳转关系**：
- → `AddCityActivity`
- → `DetailsOfTodayActivity`（带 `adcode` 参数）
- → `SettingsActivity`
- → 退出登录回到 `LoginActivity`

**缓存策略**：
- 启动 → 先读缓存秒显 → 异步请求 API → 成功后刷新 UI 并更新缓存。
- 或：先请求 API → 成功则展示并更新缓存；失败则读缓存兜底。
- 推荐初版采用后者，逻辑更清晰。

**开发注意事项**：
1. MainActivity 是演示核心，务必在布局上下功夫，推荐使用 `DrawerLayout` + 主内容滚动容器的组合。
2. 侧边栏与主内容区必须保持统一视觉语言，包括背景色阶、卡片圆角、图标风格和间距系统，避免像两个独立页面拼接。
3. 温度数字使用 `com.google.android.material` 字体或系统 `sans-serif-light`。
4. 渐变背景可使用 `GradientDrawable` 代码生成，根据天气字段动态切换；侧边栏应使用同色系较低饱和版本，避免喧宾夺主。
5. 城市列表建议展示当前选中态、天气简要信息或定位标记，但信息密度要克制。
6. 所有网络与数据库操作放到子线程（`ExecutorService` 或 `HandlerThread`），主线程只更新 UI。

---

### 6.5 `AddCityActivity`

**页面目标**：搜索并添加城市到已管理列表。

**界面组成**：
- `TopAppBar`（带返回、标题"添加城市"）。
- `SearchBar` 或顶部搜索输入框（Material 3 `SearchBar`）。
- 搜索结果列表（`RecyclerView`）：每项展示城市名 + 所属省份。
- 已添加城市列表区域（标题"已添加"，下方列表）：每项支持点击切换、左滑或长按删除。

**Material 3 组件**：`SearchBar`、`SearchView`、`MaterialToolbar`、`RecyclerView`、`MaterialCardView`、`Chip`（可选展示热门城市）。

**功能逻辑**：
1. 用户输入关键词 → 触发搜索（防抖 300ms）。
2. 方案 A：调用远程城市搜索接口获取结果；方案 B：本地 `assets/cities.json` 模糊匹配。
3. 点击搜索结果项 → 写入 `cities` 表（若不存在）→ 更新 `sort_order`。
4. 点击已添加列表项 → 更新该条 `is_selected=1`，其他置 0；同时更新 `users.current_city`；`finish` 返回主界面。
5. 长按已添加项 → 弹出 `AlertDialog` 确认删除。

**数据来源**：
- 搜索结果：远程 API 或本地 `assets`。
- 已添加城市：SQLite `cities` 表。

**涉及 API**：可选（城市搜索接口）。
**涉及 SQLite**：是（`cities` 表增删改查 + `users` 表更新）。

**跳转关系**：← 从 `MainActivity` 侧边栏中的悬浮添加城市按钮进入，完成后返回。

**开发注意事项**：
1. 搜索建议采用 `TextWatcher` + `Handler` 防抖。
2. 已添加列表与搜索结果列表可使用两个独立 `RecyclerView` + 不同 Adapter。
3. 城市不允许重复添加，按 `adcode` 判重。
4. 页面视觉需与主页和侧边栏保持统一的配色、圆角和按钮样式，避免新增页面风格跳脱。

---

### 6.6 `DetailsOfTodayActivity`

**页面目标**：展示当日及未来 4 天完整天气信息，与主界面联动。

**界面组成**：
- `CollapsingToolbarLayout`：折叠标题显示城市名，展开时显示当前温度、天气现象、发布时间，背景同主页渐变。
- 核心信息区（卡片化）：
  - 当前温度、天气现象大卡片。
  - 详细信息网格（2×3 或 3×2）：湿度、风向、风力、最高温、最低温、发布时间。
- 未来 4 天预报详细卡片：每天一行，展示日期、白天/夜间天气、高/低温、白天/夜间风向、白天/夜间风力。

**Material 3 组件**：`CollapsingToolbarLayout`、`MaterialToolbar`、`MaterialCardView`、`RecyclerView`、`MaterialDivider`。

**功能逻辑**：
1. 通过 `Intent` 接收 `adcode` 参数（从主页传入）。
2. 调用 `WeatherRepository.getCurrentWeather(adcode)` 与 `getForecast(adcode)`。
3. 优先从缓存读取（主界面刚请求过，缓存必定新鲜），再异步校验是否需刷新。
4. 下拉刷新强制重新请求。

**数据来源**：SQLite 缓存 + API。

**涉及 API**：是。
**涉及 SQLite**：是（读取 `current_weather_cache`、`forecast_weather_cache`）。

**跳转关系**：← 从 `MainActivity` 进入。

**开发注意事项**：
1. 进入该页面可优先展示缓存（来自主页刚请求的数据），体感更快。
2. `CollapsingToolbarLayout` 折叠动画需调试好，避免顶部信息撞色不清。
3. 预报列表与主页 Adapter 可共用，通过布局变体切换展示密度。

---

### 6.7 `SettingsActivity`

**页面目标**：提供应用级设置入口，并在设置页中提供明确、安全的退出登录功能。

**界面组成**：
- `TopAppBar`（带返回、标题"设置"）。
- 用户信息摘要卡片：显示当前登录用户名、绑定邮箱或手机号、当前城市。
- 通用设置区域：如主题跟随系统、关于应用、版本信息等。
- 账号操作区域：包含醒目的"退出登录"按钮，建议放在页面下部或独立危险操作卡片中。

**Material 3 组件**：`MaterialToolbar`、`MaterialCardView`、`MaterialSwitch`、`ListItem`、`FilledTonalButton`、`OutlinedButton`、`MaterialAlertDialogBuilder`。

**功能逻辑**：
1. 页面加载时读取 `SPUtils` 中的当前用户 id，并查询 `users` 表填充用户信息摘要。
2. 用户点击"退出登录"按钮后，弹出二次确认对话框，提示退出后将返回登录页。
3. 用户确认退出登录后，清除 `SPUtils` 中当前登录用户 id、登录态标记及必要的临时会话信息。
4. 使用 `Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK` 跳转回 `LoginActivity`，防止用户通过返回键回到主页面。
5. 若用户取消，则关闭弹窗并停留在设置页。

**数据来源**：
- 用户信息：SQLite `users` 表。
- 登录态：`SPUtils` / `SharedPreferences`。

**涉及 API**：否。
**涉及 SQLite**：是（仅读取 `users` 表）。

**跳转关系**：
- ← 从 `MainActivity` 侧边栏设置按钮进入。
- → 退出登录后跳转 `LoginActivity`。

**开发注意事项**：
1. 退出登录按钮需在视觉上与普通设置项区分，建议使用强调色边框或独立卡片承载。
2. 退出登录只清理登录态与会话类数据，不删除本地 `users`、`cities` 和天气缓存表数据。
3. 退出登录后重新进入应用时，应优先检查登录态是否为空，避免绕过登录页。
4. 设置页整体风格需与主页和侧边栏保持一致，避免出现系统设置页式的割裂感。

---

## 第7章 UI 设计规范

### 7.1 Material 3 使用原则

1. 统一使用 `Theme.Material3.DayNight` 作为应用主题父主题。
2. 所有按钮优先使用 Material 3 的 `MaterialButton` 变体：`Filled`、`FilledTonal`、`Outlined`、`Text`。
3. 所有卡片使用 `MaterialCardView`，默认圆角 20dp，`cardElevation=2dp`。
4. 所有输入框使用 `TextInputLayout` + `TextInputEditText`，样式 Outlined。
5. 所有页面默认适配顶部状态栏：状态栏透明，内容区通过统一的 inset 处理下沉到安全区域内，禁止标题、按钮、列表首项与状态栏重叠。
6. 所有输入类页面都要在键盘收起时自动退出文本输入状态：当前获得焦点的输入框需要 `clearFocus()`，光标与激活描边同步消失。
7. 输入框文案拆分为“字段名 + 提示语”两层：字段名通过输入框上方独立 `TextView` 展示，提示语仅作为输入框内部 hint；输入框获得焦点后 hint 立即隐藏，失焦且内容为空时再恢复。
8. 页面结构优先做减法：非信息密集页尽量使用单主容器或单表单面板，避免堆叠多层卡片、重复分组标题和过度装饰。

### 7.1.1 全局交互约束

1. 后续所有 Activity 页面都必须接入统一的状态栏适配逻辑，确保浅色、深色、横竖屏下顶部安全区表现一致。
2. 后续所有包含 `EditText` / `TextInputEditText` 的页面都必须接入统一的键盘关闭监听逻辑，键盘关闭后退出输入态。
3. 表单页面点击空白区域时应允许输入框失焦，避免键盘收起后仍保留激活边框。
4. 注册、设置、搜索、详情编辑等后续输入型页面都必须沿用“字段名外置 + hint 内置 + 焦点时隐藏 hint”的交互规范。
5. 所有需要表单或长内容的页面必须支持纵向滚动交互，页面内容区可上下拖动，禁止内容溢出后被固定高度截断。
6. 页面结构调整不能破坏原有操作入口的语义位置；如“去注册”“忘记密码”“返回登录”等次级按钮，应优先保留在原有信息层级附近，而不是机械上移到页面顶部。
5. 顶部栏统一使用 `MaterialToolbar`，避免使用旧 `ActionBar`。
6. 颜色、字体、形状通过 `colors.xml`、`type.xml`、`shapes.xml` 统一管理。

### 7.2 配色建议

采用 Material 3 动态配色体系。主色板建议：

| 角色         | 颜色   | HEX       | 说明                     |
| ------------ | ------ | --------- | ------------------------ |
| Primary      | 深海蓝 | `#1E6091` | 主色，按钮、关键文字     |
| OnPrimary    | 白     | `#FFFFFF` | 主色上的内容             |
| Secondary    | 暖金   | `#F2B134` | 强调色，用于温度数字高亮 |
| Surface      | 浅雾白 | `#F6F8FB` | 卡片与页面底色           |
| OnSurface    | 石墨黑 | `#1C1C1E` | 正文                     |
| Outline      | 浅灰   | `#D0D5DD` | 分割线、边框             |
| 天气氛围渐变 | 多组   | —         | 晴/阴/雨/雪各一组        |

暗色模式建议自动跟随系统，使用 `values-night` 覆盖。

### 7.2.1 深色模式适配要求

1. 项目必须正式适配深色模式，不作为可选优化项处理，应用主题统一基于 `Theme.Material3.DayNight`。
2. 所有核心页面必须同时提供浅色与深色两套可用视觉方案，至少覆盖 `LoginActivity`、`RegisterActivity`、`ResetPasswordActivity`、`MainActivity`、`AddCityActivity`、`SettingsActivity`、`DetailsOfTodayActivity`。
3. 颜色定义不得在布局文件中硬编码，统一收敛到 `colors.xml` 与 `values-night/colors.xml`，保证主题切换时页面可整体联动。
4. 天气主界面的渐变背景、卡片背景、文字颜色、图标颜色、分割线颜色在深色模式下必须重新映射，不能直接沿用浅色配色。
5. 深色模式下页面仍应保持天气应用的层次感与氛围感，禁止使用纯黑底配高饱和纯白字的粗糙方案，优先使用深灰蓝、深石墨、低饱和强调色构建界面。
6. 温度主数字、天气状态、最高最低温、未来预报卡片、空气质量等关键信息在深色模式下必须保证可读性，对比度应明显高于背景层。
7. 输入框、按钮、卡片、工具栏、搜索框、底部弹层等 Material 3 组件都要验证深色模式下的背景、描边、文字和点击态是否正确。
8. 若根据天气状态切换背景主题，深色模式下也必须提供对应的夜间版天气背景，例如晴天夜间、雨天夜间、多云夜间等视觉变体。
9. 深色模式切换策略建议默认跟随系统，同时在文档与实现中预留后续“浅色 / 深色 / 跟随系统”三态主题切换扩展位。
10. 交付前应完成一次专项验收，重点检查页面切换、下拉刷新、城市切换、对话框弹出、横竖屏恢复等场景下深色模式是否出现闪白、文字丢失或背景冲突。

### 7.3 字体层级建议

| 层级            | 字号 | 字重    | 使用场景         |
| --------------- | ---- | ------- | ---------------- |
| Display Large   | 96sp | Light   | 主页超大温度数字 |
| Headline Medium | 28sp | Medium  | 页面大标题       |
| Title Large     | 22sp | Medium  | 卡片标题         |
| Body Large      | 16sp | Regular | 正文             |
| Body Medium     | 14sp | Regular | 次要信息         |
| Label Small     | 12sp | Regular | 辅助说明、时间戳 |

### 7.4 卡片样式建议

1. 圆角统一 20dp（核心卡片）或 16dp（次级卡片）。
2. 阴影柔和，`cardElevation` 不超过 4dp。
3. 卡片内部 Padding 建议 20dp。
4. 卡片之间间距 16dp。
5. 主天气卡片可去除阴影，使用渐变背景替代视觉层级。

### 7.5 间距规范建议

1. 页面左右边距 20dp。
2. 同级内容之间 16dp。
3. 跨区块间 24dp。
4. 卡片内部上下 Padding 20dp，左右 20dp。

### 7.6 圆角与阴影建议

1. 按钮圆角 16dp（Filled）或 12dp（Outlined）。
2. 输入框圆角 12dp。
3. 卡片圆角 20dp。
4. 页面整体避免大量硬阴影，优先使用 Tonal Surface 区分层级。

### 7.7 主界面视觉设计重点

1. **温度数字作为唯一视觉锚点**：占据主内容视觉中心，字号最大，字重最轻，形成对比。
2. **主页面 + 侧边栏一体化设计**：侧边栏不是独立风格页面，而是主页的延展层，颜色、圆角、分隔和图标语言保持一致。
3. **渐变背景与天气联动**：晴天暖色系、雨天冷色系、夜间深蓝，**这是本项目最大的视觉差异点**。
4. **信息留白**：主卡片、侧边栏头部、城市列表项之间保持统一间距，避免信息堆叠。
5. **图标克制**：天气图标、设置按钮、侧边栏入口按钮统一使用单色矢量风格，避免彩色卡通图标破坏高级感。
6. **动效轻微**：下拉刷新、侧边栏展开、城市切换使用 Material Motion 默认过渡即可，不额外开发复杂动画。

### 7.8 轻量炫技设计建议

1. 主页背景渐变根据天气字段切换（代码级实现，低成本高视觉收益）。
2. 温度数字入场时使用 `ValueAnimator` 从 0 滚动到目标值（300ms）。
3. 卡片进入使用 `LayoutAnimation` 轻微上浮淡入。
4. `SwipeRefreshLayout` 配色与主色一致。
5. 夜间模式下主页渐变自动切换为深色系。
6. 侧边栏展开时可增加轻微蒙层与位移动画，但时长应控制在 200ms 左右，保持利落。
7. 侧边栏悬浮添加城市按钮采用与主色一致的强调色，兼顾可见性与整体统一。

### 7.9 如何兼顾简洁、美观与可实现性

1. 所有视觉效果均可用系统原生能力实现，不引入额外动画库。
2. 渐变、圆角、阴影均通过 XML Drawable 或主题配置完成。
3. 不追求复杂粒子/天气动画，避免性能与开发成本失衡。
4. 聚焦"温度 + 渐变 + 卡片"三要素，其他内容做减法。

### 7.10 深色模式落地建议

1. 资源层面采用 `values` 与 `values-night` 成对维护颜色、主题和部分 drawable 引用，减少运行时判断复杂度。
2. 背景图或渐变资源建议抽象为按天气类型命名的资源集合，浅色与深色各维护一套，便于主界面按天气状态切换。
3. 需要重点验证的页面元素包括：主页顶部温度区、未来 4 天预报卡片、搜索结果列表、设置页账号操作区、详情页数据卡片、登录注册输入区。
4. 若时间有限，优先保证“文字可读、卡片分层明确、交互反馈可见”，再优化夜间氛围背景与动效细节。
5. 深色模式适配应纳入最终交付标准，不能仅在扩展方向中提及。

---

## 第8章 项目开发实施方案

项目开发采用"先跑通最小闭环 → 再完善业务 → 最后接入真实 API"的三阶段节奏。

### 阶段一：项目骨架与数据库（预计 2 天）

| 项           | 内容                                                         |
| ------------ | ------------------------------------------------------------ |
| **开发目标** | 搭建项目结构、完成数据库初始化、跑通用户登录注册             |
| **涉及模块** | 项目配置、`DBHelper`、所有 DAO、`UserRepository`、`LoginActivity`、`RegisterActivity`、`ResetPasswordActivity` |
| **输入内容** | 技术栈清单、数据库表结构、页面原型                           |
| **输出内容** | 可注册登录的应用、数据库建表成功、登录态正常保持             |
| **完成标准** | 应用能注册用户、登录成功进入空白主页、重置密码流程通         |

**关键工作**：
1. 创建项目，配置 `build.gradle` 引入 `com.google.android.material:material`、`androidx.recyclerview`、`androidx.swiperefreshlayout`、OkHttp。
2. 编写 `DBHelper`，`onCreate` 中创建 4 张表。
3. 编写各 DAO，完成 `users` 与 `cities` 表的增删改查。
4. 完成 3 个用户类页面与登录态管理。

### 阶段二：主界面与城市管理（预计 3 天）

| 项           | 内容                                                         |
| ------------ | ------------------------------------------------------------ |
| **开发目标** | 完成 `MainActivity` 视觉骨架、`AddCityActivity`、`DetailsOfTodayActivity`，使用假数据跑通全流程 |
| **涉及模块** | 4 个 Activity、`ForecastAdapter`、`CitySearchAdapter`、`SavedCityAdapter`、`CityRepository` |
| **输入内容** | UI 设计规范、本地城市词典（`assets/cities.json`）            |
| **输出内容** | 页面 UI 完整，城市添加切换可用，详情页布局完成               |
| **完成标准** | 所有页面可互相跳转，城市数据在 SQLite 正常读写，主页展示 Mock 天气数据 |

**关键工作**：
1. 编写主页布局，落实渐变背景、卡片、预报列表。
2. 完成主页侧边栏布局，接入用户信息概要、设置按钮、可滚动城市列表与悬浮添加城市按钮。
3. 完成 `SettingsActivity`，补充退出登录与基础设置项。
4. 编写 Mock 数据方法，在 Repository 中返回假 `CurrentWeather` / `ForecastWeather`。
5. 完成城市添加/切换逻辑。
6. 完成详情页布局与假数据展示。

### 阶段三：API 接入与联调（预计 2 天）

| 项           | 内容                                                         |
| ------------ | ------------------------------------------------------------ |
| **开发目标** | 接入真实天气 API，完成缓存策略落地                           |
| **涉及模块** | `ApiConfig`、`HttpClient`、`WeatherApiService`、Parser、`WeatherRepository`、两张缓存表 DAO |
| **输入内容** | API Key、Base URL、Path、接口文档                            |
| **输出内容** | 真实天气数据上线，缓存兜底生效                               |
| **完成标准** | 有网请求实时数据，无网读取缓存，下拉刷新正常                 |

**关键工作**：
1. 在 `ApiConfig` 填入真实 API 配置。
2. 根据真实接口 JSON 结构调整 Parser。
3. 实现 Repository 的"API 优先 + 缓存兜底"逻辑。
4. 替换阶段二的 Mock 数据。

### 阶段四：UI 打磨与收尾（预计 1 天）

| 项           | 内容                                           |
| ------------ | ---------------------------------------------- |
| **开发目标** | 视觉打磨、异常处理、空态处理、应用图标与启动页 |
| **涉及模块** | 所有页面、资源文件                             |
| **输入内容** | UI 走查清单                                    |
| **输出内容** | 可交付版本                                     |
| **完成标准** | 视觉符合规范、无明显 bug、断网流畅             |

### 阶段五：测试、答辩准备（预计 1 天）

1. 全流程走查并修复遗留 bug。
2. 准备答辩讲稿与截图素材。
3. 整理实验报告。

**总工期预估**：9 个工作日，可压缩至 6 天（单人集中投入）。

---

## 第9章 测试、联调与交付建议

### 9.1 本地功能测试建议

| 测试场景         | 预期结果                                |
| ---------------- | --------------------------------------- |
| 注册新用户       | `users` 表新增一条记录，密码为哈希值    |
| 重复用户名注册   | 提示"用户名已存在"                      |
| 登录正确账号     | 进入 `MainActivity`                     |
| 登录错误密码     | Snackbar 提示错误                       |
| 重置密码身份错误 | 提示"信息不匹配"                        |
| 添加城市         | `cities` 表新增，已添加列表出现新条目   |
| 切换城市         | 主页数据刷新，`users.current_city` 更新 |
| 删除城市         | `cities` 表记录消失                     |
| 设置页退出登录   | 清除登录态并跳转 `LoginActivity`，返回键不可回到主页 |

### 9.2 数据库测试建议

1. 使用 Android Studio 内置 `Database Inspector` 直接查看表数据。
2. 测试首次启动时 4 张表是否正确创建。
3. 测试缓存表在每次 API 成功后是否更新 `update_time`。
4. 测试卸载重装后数据库是否重建。

### 9.3 页面联调建议

1. 使用固定 Mock 数据先完成 UI 联调，避免同时调试 UI 和接口。
2. 每个页面独立测试跳转与回参。
3. 检查横竖屏切换是否导致状态丢失（可在 Activity 中保存简单状态或禁止横屏）。

### 9.4 API 联调建议

1. 先在浏览器或 Postman 中直接请求接口，确认返回结构。
2. 将真实返回 JSON 复制到代码中先做本地解析测试。
3. 确认 `API_KEY` 配额与调用频率限制。
4. 请求失败时打印完整 URL 与响应体，便于排查。
5. 注意 `AndroidManifest.xml` 添加 `android.permission.INTERNET` 与 `ACCESS_NETWORK_STATE` 权限。

### 9.5 网络异常处理建议

1. `NetworkUtils.isConnected()` 在每次请求前检查。
2. OkHttp 请求设置超时（连接 10s、读取 10s）。
3. 请求失败不崩溃，统一进入兜底分支。
4. UI 层使用 Snackbar 提示"网络异常，已加载缓存数据"。

### 9.6 常见问题与排查建议

| 问题            | 排查方向                                                |
| --------------- | ------------------------------------------------------- |
| 应用启动崩溃    | 检查 `DBHelper` 建表 SQL 是否有语法错误                 |
| 天气数据不显示  | 检查 API Key 是否正确、返回 JSON 字段是否与 Parser 匹配 |
| 切换城市不生效  | 检查 `users.current_city` 是否同步更新                  |
| 缓存未生效      | 检查 DAO `insertOrReplace` 逻辑、`adcode` 是否一致      |
| UI 不居中或错位 | 检查 `ConstraintLayout` 约束是否完整                    |
| 字体加载失败    | 确认 `res/font` 目录文件名合法                          |

### 9.7 交付前检查清单

- [ ] 4 张数据表全部建成且字段正确
- [ ] 7 个 Activity 全部完成且可互相跳转
- [ ] 登录态持久化正常
- [ ] API 真实联通，无 Mock 数据残留
- [ ] 断网兜底测试通过
- [ ] 深色模式适配完成，核心页面无闪白、重叠、低对比度或状态栏颜色异常
- [ ] 应用图标与启动页已替换
- [ ] 无明显崩溃与卡顿
- [ ] 运行环境说明（最低版本、权限）已写入 README
- [ ] 打包 APK 可安装运行

### 9.8 答辩展示重点建议

1. **先演示完整流程**：注册 → 登录 → 主页天气 → 切换城市 → 添加城市 → 详情页 → 退出登录。
2. **重点讲架构图**：展示第 3 章的分层架构，说明"Repository 统一协调 API 与 SQLite"。
3. **展示数据库实时数据**：使用 Database Inspector 现场演示表中数据变化。
4. **演示断网兜底**：开飞行模式再进入主页，仍能展示缓存数据。
5. **讲清 API 预留设计**：指出 `ApiConfig` 位置，说明"只需修改这一个文件即可切换服务商"。
6. **UI 亮点展示**：主界面渐变背景随天气变化、温度数字动效。

---

## 第10章 总结与扩展方向

### 10.1 当前方案的优势

1. **架构清晰**：五层分层职责明确，Repository 作为唯一数据出口，学生理解成本低。
2. **技术栈克制**：不引入 Kotlin、Room、复杂框架，完全落在课程教学范围内。
3. **真实可联调**：API 配置集中预留，切换服务商成本极低。
4. **SQLite 深度参与**：不只是存登录信息，还承担天气缓存兜底，项目完整度高。
5. **视觉差异化**：Material 3 + 天气氛围渐变背景，摆脱学生作业模板感。
6. **答辩友好**：每一层都有明确讲解点，演示素材丰富。

### 10.2 当前方案的局限

1. 未使用 MVVM/LiveData，UI 与数据更新依赖手动回调，代码灵活度不如现代架构。
2. 未引入 Retrofit 等自动化网络库，JSON 解析需要手动编写，工作量略大。
3. 未做后台定时刷新、推送、定位，用户需手动切换城市。
4. 登录体系仅本地存储，未做云端同步。
5. 数据库未做迁移策略，若字段变更需卸载重装。

### 10.3 为什么适合作为课程项目

1. 知识点全覆盖：Activity、Intent、SQLite、SharedPreferences、RecyclerView、OkHttp、JSON、Material Design。
2. 工作量可控：6 个页面、4 张表、2~3 个接口，单人在两周内可完成。
3. 亮点突出：Repository 双数据源、Material 3 视觉、断网兜底。
4. 可延展性强：答辩时若被追问"如何升级"有明确答案。

### 10.4 后续可扩展方向

1. **定位能力**：接入 GPS 或 IP 定位，自动识别当前城市。
2. **推送通知**：每日早晨 7 点推送当天天气。
3. **桌面 Widget**：Android AppWidget 展示当前温度。
4. **多时段预报**：接入逐小时预报，增强时间维度。
5. **生活指数**：穿衣、紫外线、空气质量等附加信息。
6. **云端账号**：后端接入 Spring Boot，实现账号云同步。
7. **架构升级**：引入 MVVM + LiveData + Retrofit + Room，重构核心层。
8. **多语言与暗色模式**：完善 i18n 与 `values-night`。

### 10.5 如何从当前版本平滑升级

1. **从手动 JSON → Retrofit**：替换 `HttpClient` 与 `WeatherApiService`，Parser 转为 Gson/Moshi 注解，Repository 接口保持不变。
2. **从 SQLiteOpenHelper → Room**：按现有表结构定义 `@Entity`，DAO 接口以 `@Dao` 改写，Repository 层无感迁移。
3. **从 Activity → Fragment + Navigation**：保留 `MainActivity` 作为容器，其余页面改为 Fragment。
4. **从本地账号 → 云端账号**：新增 `AuthApiService`，`UserRepository` 增加远程校验分支，SQLite 继续作为本地缓存。

升级路径均不破坏已有数据表与 Repository 契约，具备良好的平滑演进空间。

---

**文档结束**

> 本文档为《Android 天气应用》期末大作业最终版开发文档。开发者按第 8 章阶段顺序推进，在阶段三填入真实 API 配置后即可进入联调。答辩与实验报告可直接引用第 1、3、5、7、10 章内容作为核心材料。
