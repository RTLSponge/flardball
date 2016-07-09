package au.id.rleach.flardball;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataBuilder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;


public class BallData {

    public static Key<Value<Location<World>>> SPAWN_LOCATION;
    public static final DataQuery SPAWN_LOCATION_QUERY = DataQuery.of("SPAWN_LOCATION");
    private DataBuilder<Data.Mutable> mutableBuilder;

    @Listener
    public void init(GameInitializationEvent init){
        KeyFactory.makeSingleKey(Location.class, Value.class, SPAWN_LOCATION_QUERY);
        Sponge.getGame().getDataManager().registerBuilder(Data.Mutable.class, mutableBuilder);
    }

    private static final Comparator<Location<World>> COMPARATOR;

    static {
        final Function<Location<World>, UUID> getUuid = a -> a.getExtent().getUniqueId();
        COMPARATOR =
                Comparator.comparing(
                        getUuid
                ).thenComparing(
                        Location::getPosition
                );
    }

    public static class Builder {

        public static class Mutable extends AbstractDataBuilder<Data.Mutable> {

            protected Mutable(Class<Data.Mutable> requiredClass, int supportedVersion) {
                super(requiredClass, supportedVersion);
            }

            @Override protected Optional<Data.Mutable> buildContent(DataView container) throws InvalidDataException {
                return null;
            }
        }

    }


    public static class Data {
        public static class Immutable extends AbstractImmutableSingleData<Location<World>, Immutable, Mutable> {

            public Immutable(Location<World> value) {
                super(value, SPAWN_LOCATION);
            }

            @Override protected ImmutableValue<?> getValueGetter() {
                return Sponge.getRegistry().getValueFactory().createValue(SPAWN_LOCATION, this.value).asImmutable();
            }

            @Override public Mutable asMutable() {
                return new Mutable(this.value);
            }

            @Override public int compareTo(Immutable o) {
                return COMPARATOR.compare(this.value, o.value);
            }

            @Override public int getContentVersion() {
                return 0;
            }
        }


        private static class Mutable extends AbstractSingleData<Location<World>, Mutable, Immutable> {

            public Mutable(Location<World> value) {
                super(value, SPAWN_LOCATION);
            }

            //copy constructor
            private Mutable(Mutable mutable) {
                this(mutable.getValue());
            }


            @Override protected Value<?> getValueGetter() {
                return Sponge.getRegistry().getValueFactory().createValue(SPAWN_LOCATION, this.getValue());
            }

            @Override public Optional<Mutable> fill(DataHolder dataHolder, MergeFunction overlap) {
                //TODO:
                return null;
            }

            @Override public Optional<Mutable> from(DataContainer container) {
                //TODO:
                return null;
            }

            @Override public Mutable copy() {
                return new Mutable(this);
            }

            @Override public Immutable asImmutable() {
                return new Immutable(getValue());
            }

            @Override public int compareTo(Mutable o) {
                return COMPARATOR.compare(getValue(), o.getValue());
            }

            @Override public int getContentVersion() {
                return 0;
            }
        }
    }
}
