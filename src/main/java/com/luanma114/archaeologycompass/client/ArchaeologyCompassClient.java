package com.luanma114.archaeologycompass.client;

// 模组入口：读取注册物品对象。
import com.luanma114.archaeologycompass.ExampleMod;
// Minecraft：客户端模型属性注册。
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
// NeoForge：标记本类仅在物理客户端加载，并接收客户端设置事件。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

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
     * 客户端设置阶段回调。
     *
     * <p>此时物品注册表已绑定，可以安全地通过 {@code ARCHAEOLOGY_COMPASS.get()} 获取物品实例。
     * 不能在 {@link ExampleMod} 构造函数阶段注册，因为那时延迟注册器尚未完成绑定。</p>
     *
     * @param event 客户端设置事件
     */
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ArchaeologyCompassClient::registerItemProperties);
    }

    /**
     * 注册考古罗盘的“angle”指针属性，让继承的原版指南针模型能随目标旋转。
     *
     * <p>原版 {@code minecraft:item/compass} 模型通过 {@code minecraft:angle} 谓词在 32 帧纹理中
     * 选择一帧，但该属性是按物品单独注册的，原版只注册给指南针和追溯指针。这里为考古罗盘
     * 复用同一属性，并由 {@link ArchaeologyCompassPropertyFunction} 从服务端写入物品的
     * {@link net.minecraft.core.component.DataComponents#LODESTONE_TRACKER} 数据组件读取目标坐标。</p>
     *
     * <ul>
     *   <li>组件存在且含目标：指针指向它（与原版指向逻辑一致）；</li>
     *   <li>组件不存在或目标为空：指针匀速顺时针旋转。</li>
     * </ul>
     */
    private static void registerItemProperties() {
        ItemProperties.register(
                ExampleMod.ARCHAEOLOGY_COMPASS.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new ArchaeologyCompassPropertyFunction()
        );
    }
}
