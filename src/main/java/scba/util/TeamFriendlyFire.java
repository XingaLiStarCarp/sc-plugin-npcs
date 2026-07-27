package scba.util;

import minecraft.extended.entity.GeneralHumanoidMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "scba", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TeamFriendlyFire {

    /**
     * NPC 加入世界时，如果主人在线则立即入队
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof GeneralHumanoidMob npc)) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }

        CompoundTag persistentData = npc.getPersistentData();
        if (!persistentData.hasUUID("OwnerUUID")) {
            return;
        }
        UUID ownerUUID = persistentData.getUUID("OwnerUUID");

        ServerLevel level = (ServerLevel) event.getLevel();
        Player owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner == null) {
            // 主人不在线，先不入队，等玩家上线时再处理
            return;
        }

        joinTeam(npc, owner, level);
    }

    /**
     * 玩家上线时，将所有属于该玩家的 NPC 加入队伍
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player owner = event.getEntity();
        if (owner.level().isClientSide()) {
            return;
        }

        ServerLevel level = (ServerLevel) owner.level();
        UUID ownerUUID = owner.getUUID();
        String teamName = "scba_" + ownerUUID.toString().substring(0, 8);

        // 先确保队伍存在
        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setAllowFriendlyFire(true);
        }

        // 玩家入队
        PlayerTeam oldTeam = scoreboard.getPlayersTeam(owner.getScoreboardName());
        if (oldTeam != null && !oldTeam.getName().equals(teamName)) {
            scoreboard.removePlayerFromTeam(owner.getScoreboardName(), oldTeam);
        }
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);

        // 遍历所有已加载的实体，找到该玩家的 NPC 并入队
        for (var entity : level.getAllEntities()) {
            if (entity instanceof GeneralHumanoidMob npc) {
                CompoundTag data = npc.getPersistentData();
                if (data.hasUUID("OwnerUUID") && data.getUUID("OwnerUUID").equals(ownerUUID)) {
                    scoreboard.addPlayerToTeam(npc.getStringUUID(), team);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource() == null) {
            return;
        }

        Entity attacker = resolveAttacker(event);
        LivingEntity target = event.getEntity();
        if (attacker == null || target == null) return;

        // 只拦截 NPC 攻击者
        if (!(attacker instanceof GeneralHumanoidMob)) {
            return;
        }

        if (isSameTeam(attacker, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof GeneralHumanoidMob npc)) {
            return;
        }

        CompoundTag persistentData = npc.getPersistentData();
        if (!persistentData.hasUUID("OwnerUUID")) {
            return;
        }

        UUID ownerUUID = persistentData.getUUID("OwnerUUID");
        String teamName = "scba_" + ownerUUID.toString().substring(0, 8);

        Scoreboard scoreboard = ((ServerLevel) level).getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            return;
        }

        scoreboard.removePlayerFromTeam(npc.getStringUUID(), team);

        // 如果队伍空了（只剩名字，没有成员），清理队伍
        if (team.getPlayers().isEmpty()) {
            try {
                scoreboard.removePlayerTeam(team);
            } catch (Exception e) {
                // 如果 API 不匹配，忽略
            }
        }
    }

    private static void joinTeam(GeneralHumanoidMob npc, Player owner, ServerLevel level) {
        UUID ownerUUID = owner.getUUID();
        String teamName = "scba_" + ownerUUID.toString().substring(0, 8);
        Scoreboard scoreboard = level.getScoreboard();

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setAllowFriendlyFire(true);
        }

        PlayerTeam oldTeam = scoreboard.getPlayersTeam(owner.getScoreboardName());
        if (oldTeam != null && !oldTeam.getName().equals(teamName)) {
            scoreboard.removePlayerFromTeam(owner.getScoreboardName(), oldTeam);
        }
        scoreboard.addPlayerToTeam(owner.getScoreboardName(), team);
        scoreboard.addPlayerToTeam(npc.getStringUUID(), team);
    }

    private static Entity resolveAttacker(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity) {
            return attacker;
        }

        Entity direct = event.getSource().getDirectEntity();
        if (direct instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof LivingEntity) {
                return owner;
            }
        }

        if (direct != null && direct.getClass().getName().contains("EntityKineticBullet")) {
            Entity shooter = invokeMethod(direct, "getShooter");
            if (shooter instanceof LivingEntity) {
                return shooter;
            }
            Entity owner = invokeMethod(direct, "getOwner");
            if (owner instanceof LivingEntity) {
                return owner;
            }
        }

        return attacker;
    }

    private static Entity invokeMethod(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            Object result = method.invoke(obj);
            if (result instanceof Entity) {
                return (Entity) result;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean isSameTeam(Entity a, Entity b) {
        if (a == null || b == null) return false;
        return a.getTeam() != null && a.getTeam().equals(b.getTeam());
    }
}