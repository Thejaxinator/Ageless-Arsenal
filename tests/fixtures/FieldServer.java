package aa.validation;
import com.jax.destiny2mc.combat.CombatMobEntity;
import com.jax.destiny2mc.frontier.FrontierBallistics;
import com.jax.destiny2mc.item.DestinyGunItem;
import com.jax.destiny2mc.weapon.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="atlas_validation")
public final class FieldServer {
    private static ServerPlayer charging; private static long began;
    @SubscribeEvent public static void hold(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e){
        if(charging==null)return;
        long age=charging.level().getGameTime()-began;
        if(age>30){charging=null;return;}
        var stack=charging.getMainHandItem();var w=((DestinyGunItem)stack.getItem()).spec;
        if(GunDataHelper.getMag(stack,w)<w.magSize){System.out.println("FX NATIVE accepted Telesto charge");charging=null;return;}
        FrontierBallistics.action(charging,(byte)3,charging.getYRot(),charging.getXRot(),(byte)1);
    }
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        var root=Commands.literal("aa_fx_test").requires(s->s.hasPermission(2));
        for(String mode:new String[]{"kinetic","arc","blink","volley"})
            root.then(Commands.literal(mode).executes(ctx->{var p=ctx.getSource().getPlayerOrException();run(p,mode);return 1;}));
        e.getDispatcher().register(root);
    }
    private static void run(ServerPlayer p,String mode){
        var level=p.serverLevel();
        if(mode.equals("blink")){
            var captain=level.getEntitiesOfClass(CombatMobEntity.class,new AABB(-9,59,-4,9,68,15),e->e.getTags().contains("fx_captain")).getFirst();
            try{
                var action=CombatMobEntity.class.getDeclaredMethod("safeBlink",ServerLevel.class,LivingEntity.class);action.setAccessible(true);
                var from=captain.position();captain.getRandom().setSeed(14849L);action.invoke(captain,level,p);
                if(from.distanceToSqr(captain.position())<.25)throw new IllegalStateException("no approved blink destination");
                System.out.println("FX NATIVE blink from="+from+" to="+captain.position());
            }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
            return;
        }
        String id=switch(mode){case "kinetic"->"khvostov_7g_0x";case "arc"->"coldheart";default->"telesto";};
        var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("destiny2mc",id));
        var stack=new ItemStack(item);p.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var spec=((DestinyGunItem)item).spec;GunDataHelper.setInt(stack,"d2_mag",spec.magSize);
        if(mode.equals("volley")){
            FrontierBallistics.action(p,(byte)3,p.getYRot(),p.getXRot(),(byte)1);
            charging=p;began=p.level().getGameTime();
        } else{
            FrontierBallistics.action(p,(byte)0,p.getYRot(),p.getXRot(),(byte)1);
            int mag=GunDataHelper.getMag(stack,spec);
            if(mag!=spec.magSize-1)throw new IllegalStateException("shot not accepted "+id+" mag="+mag);
            System.out.println("FX NATIVE accepted "+id+" consumed 1");
        }
    }
}
