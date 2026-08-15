package com.anantaya.adventurersbackpack.block;

import com.anantaya.adventurersbackpack.backpack.BackpackScreenHandler;
import com.anantaya.adventurersbackpack.backpack.BackpackTier;
import com.anantaya.adventurersbackpack.block.entity.BackpackBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

public class BackpackBlock extends BaseEntityBlock {

    public static final MapCodec<BackpackBlock> CODEC =
            MapCodec.unit(() -> new BackpackBlock(
                    BackpackTier.IRON,
                    BlockBehaviour.Properties.of()
            ));

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Shapes.box(
            3.0D / 16.0D,
            0.0D,
            3.0D / 16.0D,
            13.0D / 16.0D,
            13.0D / 16.0D,
            13.0D / 16.0D
    );

    private final BackpackTier tier;

    public BackpackBlock(BackpackTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WATERLOGGED, false)
        );
    }

    public BackpackTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(pos).is(FluidTags.WATER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackBlockEntity(pos, state, tier);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BackpackBlockEntity backpackBlockEntity)) {
            return InteractionResult.PASS;
        }

        BackpackTier tier = backpackBlockEntity.getTier();

        String title = switch (tier) {
            case IRON -> "Backpack";
            case DIAMOND -> "Backpack II";
            case NETHERITE -> "Backpack III";
        };

        player.openMenu(new SimpleMenuProvider(
                (syncId, playerInventory, p) -> new BackpackScreenHandler(
                        syncId,
                        playerInventory,
                        backpackBlockEntity
                ),
                Component.literal(title)
        ));

        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void playerDestroy(
            Level level,
            Player player,
            BlockPos pos,
            BlockState state,
            @Nullable BlockEntity blockEntity,
            ItemStack destroyedWith
    ) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);

        if (!level.isClientSide() && blockEntity instanceof BackpackBlockEntity backpackBlockEntity) {
            ItemStack drop = backpackBlockEntity.createDroppedBackpackStack(
                    level.registryAccess()
            );

            if (drop != null && !drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }

            if (level instanceof ServerLevel serverLevel) {
                state.spawnAfterBreak(serverLevel, pos, destroyedWith, true);
            }
        }
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity by,
            ItemStack itemStack
    ) {
        super.setPlacedBy(level, pos, state, by, itemStack);

        if (level.isClientSide()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BackpackBlockEntity backpackBlockEntity)) {
            return;
        }

        if (itemStack == null || itemStack.isEmpty()) {
            backpackBlockEntity.setChanged();
            return;
        }

        backpackBlockEntity.loadFromBackpackStack(
                itemStack,
                level.registryAccess()
        );

        backpackBlockEntity.setChanged();
    }
}