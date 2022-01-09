package com.andrei1058.spigot.sidebar;

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class EightPlayerList extends ScoreboardTeam implements VersionedTabGroup {

    private final SidebarLine prefix;
    private final SidebarLine suffix;
    private final WrappedSidebar sidebar;
    private final String id;
    private EnumNameTagVisibility nameTagVisibility;
    private Player papiSubject = null;

    public EightPlayerList(@NotNull WrappedSidebar sidebar, String identifier, SidebarLine prefix, SidebarLine suffix,
                           PushingRule pushingRule, NameTagVisibility nameTagVisibility) {
        super(null, identifier);
        this.suffix = suffix;
        this.prefix = prefix;
        this.sidebar = sidebar;
        this.setPushingRule(pushingRule);
        this.setNameTagVisibility(nameTagVisibility);
        this.id = identifier;
    }

    @Override
    public void setPrefix(String var0) {
    }

    @Override
    public String getFormattedName(String var0) {
        return prefix.getLine().concat(var0).concat(suffix.getLine());
    }

    @Override
    public String getPrefix() {
        String t = prefix.getLine();
        for (PlaceholderProvider placeholderProvider : sidebar.getPlaceholders()) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }
        if (null != getSubject()) {
            t = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t);
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return t;
    }

    @Override
    public String getSuffix() {
        String t = suffix.getLine();
        for (PlaceholderProvider placeholderProvider : sidebar.getPlaceholders()) {
            if (t.contains(placeholderProvider.getPlaceholder())) {
                t = t.replace(placeholderProvider.getPlaceholder(), placeholderProvider.getReplacement());
            }
        }

        if (null != getSubject()) {
            t = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getSubject(), t);
        }

        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return t;
    }

    @Override
    public void setNameTagVisibility(EnumNameTagVisibility enumNameTagVisibility) {
        nameTagVisibility = enumNameTagVisibility;
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    @Override
    public void add(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 3
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendCreateToPlayer(Player player) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 0);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam);
    }

    public void remove(Player player) {
        // send 4: remove entities from team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 4
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendUserCreateToReceivers(Player player) {
        // send 3: add entities to team
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(
                this, Collections.singleton(player.getName()), 3
        );
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    public void sendUpdateToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 2);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public void sendRemoveToReceivers() {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam(this, 1);
        sidebar.getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().playerConnection.sendPacket(packetPlayOutScoreboardTeam));
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public void setSubject(@javax.annotation.Nullable Player papiSubject) {
        this.papiSubject = papiSubject;
    }

    @Override
    public @org.jetbrains.annotations.Nullable Player getSubject() {
        return papiSubject;
    }

    @Override
    public void setPushingRule(PushingRule rule) {
        // there is no pushing on 1.8
    }

    @Override
    public void setNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = EnumNameTagVisibility.valueOf(nameTagVisibility.toString());
        if (null != id){
            sendUpdateToReceivers();
        }
    }
}
