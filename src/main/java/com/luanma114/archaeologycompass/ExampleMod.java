package com.luanma114.archaeologycompass;

// Minecraft：坐标、注册表、资源 ID、标签、物品、维度和方块类型。
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
// 客户端专属初始化：仅在 Dist.CLIENT 分支引用，避免独立服务端加载客户端类。
import com.luanma114.archaeologycompass.client.ArchaeologyCompassClient;
// NeoForge：模组入口、生命周期事件、配置和安全的延迟注册。
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 考古罗盘模组入口。
 *
 * <p>此类在模组加载阶段创建注册表、注册服务端配置，并将物品加入创造模式标签页。
 * 游戏运行时扫描逻辑位于 {@link ArchaeologyCompassEvents}，避免初始化代码承担游戏事件处理职责。</p>
 */
@Mod(ExampleMod.MOD_ID)
public final class ExampleMod {
    /** 模组唯一命名空间。资源目录、标签、注册 ID 和元数据必须使用相同值。 */
    public static final String MOD_ID = "archaeologycompass";

    /**
     * 可作为考古罗盘候选目标的方块标签。
     *
     * <p>定义文件位于 {@code data/archaeologycompass/tags/block/archaeology_targets.json}。
     * 整合包或其他模组可扩展此标签，无需修改 Java 代码。</p>
     */
    public static final TagKey<Block> ARCHAEOLOGY_TARGETS = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "archaeology_targets")
    );

    /** 此模组物品的延迟注册器。必须在构造函数中绑定到 Mod Event Bus。 */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    /**
     * 考古罗盘物品注册对象。
     *
     * <p>物品注册、服务端目标扫描、客户端指针渲染和目标同步已完成。
     * 当前模型为含 32 帧 {@code minecraft:angle} override 的原版指南针外观，正式发布前可替换为保持相同指向逻辑的自制外观。</p>
     */
    public static final DeferredItem<Item> ARCHAEOLOGY_COMPASS = ITEMS.registerSimpleItem(
            "archaeology_compass",
            new Item.Properties().stacksTo(1)
    );

    /**
     * NeoForge 在加载模组时调用此构造函数。
     *
     * @param modBus 模组生命周期事件总线，用于注册内容和监听创造模式标签页事件
     * @param modContainer 当前模组容器，用于注册服务端配置
     */
    public ExampleMod(IEventBus modBus, ModContainer modContainer) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
        modBus.addListener(ArchaeologyCompassNetwork::registerPayloads);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        // 仅客户端监听客户端设置事件，在其中注册指南针指针属性（需等待物品注册完成）。
        // 独立服务端不会进入该分支，从而不加载客户端渲染类。
        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(ArchaeologyCompassClient::onClientSetup);
        }
    }

    /** 将考古罗盘添加到原版“工具与实用物品”创造模式标签页。 */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ARCHAEOLOGY_COMPASS);
        }
    }

    /**
     * 表示一次扫描选出的目标位置。
     *
     * <p>维度与坐标必须同时保存，防止玩家换维度后将相同坐标误认为原目标。
     * 服务端目标缓存保存此值，并在变化时同步到对应客户端；客户端模型模块后续读取该值计算指针角度。</p>
     */
    public record Target(ResourceKey<Level> dimension, BlockPos position) {
    }
}
