package org.redmc.redmc.mixin;

import org.redmc.redmc.RedMCConstants;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Shadow public abstract void connect(String address, int port, ClientLoginPacketListener listener);

    @Shadow public abstract void send(Packet<?> packet);

    @Shadow public abstract void send(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush);

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", shift = At.Shift.BEFORE),
            cancellable = true)
    private void onHandlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        // cancel null-action team packets
        if (packet instanceof TeamS2CPacket teamPacket) {
            if (teamPacket.getPlayerListOperation() == null) {
                ci.cancel();
            }
        }

        // spoof resource pack acceptance
        if (packet instanceof ResourcePackSendS2CPacket resourcePackPacket) {
            ci.cancel();
            ClientConnection connection = (ClientConnection) (Object) this;
            connection.send(new ResourcePackStatusC2SPacket(
                    resourcePackPacket.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED
            ), null, true);
            connection.send(new ResourcePackStatusC2SPacket(
                    resourcePackPacket.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED
            ), null, true);
        }
    }

    @Inject(at = @At("HEAD"),
            method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V",
            cancellable = true)
    public void onSendPacketHead(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        // spoof client information
        if (packet instanceof CustomPayloadC2SPacket customPayloadPacket) {
            Identifier id = customPayloadPacket.payload().getId().id();

            // block channels
            for (String channel : RedMCConstants.REDMC_CLIENT_BLOCKED_CHANNELS) {
                if (id.toString().toLowerCase().contains(channel.toLowerCase())) {
                    ci.cancel();
                }
            }

            // spoof client brand
            if (id.equals(BrandCustomPayload.ID.id())) {
                CustomPayloadC2SPacket spoofed = new CustomPayloadC2SPacket(new BrandCustomPayload(
                        RedMCConstants.REDMC_CLIENT_BRAND
                ));

                // avoid infinite send() recursion
                ((ClientConnection) (Object) this).send(spoofed, null, true);
                ci.cancel();
            }
        }
    }
}
