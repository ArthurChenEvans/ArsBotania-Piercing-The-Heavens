package com.arthurchenevans.arsbotania.client;

import com.arthurchenevans.arsbotania.ArsBotania;
import com.arthurchenevans.arsbotania.entity.MichaelEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;

/** Michael: a player model in his skin, with Botania's Flügel Tiara wings and halo. */
public class MichaelRenderer extends HumanoidMobRenderer<MichaelEntity, PlayerModel<MichaelEntity>> {
    private static final ResourceLocation SKIN = ArsBotania.prefix("textures/entity/michael.png");

    public MichaelRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        addLayer(new Wings(this));
    }

    @Override
    public ResourceLocation getTextureLocation(MichaelEntity entity) {
        return SKIN;
    }

    /** The Flügel wings, drawn by Botania's own tiara renderer. */
    private static final class Wings extends RenderLayer<MichaelEntity, PlayerModel<MichaelEntity>> {
        private static final FlugelTiaraItem.Renderer FLUGEL = new FlugelTiaraItem.Renderer();

        Wings(RenderLayerParent<MichaelEntity, PlayerModel<MichaelEntity>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack ms, MultiBufferSource buffers, int light, MichaelEntity michael, float limbSwing,
                           float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            ItemStack tiara = new ItemStack(BotaniaItems.FLUGEL_TIARA);
            FlugelTiaraItem.setVariant(tiara, 1); // Flügel wings, with the halo
            FLUGEL.doRender((HumanoidModel<?>) getParentModel(), tiara, michael, ms, buffers, light, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
