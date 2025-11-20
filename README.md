# Agent Demo

这是一个基于Micronaut的后端项目，提供Agent任务执行和RAG知识库管理功能。

## 项目结构

```
agent_demo/
├── frontend/              # Vue.js前端项目
│   ├── src/              # Vue源代码
│   ├── package.json      # Vue项目依赖
│   └── vite.config.js    # Vite配置
├── frontend-angular/      # Angular前端项目（从Vue迁移）
│   ├── src/              # Angular源代码
│   ├── package.json      # Angular项目依赖
│   └── angular.json      # Angular配置
└── src/                   # 后端Java源代码
    └── main/
        └── java/          # Micronaut应用代码
```

## 前端项目

本项目包含两个独立的前端项目：

### Vue.js 项目 (`frontend/`)
- **框架**: Vue 3 + Vite
- **位置**: `frontend/`
- **文档**: 查看 [frontend/README.md](./frontend/README.md)

### Angular 项目 (`frontend-angular/`)
- **框架**: Angular 17 + TypeScript
- **位置**: `frontend-angular/`
- **文档**: 查看 [frontend-angular/README.md](./frontend-angular/README.md)
- **迁移说明**: 查看 [frontend-angular/MIGRATION.md](./frontend-angular/MIGRATION.md)

两个前端项目功能完全相同，页面效果保持一致。你可以选择使用Vue或Angular版本。

## 后端项目

### Micronaut 4.10.1 Documentation

- [User Guide](https://docs.micronaut.io/4.10.1/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.10.1/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.10.1/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)

## Feature maven-enforcer-plugin documentation

- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)

## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## Feature lombok documentation

- [Micronaut Project Lombok documentation](https://docs.micronaut.io/latest/guide/index.html#lombok)

- [https://projectlombok.org/features/all](https://projectlombok.org/features/all)


