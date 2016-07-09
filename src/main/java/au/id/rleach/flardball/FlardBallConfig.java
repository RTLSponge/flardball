package au.id.rleach.flardball;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class FlardBallConfig {

    public static final TypeToken<FlardBallConfig> TYPE = TypeToken.of(FlardBallConfig.class);
}