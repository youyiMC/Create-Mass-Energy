package com.youyimc.createmassenergy.block;

import com.mojang.serialization.MapCodec;
import com.youyimc.createmassenergy.ModBlockEntities;
import com.youyimc.createmassenergy.block.entity.RadioTelegraphBlockEntity;
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
 * 收发报机
 * <p>
 * 与数据化终端配合使用，放置在数据化终端上方时读取下方终端进行收发工作。
 * 命名后可用；GUI 内切换发送/接收模式。
 * 发送模式：查看全服接收器名单，可配多个接收器，按优先级分配。
 * 接收模式：无权配对，可被多个发报机绑定，自动发到下方数据终端。
 */
public class RadioTelegraphBlock extends BaseEntityBlock implements IWrenchable {

    public static final MapCodec<RadioTelegraphBlock> CODEC = simpleCodec(RadioTelegraphBlock::new);

    /** 是否工作中 */
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public RadioTelegraphBlock(Properties properties) {
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
        return new RadioTelegraphBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RADIO_TELEGRAPH.get(),
            RadioTelegraphBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
        BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof RadioTelegraphBlockEntity be) {
                player.openMenu(be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
