package io.github.reserveword.yescommander;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;

public class Commands {
    static class WindowClick extends AbstractClientCommand {
        private static final String name = "clickinv";
        public WindowClick() {
            super(name,I18n.format("commands.usage.clickinv",name),new ArrayList<>(0));
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            EntityPlayerSP player = FMLClientHandler.instance().getClient().player;
            PlayerControllerMP controller = FMLClientHandler.instance().getClient().playerController;
            int windowID = 0;
            int slotID;
            int actionID;
            ClickType ct;
            if (args.length == 3 && "external".equals(args[2])) {
                windowID = player.openContainer.windowId;
            } else if (args.length != 2) throw new CommandException("commands.exception.wrongargumentlenth");
            try {
                slotID = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new CommandException("commands.exception.argumentnotinteger");
            }
            String key = args[1].toLowerCase();
            if (key.matches("[1-9]")) {
                actionID = Integer.parseInt(key)-1;
                ct = ClickType.SWAP;
            }
            else if (key.matches("[scd]?[lr]")) {
                switch (key.charAt(0)) {
                    case 's':
                        ct = ClickType.QUICK_MOVE;
                        break;
                    case 'c':
                        ct = ClickType.PICKUP_ALL;
                        break;
                    case 'd':
                        ct = ClickType.QUICK_CRAFT;
                        break;
                    default:
                        ct = ClickType.PICKUP;
                }
                switch (key.charAt(key.length()-1)) {
                    case 'l':
                        actionID = 0;
                        break;
                    case 'r':
                        actionID = 1;
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            else throw new CommandException("commands.exception.argumentisnotvalid");

            controller.windowClick(windowID, slotID, actionID, ct, player);
        }
    }

    static class WorldClick extends AbstractClientCommand {
        private  static final String name = "clickworld";
        public WorldClick() {
            super(name,I18n.format("commands.usage.clickworld", name),new ArrayList<>(0));
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            int shift = 0;
            Minecraft mc = FMLClientHandler.instance().getClient();
            PlayerControllerMP controller = mc.playerController;
            BlockPos bp = sender.getPosition();
            EnumFacing ef = EnumFacing.EAST;
            Integer metadata = null;
            ResourceLocation block = null;
            if (args.length != 0 && args[0].toLowerCase().matches("[lr]")) shift = 1;
            boolean right = (shift != 0 && args[0].toLowerCase().equals("r"));
            switch (args.length-shift) {
                case 6:
                    metadata = Integer.parseInt(args[5+shift]);
                case 5:
                    block = new ResourceLocation(args[4+shift]);
                case 4:
                    ef = EnumFacing.byName(args[3+shift]);
                    if (ef == null) throw new CommandException("commands.exception.argumentisnotvalid");
                case 3:
                    String x = args[shift];
                    String y = args[1+shift];
                    String z = args[2+shift];
                    if ("-".equals(x) && "-".equals(y) && "-".equals(z)) {
                        RayTraceResult result = mc.objectMouseOver;
                        if (result == null) return;
                        else if (result.typeOfHit == RayTraceResult.Type.BLOCK) bp = result.getBlockPos();
                        else if (result.typeOfHit == RayTraceResult.Type.ENTITY) bp = new BlockPos(result.hitVec);
                        else if (block != null) return;
                        if (!checkFilter(block, metadata, bp)) return;
                        if (right) {
                            rightClick();
                        } else {
                            leftClick();
                        }
                    }
                    int xx=0,yy=0,zz=0;
                    try {
                        if (x.charAt(0) == '~') {
                            x = x.substring(1);
                            if (x.equals("")) x = "0";
                            xx = bp.getX() + Integer.parseInt(x);
                        }
                        if (y.charAt(0) == '~') {
                            y = y.substring(1);
                            if (y.equals("")) y = "0";
                            yy = bp.getY() + Integer.parseInt(y);
                        }
                        if (z.charAt(0) == '~') {
                            z = z.substring(1);
                            if (z.equals("")) z = "0";
                            zz = bp.getZ() + Integer.parseInt(z);
                        }
                    } catch (NumberFormatException e) {
                        throw new CommandException("commands.exception.argumentnotinteger");
                    }
                    bp = new BlockPos(xx, yy, zz);
                    break;
                case 0:
                    if (right) {
                        for(EnumHand enumhand:EnumHand.values()) {
                            ItemStack itemstack = mc.player.getHeldItem(enumhand);
                            if (itemstack.isEmpty()) net.minecraftforge.common.ForgeHooks.onEmptyClick(mc.player, enumhand);
                            if (!itemstack.isEmpty() && controller.processRightClick(mc.player, mc.world, enumhand) == EnumActionResult.SUCCESS)
                                break;
                        }
                    } else {
                        ForgeHooks.onEmptyLeftClick(mc.player);
                    }
                    return;
                default:
                    throw new CommandException("commands.exception.argumentisnotvalid");
            }
            if (!checkFilter(block, metadata, bp)) return;
            if (right){
                for(EnumHand enumhand: EnumHand.values()) {
                    if (controller.processRightClickBlock(mc.player, mc.world, bp, ef, new Vec3d(bp.getX()+.5,bp.getY()+.5,bp.getZ()+.5), enumhand)
                            == EnumActionResult.SUCCESS)
                        break;
                }
            } else {
                controller.clickBlock(bp, ef);
            }
        }

        public boolean checkFilter(ResourceLocation block, Integer metadata, BlockPos bp) {
            if (block != null) {
                IBlockState bs = FMLClientHandler.instance().getClient().world.getBlockState(bp);
                Block b = bs.getBlock();
                if (!block.equals(b.getRegistryName())) return false;
                if (metadata != null && metadata != b.getMetaFromState(bs))return false;
            }
            return true;
        }

        private void rightClick() {
            Minecraft mc = FMLClientHandler.instance().getClient();
            RayTraceResult objectMouseOver = mc.objectMouseOver;
            EntityPlayerSP player = mc.player;
            PlayerControllerMP controller = mc.playerController;
            for (EnumHand enumhand : EnumHand.values())
            {
                ItemStack itemstack = player.getHeldItem(enumhand);

                if (objectMouseOver != null)
                {
                    switch (objectMouseOver.typeOfHit)
                    {
                        case ENTITY:

                            if (controller.interactWithEntity(player, objectMouseOver.entityHit, objectMouseOver, enumhand) == EnumActionResult.SUCCESS)
                            {
                                return;
                            }

                            if (controller.interactWithEntity(player, objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS)
                            {
                                return;
                            }

                            break;
                        case BLOCK:
                            BlockPos blockpos = objectMouseOver.getBlockPos();

                            if (mc.world.getBlockState(blockpos).getMaterial() != Material.AIR)
                            {
                                EnumActionResult enumactionresult = controller.processRightClickBlock(player, mc.world, blockpos, objectMouseOver.sideHit, objectMouseOver.hitVec, enumhand);

                                if (enumactionresult == EnumActionResult.SUCCESS)
                                {
                                    return;
                                }
                            }
                    }
                }

                if (itemstack.isEmpty() && (objectMouseOver == null || objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) net.minecraftforge.common.ForgeHooks.onEmptyClick(player, enumhand);
                if (!itemstack.isEmpty() && controller.processRightClick(player, mc.world, enumhand) == EnumActionResult.SUCCESS)
                {
                    return;
                }
            }
        }

        private void leftClick() {
            Minecraft mc = FMLClientHandler.instance().getClient();
            RayTraceResult objectMouseOver = mc.objectMouseOver;
            EntityPlayerSP player = mc.player;
            PlayerControllerMP controller = mc.playerController;
            switch (objectMouseOver.typeOfHit)
            {
                case ENTITY:
                    controller.attackEntity(player, objectMouseOver.entityHit);
                    break;
                case BLOCK:
                    BlockPos blockpos = objectMouseOver.getBlockPos();

                    if (!mc.world.isAirBlock(blockpos))
                    {
                        controller.clickBlock(blockpos, objectMouseOver.sideHit);
                        break;
                    }

                case MISS:
                    net.minecraftforge.common.ForgeHooks.onEmptyLeftClick(player);
            }
        }
    }
}
