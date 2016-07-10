package au.id.rleach.flardball.worldgen;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

public class Setup {

    @Inject Logger logger;

    @Listener
    public void onInit(GameInitializationEvent init){
        Sponge.getRegistry().register(WorldGeneratorModifier.class, new TAAMCMod(new Range(-1, 10)));
    }

    @Listener
    public void startServer(GameStartedServerEvent start){
        Optional<WorldProperties> worldProps = Sponge.getServer().getDefaultWorld();
        worldProps.ifPresent(
                props-> {
                    props.setDifficulty(Difficulties.HARD);
                    props.setGameMode(GameModes.SURVIVAL);
                    props.setHardcore(true);
                    props.setGameRule("disableElytraMovementCheck", "true");
                    props.setGameRule("naturalRegeneration", "false");
                    props.setGameRule("reducedDebigInfo", "true");
                    props.setGameRule("spawnRadius", "0");
                    props.setGameRule("nerfHunger", "true");
                    props.setWorldBorderCenter(0, 0);
                    props.setWorldBorderDamageAmount(1);
                    props.setWorldBorderDamageThreshold(10);
                    props.setWorldBorderTargetDiameter(10000);
                    props.setWorldBorderDiameter(100);
                    props.setWorldBorderWarningTime(20*500);
                    props.setWorldBorderTimeRemaining(20*60*60*24*7);

                    Range spawn = new Range(-1, 10);
                    Range amplified = new Range(10, 20);
                    Range skylands = new Range(20, 30);
                    Range theVoid = new Range(30, Integer.MAX_VALUE);
                }
        );
    }


}
