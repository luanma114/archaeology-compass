package com.luanma114.archaeologycompass;

// Minecraft：服务端玩家类型。
import net.minecraft.server.level.ServerPlayer;
// NeoForge：注册 S2C 网络包、网络处理上下文，并使用分发器向指定玩家发送包。
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 考古罗盘网络包注册与发送入口。
 *
 * <p>网络层只引用 {@link ArchaeologyCompassClientState}，其没有客户端专属依赖。后续模型和渲染代码
 * 放在独立的 {@code Dist.CLIENT} 类中，并只读取该状态，避免独立服务端加载客户端类。</p>
 */
public final class ArchaeologyCompassNetwork {
    /** 当前考古罗盘目标同步协议版本。修改包字段顺序或类型时必须递增。 */
    private static final String PROTOCOL_VERSION = "1";

    /** 注册只允许服务端发送给客户端的目标状态包。 */
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                ArchaeologyCompassTargetPayload.TYPE,
                ArchaeologyCompassTargetPayload.STREAM_CODEC,
                ArchaeologyCompassNetwork::handleTargetPayload
        );
    }

    /**
     * 通用 S2C 包处理入口。处理器只写入无客户端依赖的目标状态容器。
     */
    private static void handleTargetPayload(ArchaeologyCompassTargetPayload payload, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            ArchaeologyCompassClientState.handleTargetPayload(payload, context);
        }
    }

    /**
     * 把当前目标状态同步给一个玩家。调用方必须先确认状态实际发生变化。
     *
     * @param player 接收目标的服务端玩家
     * @param target 新目标；{@code null} 表示客户端应进入无目标旋转状态
     */
    public static void sendTarget(ServerPlayer player, ExampleMod.Target target) {
        PacketDistributor.sendToPlayer(player, new ArchaeologyCompassTargetPayload(target));
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassNetwork() {
    }
}
