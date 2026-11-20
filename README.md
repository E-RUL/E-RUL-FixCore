# E-RUL 的私货修复核心（E-RUL FixCore）

GTNH 2.9.0 私货覆盖包的**唯一补丁入口**。原来散落的三个附加包（`fissioncompat` 独立 jar、MessTech 元数据补丁、log4j `ThrowableProxy` 补丁）在这里统一成一个工程、一个 jar、一套构建脚本。

- modid：`erulfixcore`
- 当前版本：见 `version.txt`（唯一版本源）
- 目标环境：GT New Horizons 2.9.0 / MC 1.7.10 / lwjgl3ify / RFM + unimixins

---

## 2.4.0 最新说明

新增 B 专用 `tenkei-ae2-redirect`（ENV），入口 `tools/patch-tenkei-ae2.ps1`。TenkeiTech 与 Apeiron 抢 AE2 CraftingCPUCluster 的 pushPattern 调用点，前者显式 require=1 不受 defaultRequire=0 影响。修复仅将该注解改 0，保留 Apeiron 和 eco；Tenkei 该超级接口派发路径会兼容降级。真实 Mixin 单目标转换对照通过，游戏内复测仍待用户确认。B 已装 2.4.0，A 实例未升级。仅更新核心 jar 不等于应用 ENV 补丁。

V1.1 包和 Git 均按用户要求暂停。详情见 `../接续/10_ME控制器崩溃修复.md`。

## 1. 为什么不是一个纯 jar

补丁分两种生命周期，混在一个 jar 里必然有人踩空：

| 类型 | 什么时候加载 | 能放进 mod jar 吗 | 本工程的处理 |
|---|---|---|---|
| `JAR` | FML 构造 mod 时 | ✅ 用 mixin | 编进 `dist/E-RUL-FixCore-*.jar` |
| `ENV` | 早于 FML（log4j 由 RFB 引导阶段实例化；jar 元数据在加载 mod 前就要读） | ❌ 做不到 | `patches/` 存素材，`tools/apply-env-patches.ps1` 落地 |

所以「统合」= **一个 jar + 一个脚本 + 一份清单**，而不是把所有东西硬塞进 jar。

## 2. 目录结构

```
E-RUL-FixCore/
├── version.txt                  版本唯一源（改这里，构建时自动生成 Version.java 并写进 mcmod.info）
├── build.ps1                    一键构建：javac(JDK21, --release 8) -> 7z 打包 -> sha256
├── src/
│   ├── main/java/com/erul/fixcore/
│   │   ├── FixCoreMod.java      @Mod 入口，只负责在 preInit 打印模块表
│   │   ├── FixCorePlugin.java   FML core plugin 壳（让 jar 能带 mixin 配置）
│   │   ├── FixModule.java       补丁契约接口
│   │   ├── FixKind.java         JAR / ENV
│   │   ├── FixRegistry.java     所有补丁的唯一注册表
│   │   ├── Probe.java           带缓存的运行时探测（类是否存在 / mod 是否加载）
│   │   ├── module/              每个补丁一个描述类
│   │   └── mixin/               真正的字节码补丁
│   ├── main/resources/          mcmod.info / mixins.erulfixcore.json / MANIFEST.MF
│   ├── main/stubs/              编译期占位（不打包，例如 net.minecraft.util.EnumChatFormatting）
│   └── generated/               build.ps1 生成，不要手改
├── patches/                     ENV 补丁素材
│   ├── log4j/ThrowableProxy.java
│   └── messtech/mcmod.info
├── tools/
│   ├── install.ps1              装 jar 到实例 + 退役旧 fissioncompat
│   ├── apply-env-patches.ps1    应用/跳过 ENV 补丁（幂等，带 .orig-backup）
│   └── verify.ps1               一张表看清四项安装状态
├── legacy/                      被统合掉的旧补丁 jar（install.ps1 自动搬入）
└── dist/                        构建产物
```

## 3. 当前补丁清单

