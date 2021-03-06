package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.util.BlockFilter;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.Set;

@Module(moduleName = "Bridge", onEnable="onInitialize", onDisable="onDisable")
public class Bridge extends SimpleArea implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<Integer> maximumLength = new ConfigValue<>("maximum-length", "The maximum length the bridge can be.", 16);
    private ConfigValue<Integer> maximumWidth = new ConfigValue<>("maximum-width", "The maximum width each side of the bridge can be. The overall max width is this*2 + 1.", 5);

    @Override
    public void onInitialize() throws CraftBookException {
        super.loadCommonConfig(config);

        maximumLength.load(config);
        maximumWidth.load(config);
    }

    @Override
    public void onDisable() {
        super.saveCommonConfig(config);

        maximumLength.save(config);
        maximumWidth.save(config);
    }

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Humanoid human, Boolean forceState) {

        if (!SignUtil.getTextRaw(sign, 1).equals("[Bridge End]")) {

            Direction back = SignUtil.getBack(block);

            Location baseBlock = block.getRelative(Direction.DOWN);

            if(!BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), baseBlock.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Can't use this material for a bridge!").build());
                return true;
            }

            Location otherSide = getOtherEnd(block, SignUtil.getBack(block), maximumLength.getValue());
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Missing other end!").build());
                return true;
            }
            Location otherBase = otherSide.getRelative(Direction.DOWN);

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks, rightBlocks;

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            leftBlocks = BlockUtil.getMinimumLength(left, otherLeft, baseBlock.getBlock(), SignUtil.getLeft(block), maximumWidth.getValue());

            //Calculate right distance
            Location otherRight = otherBase.getRelative(SignUtil.getRight(block));

            rightBlocks = BlockUtil.getMinimumLength(right, otherRight, baseBlock.getBlock(), SignUtil.getRight(block), maximumWidth.getValue());

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(Direction.DOWN).getBlock();
            if (baseBlock.getBlock().equals(type) || (forceState != null && !forceState)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getBlockX() != otherSide.getBlockX() || baseBlock.getBlockZ() != otherSide.getBlockZ()) {

                baseBlock.setBlock(type);

                left = baseBlock.getRelative(SignUtil.getLeft(block));

                for(int i = 0; i < leftBlocks; i++) {
                    left.setBlock(type);
                    left = left.getRelative(SignUtil.getLeft(block));
                }

                right = baseBlock.getRelative(SignUtil.getRight(block));

                for(int i = 0; i < rightBlocks; i++) {
                    right.setBlock(type);
                    right = right.getRelative(SignUtil.getRight(block));
                }

                baseBlock = baseBlock.getRelative(back);
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Text.builder("Bridge not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public boolean isMechanicSign(Sign sign) {
        return SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge]") || SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge End]");
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Bridge]", "[Bridge End]"};
    }

    @Override
    public Set<BlockFilter> getDefaultBlocks() {
        Set<BlockFilter> states = Sets.newHashSet();
        states.add(new BlockFilter("PLANKS"));
        states.add(new BlockFilter("BOOKSHELF"));
        states.add(new BlockFilter("COBBLESTONE"));
        return states;
    }

    @Override
    public String getPath() {
        return "mechanics/bridge";
    }

    @Override
    public String getMainDocumentation() {
        return  "=======" +
                "Bridges" +
                "=======" +
                "**Bridges** are configurable-width flat sections of the world that you can toggle on and off. The width of the bridge can be changed." +
                "" +
                "They can be toggled using two different methods:" +
                "* Right clicking a sign" +
                "* Powering the sign with redstone" +
                "" +
                "";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[]{
                allowedBlocks,
                maximumLength,
                maximumWidth
        };
    }
}
