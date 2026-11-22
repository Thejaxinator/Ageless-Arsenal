package com.jax.destiny2mc.tenno.client;

import com.jax.destiny2mc.binder.client.CombatHudLayout;
import com.jax.destiny2mc.tenno.*;
import com.jax.destiny2mc.weapon.GunDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** The same lower-left layout owns ammo and Binder icons. The four ordinary themes
 * preserve centered ability slots while Minimal and BL2 keep the existing flush row. */
public final class TennoWeaponHud {
    public static void draw(GuiGraphics g,ItemStack held,long now,CombatHudLayout layout,int theme) {
        var gun=(TennoGunItem)held.getItem();var spec=gun.profile();
        var stats=TennoGunStats.of(Minecraft.getInstance().player,held);
        int mag=TennoGunItem.loaded(held,stats), width=CombatHudLayout.ammoWidth(theme);
        int accent=switch(theme){case 0->0xffe5c076;case 1->0xff91ad71;case 2->0xffdbdbdb;case 3->0xffffcf38;case 4->0xfffb9154;default->0xff69d6e5;};
        var pose=g.pose();pose.pushPose();pose.translate(layout.left(),layout.ammoBaseline(),0);pose.scale(layout.scale(),layout.scale(),1);
        try{
            var font=Minecraft.getInstance().font; int h=theme==2?22:27;
            if(theme!=2&&theme!=5)g.fill(0,-h,width,0,0xbb11191d);
            if(theme!=2)g.fill(0,-h,2,0,accent);
            String count=mag+" / "+stats.magazine();int textEnd=width-3;
            g.drawString(font,count,textEnd-font.width(count),-12,0xfff2f6f6,true);
            String info=TennoGunItem.reloadUntil(held)>now?"Reloading":GunDataHelper.getLong(held,"wf_gale")>now?"Gale Force":
                spec.family==TennoGunSpec.Family.BUBONICO?"Battery":spec.mode().split(" / ")[0];
            int room=width-font.width(count)-13;
            g.drawString(font,font.plainSubstrByWidth(info,room),5,-12,accent,true);
            g.drawString(font,held.getHoverName().getVisualOrderText(),5,-h+2,0xfff5f5ed,true);
            g.fill(4,-2,width-2,-1,0x55485860);
            g.fill(4,-2,4+Math.round((width-6)*mag/(float)stats.magazine()),-1,accent);
        }finally{pose.popPose();}
    }
}
