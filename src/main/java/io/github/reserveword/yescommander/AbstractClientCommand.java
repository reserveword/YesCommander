package io.github.reserveword.yescommander;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class AbstractClientCommand implements IClientCommand{
    public AbstractClientCommand(String name, String usage, List<String> alias, Function<String[],Void> func) {
        this.name = name;
        this.usage = usage;
        this.alias = alias;
        this.func = func;
    }

    public AbstractClientCommand(String name, String usage, List<String> alias) {
        this.name = name;
        this.usage = usage;
        this.alias = alias;
        this.func = null;
    }

    public final String name,usage;
    public final List<String> alias;
    public final Function<String[],Void> func;

    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return usage;
    }

    @Override
    public List<String> getAliases() {
        return alias;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (func == null) return;
        try {
            func.apply(args);
        } catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return new ArrayList<>(0);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }
}
