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
    /** 当前连接收到的最近考古目标；{@code null} 表示无目标。 */
    private static ExampleMod.Target target;

    /** 将服务端 S2C 包中的目标写入本地状态。 */
    public static void handleTargetPayload(ArchaeologyCompassTargetPayload payload, IPayloadContext context) {
        target = payload.target();
    }

    /** 提供给后续客户端模型属性读取的当前目标。 */
    public static ExampleMod.Target getTarget() {
        return target;
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassClientState() {
    }
}
