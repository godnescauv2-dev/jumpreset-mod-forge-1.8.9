package me.jumpreset;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class CommandJumpReset extends CommandBase {

    @Override
    public String getCommandName() {
        return "jumpreset";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("jr");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/jumpreset [on|off|strength <0-100>|chance <0-100>]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            JumpResetMod.enabled = !JumpResetMod.enabled;
        } else {
            switch (args[0].toLowerCase()) {
                case "on":
                    JumpResetMod.enabled = true;
                    break;
                case "off":
                    JumpResetMod.enabled = false;
                    break;
                case "toggle":
                    JumpResetMod.enabled = !JumpResetMod.enabled;
                    break;
                case "strength":
                    if (args.length > 1) {
                        try {
                            double val = Double.parseDouble(args[1]);
                            JumpResetMod.strength = Math.max(0, Math.min(100, val));
                        } catch (NumberFormatException e) {
                            msg(EnumChatFormatting.RED + "Uso: /jumpreset strength <0-100>");
                            return;
                        }
                    }
                    break;
                case "chance":
                    if (args.length > 1) {
                        try {
                            double val = Double.parseDouble(args[1]);
                            JumpResetMod.chance = Math.max(0, Math.min(100, val));
                        } catch (NumberFormatException e) {
                            msg(EnumChatFormatting.RED + "Uso: /jumpreset chance <0-100>");
                            return;
                        }
                    }
                    break;
                default:
                    msg(EnumChatFormatting.RED + "Args: on|off|toggle|strength|chance");
                    return;
            }
        }

        String status = JumpResetMod.enabled
                ? EnumChatFormatting.GREEN + "ATIVADO"
                : EnumChatFormatting.RED + "DESATIVADO";

        msg(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.GOLD + "JR" + EnumChatFormatting.GRAY + "] " +
                EnumChatFormatting.WHITE + "Jump Reset " + status);
        msg(EnumChatFormatting.GRAY + "  └ " + EnumChatFormatting.AQUA + "Strength: " +
                EnumChatFormatting.WHITE + (int) JumpResetMod.strength + "%" +
                EnumChatFormatting.GRAY + " | " + EnumChatFormatting.AQUA + "Chance: " +
                EnumChatFormatting.WHITE + (int) JumpResetMod.chance + "%");
    }

    private void msg(String text) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
