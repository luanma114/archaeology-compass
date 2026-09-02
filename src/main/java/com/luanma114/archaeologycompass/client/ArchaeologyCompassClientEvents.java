package com.luanma114.archaeologycompass.client;

// 模组入口与客户端同步状态。
import com.luanma114.archaeologycompass.ArchaeologyCompassClientState;
import com.luanma114.archaeologycompass.ExampleMod;
// NeoForge：客户端断线事件、事件订阅与客户端端标记。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * 考古罗盘的客户端游戏事件处理。
 *
 * <p>仅在物理客户端注册（{@code Dist.CLIENT}），监听与连接生命周期相关的事件，
 * 在玩家断开连接或切换世界时清空本地目标状态。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public final class ArchaeologyCompassClientEvents {

    /** 玩家登出/断开时清空目标，避免残留上一存档的目标状态。 */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ArchaeologyCompassClientState.reset();
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassClientEvents() {
    }
}
