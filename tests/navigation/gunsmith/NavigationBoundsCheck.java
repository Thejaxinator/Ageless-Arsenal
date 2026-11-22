import com.jax.destiny2mc.client.ui.*;
import java.util.*;
class NavigationBoundsCheck {
  static class Canvas implements UiCanvas {
    List<String> labels = new ArrayList<>();
    public void text(int x,int y,String label,int tint,float scale){labels.add(label);}
    public void fill(int x,int y,int w,int h,int c) {if(w<=0||h<=0)throw new AssertionError("Bad rectangle");}
    public void image(String n,int x,int y,int w,int h){} public void item(int id,int x,int y,int s){}
  }
  static void require(boolean condition){if(!condition)throw new AssertionError();}
  public static void main(String[] args){
    for(int franchise=0;franchise<2;franchise++)for(int tool=0;tool<2;tool++){
      Canvas c=new Canvas();GunsmithNavigation.draw(c,franchise,tool,0,0);
      require(c.labels.equals(List.of("Gunsmith",franchise==0?"BANSHEE-44  /  THE TOWER":"TENNO FOUNDRY","Destiny","Warframe",franchise==0?"Crafting":"Blueprints",franchise==0?"Perks":"Weapon Mods")));
    }
    for(int x=0;x<800;x++)for(int y=0;y<72;y++){
      int p=y<6||y>=36?-1:x>=397&&x<581?0:x>=588&&x<772?1:-1;
      int t=y<40||y>=64?-1:x>=421&&x<580?0:x>=597&&x<772?1:-1;
      require(GunsmithNavigation.franchiseAt(x+.5,y+.5)==p);
      require(GunsmithNavigation.toolAt(x+.5,y+.5)==t);
    }
    System.out.println("PASS: all four routes have exactly two franchise and two tool labels. All 57600 header coordinates agree with the declared hit areas.");
  }
}
