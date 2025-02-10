package sylenthuntress.thermia.temperature;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record GrantedThermoregulation(List<String> playerUUIDs) {
    public final static GrantedThermoregulation DEFAULT = new GrantedThermoregulation(List.of());

    public static Codec<GrantedThermoregulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("player_uuids").forGetter(GrantedThermoregulation::playerUUIDs)
    ).apply(instance, GrantedThermoregulation::new));

    public static PacketCodec<ByteBuf, GrantedThermoregulation> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public static GrantedThermoregulation addPlayer(ArrayList<String> playerUUIDs, GameProfile profile) {
        final String uuid = profile.getId().toString();
        if (uuid != null) {
            playerUUIDs.add(uuid);
        }

        return new GrantedThermoregulation(playerUUIDs);
    }
}
