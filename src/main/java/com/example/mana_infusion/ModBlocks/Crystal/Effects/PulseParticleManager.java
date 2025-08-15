package com.example.mana_infusion.ModBlocks.Crystal.Effects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;


public class PulseParticleManager {
    private static final Map<BlockPos, AnimatedPulse> activePulses = new HashMap<>();

    // Start a new pulse effect at a block position
    public static void startPulse(Level level, BlockPos pos, PulseConfig config) {
        if (level.isClientSide) {
            activePulses.put(pos, new AnimatedPulse(level, pos, config));
        }
    }

    // Call this every tick (in your mod's client tick event)
    public static void tickAllPulses() {
        if (activePulses.isEmpty()) return;

        activePulses.entrySet().removeIf(entry -> {
            AnimatedPulse pulse = entry.getValue();
            pulse.tick();
            boolean finished = pulse.isFinished();
            if (finished) {
            }
            return finished;
        });
    }

    // Debug helper
    public static int getActivePulseCount() {
        return activePulses.size();
    }

    // Configuration for pulse effects
    public static class PulseConfig {
        public float maxRadius = 6.0f;           // Maximum radius in blocks
        public int duration = 60;                // Duration in ticks (3 seconds)
        public ParticleOptions particleType = ParticleTypes.ENCHANT;
        public int particlesPerRing = 24;        // Particles per circle
        public float waveThickness = 1.5f;       // How thick the pulse wave is
        public boolean fadeOut = true;           // Whether particles fade over time
        public float verticalSpread = 0.3f;      // Random vertical offset
        public float particleSpeed = 0.02f;      // How fast particles move outward

        public PulseConfig() {}

        public PulseConfig maxRadius(float radius) { this.maxRadius = radius; return this; }
        public PulseConfig duration(int ticks) { this.duration = ticks; return this; }
        public PulseConfig particle(ParticleOptions particle) { this.particleType = particle; return this; }
        public PulseConfig particleCount(int count) { this.particlesPerRing = count; return this; }
    }
}

// Individual animated pulse instance
class AnimatedPulse {
    private final Level level;
    private final Vec3 center;
    private final PulseParticleManager.PulseConfig config;
    private int currentTick = 0;

    public AnimatedPulse(Level level, BlockPos pos, PulseParticleManager.PulseConfig config) {
        this.level = level;
        this.center = Vec3.atCenterOf(pos);
        this.config = config;
    }

    public void tick() {
        if (!level.isClientSide) return;

        currentTick++;

        // Calculate current pulse progress (0.0 to 1.0)
        float progress = (float) currentTick / config.duration;
        float currentRadius = config.maxRadius * progress;


        // Create the expanding wave effect
        spawnWaveRing(currentRadius, progress);

        // Optional: Add a trailing wave for more dramatic effect
        if (config.waveThickness > 1.0f && currentRadius > 1.0f) {
            float trailRadius = currentRadius - config.waveThickness;
            if (trailRadius > 0) {
                spawnWaveRing(trailRadius, progress, 0.5f); // Dimmer trail
            }
        }
    }

    private void spawnWaveRing(float radius, float progress) {
        spawnWaveRing(radius, progress, 1.0f);
    }

    private void spawnWaveRing(float radius, float progress, float intensityMultiplier) {
        // Adjust particle count based on radius (more particles for larger circles)
        int particleCount = Math.max(8, (int)(config.particlesPerRing * (radius / config.maxRadius)));

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;

            // Calculate position on the circle
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + (level.random.nextDouble() - 0.5) * config.verticalSpread;

            // Calculate outward velocity
            double velX = Math.cos(angle) * config.particleSpeed;
            double velZ = Math.sin(angle) * config.particleSpeed;
            double velY = level.random.nextGaussian() * 0.01; // Small random vertical movement

            // Spawn particle with custom behavior
            spawnCustomParticle(x, y, z, velX, velY, velZ, progress, intensityMultiplier);
        }
    }

    private void spawnCustomParticle(double x, double y, double z, double velX, double velY, double velZ,
                                     float progress, float intensityMultiplier) {

        if (config.particleType == ParticleTypes.DUST) {
            // For dust particles, we can set color
            float red = 0.2f + progress * 0.8f;
            float green = 0.6f - progress * 0.3f;
            float blue = 1.0f - progress * 0.5f;
            float size = 1.0f * intensityMultiplier;

            DustParticleOptions dustOptions = new DustParticleOptions(
                    new Vector3f(red, green, blue), size);
            level.addParticle(dustOptions, x, y, z, velX, velY, velZ);

        } else {
            // For other particle types
            level.addParticle(config.particleType, x, y, z, velX, velY, velZ);
        }
    }

    public boolean isFinished() {
        return currentTick >= config.duration;
    }
}