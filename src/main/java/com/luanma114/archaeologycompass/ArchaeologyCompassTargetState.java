package com.luanma114.archaeologycompass;

// Java：按玩家 UUID 保存目标，避免直接持有会在玩家离线后失效的 ServerPlayer 引用。
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * 考古罗盘的服务端目标缓存。
 *
 * <p>每个拥有罗盘且扫描到目标的玩家对应一个最近考古目标。扫描事件通过此缓存判断目标是否变化，
 * 再决定是否向客户端同步；客户端渲染读取同步后的本地状态。本类不扫描世界，也不引用客户端代码。</p>
 *
 * <p>缓存只存在于服务器进程内，不写入玩家存档。退出时清理，重新登录时由登录事件重新扫描建立。</p>
 */
public final class ArchaeologyCompassTargetState {
    /** 以玩家 UUID 为键保存上次扫描选出的目标；无目标时移除键，不使用 null 作为 Map 值。 */
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
     * <p>写入本次目标并比较旧值，由扫描事件根据返回值决定是否发送 S2C 同步包，
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
     * <p>扫描事件在物品栏中没有罗盘或未发现候选方块时调用本方法，返回 {@code true} 时同步
     * “无目标”状态，使客户端指针旋转。退出和重生事件也用它清理缓存，但不由本方法发送网络包。</p>
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
