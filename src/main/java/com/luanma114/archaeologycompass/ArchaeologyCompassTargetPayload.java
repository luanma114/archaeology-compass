package com.luanma114.archaeologycompass;

// Minecraft：网络包类型、坐标和维度注册表编码器。
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * 服务端发送给客户端的考古罗盘目标状态包。
 *
 * <p>服务端在目标变化、清除旧目标或初始化扫描状态时发送本包。{@code target == null} 表示清除目标，
 * 客户端收到后清空本地目标并标记已收到状态，让指针旋转、提示结束等待。</p>
 */
public record ArchaeologyCompassTargetPayload(ExampleMod.Target target) implements CustomPacketPayload {
    /** 网络包唯一 ID：{@code archaeologycompass:target_state}。 */
    public static final Type<ArchaeologyCompassTargetPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "target_state")
    );

    /**
     * 将可空目标编码到网络缓冲区。
     *
     * <p>首个布尔值表示是否存在目标。存在时写入维度资源 ID 和压缩方块坐标。
     * 使用 {@link BlockPos#asLong()} 减少网络传输字节数。</p>
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ArchaeologyCompassTargetPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeBoolean(payload.target != null);
                if (payload.target != null) {
                    buffer.writeResourceLocation(payload.target.dimension().location());
                    buffer.writeLong(payload.target.position().asLong());
                }
            },
            buffer -> {
                if (!buffer.readBoolean()) {
                    return new ArchaeologyCompassTargetPayload(null);
                }

                ResourceLocation dimensionId = buffer.readResourceLocation();
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
                return new ArchaeologyCompassTargetPayload(new ExampleMod.Target(dimension, BlockPos.of(buffer.readLong())));
            }
    );

    /** 返回 NeoForge 用于路由本包的唯一类型。 */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
