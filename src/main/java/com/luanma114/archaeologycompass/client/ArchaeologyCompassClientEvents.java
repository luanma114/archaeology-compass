package com.luanma114.archaeologycompass.client;

// 模组入口与客户端同步状态。
import com.luanma114.archaeologycompass.ArchaeologyCompassClientState;
import com.luanma114.archaeologycompass.Config;
import com.luanma114.archaeologycompass.ExampleMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
// NeoForge：客户端断线事件、事件订阅与客户端端标记。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * 考古罗盘的客户端游戏事件处理。
 *
 * <p>仅在物理客户端注册（{@code Dist.CLIENT}）。登出时清空目标和首次接收标记，
 * 防止状态跨连接残留；物品提示展示用途，并在按住 Shift 时展示扫描规则、服务端范围和搜索状态。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public final class ArchaeologyCompassClientEvents {

    /** 玩家登出/断开时清空目标，避免残留上一存档的目标状态。 */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ArchaeologyCompassClientState.reset();
    }

    /** 展示用途与 Shift 详情；范围只读取已加载的 SERVER 配置，搜索状态使用收到的 S2C 结果。 */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ExampleMod.ARCHAEOLOGY_COMPASS.get())) {
            return;
        }

        var tooltip = event.getToolTip();
        tooltip.add(Component.translatable("tooltip.archaeologycompass.purpose").withStyle(ChatFormatting.GRAY));
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable("tooltip.archaeologycompass.loaded_chunks").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.archaeologycompass.no_target_spin").withStyle(ChatFormatting.GRAY));
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        if (Config.SPEC.isLoaded()) {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.range",
                    Config.HORIZONTAL_RADIUS.get(), Config.VERTICAL_RADIUS.get()).withStyle(ChatFormatting.GRAY));
        }

        if (!player.getInventory().contains(stack -> stack.is(ExampleMod.ARCHAEOLOGY_COMPASS.get()))) {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.inactive").withStyle(ChatFormatting.YELLOW));
            return;
        }

        ExampleMod.Target target = ArchaeologyCompassClientState.getTarget();
        if (!ArchaeologyCompassClientState.hasReceivedTarget()
                || target != null && target.dimension() != player.level().dimension()) {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.waiting").withStyle(ChatFormatting.YELLOW));
        } else if (target == null) {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.not_found").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.translatable("tooltip.archaeologycompass.found").withStyle(ChatFormatting.GREEN));
        }
    }

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassClientEvents() {
    }
}