| id | 类型 | 修什么 | 状态判定 |
|---|---|---|---|
| `gtnl-apecore` | JAR | GTNotLeisure 0.2.7-rc2.1 删掉了 `utils.enums.ModList` 与 `config.MainConfig`，Apeiron alpha0.0.8 仍在 `GtnlRecipeChances.apply` 里引用 → 任何 GTNL 蒸汽多方块一算配方就 `NoClassDefFoundError` 崩服 | `apeiron` + `sciencenotleisure` 都在，且两个类缺失 → ACTIVE |
| `gt-tooltip-api` | JAR | GT 5.09.54.265 删掉了 `MultiblockTooltipBuilder.toolTipFinisher(EnumChatFormatting,int,String...)`，按旧 API 编译的附属会 `NoSuchMethodError` | `gregtech` 在 → ACTIVE |
| `log4j-throwable` | ENV | log4j 2.0-beta9 的 `ThrowableProxy.loadClass` 只 catch `Exception`，RFB 抛 `Error` → 「打印错误」本身二次崩溃，把真实错误盖掉 | `apply-env-patches.ps1` 执行过 → MANUAL 消失 |
| `messtech-meta` | ENV | MessTech 的 `item.ArcaneSlate` FATAL（真正致命性由 log4j 那条解决，这条只是把 dreamcraft 依赖声明补上） | 同上 |
| `gtnr-dup-mixin` | ENV | GT-Never-Reset × CutCorners 抢同一个 int 常量（熔炉 200 / 作物架 256 / 净水厂 2400）→ 后手注入失败把 gregtech 带崩。按「CutCorners 优先」把 GTNR 三个冲突 mixin 换空 stub | `tools/patch-gtnr-dup.ps1`，幂等 + `.orig-backup` |
| `tenkei-soft-mixin` | ENV | TenkeiTech 与 Apeiron 在 `MTEEyeOfHarmony.processRecipe` 抢同一个 `@Redirect`（priority 1000），跳过后被判 critical 注入失败把 tectech 带崩。把 `mixins.tenkeitech.json` 的 `defaultRequire` 改 0，行为不变 | `tools/patch-mixin-soft.ps1` |

启动日志里能看到这张表：

```
[E-RUL FixCore] ===== E-RUL 的私货修复核心 2.0.0 (modid erulfixcore) =====
[E-RUL FixCore]   [JAR] gtnl-apecore       ACTIVE   Apeiron x GTNL rc2.1 类缺失防护
[E-RUL FixCore]   [ENV] log4j-throwable    MANUAL   log4j 堆栈渲染二次崩溃修复
[E-RUL FixCore]   -> 1 jar fix(es) active, 2 env fix(es) pending
```

`ACTIVE` = 条件成立且已生效；`SKIP` = 目标 mod 不在或类已恢复（不会白动手）；`MANUAL` = 需要跑 env 脚本。

## 3.1 分支（A / B）

同一份源码，构建时出两个 jar，**模块按分支裁剪**（`FixModule.branches()`）：

| 分支 | 对应包 | jar | 独有模块 |
|---|---|---|---|
| **A** | 流程优化版（`GT New Horizons 2.9.0`） | `E-RUL-FixCore-<ver>-A.jar` | 无（A 是基线：`gtnl-apecore` / `gt-tooltip-api` / `log4j-throwable` / `messtech-meta`） |
| **B** | 流程毁灭版（`GTNH-2.9.0-B`） | `E-RUL-FixCore-<ver>-B.jar` | `gtnr-dup-mixin`（GTNR 让位 CutCorners） |

两个 jar 的 modid 都是 `erulfixcore`，但**不能同时在同一个实例里**（名称分别是「… A 版」「… B 版」，启动表的模块数也不同）。A 版不会输出任何 B 专用模块，反之亦然。

新增 B 专用补丁的写法：模块类里 override `branches()` 返回 `new String[] { "B" }`，其余不变。

## 4. 安装

