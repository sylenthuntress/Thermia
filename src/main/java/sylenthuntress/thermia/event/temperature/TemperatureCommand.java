package sylenthuntress.thermia.event.temperature;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sylenthuntress.thermia.access.temperature.LivingEntityAccess;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;

public class TemperatureCommand implements CommandRegistrationCallback {
    private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            name -> Text.stringifiedTranslatable("commands.temperature.failure.entity.invalid", name)
    );
    private static final DynamicCommandExceptionType INVALID_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Text.stringifiedTranslatable("commands.temperature.failure.position.invalid", position)
    );
    private static final DynamicCommandExceptionType UNLOADED_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Text.stringifiedTranslatable("commands.temperature.failure.position.unloaded", position)
    );

    private static int executeValueGetCurrent(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingTarget) {
            TemperatureManager temperatureManager = ((LivingEntityAccess) livingTarget).thermia$getTemperatureManager();
            source.sendFeedback(
                    () -> Text.translatable(
                            "commands.temperature.get.entity.success",
                            "Current",
                            target.getName(),
                            temperatureManager.getTemperature()
                    ),
                    false
            );
            return (int) ((temperatureManager.getTemperature() * multiplier) * 1000);
        } else
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
    }

    private static int executeValueGetBody(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingTarget) {
            double bodyTemperature = livingTarget.getAttributeValue(ThermiaAttributes.BODY_TEMPERATURE);
            source.sendFeedback(
                    () -> Text.translatable(
                            "commands.temperature.get.entity.success",
                            "Body",
                            target.getName(),
                            bodyTemperature
                    ),
                    false
            );
            return (int) ((bodyTemperature * multiplier) * 1000);
        } else
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
    }

    private static int executeValueGetRegional(ServerCommandSource source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
        if (!World.isValid(blockPos))
            throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
        if (!source.getWorld().isPosLoaded(blockPos))
            throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

        double regionalTemperature = TemperatureHelper.getBiomeTemperature(source.getWorld(), blockPos);
        source.sendFeedback(
                () -> Text.translatable(
                        "commands.temperature.get.position.success",
                        "Regional",
                        blockPos.toShortString(),
                        regionalTemperature
                ),
                false
        );
        return (int) ((regionalTemperature * multiplier) * 1000);
    }

    private static int executeValueGetAmbient(ServerCommandSource source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
        if (!World.isValid(blockPos))
            throw INVALID_POSITION_EXCEPTION.create(blockPos.toShortString());
        if (!source.getWorld().isPosLoaded(blockPos))
            throw UNLOADED_POSITION_EXCEPTION.create(blockPos.toShortString());

        double ambientTemperature = TemperatureHelper.getAmbientTemperature(source.getWorld(), blockPos);
        source.sendFeedback(
                () -> Text.translatable(
                        "commands.temperature.get.position.success",
                        "Ambient",
                        blockPos.toShortString(),
                        ambientTemperature
                ),
                false
        );
        return (int) ((ambientTemperature * multiplier) * 1000);
    }

    private static int executeValueGetTarget(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingEntity) {
            double targetTemperature = TemperatureHelper.getTargetTemperature(livingEntity);
            source.sendFeedback(
                    () -> Text.translatable(
                            "commands.temperature.get.entity.success",
                            "Target",
                            target.getName(),
                            targetTemperature
                    ),
                    false
            );
            return (int) ((targetTemperature * multiplier) * 1000);
        } else
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
    }

    private static int executeValueSet(ServerCommandSource source, Entity target, double value) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingTarget) {
            TemperatureManager temperatureManager = ((LivingEntityAccess) livingTarget).thermia$getTemperatureManager();
            source.sendFeedback(
                    () -> Text.translatable(
                            "commands.temperature.change.success",
                            target.getName(),
                            temperatureManager.getTemperature(),
                            value
                    ),
                    false
            );
            return (int) (temperatureManager.setTemperature(value) * 1000);
        } else
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
    }

    private static int executeValueAdd(ServerCommandSource source, Entity target, double value, boolean applyModifiers) throws CommandSyntaxException {
        if (target instanceof LivingEntity livingTarget) {
            TemperatureManager temperatureManager = ((LivingEntityAccess) livingTarget).thermia$getTemperatureManager();
            double temperature = temperatureManager.getTemperature();
            double newTemperature = temperatureManager.modifyTemperature(applyModifiers, value);
            source.sendFeedback(
                    () -> Text.translatable(
                            "commands.temperature.change.success",
                            target.getName(),
                            temperature,
                            newTemperature
                    ),
                    false
            );
            return (int) (newTemperature * 1000);
        } else
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
                CommandManager.literal("temperature")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(
                                CommandManager.argument("target", EntityArgumentType.entity())
                                        .then(
                                                CommandManager.literal("get")
                                                        .executes(
                                                                context -> executeValueGetCurrent(
                                                                        context.getSource(),
                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                        1.0F
                                                                )
                                                        )
                                                        .then(
                                                                CommandManager.argument("factor", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> executeValueGetCurrent(
                                                                                        context.getSource(),
                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                )
                                                                        )
                                                                        .then(
                                                                                CommandManager.literal("current")
                                                                                        .executes(
                                                                                                context -> executeValueGetCurrent(
                                                                                                        context.getSource(),
                                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                                )
                                                                                        )
                                                                        )
                                                                        .then(
                                                                                CommandManager.literal("body")
                                                                                        .executes(
                                                                                                context -> executeValueGetBody(
                                                                                                        context.getSource(),
                                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                                )
                                                                                        )
                                                                        )
                                                                        .then(
                                                                                CommandManager.literal("target")
                                                                                        .executes(
                                                                                                context -> executeValueGetTarget(
                                                                                                        context.getSource(),
                                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                                )
                                                                                        )
                                                                        )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("set")
                                                        .then(
                                                                CommandManager.argument("value", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> executeValueSet(
                                                                                        context.getSource(),
                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                        FloatArgumentType.getFloat(context, "value")
                                                                                )
                                                                        )

                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("add")
                                                        .then(
                                                                CommandManager.argument("value", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> executeValueAdd(
                                                                                        context.getSource(),
                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                        FloatArgumentType.getFloat(context, "value"),
                                                                                        false
                                                                                )
                                                                        )
                                                                        .then(
                                                                                CommandManager.argument("apply_modifiers", BoolArgumentType.bool())
                                                                                        .executes(
                                                                                                context -> executeValueAdd(
                                                                                                        context.getSource(),
                                                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                                                        FloatArgumentType.getFloat(context, "value"),
                                                                                                        BoolArgumentType.getBool(context, "apply_modifiers")
                                                                                                )
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                CommandManager.argument("block_pos", BlockPosArgumentType.blockPos())
                                        .then(
                                                CommandManager.literal("get")
                                                        .executes(
                                                                context -> executeValueGetAmbient(
                                                                        context.getSource(),
                                                                        BlockPosArgumentType.getBlockPos(context, "block_pos"),
                                                                        1.0F
                                                                )
                                                        )
                                                        .then(
                                                                CommandManager.argument("factor", FloatArgumentType.floatArg())
                                                                        .executes(
                                                                                context -> executeValueGetAmbient(
                                                                                        context.getSource(),
                                                                                        BlockPosArgumentType.getBlockPos(context, "block_pos"),
                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                )
                                                                        )
                                                                        .then(
                                                                                CommandManager.literal("ambient")
                                                                                        .executes(
                                                                                                context -> executeValueGetAmbient(
                                                                                                        context.getSource(),
                                                                                                        BlockPosArgumentType.getBlockPos(context, "block_pos"),
                                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                                )
                                                                                        )
                                                                        )
                                                                        .then(
                                                                                CommandManager.literal("regional")
                                                                                        .executes(
                                                                                                context -> executeValueGetRegional(
                                                                                                        context.getSource(),
                                                                                                        BlockPosArgumentType.getBlockPos(context, "block_pos"),
                                                                                                        FloatArgumentType.getFloat(context, "factor")
                                                                                                )
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }
}
