package sylenthuntress.thermia.registry.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sylenthuntress.thermia.registry.ThermiaAttributes;
import sylenthuntress.thermia.temperature.TemperatureHelper;
import sylenthuntress.thermia.temperature.TemperatureManager;
import sylenthuntress.thermia.temperature.TemperatureModifier;

import java.util.ArrayList;
import java.util.stream.Stream;

public class TemperatureCommand {
    private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            name -> Text.stringifiedTranslatable("commands.temperature.failure.entity.invalid", name)
    );
    private static final DynamicCommandExceptionType INVALID_MODIFIER_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("commands.temperature.failure.modifier.invalid", id.toString())
    );
    private static final DynamicCommandExceptionType DUPLICATE_MODIFIER_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("commands.temperature.failure.modifier.duplicate", id.toString())
    );
    private static final DynamicCommandExceptionType INVALID_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Text.stringifiedTranslatable("commands.temperature.failure.position.invalid", position)
    );
    private static final DynamicCommandExceptionType UNLOADED_POSITION_EXCEPTION = new DynamicCommandExceptionType(
            position -> Text.stringifiedTranslatable("commands.temperature.failure.position.unloaded", position)
    );

    // Thanks to eggohito for the suggestion on optimizing this!
    public static void register(CommandNode<ServerCommandSource> baseNode) {
        var powerNode = CommandManager.literal("temperature")
                .requires(source -> source.hasPermissionLevel(2))
                .build();

        //  Add the sub-nodes as children of the target selection node
        var targetNode = CommandManager.argument("target", EntityArgumentType.entity())
                .build();
        targetNode.addChild(GetEntityTemperatureNode.get());
        targetNode.addChild(SetTemperatureNode.get());
        targetNode.addChild(AddTemperatureNode.get());
        targetNode.addChild(ModifyTemperatureNode.get());

        //  Add the sub-nodes as children of the position selection node
        var positionNode = CommandManager.argument("position", BlockPosArgumentType.blockPos())
                .build();
        positionNode.addChild(GetPositionTemperatureNode.get());

        //  Add the selection sub-nodes as children of the main node
        powerNode.addChild(targetNode);
        powerNode.addChild(positionNode);

        //  Add alias
        var aliasNode = CommandManager.literal("temp")
                .requires(source -> source.hasPermissionLevel(2))
                .build();
        powerNode.getChildren().forEach(aliasNode::addChild);

        //  Add the main nodes as a child of the base node
        baseNode.addChild(powerNode);
        baseNode.addChild(aliasNode);
    }

    public static class GetEntityTemperatureNode {
        public static LiteralCommandNode<ServerCommandSource> get() {
            return CommandManager.literal("get")
                    .executes(context -> executeCurrent(context.getSource(), EntityArgumentType.getEntity(context, "target"), 1.0F))
                    .then(CommandManager.literal("current")
                            .executes(context -> executeCurrent(context.getSource(), EntityArgumentType.getEntity(context, "target"), 1)).then(CommandManager.argument("scale", FloatArgumentType.floatArg()).executes(context -> executeCurrent(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(CommandManager.literal("unmodified")
                            .executes(context -> executeUnmodified(context.getSource(), EntityArgumentType.getEntity(context, "target"), 1))
                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeUnmodified(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(CommandManager.literal("body")
                            .executes(context -> executeBody(context.getSource(), EntityArgumentType.getEntity(context, "target"), 1))
                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeBody(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(CommandManager.literal("target")
                            .executes(context -> executeTarget(context.getSource(), EntityArgumentType.getEntity(context, "target"), 1))
                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeTarget(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "scale"))))).build();
        }

        private static int executeUnmodified(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(livingTarget);
                source.sendFeedback(
                        () -> Text.translatable(
                                "commands.temperature.get.entity.success",
                                "Unmodified",
                                target.getName(),
                                temperatureManager.getTemperature()
                        ),
                        false
                );
                return (int) ((temperatureManager.getTemperature() * multiplier) * 1000);
            } else
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }

        private static int executeCurrent(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(livingTarget);
                source.sendFeedback(
                        () -> Text.translatable(
                                "commands.temperature.get.entity.success",
                                "Current",
                                target.getName(),
                                temperatureManager.getModifiedTemperature()
                        ),
                        false
                );
                return (int) ((temperatureManager.getModifiedTemperature() * multiplier) * 1000);
            } else
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }

        private static int executeBody(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
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

        private static int executeTarget(ServerCommandSource source, Entity target, float multiplier) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingEntity) {
                double targetTemperature = TemperatureHelper.getTemperatureManager(livingEntity).getTargetTemperature();
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
    }

    public static class SetTemperatureNode {
        public static LiteralCommandNode<ServerCommandSource> get() {
            return CommandManager.literal("set")
                    .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                            .executes(context -> execute(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "value")))).build();
        }

        private static int execute(ServerCommandSource source, Entity target, double value) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(livingTarget);
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
    }

    public static class AddTemperatureNode {
        public static LiteralCommandNode<ServerCommandSource> get() {
            return CommandManager.literal("add")
                    .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                            .executes(context -> execute(context.getSource(), EntityArgumentType.getEntity(context, "target"), FloatArgumentType.getFloat(context, "value")))).build();
        }

        private static int execute(ServerCommandSource source, Entity target, double value) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                TemperatureManager temperatureManager = TemperatureHelper.getTemperatureManager(livingTarget);
                double newTemperature = temperatureManager.modifyTemperature(value);
                source.sendFeedback(
                        () -> Text.translatable(
                                "commands.temperature.change.success",
                                target.getName(),
                                newTemperature - value,
                                newTemperature
                        ),
                        false
                );
                return (int) (newTemperature * 1000);
            } else
                throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }
    }

    public static class ModifyTemperatureNode {
        public static LiteralCommandNode<ServerCommandSource> get() {
            return CommandManager.literal("modifier")
                    .then(CommandManager.literal("add")
                            .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                    .then(CommandManager.argument("value", DoubleArgumentType.doubleArg())
                                            .then(CommandManager.literal("add_value")
                                                    .executes(context -> executeAdd(context.getSource(), EntityArgumentType.getEntity(context, "target"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "value"), TemperatureModifier.Operation.ADD_VALUE)))
                                            .then(CommandManager.literal("add_multiplied_value")
                                                    .executes(context -> executeAdd(context.getSource(), EntityArgumentType.getEntity(context, "target"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "value"), TemperatureModifier.Operation.ADD_MULTIPLIED_VALUE))))))
                    .then(CommandManager.literal("remove")
                            .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                    .suggests((context, builder) -> CommandSource.suggestIdentifiers(streamModifiers(EntityArgumentType.getEntity(context, "target")), builder))
                                    .executes(context -> executeRemove(context.getSource(), EntityArgumentType.getEntity(context, "target"), IdentifierArgumentType.getIdentifier(context, "id")))))
                    .then(CommandManager.literal("value")
                            .then(CommandManager.literal("get")
                                    .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                            .suggests((context, builder) -> CommandSource.suggestIdentifiers(streamModifiers(EntityArgumentType.getEntity(context, "target")), builder))
                                            .executes(context -> executeGet(context.getSource(), EntityArgumentType.getEntity(context, "target"), IdentifierArgumentType.getIdentifier(context, "id"), 1))
                                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                                    .executes(context -> executeGet(context.getSource(), EntityArgumentType.getEntity(context, "target"), IdentifierArgumentType.getIdentifier(context, "id"), FloatArgumentType.getFloat(context, "scale"))))))).build();
        }

        private static int executeRemove(ServerCommandSource source, Entity target, Identifier id) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                if (TemperatureHelper.getTemperatureManager(livingTarget).getTemperatureModifiers().removeModifier(id)) {
                    source.sendFeedback(
                            () -> Text.stringifiedTranslatable(
                                    "commands.temperature.modifier.remove.success",
                                    id.toString(),
                                    target.getName()
                            ),
                            false
                    );
                    return 1;
                }
                throw INVALID_MODIFIER_EXCEPTION.create(id);
            }
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }

        private static Stream<Identifier> streamModifiers(Entity target) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                ArrayList<TemperatureModifier> temperatureModifiers = TemperatureHelper.getTemperatureManager(livingTarget).getTemperatureModifiers().getList();
                return temperatureModifiers.stream().map(TemperatureModifier::id);
            }
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }


        private static int executeGet(ServerCommandSource source, Entity target, Identifier id, double scale) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                TemperatureModifier modifier = TemperatureHelper.getTemperatureManager(livingTarget).getTemperatureModifiers().getModifier(id);
                if (modifier != null) {
                    source.sendFeedback(
                            () -> Text.stringifiedTranslatable(
                                    "commands.temperature.modifier.get.success",
                                    id.toString(),
                                    target.getName(),
                                    modifier.value()
                            ),
                            false
                    );
                    return (int) ((modifier.value() * scale) * 1000);
                }
                throw INVALID_MODIFIER_EXCEPTION.create(id);
            }
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }

        private static int executeAdd(ServerCommandSource source, Entity target, Identifier id, double value, TemperatureModifier.Operation operation) throws CommandSyntaxException {
            if (target instanceof LivingEntity livingTarget) {
                if (TemperatureHelper.getTemperatureManager(livingTarget).getTemperatureModifiers().addModifier(new TemperatureModifier(id, value, operation))) {
                    source.sendFeedback(
                            () -> Text.stringifiedTranslatable(
                                    "commands.temperature.modifier.add.success",
                                    id.toString(),
                                    target.getName()
                            ),
                            false
                    );
                    return 1;
                }
                throw DUPLICATE_MODIFIER_EXCEPTION.create(id);
            }
            throw ENTITY_FAILED_EXCEPTION.create(target.getName());
        }
    }

    public static class GetPositionTemperatureNode {
        public static LiteralCommandNode<ServerCommandSource> get() {
            return CommandManager.literal("get")
                    .executes(context -> executeAmbient(context.getSource(), BlockPosArgumentType.getBlockPos(context, "position"), 1.0F))
                    .then(CommandManager.literal("ambient")
                            .executes(context -> executeAmbient(context.getSource(), BlockPosArgumentType.getBlockPos(context, "position"), 1))
                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeAmbient(context.getSource(), BlockPosArgumentType.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale")))))
                    .then(CommandManager.literal("regional")
                            .executes(context -> executeRegional(context.getSource(), BlockPosArgumentType.getBlockPos(context, "position"), 1))
                            .then(CommandManager.argument("scale", FloatArgumentType.floatArg())
                                    .executes(context -> executeAmbient(context.getSource(), BlockPosArgumentType.getBlockPos(context, "position"), FloatArgumentType.getFloat(context, "scale"))))).build();
        }

        private static int executeRegional(ServerCommandSource source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
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

        private static int executeAmbient(ServerCommandSource source, BlockPos blockPos, float multiplier) throws CommandSyntaxException {
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
    }
}
