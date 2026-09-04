package com.youyimc.createmassenergy.compat;

import com.youyimc.createmassenergy.CreateMassenergy;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Create Aeronautics（Sable 物理结构库）软依赖联动桥接。
 * <p>
 * 本模组不强制依赖航空学。当 Sable 未加载时，所有联动逻辑自动跳过。
 * <p>
 * 联动能力：
 * <ul>
 *   <li>明确注册 Create 的移动检查，使本模组机器方块可被搬上航空学物理结构</li>
 *   <li>检测 BE 是否位于航空学物理结构（sub-level）中</li>
 *   <li>为无线电跨结构传输提供 level 判断辅助</li>
 * </ul>
 * <p>
 * 设计要点：对 {@code dev.ryanhcode.sable.*} 类的所有引用都隔离在
 * {@link SableAccess} 内部类中。由于 JVM 惰性加载类，只有 Sable 真正存在时
 * 才会加载 {@code SableAccess}，保证软依赖下本类及模组其余代码不触发
 * {@code NoClassDefFoundError}。
 * <p>
 * 时序要点：{@link DeferredBlock} 只有在 registry 绑定完成后才能安全调用
 * {@code get()}，因此本类不立即解析方块，而是把 {@code DeferredBlock} 保存起来，
 * 由调用方在 {@code RegisterEvent}（方块注册完成）之后触发 {@link #init(List)}。
 */
public final class AeronauticsCompat {

    /** Sable 的 mod id */
    public static final String SABLE_MOD_ID = "sable";

    private AeronauticsCompat() {}

    /** 判断 Sable（航空学物理结构库）是否已加载 */
    public static boolean isSableLoaded() {
        return ModList.get() != null && ModList.get().isLoaded(SABLE_MOD_ID);
    }

    /**
     * 初始化航空学联动（必须在方块注册完成后的 {@code RegisterEvent} 中调用）。
     * <p>
     * 仅在 Sable 已加载时注册 Create 的移动检查，使本模组机器能被搬上物理结构。
     * 若 Sable 未加载，此方法为无操作，模组正常加载。
     *
     * @param machineBlocks 本模组可被航空学结构搬运的机器方块（DeferredBlock）
     */
    public static void init(List<DeferredBlock<? extends Block>> machineBlocks) {
        if (!isSableLoaded()) {
            CreateMassenergy.LOGGER.info("Create Aeronautics (Sable) 未加载，跳过航空学物理结构联动");
            return;
        }
        try {
            List<Block> resolved = new ArrayList<>();
            for (DeferredBlock<? extends Block> db : machineBlocks) {
                if (db != null && db.isBound()) {
                    resolved.add(db.get());
                }
            }
            if (resolved.isEmpty()) {
                CreateMassenergy.LOGGER.info("航空学联动：无已绑定的机器方块可注册，跳过");
                return;
            }
            // 明确允许本模组机器方块被搬上 contraption / 物理结构
            BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) ->
                    resolved.contains(state.getBlock())
                            ? CheckResult.SUCCESS
                            : CheckResult.PASS);
            CreateMassenergy.LOGGER.info("已注册航空学物理结构联动（{} 个机器方块可被搬运）", resolved.size());
        } catch (Throwable t) {
            CreateMassenergy.LOGGER.warn("注册航空学移动检查失败: {}", t.toString());
        }
    }

    /**
     * 返回指定方块实体所在的航空学 sub-level（物理结构）。
     * <p>
     * 若 Sable 未加载、或该方块实体不在任何物理结构上，返回 {@code null}。
     */
    public static Object getContainingSubLevel(BlockEntity be) {
        if (!isSableLoaded() || be == null) {
            return null;
        }
        return SableAccess.getContainingSubLevel(be);
    }

    /**
     * 判断指定方块实体是否位于航空学物理结构（sub-level）上。
     */
    public static boolean isInSubLevel(BlockEntity be) {
        if (!isSableLoaded() || be == null) {
            return false;
        }
        return SableAccess.isInSubLevel(be);
    }

    /**
     * 所有对 Sable 类的直接引用隔离在此内部类中。
     * <p>
     * 由于只有 Sable 加载时才会被调用，此内部类只会在 Sable 存在时被 JVM 加载，
     * 避免软依赖缺失导致的类加载异常。
     */
    private static final class SableAccess {
        static boolean isInSubLevel(BlockEntity be) {
            return getContainingSubLevel(be) != null;
        }

        static Object getContainingSubLevel(BlockEntity be) {
            if (dev.ryanhcode.sable.Sable.HELPER == null) {
                return null;
            }
            try {
                return dev.ryanhcode.sable.Sable.HELPER.getContaining(be);
            } catch (Throwable t) {
                CreateMassenergy.LOGGER.debug("Failed to query sub-level for BE at {}: {}",
                        be.getBlockPos(), t.toString());
                return null;
            }
        }
    }
}
