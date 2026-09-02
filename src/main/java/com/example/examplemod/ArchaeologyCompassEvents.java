package com.example.examplemod;

// Minecraft：方块坐标、服务端玩家和世界读取 API。
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
// NeoForge：自动订阅事件，并在每个玩家 Tick 后触发扫描入口。
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 考古罗盘服务端扫描原型。
 *
 * <p>监听玩家 Tick，仅在玩家持有罗盘且达到配置扫描周期时搜索已加载区块。
 * 方法返回最近候选目标，但当前版本尚未缓存结果、同步客户端或驱动物品指针。</p>
 *
 * <p><strong>性能限制：</strong>当前实现遍历范围内已加载方块，适合验证标签和定位规则，
 * 不适合直接作为发布版扫描器。后续应改为分帧扫描或区块目标索引。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public final class ArchaeologyCompassEvents {
    /**
     * 在玩家 Tick 后阶段执行服务端扫描。
     *
     * <p>客户端玩家不会是 {@link ServerPlayer}，因此会直接返回。扫描频率由服务端配置控制，
     * 并且仅在主手或副手持有考古罗盘时运行。</p>
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % Config.SCAN_INTERVAL_TICKS.get() != 0) {
            return;
        }

        if (!player.getMainHandItem().is(ExampleMod.ARCHAEOLOGY_COMPASS.get())
                && !player.getOffhandItem().is(ExampleMod.ARCHAEOLOGY_COMPASS.get())) {
            // 玩家未持有罗盘时清除旧目标，并通知客户端停止指向已失效位置。
            if (ArchaeologyCompassTargetState.clear(player.getUUID())) {
                ArchaeologyCompassNetwork.sendTarget(player, null);
            }
            return;
        }

        ExampleMod.Target target = findNearestTarget(player);
        if (target == null) {
            // 范围内没有候选方块。仅在之前有目标时发送一次“无目标”状态，避免重复网络包。
            if (ArchaeologyCompassTargetState.clear(player.getUUID())) {
                ArchaeologyCompassNetwork.sendTarget(player, null);
            }
            return;
        }

        // 目标坐标或维度变化时才同步；同一目标的后续扫描不发送重复包。
        if (ArchaeologyCompassTargetState.set(player.getUUID(), target)) {
            ArchaeologyCompassNetwork.sendTarget(player, target);
        }
    }

    /**
     * 搜索玩家周围已加载区块内距离最近的候选方块。
     *
     * <p>只读取 {@link ExampleMod#ARCHAEOLOGY_TARGETS} 方块标签。当前尚未检查可疑沙子/可疑沙砾
     * 是否已被刷空；该判定需要在后续通过其方块实体数据实现。</p>
     *
     * <p>{@link Level#hasChunkAt(BlockPos)} 防止扫描逻辑触发区块加载。平方距离用于比较，
     * 可避免每次候选比较时计算平方根。</p>
     *
     * @return 最近候选目标；范围内没有候选方块时返回 {@code null}
     */
    private static ExampleMod.Target findNearestTarget(ServerPlayer player) {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        int horizontalRadius = Config.HORIZONTAL_RADIUS.get();
        int verticalRadius = Config.VERTICAL_RADIUS.get();

        // 可变坐标避免在内层循环为每个方块分配新的 BlockPos。
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = center.getX() - horizontalRadius; x <= center.getX() + horizontalRadius; x++) {
            for (int z = center.getZ() - horizontalRadius; z <= center.getZ() + horizontalRadius; z++) {
                // 未加载区块不参与搜索，也绝不能因为罗盘扫描而被强制加载。
                if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) {
                    continue;
                }

                int minY = Math.max(level.getMinBuildHeight(), center.getY() - verticalRadius);
                int maxY = Math.min(level.getMaxBuildHeight() - 1, center.getY() + verticalRadius);
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(ExampleMod.ARCHAEOLOGY_TARGETS)) {
                        continue;
                    }

                    double distance = cursor.distToCenterSqr(player.position());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        // 复制可变坐标；后续循环会继续改变 cursor。
                        nearest = cursor.immutable();
                    }
                }
            }
        }

        return nearest == null ? null : new ExampleMod.Target(level.dimension(), nearest);
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassEvents() {
    }
}
