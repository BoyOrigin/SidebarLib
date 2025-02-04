package com.andrei1058.spigot.sidebar.v1_17_R1;

import com.andrei1058.spigot.sidebar.*;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardScore;
import net.minecraft.world.scores.ScoreboardTeam;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SidebarImpl extends WrappedSidebar {

    public SidebarImpl(
            @NotNull SidebarLine title,
            @NotNull Collection<SidebarLine> lines,
            Collection<PlaceholderProvider> placeholderProvider
    ) {
        super(title, lines, placeholderProvider);
    }

    public ScoreLine createScore(SidebarLine line, int score, String color) {
        return new SidebarImpl.NarniaScoreLine(line, score, color);
    }

    public SidebarObjective createObjective(String name, IScoreboardCriteria iScoreboardCriteria, SidebarLine title, int type) {
        return new NarniaSidebarObjective(name, iScoreboardCriteria, title, type);
    }

    protected class NarniaSidebarObjective extends ScoreboardObjective implements SidebarObjective {

        private SidebarLine displayName;
        private final int type;

        public NarniaSidebarObjective(String name, IScoreboardCriteria criteria, SidebarLine displayName, int type) {
            super(null, name, criteria, new ChatComponentText(name), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
            this.displayName = displayName;
            this.type = type;
        }

        @Override
        public void setTitle(SidebarLine title) {
            this.displayName = title;
            this.sendUpdate();
        }

        @Override
        public void sendCreate(Player player) {

            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 0);
            playerConnection.sendPacket(packetPlayOutScoreboardObjective);
            PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective = new PacketPlayOutScoreboardDisplayObjective(type, this);
            playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective);
            if (getName().equalsIgnoreCase("health")) {
                PacketPlayOutScoreboardDisplayObjective packetPlayOutScoreboardDisplayObjective2 = new PacketPlayOutScoreboardDisplayObjective(0, this);
                playerConnection.sendPacket(packetPlayOutScoreboardDisplayObjective2);
            }

        }

        @Override
        public void sendRemove(Player player) {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 1);
            ((CraftPlayer)player).getHandle().b.sendPacket(packetPlayOutScoreboardObjective);
        }

        @Override
        public IChatBaseComponent getDisplayName() {
            String t = displayName.getLine();
            if (t.length() > 32) {
                t = t.substring(0, 32);
            }
            return new ChatComponentText(t);
        }


        @Override
        public void setDisplayName(IChatBaseComponent var0) {
        }

        @Override
        public void setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay var0) {

        }

        // must be called when updating the name
        public void sendUpdate() {
            PacketPlayOutScoreboardObjective packetPlayOutScoreboardObjective = new PacketPlayOutScoreboardObjective(this, 2);
            getReceivers().forEach(player -> ((CraftPlayer) player).getHandle().b.sendPacket(packetPlayOutScoreboardObjective));
        }
    }

    public class NarniaScoreLine extends ScoreboardScore implements ScoreLine, Comparable<ScoreLine> {

        private int score;
        private String prefix = " ", suffix = "";
        private final TeamLine team;
        private SidebarLine text;

        public NarniaScoreLine(@NotNull SidebarLine text, int score, @NotNull String color) {
            super(null, (ScoreboardObjective) getSidebarObjective(), color);
            this.score = score;
            this.text = text;
            this.team = new TeamLine(color);


            SidebarLine.markHasPlaceholders(text, getPlaceholders());

            //noinspection ResultOfMethodCallIgnored
            setContent(parsePlaceholders(text));
        }

        @Override
        public SidebarLine getLine() {
            return text;
        }

        @Override
        public void setLine(SidebarLine line) {
            this.text = line;
        }

        @Override
        public int getScoreAmount() {
            return score;
        }

        @Override
        public void setScoreAmount(int score) {
            this.setScore(score);
        }

        @Override
        public void sendCreateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public void sendCreate(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team, true);
            conn.sendPacket(packetPlayOutScoreboardTeam);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        @Override
        public void sendRemove(Player player) {
            PlayerConnection conn = ((CraftPlayer) player).getHandle().b;
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            conn.sendPacket(packetPlayOutScoreboardTeam);
            conn.sendPacket(packetPlayOutScoreboardScore);
        }

        public void sendRemoveToAllReceivers() {
            PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.a(team);
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.sendPacket(packetPlayOutScoreboardTeam));
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.b, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), getScoreAmount()
            );
            getReceivers().forEach(p -> ((CraftPlayer) p).getHandle().b.sendPacket(packetPlayOutScoreboardScore));
        }

        public void sendUpdate(Player player) {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            ((CraftPlayer) player).getHandle().b.sendPacket(packetTeamUpdate);
        }

        @Contract(pure = true)
        public boolean setContent(@NotNull String content) {
            if (!getReceivers().isEmpty()) {
                content = SidebarManager.getInstance().getPapiSupport().replacePlaceholders(getReceivers().get(0), content);
            }
            var oldPrefix = this.prefix;
            var oldSuffix = this.suffix;

            if (content.length() > 32) {
                this.prefix = content.substring(0, 32);
                if (this.prefix.charAt(31) == ChatColor.COLOR_CHAR) {
                    this.prefix = content.substring(0, 31);
                    setSuffix(content.substring(31));
                } else {
                    setSuffix(content.substring(32));
                }
            } else {
                this.prefix = content;
                this.suffix = "";
            }
            return !oldPrefix.equals(this.prefix) || !oldSuffix.equals(this.suffix);
        }

        public void setSuffix(@NotNull String secondPart) {
            if (secondPart.isEmpty()) {
                this.suffix = "";
                return;
            }
            secondPart = org.bukkit.ChatColor.getLastColors(this.prefix) + secondPart;
            this.suffix = secondPart.length() > 32 ? secondPart.substring(0, 32) : secondPart;
        }

        public void sendUpdateToAllReceivers() {
            PacketPlayOutScoreboardTeam packetTeamUpdate = PacketPlayOutScoreboardTeam.a(team, false);
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetTeamUpdate));
        }

        public int compareTo(@NotNull ScoreLine o) {
            return Integer.compare(score, o.getScoreAmount());
        }

        @Override
        public void setScore(int score) {
            this.score = score;
            PacketPlayOutScoreboardScore packetPlayOutScoreboardScore = new PacketPlayOutScoreboardScore(
                    ScoreboardServer.Action.a, ((ScoreboardObjective) getSidebarObjective()).getName(), getPlayerName(), score
            );
            getReceivers().forEach(r -> ((CraftPlayer) r).getHandle().b.sendPacket(packetPlayOutScoreboardScore));
        }

        @Override
        public int getScore() {
            return score;
        }

        public void c() {
        }

        @Override
        public void addScore(int i) {
        }

        @Override
        public void incrementScore() {
        }

        public String getColor() {
            return team.getName().charAt(0) == ChatColor.COLOR_CHAR ? team.getName() : ChatColor.COLOR_CHAR + team.getName();
        }

        private class TeamLine extends ScoreboardTeam {

            public TeamLine(String color) {
                super(null, color);
                getPlayerNameSet().add(color);
            }

            @Override
            public IChatBaseComponent getPrefix() {
                return new ChatComponentText(prefix);
            }

            @Override
            public void setPrefix(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public void setSuffix(@Nullable IChatBaseComponent var0) {
            }

            @Override
            public IChatBaseComponent getSuffix() {
                return new ChatComponentText(suffix);
            }

            @Override
            public void setAllowFriendlyFire(boolean var0) {
            }

            @Override
            public void setCanSeeFriendlyInvisibles(boolean var0) {
            }

            @Override
            public void setNameTagVisibility(EnumNameTagVisibility var0) {
            }

            @Override
            public void setCollisionRule(EnumTeamPush var0) {
            }

            @Override
            public void setColor(EnumChatFormat var0) {
            }

            @Override
            public IChatMutableComponent getFormattedName(IChatBaseComponent var0) {
                return new ChatComponentText(prefix + var0 + suffix);
            }
        }
    }
}
