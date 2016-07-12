package au.id.rleach.flardball;

import au.id.rleach.flardball.worldgen.Setup;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.*;

@Plugin( id = au.id.rleach.flardball.Plugin.ID,
         name = au.id.rleach.flardball.Plugin.NAME,
         version = au.id.rleach.flardball.Plugin.VERSION,
         description = au.id.rleach.flardball.Plugin.DESCRIPTION
)
public class FlardBall {
    @Inject
    @ConfigDir(sharedRoot = true)
    private Path configDir;

    @Inject
    private PluginContainer container;

    private CommentedConfigurationNode configNode;
    private FlardBallConfig flardBallConfig;
    private PermissionService permissionService;
    private Optional<CommandMapping> spawnBall;
    private Optional<CommandMapping> deleteBall;
    private Map<UUID, Location<World>> ballSpawns = Maps.newLinkedHashMap();

    @Listener
    public void construct(GameConstructionEvent constructionEvent) {
        Sponge.getEventManager().registerListeners(this, new Setup());
    }

    @Listener
    public void onInit(GameInitializationEvent event){
        permissionService = Sponge.getServiceManager().provide(PermissionService.class).get();
        setup();
    }

    private void setup(){
        configNode = ConfigLoader.loadConfigUnchecked("flardball.conf", configDir, container);
        try {
            flardBallConfig = configNode.getValue(FlardBallConfig.TYPE);
        } catch (ObjectMappingException e) {
            System.out.print(e);
        }
        spawnBall = Sponge.getCommandManager().register(this, spawnBallCmd(), "spawnball");
        deleteBall = Sponge.getCommandManager().register(this, deleteBallCmd(), "delball");
    }

    void registerPD(final String role, final String permission, final String description){
        final Optional<PermissionDescription.Builder> pdbuilder = permissionService.newDescriptionBuilder(this);
        pdbuilder.ifPresent(
                pd->pd.assign(PermissionDescription.ROLE_USER, true).id(permission).description(Text.of(description)).register()
        );
    }

    private void removeMapping(final Optional<CommandMapping> mapping){
        mapping.ifPresent(
            Sponge.getCommandManager()::removeMapping
        );
    }

    private boolean spawnBall(Location<World> location) throws CommandException {
        if(ballSpawns.containsKey(location)) throw new CommandException(Text.of("BallSpawn already exists"));
        Optional<Entity> ball = location.getExtent().createEntity(EntityTypes.SHEEP, location.getPosition());
        if(ball.isPresent()) {
            Entity b = ball.get();
            ballSpawns.put(b.getUniqueId(), location);
            b.offer(Keys.INVULNERABILITY_TICKS, Integer.MAX_VALUE);
            b.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN,"Ball"));
            location.getExtent().spawnEntity(b, Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()).build());
        }
        return ball.isPresent();
    }

    private boolean deleteBall(Entity entity) throws CommandException {
        Location<World> loc = ballSpawns.remove(entity.getUniqueId());
        if(loc!=null){
            entity.remove();
            return true;
        }
        throw new CommandException(Text.of("Entity given was not a valid ball"));
    }

    private void listBalls(CommandSource source){
        PaginationList.Builder pagination = createPagination(source);
        pagination.title(Text.of("Ball Spawns"))
                .padding(Text.of(" "))
                .contents(toCommandTexts(ballSpawns))
                .build()
                .sendTo(source);
    }

    private Iterable<Text> toCommandTexts(Map<UUID, Location<World>> ballSpawns) {
        Collection<Text> out = Lists.newLinkedList();
        ballSpawns.forEach((uuid, loc) -> out.add(toLine(uuid, loc)));
        return out;
    }

    private Text toLine(UUID uuid, Location<World> loc) {
        Text.Builder out = Text.builder();
        out.style(TextStyles.UNDERLINE).color(TextColors.RED)
        .onHover(TextActions.showText(Text.of("/delball "+uuid)))
        .onClick(TextActions.runCommand("/delball "+uuid))
        .append(Text.of("Delete "+loc, " ", uuid));
        return out.build();
    }

    private CommandSpec spawnBallCmd(){
        LiteralText locationText = Text.of("location");
        return CommandSpec.builder()
                .description(Text.of("Create a FlardBall spawn point"))
                .arguments(GenericArguments.location(locationText))
                .permission("flardball.spawnball")
                .executor((src, args)->{
                    Optional<Location<World>> locOpt = args.getOne(locationText);
                    Location<World> loc = locOpt.orElseThrow(() -> new CommandException(Text.of("Invalid Location")));
                    boolean out = spawnBall(loc);
                    if(out) return CommandResult.success();
                    else return CommandResult.empty();
                })
                .build();
    }
    private CommandSpec deleteBallCmd(){
        Text uuidText = Text.of("uuid");
        return CommandSpec.builder()
                .description(Text.of("Delete a FlardBall spawn point"))
                .arguments(GenericArguments.optional(GenericArguments.entity(uuidText)))
                .executor((src, args)->{
                    Optional<Entity> entity = args.getOne(uuidText);
                    if(entity.isPresent()){
                        boolean out = deleteBall(entity.get());
                        if(!out){
                            return CommandResult.empty();
                        } else {
                            return CommandResult.success();
                        }
                    } else {
                        listBalls(src);
                        return CommandResult.success();
                    }
                })
                .permission("flardball.delete")
                .build();
    }

    private PaginationList.Builder createPagination(CommandSource src) {
        PaginationList.Builder builder = Sponge.getGame().getServiceManager().provide(PaginationService.class).get().builder();
        return builder;
    }
}
