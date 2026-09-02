package com.luanma114.archaeologycompass.client;

// 模组入口与客户端同步状态：读取服务端通过 S2C 包下发的目标。
import com.luanma114.archaeologycompass.ArchaeologyCompassClientState;
import com.luanma114.archaeologycompass.ExampleMod;
// Minecraft：客户端模型属性接口与角度计算。
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
// NeoForge：标记本类仅在物理客户端加载。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 考古罗盘的指针模型属性函数。
 *
 * <p>从 {@link ArchaeologyCompassClientState} 读取服务端通过 S2C 包下发的目标，替代原版
 * {@link net.minecraft.client.renderer.item.CompassItemPropertyFunction} 的随机抖动旋转：
 * 无目标时匀速顺时针旋转，有目标时按原版指向逻辑指向目标。</p>
 *
 * <p>不读取物品上的 {@code LodestoneTracker} 数据组件，因为服务端修改物品组件后依赖库存同步，
 * 在结构方块等场景下同步不可靠；显式的 S2C 网络包更为可靠。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ArchaeologyCompassPropertyFunction implements ClampedItemPropertyFunction {

    /** 无目标时指针旋转的角度步长。以 1/64 圈为一步，基准每 Tick 前进 2 步（每 32 Tick 一圈）。 */
    private static final int SPIN_STEPS_PER_REVOLUTION = 64;

    /** 每 Tick 前进的步数。3 步 = 基准的 1.5 倍（约每 21.3 Tick 转一圈）。 */
    private static final int SPIN_STEPS_PER_TICK = 3;

    /** 指向目标时的平滑插值，避免玩家转身时指针瞬间跳变。 */
    private final Wobble wobble = new Wobble();

    @Override
    public float unclampedCall(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        Entity entity1 = entity != null ? entity : stack.getEntityRepresentation();
        if (entity1 == null) {
            return 0.0F;
        }

        if (level == null && entity1.level() instanceof ClientLevel clientLevel) {
            level = clientLevel;
        }
        if (level == null) {
            return 0.0F;
        }

        return getRotation(level, entity1);
    }

    /** 根据服务端下发的目标是否存在，选择指向或匀速旋转。 */
    private float getRotation(ClientLevel level, Entity entity) {
        ExampleMod.Target target = ArchaeologyCompassClientState.getTarget();
        if (!isValidTarget(entity, target)) {
            return getClockwiseSpin(level.getGameTime());
        }
        return getRotationTowardsTarget(entity, level.getGameTime(), target.position());
    }

    /** 目标存在、与玩家同维度、且不处于玩家脚下时才视为有效。 */
    private static boolean isValidTarget(Entity entity, ExampleMod.Target target) {
        return target != null
                && target.dimension() == entity.level().dimension()
                && !(target.position().distToCenterSqr(entity.position()) < 1.0E-5F);
    }

    /**
     * 无目标时的匀速顺时针旋转。
     *
     * <p>返回值随时间从 0 线性增长到 1 再回到 0，对应模型 32 帧按顺序循环，即顺时针旋转。
     * 每 Tick 前进 {@link #SPIN_STEPS_PER_TICK} 步，用取模避免长时间运行后浮点精度下降。</p>
     */
    private static float getClockwiseSpin(long gameTime) {
        long step = (gameTime * SPIN_STEPS_PER_TICK) % SPIN_STEPS_PER_REVOLUTION;
        return (float) step / (float) SPIN_STEPS_PER_REVOLUTION;
    }

    /** 指向目标时的角度计算，逻辑与原版指南针一致（含朝向补偿与平滑）。 */
    private float getRotationTowardsTarget(Entity entity, long gameTime, BlockPos pos) {
        double angleToPos = getAngleFromEntityToPos(entity, pos);
        double wrappedRotationY = getWrappedVisualRotationY(entity);
        if (entity instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (this.wobble.shouldUpdate(gameTime)) {
                this.wobble.update(gameTime, 0.5 - (wrappedRotationY - 0.25));
            }
            double rotation = angleToPos + this.wobble.rotation;
            return Mth.positiveModulo((float) rotation, 1.0F);
        }
        double rotation = 0.5 - (wrappedRotationY - 0.25 - angleToPos);
        return Mth.positiveModulo((float) rotation, 1.0F);
    }

    /** 实体到目标方块中心的世界方位角（归一化到 [-0.5, 0.5]）。 */
    private static double getAngleFromEntityToPos(Entity entity, BlockPos pos) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX()) / (float) (Math.PI * 2);
    }

    /** 玩家当前朝向（yaw）归一化到 [0, 1)。 */
    private static double getWrappedVisualRotationY(Entity entity) {
        return Mth.positiveModulo((double) (entity.getVisualRotationYInDegrees() / 360.0F), 1.0);
    }

    /** 平滑插值辅助类，复制原版指南针的抖动平滑逻辑，仅用于指向过渡。 */
    private static final class Wobble {
        double rotation;
        private double deltaRotation;
        private long lastUpdateTick;

        boolean shouldUpdate(long gameTime) {
            return this.lastUpdateTick != gameTime;
        }

        void update(long gameTime, double target) {
            this.lastUpdateTick = gameTime;
            double delta = target - this.rotation;
            delta = Mth.positiveModulo(delta + 0.5, 1.0) - 0.5;
            this.deltaRotation += delta * 0.1;
            this.deltaRotation *= 0.8;
            this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
        }
    }
}
