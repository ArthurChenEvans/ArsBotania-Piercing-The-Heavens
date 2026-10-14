package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.core.divine.WingStyle;
import com.arthurchenevans.arsbotania.event.DivineFlightEvents;
import com.arthurchenevans.arsbotania.item.DivineRobesItem;
import com.mojang.blaze3d.vertex.PoseStack;
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
 * Flügel Tiara renderer; Divine wings are drawn the way Botania draws its Flügel wings.
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

    /** Botania's Flügel wing placement and flap, with the Divine wing art and the Flügel halo. */
    private void renderDivine(AbstractClientPlayer player, ItemStack robes, PoseStack ms, MultiBufferSource buffers, int light, float partialTicks) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(DIVINE_WINGS);
        ClientXplatAbstractions.INSTANCE.markSpriteActive(model.getParticleIcon());
        boolean flying = player.getAbilities().flying;
        float tickTime = player.tickCount + partialTicks;
        float flap = 20 + (Mth.sin(tickTime * (flying ? 0.4f : 0.2f)) + 0.5f) * (flying ? 30 : 5);

        ms.pushPose();
        getParentModel().body.translateAndRotate(ms);
        ms.translate(0, 0.5, 0.2);
        for (int i = 0; i < 2; i++) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(i == 0 ? flap : 180 - flap));
            ms.translate(-1, 0, 0);
            ms.mulPose(Axis.ZP.rotationDegrees(-60));
            ms.scale(1.5F, -1.5F, -1.5F);
            // Full brightness: Divine wings glow.
            Minecraft.getInstance().getItemRenderer().render(robes, ItemDisplayContext.NONE, false, ms, buffers,
                    0xF000F0, OverlayTexture.NO_OVERLAY, model);
            ms.popPose();
        }
        ms.popPose();

        ms.pushPose();
        FlugelTiaraItem.ClientLogic.renderHalo(getParentModel(), ms, buffers, tickTime);
        ms.popPose();
    }
}
