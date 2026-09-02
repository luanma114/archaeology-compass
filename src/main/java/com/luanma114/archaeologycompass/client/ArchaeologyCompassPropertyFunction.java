package com.luanma114.archaeologycompass.client;

// Minecraft：客户端模型属性接口、坐标/角度计算与物品数据组件。
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.phys.Vec3;
// NeoForge：标记本类仅在物理客户端加载。
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 考古罗盘的指针模型属性函数。
 *
 * <p>替代原版 {@link net.minecraft.client.renderer.item.CompassItemPropertyFunction} 的随机抖动旋转，
 * 在无目标时改为匀速顺时针旋转；有目标时的指向计算与原版完全一致。</p>
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

        return getRotation(stack, level, entity1);
    }

    /**
     * 根据物品上是否存在有效目标，选择指向或匀速旋转。
     */
    private float getRotation(ItemStack stack, ClientLevel level, Entity entity) {
        GlobalPos target = getTarget(stack);
        if (!isValidTarget(entity, target)) {
            return getClockwiseSpin(level.getGameTime());
        }
        return getRotationTowardsTarget(entity, level.getGameTime(), target.pos());
    }

    /** 从服务端写入物品的磁石目标组件读取目标坐标；无目标返回 {@code null}。 */
    private static GlobalPos getTarget(ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        return tracker == null ? null : tracker.target().orElse(null);
    }

    /** 目标存在、与玩家同维度、且不处于玩家脚下时才视为有效。 */
    private static boolean isValidTarget(Entity entity, GlobalPos target) {
        return target != null
                && target.dimension() == entity.level().dimension()
                && !(target.pos().distToCenterSqr(entity.position()) < 1.0E-5F);
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
