package com.luanma114.archaeologycompass;

// Minecraft：方块实体、区块、服务端玩家、NBT 和世界读取 API。
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
// NeoForge：玩家登录、换维度、退出和 Tick 事件。
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 考古罗盘服务端扫描与目标失效处理。
 *
 * <p>仅搜索已加载区块的方块实体，而不是遍历范围内每一个方块。可疑沙子和可疑沙砾都具有方块实体，
 * 因此此实现将默认扫描开销从百万级方块状态读取降为邻近区块的方块实体遍历。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public final class ArchaeologyCompassEvents {
    /** 玩家物品栏中有罗盘时，按服务端配置周期更新目标。 */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % Config.SCAN_INTERVAL_TICKS.get() != 0) {
            return;
        }

        updateOrClearTarget(player);
    }

    /**
     * 根据玩家物品栏中是否拥有罗盘，立即建立或清除目标状态。
     *
     * <p>只要玩家物品栏（含主手、副手）中存在罗盘就持续扫描，罗盘不在手中时同样生效；
     * 完全失去罗盘时才清除目标。供登录、换维度和周期 Tick 共用。</p>
     */
    private static void updateOrClearTarget(ServerPlayer player) {
        if (player.getInventory().contains(stack -> stack.is(ExampleMod.ARCHAEOLOGY_COMPASS.get()))) {
            updateTarget(player);
        } else {
            clearTarget(player);
        }
    }

    /**
     * 玩家登录后立即建立目标状态。
     *
     * <p>不等待普通扫描周期，避免客户端登录或重新连接后最多等待一秒才看到正确罗盘状态。</p>
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updateOrClearTarget(player);
        }
    }

    /**
     * 玩家换维度后立即重新扫描新维度。
     *
     * <p>不复用旧维度目标；若新维度没有候选方块，立即同步无目标状态。</p>
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            updateOrClearTarget(player);
        }
    }

    /** 玩家退出时删除服务端内存缓存，保证下次登录会重新同步目标。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ArchaeologyCompassTargetState.clear(event.getEntity().getUUID());
    }

    /** 查找最近目标，并且仅在目标坐标或维度变化时发送同步包。 */
    private static void updateTarget(ServerPlayer player) {
        ExampleMod.Target target = findNearestTarget(player);
        if (target == null) {
            clearTarget(player);
            return;
        }

        if (ArchaeologyCompassTargetState.set(player.getUUID(), target)) {
            ArchaeologyCompassNetwork.sendTarget(player, target);
        }
    }

    /**
     * 清除服务端缓存；旧目标存在时向客户端发送一次无目标状态。
     */
    private static void clearTarget(ServerPlayer player) {
        if (ArchaeologyCompassTargetState.clear(player.getUUID())) {
            ArchaeologyCompassNetwork.sendTarget(player, null);
        }
    }

    /**
     * 在玩家周围已加载区块的方块实体中选择最近有效考古目标。
     *
     * <p>按区块半径遍历。{@link ServerLevel#getChunkSource()} 的 {@code getChunkNow} 不加载新区块；
     * 因此罗盘不会因搜索扩大服务端内存、磁盘 I/O 或世界生成范围。</p>
     */
    private static ExampleMod.Target findNearestTarget(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        int chunkRadius = (Config.HORIZONTAL_RADIUS.get() + 15) >> 4;
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        double maxDistanceSquared = (double) Config.HORIZONTAL_RADIUS.get() * Config.HORIZONTAL_RADIUS.get();
        double nearestDistance = Double.MAX_VALUE;
        BlockPos nearest = null;

        for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos position = blockEntity.getBlockPos();
                    if (Math.abs(position.getY() - center.getY()) > Config.VERTICAL_RADIUS.get()
                            || !isValidArchaeologyTarget(level, blockEntity)) {
                        continue;
                    }

                    double distance = position.distToCenterSqr(player.position());
                    if (distance <= maxDistanceSquared && distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = position;
                    }
                }
            }
        }

        return nearest == null ? null : new ExampleMod.Target(level.dimension(), nearest);
    }

    /**
     * 判断候选方块是否仍可考古。
     *
     * <p>原版 {@link BrushableBlockEntity} 的 NBT 键为 {@code LootTable}（首字母大写）与 {@code item}。
     * 未刷扫时含 {@code LootTable}，首次刷扫后替换为 {@code item}，物品完全刷出后二者都消失。
     * 因此任一键存在都有效；都不存在时方块已刷空，不再定位。</p>
     *
     * <p>第三方方块必须同时加入目标标签且使用原版 {@link BrushableBlockEntity}。这避免将仅名称相似、
     * 却没有考古状态的方块误判为目标。</p>
     */
    private static boolean isValidArchaeologyTarget(ServerLevel level, BlockEntity blockEntity) {
        if (!level.getBlockState(blockEntity.getBlockPos()).is(ExampleMod.ARCHAEOLOGY_TARGETS)
                || !(blockEntity instanceof BrushableBlockEntity brushable)) {
            return false;
        }

        CompoundTag data = brushable.saveWithoutMetadata(level.registryAccess());
        return data.contains("LootTable") || data.contains("item");
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassEvents() {
    }
}
