package com.luanma114.archaeologycompass;

// NeoForge：IPayloadContext 是网络包处理上下文。
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 通用目标状态接收器。
 *
 * <p>此类不引用任何 Minecraft 客户端、模型或渲染 API，因此可以被双端网络注册器安全调用。
 * 后续纯客户端渲染类只读取 {@link #getTarget()}，不要反向让网络类引用渲染类。</p>
 */
public final class ArchaeologyCompassClientState {
    /**
     * 当前连接收到的最近考古目标；{@code null} 表示无目标。
     *
     * <p>网络包处理器可能在非渲染线程运行，而模型属性在渲染线程读取，因此用 {@code volatile}
     * 保证可见性。{@link ExampleMod.Target} 是不可变记录，读取线程只会拿到完整的快照。</p>
     */
    private static volatile ExampleMod.Target target;

    /** 将服务端 S2C 包中的目标写入本地状态。 */
    public static void handleTargetPayload(ArchaeologyCompassTargetPayload payload, IPayloadContext context) {
        target = payload.target();
    }

    /** 提供给客户端指针模型属性读取的当前目标。 */
    public static ExampleMod.Target getTarget() {
        return target;
    }

    /**
     * 清空本地目标。
     *
     * <p>玩家断开连接或切换世界时由客户端事件调用，避免残留上一个存档/服务器的目标，
     * 使指针在进入新世界且尚未收到新 S2C 包前错误指向旧坐标。</p>
     */
    public static void reset() {
        target = null;
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassClientState() {
    }
}
