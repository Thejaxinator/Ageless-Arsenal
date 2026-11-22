package com.jax.destiny2mc.client.armor;

import java.util.Set;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.ClientHooks;

/** Native ModelPart fixture. A running client's equivalent operation is performed by
 * IClientItemExtensions.getGenericArmorModel after the armor renderer sets slot visibility. */
public final class ArmorGeometryCheck {
    public static void main(String[] args) {
        var vanilla = new HumanoidModel<LivingEntity>(HumanoidModel.createMesh(new CubeDeformation(1),0).getRoot().bake(64,32));
        vanilla.head.yRot = .64f;
        vanilla.body.xRot = .47f;
        vanilla.rightArm.xRot = -.88f;
        vanilla.leftLeg.xRot = .52f;
        vanilla.rightLeg.xRot = -.52f;
        int children=0;
        for (String id : ArmorGeometry.ITEMS) {
            var root=ArmorGeometry.layer(id).bakeRoot();
            var model=new HumanoidModel<LivingEntity>(root);
            vanilla.setAllVisible(false);
            vanilla.body.visible=true; vanilla.leftArm.visible=true; vanilla.rightArm.visible=true;
            ClientHooks.copyModelProperties(vanilla,model);
            if(model.head.yRot!=.64f || model.rightArm.xRot!=-.88f || model.leftLeg.xRot!=.52f ||
                model.body.xRot!=.47f || model.rightLeg.xRot!=-0.52f ||
                model.head.visible || !model.body.visible || !model.leftArm.visible || !model.rightArm.visible)throw new AssertionError(id);
            int count=(int)root.getAllParts().filter(p->!p.isEmpty()).count();
            if(count<6)throw new AssertionError("Missing shell geometry for "+id);
            children+=count;
            System.out.println(id+" "+count+" posed parts");
        }
        if(ArmorGeometry.ITEMS.length!=15 || children!=283)throw new AssertionError(children);
        System.out.println("ARMOR POSE PASS 15 native mesh layers; 283 posed parts, parent visibility and animation preserved.");
    }
}
