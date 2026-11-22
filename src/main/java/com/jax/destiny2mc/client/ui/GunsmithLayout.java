package com.jax.destiny2mc.client.ui;
import java.util.List;
public final class GunsmithLayout {
 public record Row(int id,String title,String subtitle){}
 public record Cost(String name,int have,int need){}
 public record State(int page,int selected,int scroll,List<Row> rows,String title,String subtitle,String intrinsic,int item,
   List<Cost> costs,boolean canApply,String[] sockets,int socket,List<Row> options,int chosen,String description,String status){}
 public static int rowHit(double x,double y,int scroll,int count){if(x<26||x>224||y<115||y>=379)return -1;int i=scroll+(int)((y-115)/22);return i<count?i:-1;}
 public static int socketHit(double x,double y){if(x<246||x>=766||y<244||y>=342)return -1;int col=(int)((x-246)/176),row=(int)((y-244)/52);if(col>2||y>=244+row*52+44||x>=246+col*176+168)return -1;return row*3+col;}
 public static int optionHit(double x,double y,int count){if(x<526||x>=772||y<117||y>=117+count*21)return -1;return (int)((y-117)/21);}
 public static void draw(UiCanvas c,State s){
  c.fill(0,0,800,450,0xff131b20);c.image("stars",0,0,800,450);c.fill(0,0,800,450,0xb51a242b);
  c.fill(26,84,200,318,0x78101a21);c.text(36,94,s.page==0?"WEAPON BLUEPRINTS":"YOUR WEAPONS",0xffd5c59c,.9f);
  int end=Math.min(s.rows.size(),s.scroll+12);
  for(int i=s.scroll;i<end;i++){
   Row r=s.rows.get(i);int y=115+(i-s.scroll)*22;boolean sel=i==s.selected;c.fill(29,y,194,21,sel?0xffdad8cf:0x202f414b);
   c.text(36,y+7,r.title,sel?0xff25323b:0xffbecad0,.85f);
  }
  if(s.rows.isEmpty())c.wrap(37,140,"No guns in your inventory. Craft a weapon first.",25,0xffa4b3bc);
  c.text(36,386,s.rows.size()+" weapons  /  SCROLL to browse",0xff8a9da8,.75f);
  c.text(246,86,s.title,0xfff0e9d7,1.15f);c.text(247,105,s.subtitle,0xff8fa8b7,.85f);
  if(s.page==0){
   c.ring(374,224,112,94,0x405e7886);c.ring(374,224,118,100,0x204e6877);c.item(s.item,272,137,202);
   c.fill(526,125,246,226,0x60111b22);c.text(540,139,"MATERIALS REQUIRED",0xffd7c89e,.9f);c.line(540,160,757,160,0x605d7683);
   int i=0;for(Cost cost:s.costs){int y=179+i++*43;c.text(540,y,cost.name,0xffc9d3d7,.8f);c.text(540,y+14,cost.have+" / "+cost.need,cost.have>=cost.need?0xffacc8b3:0xffda9b85,1);}
   c.wrap(248,343,s.intrinsic,40,0xffa8b9c2);
   button(c,526,363,246,s.canApply?"CRAFT WEAPON":"MATERIALS MISSING",s.canApply);
  }else{
   c.ring(375,182,70,47,0x405e7886);c.item(s.item,297,123,155);
   c.fill(520,85,253,151,0xb8132029);c.text(532,96,s.sockets.length==6?"CHOOSE "+new String[]{"BARREL","MAGAZINE","TRAIT 1","TRAIT 2","MASTERWORK","MOD"}[s.socket]:"SELECT A WEAPON",0xffd5c59c,.85f);
   int oi=0;for(Row r:s.options){int y=117+oi++*21;boolean sel=r.id==s.chosen;c.fill(526,y,240,19,sel?0xffdcd9cc:0x383f5663);c.text(534,y+6,r.title,sel?0xff23313a:0xffb8c8d0,.85f);}
   String[] titles={"BARREL","MAGAZINE","TRAIT 1","TRAIT 2","MASTERWORK","MOD"};
   for(int slot=0;slot<6;slot++){
    int x=246+(slot%3)*176,y=244+(slot/3)*52;boolean sel=slot==s.socket;c.fill(x,y,168,44,sel?0xff304956:0x80304551);c.outline(x,y,168,44,sel?0xffcfc198:0xff4b626f);c.text(x+9,y+7,titles[slot],sel?0xffe4d4a6:0xff92aaba,.7f);c.text(x+9,y+24,s.sockets.length==6?s.sockets[slot]:"--",0xffd8e0e1,.8f);
   }
   c.wrap(248,358,s.description,72,0xffbfced4);c.text(248,393,"Cost: 1 Ether Alloy + 2 Copper Ingots  /  Creative: free",0xff93aab7,.8f);
   button(c,631,411,141,s.canApply?"APPLY PERK":"SELECT A CHANGE",s.canApply);
  }
  if(!s.status.isEmpty())c.text(246,431,s.status,0xffc8d9b1,.8f);
  c.text(28,425,"ESC  Close",0xff91a5b0,.85f);
 }
 private static void button(UiCanvas c,int x,int y,int w,String label,boolean enabled){c.fill(x,y,w,26,enabled?0xffb8a371:0xff36434b);c.outline(x,y,w,26,enabled?0xffdcc895:0xff657884);c.text(x+12,y+9,label,enabled?0xff171e22:0xff94a3ad,.85f);}
}
