package com.example.examplemod;

// Java：按玩家 UUID 保存目标，避免直接持有会在玩家离线后失效的 ServerPlayer 引用。
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * 考古罗盘的服务端目标缓存。
 *
 * <p>每个在线玩家对应一个最近考古目标。缓存是下一步网络同步和客户端指针渲染的唯一数据来源。
 * 本类只保存状态，不扫描世界，也不引用客户端代码。</p>
 *
 * <p>当前缓存仅在服务器本次运行期间存在，不写入玩家存档。玩家重新登录后由下一次扫描重新建立目标。</p>
 */
public final class ArchaeologyCompassTargetState {
    /** 以玩家 UUID 为键保存目标；值永远是有效目标，不使用 null 作为 Map 值。 */
    private static final Map<UUID, ExampleMod.Target> TARGETS = new HashMap<>();

    /**
     * 读取玩家当前锁定目标。
     *
     * @param playerId 玩家 UUID
     * @return 已缓存目标；没有目标时返回 {@code null}
     */
    @Nullable
    public static ExampleMod.Target get(UUID playerId) {
        return TARGETS.get(playerId);
    }

    /**
     * 保存本次完整扫描找到的最近目标。
     *
     * <p>仅当新旧目标不同才写入 Map。下一步网络模块将以此返回值决定是否发送 S2C 同步包，
     * 避免每个扫描周期重复发送相同坐标。</p>
     *
     * @param playerId 玩家 UUID
     * @param target 本次扫描得到的目标
     * @return 目标是否发生变化
     */
    public static boolean set(UUID playerId, ExampleMod.Target target) {
        ExampleMod.Target previous = TARGETS.put(playerId, target);
        return !target.equals(previous);
    }

    /**
     * 清除玩家目标。
     *
     * <p>用于未持有罗盘或本次扫描未发现候选方块。下一步网络模块会在本方法返回 {@code true} 时
     * 同步“无目标”状态，使客户端指针旋转。</p>
     *
     * @param playerId 玩家 UUID
     * @return 清除前是否存在目标
     */
    public static boolean clear(UUID playerId) {
        return TARGETS.remove(playerId) != null;
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassTargetState() {
    }
}
