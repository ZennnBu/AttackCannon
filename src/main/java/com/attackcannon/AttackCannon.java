
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

    @Override
    public void onEnable() {
        getLogger().info("AttackCannon enabled!");
    }

    @Override
    public void onDisable() {
        if (attackTask != null) {
            attackTask.cancel();
        }
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
            player.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("here")) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /attack here");
            return true;
        }

        if (attackRunning) {
            player.sendMessage(ChatColor.RED + "An attack is already running!");
            return true;
        }

        startAttack(player);

        return true;
    }

    private void startAttack(Player player) {

        World world = player.getWorld();
        Location center = player.getLocation().clone();

        int totalTnt = 1000;
        int totalStrikes = 5;
        int tntPerStrike = totalTnt / totalStrikes;

        int radius = 64;
        int spawnHeight = 50;

        attackRunning = true;

        player.sendMessage(
                ChatColor.RED + "" + ChatColor.BOLD
                        + "ATTACK CANNON ACTIVATED!"
        );

        world.playSound(
                center,
                org.bukkit.Sound.ENTITY_WITHER_SPAWN,
                1.0f,
                0.7f
        );

        Random random = new Random();

        final int[] strikeNumber = {0};

        attackTask = Bukkit.getScheduler().runTaskTimer(
                this,
                new Runnable() {

                    @Override
                    public void run() {

                        if (strikeNumber[0] >= totalStrikes) {
                            attackTask.cancel();
                            attackTask = null;
                            attackRunning = false;

                            player.sendMessage(
                                    ChatColor.GREEN
                                            + "AttackCannon sequence completed!"
                            );

                            return;
                        }

                        int currentStrike = strikeNumber[0];

                        Location strikeCenter = center.clone();

                        // First strike at the target location.
                        // Four additional strikes are placed in a line.
                        if (currentStrike > 0) {
                            strikeCenter.add(
                                    currentStrike * 32,
                                    0,
                                    0
                            );
                        }

                        strikeCenter.setY(
                                Math.min(
                                        world.getMaxHeight() - 10,
                                        center.getY() + spawnHeight
                                )
                        );

                        player.sendMessage(
                                ChatColor.RED
                                        + "Strike "
                                        + (currentStrike + 1)
                                        + "/5 incoming!"
                        );

                        spawnStrike(
                                world,
                                strikeCenter,
                                radius,
                                tntPerStrike,
                                random
                        );

                        strikeNumber[0]++;
                    }
                },
                0L,
                100L
        );
    }

    private void spawnStrike(
            World world,
            Location center,
            int radius,
            int amount,
            Random random
    ) {

        final int[] spawned = {0};

        Bukkit.getScheduler().runTaskTimer(
                this,
                new Runnable() {

                    @Override
                    public void run() {

                        int batch = Math.min(
                                10,
                                amount - spawned[0]
                        );

                        for (int i = 0; i < batch; i++) {

                            double angle =
                                    random.nextDouble()
                                            * Math.PI * 2;

                            double distance =
                                    Math.sqrt(random.nextDouble())
                                            * radius;

                            double x =
                                    center.getX()
                                            + Math.cos(angle)
                                            * distance;

                            double z =
                                    center.getZ()
                                            + Math.sin(angle)
                                            * distance;

                            Location spawnLocation =
                                    new Location(
                                            world,
                                            x,
                                            center.getY(),
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

                            // Slow initial fall.
                            tnt.setVelocity(
                                    new Vector(0, -0.2, 0)
                            );

                            // Keep explosions from damaging blocks.
                            tnt.setBlockDamage(false);

                            spawned[0]++;
                        }

                        if (spawned[0] >= amount) {
                            Bukkit.getScheduler()
                                    .cancelTask(
                                            Bukkit.getScheduler()
                                                    .getCurrentTaskId()
                                    );
                        }
                    }
                },
                0L,
                1L
        );
    }
          }
