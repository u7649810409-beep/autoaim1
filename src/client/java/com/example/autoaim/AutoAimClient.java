package com.example.autoaim;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AutoAimClient implements ClientModInitializer {

    // ---- Einstellungen ----
    private static final double RANGE = 3.0;
    private static final float SMOOTHING = 1.0f;
    private static final boolean TARGET_HOSTILE = true;
    private static final boolean TARGET_PLAYERS = false;

    private static KeyBinding toggleKey;
    private static KeyBinding enableKey;
    private static KeyBinding disableKey;
    private static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.autoaim"
        ));

        enableKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.enable",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.autoaim"
        ));

        disableKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoaim.disable",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.autoaim"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            setEnabled(client, !enabled);
        }
        while (enableKey.wasPressed()) {
            setEnabled(client, true);
        }
        while (disableKey.wasPressed()) {
            setEnabled(client, false);
        }

        if (!enabled || client.player == null || client.world == null || client.currentScreen != null) return;

        PlayerEntity player = client.player;
        Vec3d eyes = player.getEyePos();

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        Box searchBox = player.getBoundingBox().expand(RANGE + 1.0);
        for (LivingEntity e : client.world.getEntitiesByClass(LivingEntity.class, searchBox, this::isValidTarget)) {
            if (e == player) continue;
            double d = distanceToHitbox(eyes, e.getBoundingBox());
            if (d <= RANGE && d < bestDist) {
                bestDist = d;
                best = e;
            }
        }

        if (best == null) return;

        Vec3d target = best.getBoundingBox().getCenter();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        float yawDiff = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        float pitchDiff = targetPitch - player.getPitch();

        player.setYaw(player.getYaw() + yawDiff * SMOOTHING);
        player.setPitch(MathHelper.clamp(player.getPitch() + pitchDiff * SMOOTHING, -90f, 90f));
    }

    private void setEnabled(MinecraftClient client, boolean value) {
        enabled = value;
        if (client.player != null) {
            client.player.sendMessage(Text.literal("AutoAim: " + (enabled ? "AN" : "AUS")), true);
        }
    }

    private boolean isValidTarget(LivingEntity e) {
        if (!e.isAlive()) return false;
        if (TARGET_HOSTILE && e instanceof HostileEntity) return true;
        return TARGET_PLAYERS && e instanceof PlayerEntity;
    }

    private static double distanceToHitbox(Vec3d p, Box box) {
        double cx = MathHelper.clamp(p.x, box.minX, box.maxX);
        double cy = MathHelper.clamp(p.y, box.minY, box.maxY);
        double cz = MathHelper.clamp(p.z, box.minZ, box.maxZ);
        return p.distanceTo(new Vec3d(cx, cy, cz));
    }
}