```powershell
cd E:\MC\_work\00_工程\E-RUL-FixCore
powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1                 # 一次出 A/B 两个 jar
powershell -NoProfile -ExecutionPolicy Bypass -File tools\install.ps1 -Branch A
powershell -NoProfile -ExecutionPolicy Bypass -File tools\install.ps1 -Branch B
powershell -NoProfile -ExecutionPolicy Bypass -File tools\apply-env-patches.ps1   # 共享库补丁（全局）
powershell -NoProfile -ExecutionPolicy Bypass -File tools\patch-gtnr-dup.ps1      # 仅 B 需要
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify.ps1 -Branch A
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify.ps1 -Branch B
```

`install.ps1` 会顺手把 `mods\fissioncompat-*.jar` 搬进 `legacy\`（已被核心取代），并清掉旧版本的 `E-RUL-FixCore-*.jar`。

`apply-env-patches.ps1` 幂等：每项补丁打完后把 jar 的 sha256 记进 `build\env\state.txt`，再跑一次就跳过；第一次打之前自动备份成 `*.orig-backup`。

## 5. 新增一个补丁

**A. 纯 mixin（大多数情况）**

1. `src/main/java/com/erul/fixcore/mixin/MixinXxx.java` 写补丁
2. `src/main/resources/mixins.erulfixcore.json` 的 `mixins` 数组加一行
3. `src/main/java/com/erul/fixcore/module/XxxModule.java` 描述它（id / 标题 / 必需 mod / note），并在 `FixRegistry.build()` 里 `add`

不做第 3 步也能跑，但启动表里不会有它——**约定：任何补丁都必须有 Module**，这是后期维护时唯一的查找入口。

**B. 需要落在 mods 之外（ENV）**

1. 素材放 `patches/<id>/`
2. `tools/apply-env-patches.ps1` 里加一个 `$id` 段（照抄 log4j 那段：备份 → 应用 → `Save-State`）
3. 加一个 `FixKind.ENV` 的 Module

**约定**

- 补丁必须**内在安全**：目标不在 / 类已恢复就自动跳过（用 `Probe`），不做版本号硬判断
- 补丁不得改变游戏平衡，只修「本不该崩/本不该丢」的东西
- 新补丁一律 `require = 0` 或 `required: false` 语义：宁可这条不生效，也不能让核心自己把游戏带崩

## 6. 回滚

| 想撤销 | 做法 |
|---|---|
| 某个 JAR 补丁 | 把 `mixins.erulfixcore.json` 里对应 mixin 删掉后重新 build + install（或先临时把整个核心 jar 移出 `mods\`） |
| log4j 补丁 | `log4j-core-2.0-beta9-fixed.jar.orig-backup` 改名回 `.jar`，删掉 `build\env\state.txt` 里对应行 |
| MessTech 元数据 | 同上，`MessTech-*.jar.orig-backup` |
| 整包回到统合前 | `legacy\fissioncompat-1.0.0.jar` 放回 `mods\`，删掉 `E-RUL-FixCore-*.jar` |

## 7. 边界（明确修不了 / 不该这里修的）

- **Qz-UILib 5.x 与 lwjgl3ify**：上游 1.7.10 产物里根本没有 `Lwjgl3GlApi` 这个类，不是链接问题而是缺实现，补不出来。只能用 4.11.1。
- **Apeiron 的配方概率联动**：`gtnl-apecore` 是「降级成原版行为」，不是「恢复功能」——恢复要等 Apeiron 适配 GTNL 新 API。
- **MessTech 的 `ArcaneSlate` 判定**：那是 MessTech 自己的注册时序问题，核心只保证它不再致命，不替它修逻辑。

## 8. 构建依赖（都是本地现成的，不联网）

| 依赖 | 来源 |
|---|---|
| javac 21 | `E:\MC\jdk21\jdk-21.0.13+9` |
| Forge universal | `.minecraft\libraries\net\minecraftforge\forge\...` |
| unimixins（mixin API） | 实例 `mods\+unimixins-all-*.jar` |
| GregTech | 实例 `mods\gregtech-*.jar` |
| log4j-api | `.minecraft\libraries\org\apache\logging\log4j\log4j-api\...` |

全部用 glob 定位，整合包升级换版本号不用改脚本。编译带 `-proc:none`：unimixins 自带的 Mixin 注解处理器会强制在编译期解析 `@Mixin(targets=...)`，对可选模组做不到。
