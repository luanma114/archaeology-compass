package com.example.examplemod;

// NeoForge：在模组生命周期中注册网络包，并使用分发器向指定玩家发送 S2C 包。
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;

/**
 * 考古罗盘网络包注册与发送入口。
 *
 * <p>网络协议版本独立于模组版本。客户端与服务端协议版本不同会拒绝连接，
 * 避免两端使用不同字段格式读取同一网络包。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ArchaeologyCompassNetwork {
    /** 当前考古罗盘目标同步协议版本。修改包字段顺序或类型时必须递增。 */
    private static final String PROTOCOL_VERSION = "1";

    /**
     * 在 Mod Event Bus 注册只允许服务端发送给客户端的目标状态包。
     *
     * <p>默认在主游戏线程处理。客户端处理器仅更新内存状态，不执行耗时操作。</p>
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                ArchaeologyCompassTargetPayload.TYPE,
                ArchaeologyCompassTargetPayload.STREAM_CODEC,
                ExampleModClient::handleTargetPayload
        );
    }

    /**
     * 把当前目标状态同步给一个玩家。
     *
     * <p>调用方必须先确认状态实际发生变化，避免每次扫描重复发送相同坐标。</p>
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
