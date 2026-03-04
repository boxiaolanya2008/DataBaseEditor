# 数据库编辑器 (Database Editor)

一款功能强大的 Android SQLite 数据库管理工具，采用现代 Kotlin 和 Jetpack Compose 技术栈开发。

[![Android](https://img.shields.io/badge/Android-12%2B-green?logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-blue?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.09.00-orange)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## 📋 功能特性

### 🔍 数据库访问
- **多方式打开数据库**
  - 从本地文件系统选择数据库文件
  - 浏览应用自身数据库目录
  - Root 模式下访问其他应用的数据库

### 📊 数据管理
- **分页浏览表数据**（每页 50 行）
- **完整的 CRUD 操作**
  - 添加、编辑、删除记录
  - 实时数据更新
- **智能搜索功能**
  - 全表搜索
  - 指定列搜索
  - 实时过滤
- **操作历史记录**（最多 50 条撤销操作）

### 💻 SQL 编辑器
- **自定义 SQL 查询**
- 执行 SELECT/UPDATE/INSERT/DELETE 语句
- **实时查看查询结果**
- 语法高亮支持

### 🛠️ 数据库工具
- **完整数据库架构查看**
- **数据库文件导出**
- **实时数据刷新**
- **表结构分析**

## 🚀 技术架构

### 核心技术栈
- **语言**: Kotlin 2.3.10
- **UI框架**: Jetpack Compose 2024.09.00
- **数据库**: AndroidX SQLite 2.4.0
- **异步处理**: Kotlin Coroutines 1.8.1
- **架构模式**: MVVM + Clean Architecture

### 项目结构
```
app/src/main/java/cn/database/editor/
├── data/           # 数据层
│   ├── model/      # 数据模型
│   ├── repository/ # 数据仓库
│   └── service/    # 业务服务
├── ui/             # 界面层
│   ├── component/  # UI组件
│   ├── navigation/ # 导航管理
│   ├── screen/     # 界面屏幕
│   ├── theme/      # 主题样式
│   └── viewmodel/  # ViewModel
└── util/           # 工具类
```

## 📦 系统要求

- **最低版本**: Android 12 (API 31)
- **推荐版本**: Android 13+
- **编译版本**: Android 13 (API 36)

## 🔧 安装与构建

### 环境要求
- Android Studio Giraffe 或更高版本
- Android SDK 36
- Java 21

### 构建步骤
1. 克隆项目
```bash
git clone <repository-url>
cd DatabaseEditor
```

2. 同步 Gradle 依赖
```bash
./gradlew build
```

3. 构建 APK
```bash
./gradlew assembleDebug
```

4. 安装到设备
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 使用指南

### 打开数据库
1. **本地文件**: 点击浮动按钮选择本地数据库文件
2. **应用数据库**: 在 Root 模式下浏览应用列表
3. **文件类型**: 支持 `.db`, `.sqlite`, `.sqlite3` 等格式

### 数据操作
1. **浏览数据**: 点击表名查看数据，支持分页导航
2. **搜索数据**: 使用搜索框进行实时数据过滤
3. **编辑数据**: 长按记录进行编辑或删除操作
4. **添加数据**: 点击添加按钮插入新记录

### SQL 查询
1. 进入 SQL 编辑器界面
2. 输入 SQL 查询语句
3. 点击执行按钮查看结果
4. 支持保存常用查询

## 🏗️ 架构说明

### MVVM 架构
```
View (Compose UI) ← ViewModel ← Repository ← Service/Model
```

### 核心组件
- **DatabaseService**: 数据库操作核心服务
- **FileService**: 文件系统操作服务
- **OperationHistory**: 操作历史记录管理
- **AppNavigation**: 应用导航管理

### 数据流
1. **UI 层**: Jetpack Compose 组件
2. **ViewModel**: 状态管理和业务逻辑
3. **Repository**: 数据访问抽象层
4. **Service**: 具体业务实现

## 🔍 开发指南

### 添加新功能
1. 在 `data/model` 中定义数据模型
2. 在 `data/service` 中实现业务逻辑
3. 在 `ui/viewmodel` 中创建 ViewModel
4. 在 `ui/screen` 中实现界面
5. 在 `ui/navigation` 中添加路由

### 代码规范
- 使用 Kotlin 协程进行异步操作
- 遵循 Jetpack Compose 最佳实践
- 使用 Result 模式处理错误
- 保持代码的可测试性

## 🧪 测试

### 单元测试
```bash
./gradlew test
```

### 仪器测试
```bash
./gradlew connectedAndroidTest
```

## 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目。

### 贡献指南
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📞 支持

如果您遇到问题或有建议，请通过以下方式联系：
- 提交 [Issue](https://github.com/your-repo/issues)
- 发送邮件到: support@example.com

## 🔮 路线图

- [ ] 支持更多数据库格式
- [ ] 数据导入导出功能
- [ ] 查询历史记录
- [ ] 数据可视化图表
- [ ] 多语言支持

---

⭐ 如果这个项目对您有帮助，请给个星标支持！