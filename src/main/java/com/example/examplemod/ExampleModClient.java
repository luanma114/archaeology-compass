package com.example.examplemod;

// NeoForge：仅在客户端加载此类，后续在这里放模型属性和目标同步后的指针显示代码。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 客户端专属代码入口预留。
 *
 * <p>此类只会在客户端加载。后续在此注册考古罗盘模型属性、接收目标同步包，
 * 并根据服务端同步的目标坐标计算指针角度。当前没有客户端逻辑。</p>
 */
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public final class ExampleModClient {
    /** 工具类不允许实例化。 */
    private ExampleModClient() {
    }
}
