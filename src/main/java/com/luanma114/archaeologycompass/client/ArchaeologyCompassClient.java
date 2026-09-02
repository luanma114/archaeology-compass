package com.luanma114.archaeologycompass.client;

// 模组入口：读取注册物品对象。
import com.luanma114.archaeologycompass.ExampleMod;
// Minecraft：客户端模型属性注册、指南针指针计算与物品数据组件。
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.LodestoneTracker;
// NeoForge：标记本类仅在物理客户端加载。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 考古罗盘的客户端专属初始化。
 *
 * <p>本类只引用客户端渲染 API，只能在物理客户端加载。独立服务端绝不加载本类，
 * 所有方法都必须由 {@link ExampleMod} 在 {@code Dist.CLIENT} 判定成立时才调用。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ArchaeologyCompassClient {

    /** 工具类不允许实例化。 */
    private ArchaeologyCompassClient() {
    }

    /**
     * 注册考古罗盘的“angle”指针属性，让继承的原版指南针模型能随目标旋转。
     *
     * <p>原版 {@code minecraft:item/compass} 模型通过 {@code minecraft:angle} 谓词在 32 帧纹理中
     * 选择一帧，但该属性是按物品单独注册的，原版只注册给指南针和追溯指针。这里为考古罗盘
     * 复用同一属性，并直接从服务端写入物品的 {@link DataComponents#LODESTONE_TRACKER}
     * 数据组件读取目标坐标。</p>
     *
     * <ul>
     *   <li>组件存在且含目标：返回目标 {@link net.minecraft.core.GlobalPos}，指针指向它；</li>
     *   <li>组件不存在或目标为空：返回 {@code null}，指针进入持续旋转状态。</li>
     * </ul>
     *
     * <p>维度校验由 {@link CompassItemPropertyFunction} 内部完成：目标维度与当前维度不一致时
     * 同样进入旋转，无需在此重复判断。</p>
     */
    public static void registerItemProperties() {
        ItemProperties.register(
                ExampleMod.ARCHAEOLOGY_COMPASS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> {
                    LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
                    return tracker == null ? null : tracker.target().orElse(null);
                })
        );
    }
}
