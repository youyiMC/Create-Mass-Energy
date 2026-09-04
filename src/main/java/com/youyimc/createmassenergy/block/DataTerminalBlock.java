package com.youyimc.createmassenergy.block;

import com.mojang.serialization.MapCodec;
import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.entity.DataTerminalBlockEntity;
import com.youyimc.createmassenergy.util.InventoryHelper;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 数据化终端
 * <p>
 * 功能：在 GUI 内切换物品 数据化分解/还原 工作模式。
 * 分解模式：物品分解为数据并产出 FE（50% 能量损耗）。
 * 还原模式：根据质能方程消耗 FE 将数据转化回对应物品（无额外损耗）。
 * 特性：可作为容器被漏斗等输入/输出物品；10kFE 能量容积；64MB 存储空间（可升级叠加）；
 * 8 格物品栏；被破坏时未存储物品/模块/方块本体掉落，已数据化部分直接丢失。
 */
public class DataTerminalBlock extends BaseEntityBlock implements IWrenchable {

    public static final MapCodec<DataTerminalBlock> CODEC = simpleCodec(DataTerminalBlock::new);

    /** 是否工作中 */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    public DataTerminalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WORKING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WORKING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WORKING, false);
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
        return new DataTerminalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.DATA_TERMINAL.get(),
            DataTerminalBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
        BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof DataTerminalBlockEntity be) {
                player.openMenu(be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DataTerminalBlockEntity terminal) {
                // 文档：被破坏时，还未被存储的物品、各项模块和终端方块本体正常掉落；已数据化部分直接丢失
                InventoryHelper.dropContents(level, pos, terminal.getInventory());
                InventoryHelper.dropContents(level, pos, terminal.getUpgradeSlots());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
