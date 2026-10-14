package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.DivineWingLayout;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.event.DivineFlightEvents;
import com.arthurchenevans.arsbotania.item.DivineRobesItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import vazkii.botania.client.core.helper.RenderHelper;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.xplat.ClientXplatAbstractions;

/**
 * Draws wings on players wearing Divine robes. Botania's styles go through Botania's own
 * Flügel Tiara renderer; Divine wings are three bigger pairs of recoloured Flügel wings.
 */
public class DivineWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ModelResourceLocation DIVINE_WINGS = ModelResourceLocation.standalone(ArsBotania.prefix("item/divine_wings"));
    private static final FlugelTiaraItem.Renderer BOTANIA_WINGS = new FlugelTiaraItem.Renderer();

    public DivineWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack ms, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing,
                       float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack robes = DivineFlightEvents.wornRobes(player);
        if (robes.isEmpty() || player.isInvisible()) return;

        WingStyle style = DivineRobesItem.wingStyle(robes);
        if (style.isBotaniaWings()) {
            ItemStack tiara = new ItemStack(BotaniaItems.FLUGEL_TIARA);
            FlugelTiaraItem.setVariant(tiara, style.botaniaVariant());
            BOTANIA_WINGS.doRender(getParentModel(), tiara, player, ms, buffers, light, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
        } else if (style == WingStyle.DIVINE) {
            renderDivine(player, robes, ms, buffers, light, partialTicks);
        }
    }

    /** Three pairs of Divine wings, archangel style, then a rainbow version of the Flügel halo. */
    private void renderDivine(AbstractClientPlayer player, ItemStack robes, PoseStack ms, MultiBufferSource buffers, int light, float partialTicks) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(DIVINE_WINGS);
        ClientXplatAbstractions.INSTANCE.markSpriteActive(model.getParticleIcon());
        boolean flying = player.getAbilities().flying;
        float tickTime = player.tickCount + partialTicks;

        for (DivineWingLayout.Pair pair : DivineWingLayout.PAIRS) {
            float flap = DivineWingLayout.flap(pair, tickTime, flying);
            ms.pushPose();
            getParentModel().body.translateAndRotate(ms);
            ms.translate(0, pair.height(), 0.2);
            for (int side = 0; side < 2; side++) {
                ms.pushPose();
                ms.mulPose(Axis.YP.rotationDegrees(side == 0 ? flap : 180 - flap));
                // Botania pivots its 1.5-scale wing one block out; keep the root at the spine as it grows.
                ms.translate(-pair.scale() / 1.5f, 0, 0);
                ms.mulPose(Axis.ZP.rotationDegrees(pair.tilt()));
                ms.scale(pair.scale(), -pair.scale(), -pair.scale());
                // Full brightness: Divine wings glow.
                Minecraft.getInstance().getItemRenderer().render(robes, ItemDisplayContext.NONE, false, ms, buffers,
                        0xF000F0, OverlayTexture.NO_OVERLAY, model);
                ms.popPose();
            }
            ms.popPose();
        }

        ms.pushPose();
        renderRainbowHalo(ms, buffers, tickTime);
        ms.popPose();
    }

    /** Botania's Flügel halo, placed the same way, with each corner a different cycling hue. */
    private void renderRainbowHalo(PoseStack ms, MultiBufferSource buffers, float tickTime) {
        getParentModel().body.translateAndRotate(ms);
        ms.translate(0.2, -0.65, 0);
        ms.mulPose(Axis.ZP.rotationDegrees(30));
        ms.mulPose(Axis.YP.rotationDegrees(tickTime));
        ms.scale(0.75F, -0.75F, -0.75F);

        VertexConsumer buffer = buffers.getBuffer(RenderHelper.HALO);
        Matrix4f mat = ms.last().pose();
        float[][] corners = {{-1F, -1F, 0, 0}, {1F, -1F, 1, 0}, {1F, 1F, 1, 1}, {-1F, 1F, 0, 1}};
        for (int c = 0; c < corners.length; c++) {
            int rgb = Mth.hsvToRgb(DivineWingLayout.haloHue(tickTime, c), 0.55F, 1F);
            buffer.addVertex(mat, corners[c][0], 0, corners[c][1])
                    .setColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255)
                    .setUv(corners[c][2], corners[c][3]);
        }
    }
}
