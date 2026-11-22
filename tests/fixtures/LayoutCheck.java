import com.jax.destiny2mc.binder.client.CombatHudLayout;
public class LayoutCheck {
  public static void main(String[] args) {
    int[] width={102,82,98,123,119,103}, height={34,32,25,34,34,29};
    int count=0;
    for(int w:new int[]{280,320,360,424,480,640,960,1920})
      for(int h:new int[]{180,240,270,480,720})
        for(float s:new float[]{.7f,.82f,.95f,1.1f})
          for(int mode=0;mode<6;mode++)
            for(boolean armed:new boolean[]{false,true})
              for(boolean off:new boolean[]{false,true})
                for(boolean bound:new boolean[]{false,true}) {
                  if(!armed&&!bound)continue;
                  var a=CombatHudLayout.place(w,h,mode,s,bound?width[mode]:0,bound?height[mode]:0,83,armed,off);
                  int rowLeft=CombatHudLayout.abilityLeft(a.left(),mode,width[mode],a.scale());
                  int rowRight=bound ? rowLeft + Math.round(width[mode]*a.scale()) : a.left();
                  int referenceWidth=Math.round(CombatHudLayout.ammoWidth(mode)*a.scale());
                  int right=Math.max(rowRight,armed?a.left()+referenceWidth:a.left());
                  if(mode==2||mode==3) {
                    if(rowLeft!=a.left())throw new AssertionError("Left-aligned style moved");
                  } else if(Math.abs((rowLeft-a.left())*2 + width[mode]*a.scale()-CombatHudLayout.ammoWidth(mode)*a.scale()) > 1.01) {
                    throw new AssertionError("Midpoints differ by over half a GUI pixel");
                  }
                  int top=armed?a.ammoBaseline()-(int)Math.ceil(CombatHudLayout.ammoHeight(mode)*a.scale()):a.abilityTop();
                  int bottom=a.abilityTop()+(bound?(int)Math.ceil(height[mode]*a.scale()):0);
                  if(a.left()<0||right>w||top<0||bottom>h||bound&&armed&&a.ammoBaseline()>=a.abilityTop())
                    throw new AssertionError("overlap "+a);
                  if(right+4>w/2-(off?121:91)&&bottom>h-83)throw new AssertionError("hotbar/button "+a);
                  int preferredRight=Math.max(a.left()+(armed?(int)Math.ceil(CombatHudLayout.ammoWidth(mode)*s):0),
                      bound?CombatHudLayout.abilityLeft(a.left(),mode,width[mode],s)+(int)Math.ceil(width[mode]*s):a.left());
                  if(preferredRight+4<=w/2-(off?121:91)&&bottom!=h-8)throw new AssertionError("wide anchor "+a);
                  count++;
                }
    System.out.println("Layout checks passed for "+count+" armed/unarmed, scale, window, theme and offhand combinations.");
  }
}
