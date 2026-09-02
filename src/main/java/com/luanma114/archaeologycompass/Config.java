package com.luanma114.archaeologycompass;

// NeoForge：声明配置项、默认值和允许范围，并在服务器读取配置文件时自动校验。
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 考古罗盘服务端配置定义。
 *
 * <p>所有值由服务端加载和控制。单人世界中集成服务端同样使用此配置，联机时客户端不应将其作为权威扫描规则。</p>
 */
public final class Config {
    /** 用于构建并约束配置项的 NeoForge builder。 */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 玩家 X/Z 平面上的扫描半径，单位为方块。限制上限避免过大扫描区域。 */
    public static final ModConfigSpec.IntValue HORIZONTAL_RADIUS = BUILDER
            .comment("Horizontal scan radius in blocks.")
            .defineInRange("horizontalRadius", 64, 1, 128);

    /** 玩家上下方向的扫描半径，单位为方块。 */
    public static final ModConfigSpec.IntValue VERTICAL_RADIUS = BUILDER
            .comment("Vertical scan radius in blocks.")
            .defineInRange("verticalRadius", 32, 1, 64);

    /** 两次完整扫描之间的最小 Tick 间隔。20 Tick 通常约等于一秒。 */
    public static final ModConfigSpec.IntValue SCAN_INTERVAL_TICKS = BUILDER
            .comment("Ticks between full scans.")
            .defineInRange("scanIntervalTicks", 20, 1, 1200);

    /** 注册给 NeoForge 的完整服务端配置规范。 */
    public static final ModConfigSpec SPEC = BUILDER.build();

    /** 工具类不允许实例化。 */
    private Config() {
    }
}
