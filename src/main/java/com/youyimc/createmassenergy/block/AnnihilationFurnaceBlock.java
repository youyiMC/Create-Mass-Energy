package com.youyimc.createmassenergy.block;

import com.mojang.serialization.MapCodec;
import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.entity.AnnihilationFurnaceBlockEntity;
import com.youyimc.createmassenergy.util.InventoryHelper;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 物品湮灭炉
 * <p>
 * 功能：以极快的速度销毁物品并根据质能方程输出 FE，实际产出能量具有 40% 能量损耗。
 * 特性：可作为容器通过漏斗等方式输入物品；可被扳手潜行右键拆除。
 * 放置时正面朝向玩家（像熔炉那样可旋转）。
 */
public class AnnihilationFurnaceBlock extends BaseEntityBlock implements IWrenchable {

    public static final MapCodec<AnnihilationFurnaceBlock> CODEC = simpleCodec(AnnihilationFurnaceBlock::new);

    /** 是否工作中（用于渲染状态） */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    /** 朝向（正面方向） */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public AnnihilationFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WORKING, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 正面朝向放置玩家（玩家面朝的方向的反方向，即正面朝自己）
        return defaultBlockState().setValue(WORKING, false)
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * 机械动力扳手旋转：点击水平面时正面朝向点击面，点击顶/底面时顺时针旋转 90°
     */
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis().isHorizontal()) {
            return originalState.setValue(FACING, targetedFace);
        }
        return originalState.setValue(FACING, originalState.getValue(FACING).getClockWise());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnnihilationFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.ANNIHILATION_FURNACE.get(),
            AnnihilationFurnaceBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
        BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof AnnihilationFurnaceBlockEntity be) {
                player.openMenu(be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AnnihilationFurnaceBlockEntity furnace) {
                // 掉落输入槽中的物品（湮灭炉只有物品输入槽）
                InventoryHelper.dropContents(level, pos, furnace.getInputInventory());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
