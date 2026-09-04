
# Create: Mass-Energy (机械动力：质能)

A Minecraft 1.21.1 / NeoForge addon for **Create** that brings the classic mass–energy conversion (`E = mc²`, here as `E = q·t²`) into your factory. Turn items into energy, energy into data, and beam that data across the world with radio telegraphs — or just listen to them hum.

---

## English

**Create: Mass-Energy** is a content mod that adds three workstations for converting between **items, energy and data**, plus a line of **upgrade modules** to scale them up. It is designed to work alongside [Create](https://modrinth.com/mod/create) and optionally [Create Aeronautics](https://modrinth.com/mod/create-aeronautics).

### Features

- **Annihilation Furnace** — feeds on items and converts their mass into FE (Forge Energy) following the mass–energy equation `E = q·t²` (theoretical 400 FE/item, delivered at 60% after losses). Stack it with hoppers for input and energy cables for output.
- **Data Terminal** — converts physical items into abstract *data* (and back). Supports NBT-tagged items (enchanted gear, packaged items, etc.), each extra NBT tag costs 4 KB of storage. A built-in read-only viewer shows exactly what is stored.
- **Radio Telegraph** — wirelessly transmits *data* between terminals across any distance. Name a station, pair receivers in a priority list, and let it beam data around the world.
- **Upgrade modules**:
  - *Thread upgrades* (Dual/Quad/Octo/16-core/32-core) — raise processing & transmission speed, scaling throughput for a single item type when threads are available.
  - *Storage upgrades* (floppy disks, 16–512 GB hard disks) — expand data-terminal capacity.
- **Ambient sound design** — the data terminal hums while actually reading/writing and “spins down” when idle; the radio telegraph beeps on send/receive, and has **festive idle tunes** that play on real-world holidays (with hidden achievements!).
- **Create & Aeronautics friendly** — machines are movable contraption blocks and work on Create Aeronautics physics structures (soft dependency, optional).
- **18+ advancements**, including hidden holiday easter eggs.

### Workstations

| Machine | Converts | Notes |
|---|---|---|
| Annihilation Furnace | items → FE | 20 items/s/thread base, expandable |
| Data Terminal | items ↔ data | 64 MB base storage, NBT-aware |
| Radio Telegraph | data (wireless) | needs a name; pairs receivers |

### Installation / Building

Requires **Minecraft 1.21.1** and **NeoForge 21.1.x**. Place the built jar into your `mods` folder.

To build from source:

```bash
./gradlew build
# output: build/libs/createmassenergy-<version>.jar
```

Create is a **required** dependency; Create Aeronautics / Sable is optional (used for physics-structure compatibility when present).

### License

Licensed under the **GNU General Public License v3.0 (or later)**. See [LICENSE](LICENSE).

---

## 中文

**机械动力：质能** 是一个为 [机械动力（Create）](https://modrinth.com/mod/create) 打造的 1.21.1 / NeoForge 附属模组，把经典的质能转换（`E = mc²`，本模组中为 `E = q·t²`）带入你的工厂——把物品变成能量，把能量变成数据，再用收发报机把数据发射到全世界。

### 特性

- **物品湮灭炉** —— 吞噬物品并按质能方程 `E = q·t²` 将其质量转化为 FE 能量（理论 400 FE/物品，经损耗后按 60% 产出）。上方漏斗供料、四周接能量线缆即可持续发电。
- **数据化终端** —— 在实体物品与抽象的“数据”之间相互转换（分解/还原）。完整支持带 NBT 的物品（附魔装备、纸包裹等），每多一条 NBT 标签额外占用 4KB 存储。内置只读“存储查看器”可查看当前存了什么。
- **收发报机** —— 在不同终端之间**无线**传输数据，无视距离。命名电台、在优先级列表里配对接收机，即可把数据发射到世界各地。
- **升级模块**：
  - *线程升级*（双核/4核/8核/16核/32核）—— 提高处理与传输速度；线程富余时可对单一物品种类线性加速。
  - *存储升级*（软盘、16–512GB 硬盘）—— 扩展数据化终端的存储容量。
- **环境音效设计** —— 数据化终端仅在真正读写时发出硬盘声、空闲时“停转”；收发报机收发时发出电报声，并在现实节日播放**节日特殊待机音乐**（还藏着彩蛋成就！）。
- **兼容机械动力与航空学** —— 机器是可被搬上移动结构的方块，并能在机械动力航空学的物理结构上工作（软依赖，可选）。
- **18+ 项成就**，含隐藏节日彩蛋。

### 工作站

| 机器 | 转换 | 说明 |
|---|---|---|
| 物品湮灭炉 | 物品 → FE | 基础 20 物品/秒/线程，可升级扩展 |
| 数据化终端 | 物品 ↔ 数据 | 基础 64MB 存储，支持带 NBT 物品 |
| 收发报机 | 数据（无线） | 需命名；可配对多个接收机 |

### 安装 / 构建

需要 **Minecraft 1.21.1** 与 **NeoForge 21.1.x**。将构建出的 jar 放入 `mods` 文件夹即可。

从源码构建：

```bash
./gradlew build
# 产物: build/libs/createmassenergy-<version>.jar
```

机械动力（Create）为**必需**依赖；机械动力航空学（Create Aeronautics / Sable）为可选依赖（存在时启用物理结构兼容）。

### 许可证

以 **GNU 通用公共许可证 v3.0（或更高版本）** 授权。详见 [LICENSE](LICENSE)。

---

*Built with the [NeoForge MDK](https://github.com/NeoForged/MDK). Powered by [Create](https://modrinth.com/mod/create).*

