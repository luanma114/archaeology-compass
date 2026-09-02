package com.example.examplemod;

// NeoForge：仅在客户端加载此类；IPayloadContext 是网络包处理上下文。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端专属目标状态与后续指针渲染入口。
 *
 * <p>独立服务器不会加载本类。服务端通过 {@link ArchaeologyCompassTargetPayload} 发送的目标只写入
 * 当前客户端内存；客户端不得反向修改服务端扫描结果。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public final class ExampleModClient {
    /** 当前客户端收到的最近考古目标；{@code null} 表示无目标。 */
    private static ExampleMod.Target target;

    /**
     * 处理服务端发送的目标状态包。
     *
     * <p>NeoForge 默认在主游戏线程调用此处理器，因此可直接替换客户端缓存。后续物品模型属性读取
     * {@link #getTarget()}：有目标时计算指针角度，无目标时返回连续变化角度。</p>
     */
    public static void handleTargetPayload(ArchaeologyCompassTargetPayload payload, IPayloadContext context) {
        target = payload.target();
    }

    /** 提供给后续模型属性读取的当前目标。 */
    public static ExampleMod.Target getTarget() {
        return target;
    }

    /** 工具类不允许实例化。 */
    private ExampleModClient() {
    }
}
