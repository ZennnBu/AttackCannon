
package com.attackcannon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

public final class AttackCannon extends JavaPlugin {

    private BukkitTask attackTask;
    private boolean attackRunning = false;

    private static final int TOTAL_STRIKES = 5;
    private static final int TNT_PER_STRIKE = 200;
    private static final int TOTAL_TNT = 1000;

    private static final int RADIUS = 64;
    private static final int TNT_PER_TICK = 10;
    private static final int STRIKE_DELAY = 100;

    @Override
    public void onEnable() {
        getLogger().info("AttackCannon enabled!");
    }

    @Override
    public void onDisable() {
        if (attackTask != null) {
            attackTask.cancel();
        }

        attackRunning = false;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("attackcannon.use")) {
            player.sendMessage(
                    ChatColor.RED + "You don't have permission."
            );
            return true;
        }

        if (args.length != 1
                || !args[0].equalsIgnoreCase("here")) {

            player.sendMessage(
                    ChatColor.YELLOW + "Usage: /attack here"
            );
            return true;
        }

        if (attackRunning) {
            player.sendMessage(
                    ChatColor.RED + "An attack is already running!"
            );
            return true;
        }

        startAttack(player);
        return true;
    }

    private void startAttack(Player player) {

        World world = player.getWorld();
        Location target = player.getLocation().clone();

        attackRunning = true;

        int[] strikeIndex = {0};
        int[] spawnedThisStrike = {0};
        int[] delayTicks = {0};

        Random random = new Random();

        player.sendMessage(
                ChatColor.RED + "" + ChatColor.BOLD
                        + "ATTACK CANNON ACTIVATED!"
        );

        player.sendMessage(
                ChatColor.YELLOW
                        + "5 strikes incoming!"
        );

        world.playSound(
                target,
                org.bukkit.Sound.ENTITY_WITHER_SPAWN,
                1.0f,
                0.7f
        );

        attackTask = Bukkit.getScheduler().runTaskTimer(
                this,
                () -> {

                    if (strikeIndex[0] >= TOTAL_STRIKES) {

                        attackTask.cancel();
                        attackTask = null;
                        attackRunning = false;

                        player.sendMessage(
                                ChatColor.GREEN
                                        + "AttackCannon completed!"
                        );

                        return;
                    }

                    if (delayTicks[0] > 0) {
                        delayTicks[0]--;
                        return;
                    }

                    if (spawnedThisStrike[0] == 0) {

                        player.sendMessage(
                                ChatColor.RED
                                        + "Strike "
                                        + (strikeIndex[0] + 1)
                                        + "/5 incoming!"
                        );
                    }

                    Location strikeCenter = target.clone();

                    // Strike 1 is at the target.
                    // Strikes 2-5 form a straight line.
                    int[] offsets = {
                            0, 32, 64, 96, 128
                    };

                    strikeCenter.add(
                            offsets[strikeIndex[0]],
                            0,
                            0
                    );

                    strikeCenter.setY(
                            Math.min(
                                    world.getMaxHeight() - 10,
                                    target.getY() + 50
                            )
                    );

                    int remaining =
                            TNT_PER_STRIKE
                                    - spawnedThisStrike[0];

                    int batch = Math.min(
                            TNT_PER_TICK,
                            remaining
                    );

                    for (int i = 0; i < batch; i++) {

                        double angle =
                                random.nextDouble()
                                        * Math.PI * 2;

                        double distance =
                                Math.sqrt(random.nextDouble())
                                        * RADIUS;

                        double x =
                                strikeCenter.getX()
                                        + Math.cos(angle)
                                        * distance;

                        double z =
                                strikeCenter.getZ()
                                        + Math.sin(angle)
                                        * distance;

                        Location spawnLocation =
                                new Location(
                                        world,
                                        x,
                                        strikeCenter.getY(),
                                        z
                                );

                        TNTPrimed tnt =
                                world.spawn(
                                        spawnLocation,
                                        TNTPrimed.class
                                );

                        tnt.setFuseTicks(80);
                        tnt.setYield(4.0f);
                        tnt.setIsIncendiary(false);

                        tnt.setVelocity(
                                new Vector(0, -0.2, 0)
                        );


                        spawnedThisStrike[0]++;
                    }

                    if (spawnedThisStrike[0]
                            >= TNT_PER_STRIKE) {

                        strikeIndex[0]++;
                        spawnedThisStrike[0] = 0;

                        if (strikeIndex[0]
                                < TOTAL_STRIKES) {

                            delayTicks[0] = STRIKE_DELAY;
                        }
                    }

                },
                0L,
                1L
        );
    }
                }
