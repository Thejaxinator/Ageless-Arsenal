package com.jax.destiny2mc.client.atlas;

import com.jax.destiny2mc.atlas.*;
import com.jax.destiny2mc.frontier.*;
import com.jax.destiny2mc.entity.FallenMobEntity;
import com.jax.destiny2mc.combat.CombatMoves;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/** World locator and model archive. Model previews never enter or tick the world. */
public class AtlasScreen extends Screen {
 public static final int W=960,H=540;
 private static final int INK=0xff292d2b,SECONDARY=0xff2d342e,CREAM=0xfff3f0e7;
 private static final int GOLD=0xffd7c190,WHITE=0xfff0eee7,MUTED=0xffbdc2bc,CARD=0xff242a28;
 private static final String[] REALMS={"all","destiny","warframe"};
 private static final String[] FACTIONS_ALL={"all","fallen","hive","vex","cabal","taken","scorn","dread","corpus"};
 private static final String[] FACTIONS_DESTINY={"all","fallen","hive","vex","cabal","taken","scorn","dread"};
 private static final String[] FACTIONS_WARFRAME={"all","corpus"};
 private int tab,listScroll,guideScroll,ticks,pose=1;
 private String site="petition_court",enemy="vorgeth",realm="all",faction="all",query="";
 private boolean bosses,searchFocus,paused,dragging;
 private float yaw=-24,pitch=-8,zoom=1,scale=1;
 private LivingEntity preview;
 private String previewId="",previewError="",controlSignature="";
 private int ox,oy,guideLines,guideVisible=1;
 private final List<AtlasControl> controls=new ArrayList<>();

 public AtlasScreen(){super(Component.literal("Ageless Arsenals Field Guide"));}
 @Override public boolean isPauseScreen(){return false;}
 @Override protected void init(){
  scale=Math.min(width/(float)W,height/(float)H);
  ox=Math.round((width-W*scale)/2);oy=Math.round((height-H*scale)/2);
  controlSignature="";syncControls();
  GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(),GLFW.GLFW_CURSOR,GLFW.GLFW_CURSOR_HIDDEN);
  if(minecraft.player!=null)PacketDistributor.sendToServer(new AtlasNetwork.Request(3,""));
 }
 public void selectForReview(String id,int page){
  tab=page;if(page==0){site=id;var s=DungeonSites.site(id);if(s!=null)enemy=s.boss();}else enemy=id;
  resetPreview();
 }
 private void resetPreview(){
  preview=null;previewId="";previewError="";guideScroll=0;resetView();
  if(tab==1){var list=entries();for(int i=0;i<list.size();i++)if(list.get(i).id().equals(enemy)&&(i<listScroll||i>=listScroll+9))listScroll=Math.max(0,i-4);}
  controlSignature="";
 }
 private void resetView(){zoom=1;yaw=-24;pitch=-8;}
 private void openBestiary(){tab=1;query="";realm="all";faction="all";bosses=false;searchFocus=false;resetPreview();}
 private void openLocator(){tab=0;searchFocus=false;chooseSite(site);}
 private boolean ruin(){return tab==0&&DungeonSites.site(site)==null;}
 private int bx(double x){return (int)((x-ox)/scale);}
 private int by(double y){return (int)((y-oy)/scale);}
 private boolean inside(int x,int y,int l,int t,int w,int h){return x>=l&&y>=t&&x<l+w&&y<t+h;}
 private String[] activeFactions(){return switch(realm){case "destiny"->FACTIONS_DESTINY;case "warframe"->FACTIONS_WARFRAME;default->FACTIONS_ALL;};}
 private List<BestiaryCatalog.Entry> entries(){return BestiaryCatalog.filter(query,realm,faction,bosses);}
 private List<String> sites(){List<String> out=new ArrayList<>();DungeonSites.ALL.forEach(s->out.add(s.id()));out.addAll(DungeonSites.RUINS);return out;}
 private BestiaryCatalog.Entry selected(){return BestiaryCatalog.get(enemy);}
 private static String siteName(String id){return switch(id){case "cosmodrome_remnant" -> "Fallen Ether Bunker";case "vex_conduit" -> "Vex Transit Gate";case "warmind_relay" -> "Cabal Forward Base";case "exodus_bulkhead" -> "Scorn Ether Still";case "creation_annex" -> "Dread Resonance Needle";case "petition_court" -> "Court of Petitions";case "heresy_pit" -> "Cradle of Embers";case "avarice_vault" -> "Rusted Vault";case "siren_spire" -> "Siren’s Watch";default -> "";};}
 private static String siteDetail(String id){return switch(id){case "cosmodrome_remnant" -> "A ribbed salvage shelter. The curved roof covers the ether tanks and a service passage.";case "vex_conduit" -> "A stepped bronze portal on a causeway. Follow the luminous channel beneath the gate to its cache.";case "warmind_relay" -> "A red-armored command bunker, with a recessed gate, paired firing slits and a rooftop signal mast.";case "exodus_bulkhead" -> "A scavenged refinery built around a caged dark-ether vat. The service deck runs behind the vessel.";case "creation_annex" -> "A split black pyramid conducts amber resonance through a suspended central prism.";case "petition_court" -> "An open-roof Awoken chapel. Pointed arches surround the petition basin and leave the Keepers room to appear.";case "heresy_pit" -> "A Hive sacrificial crucible with paired horned buttresses and a fiery crown above the far apse.";case "avarice_vault" -> "An Eliksni salvage hangar. A great circular vault door fills the rear bulkhead and two gantries flank the crystal floor.";case "siren_spire" -> "A Warmind communications platform occupied by Vex. Four receiver circuits converge below its antenna gantry.";default -> "";};}
 private static String siteSize(String id){return switch(id){case "cosmodrome_remnant" -> "25 × 14 × 25 blocks";case "vex_conduit" -> "25 × 18 × 25 blocks";case "warmind_relay" -> "27 × 16 × 27 blocks";case "exodus_bulkhead" -> "23 × 17 × 23 blocks";case "creation_annex" -> "23 × 19 × 23 blocks";case "petition_court" -> "31 × 18 × 31 blocks";case "heresy_pit" -> "31 × 18 × 31 blocks";case "avarice_vault" -> "31 × 15 × 31 blocks";case "siren_spire" -> "31 × 20 × 31 blocks";default -> "";};}

 private void chooseEnemy(String id){enemy=id;resetPreview();}
 private void chooseSite(String id){site=id;var s=DungeonSites.site(id);if(s!=null)enemy=s.boss();resetPreview();}
 private void request(int action){
  PacketDistributor.sendToServer(new AtlasNetwork.Request(action,site));
  if(action==0)AtlasClient.RESULTS.put(site,new AtlasNetwork.Reply(site,"scanning",0,0,0,"minecraft:overworld","Scanning for nearby structures...",false));
 }
 private boolean scanning(){var r=AtlasClient.RESULTS.get(site);return r!=null&&r.status().equals("scanning");}

 @Override public void tick(){
  if(!paused)ticks++;
  if(ruin()||minecraft.level==null)return;
  if(preview==null&&!previewId.equals(enemy)){
   previewId=enemy;
   try{
    Entity e=BuiltInRegistries.ENTITY_TYPE.get(FrontierContent.id(enemy)).create(minecraft.level);
    if(e instanceof LivingEntity living){preview=living;preview.moveTo(0,0,0);preview.setCustomNameVisible(false);if(preview instanceof Mob m)m.setNoAi(true);}
    else previewError="No living model is registered for this entry.";
   }catch(RuntimeException ex){previewError="Model could not be loaded.";org.slf4j.LoggerFactory.getLogger("AgelessArsenals").error("Preview creation failed for {}",enemy,ex);}
  }
  if(preview!=null){
   preview.tickCount=ticks;if(!paused)preview.walkAnimation.update(pose==1?.65f:0,.4f);
   preview.yBodyRot=preview.yBodyRotO=180;preview.yHeadRot=preview.yHeadRotO=180;
   preview.setYRot(180);preview.yRotO=180;preview.setXRot(0);preview.xRotO=0;
   if(preview instanceof Mob m)m.setAggressive(pose!=0);
   if(preview instanceof FrontierMob m){int state=pose==2?FrontierMob.FIRE:pose==3?special(m):FrontierMob.IDLE;int length=state==FrontierMob.SHIELD?80:state==FrontierMob.LEAP?46:state==FrontierMob.SLAM?52:state==FrontierMob.BLINK?28:60;if(com.jax.destiny2mc.client.frontier.EclipseRenderer.supports(m.unit.kind())){
     if(pose==2 && (m.unit.kind()==105 || m.unit.kind()==107)) {state=FrontierMob.MELEE;length=30;}
     else if(m.unit.kind()==139 && pose==3) {state=FrontierMob.SLAM;length=38;}
     else if(state==FrontierMob.FIRE) length=m.unit.kind()==139?30:36;
    }
    // Preview the same primary action the combat AI schedules. The Screeb fuse
    // and Ravager swing would be invisible if every roster entry previewed FIRE.
    if(pose==2){
     switch(m.unit.kind()){
      case 116,121,126,127,137,138 -> {state=FrontierMob.MELEE;length=30;}
      case 118 -> {state=FrontierMob.CAST;length=34;}
     }
    }
    m.previewAnimation(state,ticks%length);}
   if(preview instanceof FallenMobEntity m)previewFallen(m);
  }
 }
 private void previewFallen(FallenMobEntity mob){
  if(pose<2){mob.setAnim(CombatMoves.IDLE,0);return;}
  boolean ability=pose==3;
  int state=ability?legacySpecial():CombatMoves.FIRE;
  int length=CombatMoves.duration(state);
  switch(enemy){
   case "hive_thrall" -> {state=CombatMoves.THRALL_SWIPE;length=CombatMoves.duration(state);}
   case "fallen_wretch" -> {state=ability?3:4;length=ability?14:11;}
   case "fallen_bruiser" -> {state=ability?5:4;length=ability?14:12;}
   case "devil_walker" -> {state=ability?2:1;length=ability?130:12;}
   case "sepiks_prime" -> {state=ability?5:1;length=ability?380:10;}
   case "vanok_arclite" -> {state=ability?3:1;length=ability?18:12;}
   case "archon_priest" -> {state=ability?0:1;length=ability?0:10;}
   case "blast_servitor","exploder_shank" -> {if(ability){state=0;length=0;}}
  }
  int cycle=state==CombatMoves.THRALL_SWIPE?length+CombatMoves.THRALL_SWIPE_COOLDOWN:length;
  int elapsed=cycle==0?0:ticks%cycle;
  if(elapsed<length)mob.setAnim(state,length-elapsed);else mob.setAnim(CombatMoves.IDLE,0);
 }
 private String primaryPoseLabel(){
  return switch(enemy){
   case "hive_thrall" -> "COMBO";
   case "taken_thrall" -> "SWIPE";
   case "cabal_warbeast" -> "BITE";
   case "fallen_wretch" -> "JAB";
   case "fallen_bruiser" -> "STRIKE";
   default -> "FIRE";
  };
 }

 private int special(FrontierMob m){return switch(m.unit.move()){case "shield","repulse","tether","totem"->FrontierMob.SHIELD;case "blink","mist","taniks"->FrontierMob.BLINK;case "pounce","jetpack","torment","nezarec"->FrontierMob.LEAP;case "darkblade","crota","golgoroth","dungeon_zulmak"->FrontierMob.SLAM;case "rhulk"->FrontierMob.CHARGE;default->FrontierMob.CAST;};}
 private int legacySpecial(){return switch(enemy){case "vex_wyvern","persys_boss"->CombatMoves.DIVE;case "hive_knight","lucent_knight"->CombatMoves.SHIELD;case "hive_wizard"->CombatMoves.CLOUD;case "oryx_boss"->CombatMoves.DARKNESS;case "vex_sphinx"->CombatMoves.TENTACLE;case "fallen_captain_rifle","fallen_captain_scorch","vex_minotaur"->CombatMoves.BLINK;case "vex_hobgoblin"->CombatMoves.STASIS;default->CombatMoves.FIRE;};}

 @Override public void renderBackground(GuiGraphics g,int x,int y,float p){}
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  syncControls();g.fill(0,0,width,height,0xffa5aaa3);
  g.pose().pushPose();g.pose().translate(ox,oy,0);g.pose().scale(scale,scale,1);
  AtlasCanvas c=new AtlasCanvas(g,font);int x=bx(mx),y=by(my);
  c.image("stars",0,0,W,H);
  g.fillGradient(0,0,W,H,0xf2b9bdb4,0xf2a3aaa2);
  c.fill(0,0,W,66,0xff414844);
  c.label(24,13,"AGELESS ARSENALS",WHITE,1.1f,2.3f);
  c.title(24,33,tab==1?"Bestiary":"Destination signals",WHITE,2.1f);
  nav(c,"locator",608,19,148,"LOCATOR",tab==0,x,y);
  nav(c,"bestiary",764,19,172,"BESTIARY",tab==1,x,y);
  c.fill(24,66,912,1,0xffd9d9ce);
  if(tab==0)locatorList(c,x,y);else enemyList(c,x,y);
  centerPanel(c,g,paused?0:partial,x,y);details(c,g,x,y);
  c.line(24,509,936,509,0xff747d74);
  c.text(24,521,"TAB  Focus     ENTER  Select     1 / 2  Pages     /  Search     ESC  Close",INK,.94f);
  c.text(679,521,"BESTIARY  /  "+BestiaryCatalog.ALL.size()+" RECORDS",INK,.94f);
  // D1's circular pointer, with a dark outer stroke for both menu surfaces.
  if(mx>=0&&my>=0&&mx<width&&my<height){c.ring(x,y,7,7,INK);c.ring(x,y,6,6,CREAM);if(dragging)c.fill(x-1,y-1,3,3,CREAM);}
  g.pose().popPose();
 }
 private boolean focused(String id){return getFocused() instanceof AtlasControl a&&a.id.equals(id);}
 private void focusMark(AtlasCanvas c,String id,int x,int y,int w,int h){if(focused(id)){c.outline(x-2,y-2,w+4,h+4,INK);c.outline(x-1,y-1,w+2,h+2,CREAM);}}
 private void nav(AtlasCanvas c,String id,int x,int y,int w,String label,boolean active,int mx,int my){
  boolean over=inside(mx,my,x,y,w,29);c.fill(x,y,w,29,active?CREAM:over?0xff59615a:0xff414844);
  c.label(x+15,y+10,label,active?INK:WHITE,1,1.2f);c.fill(x,y+28,w,1,active?CREAM:0xff81887e);focusMark(c,id,x,y,w,29);
 }
 private void button(AtlasCanvas c,String id,int x,int y,int w,int h,String label,boolean active,boolean enabled,boolean dark,int mx,int my){
  boolean over=enabled&&inside(mx,my,x,y,w,h);
  int bg=!enabled?(dark?0xff343b36:0xffa2a89f):active?CREAM:over?(dark?0xff555e55:0xffd9dbd1):(dark?0xff3e473f:0xffbac0b5);
  int fg=active||!dark?INK:WHITE;if(!enabled)fg=dark?0xffb6beb1:SECONDARY;
  c.fill(x,y,w,h,bg);c.fill(x,y+h-1,w,1,active?GOLD:dark?0xff6c786b:0xff7c877b);
  float size=w<44?.72f:w<55?.82f:.95f;int labelWidth=font.width(AtlasCanvas.styled(label));
  c.strong(x+Math.max(3,(w-(int)(labelWidth*size))/2),y+(h-9)/2,label,fg,size);
  if(enabled)focusMark(c,id,x,y,w,h);
 }
 private void locatorList(AtlasCanvas c,int mx,int my){
  c.label(34,91,"SIGNALS",INK,.98f,1.4f);
  c.text(34,112,DungeonSites.ALL.size()+" dungeons  /  "+DungeonSites.RUINS.size()+" landmarks",SECONDARY,.95f);
  int row=0;for(String id:sites()){
   int y=140+row*36;boolean active=id.equals(site),over=inside(mx,my,32,y,208,32);
   if(active||over)c.fill(32,y,208,32,active?CREAM:0xffd0d3c9);
   c.fill(32,y,active?3:1,32,active?0xff927c4e:0xff7c877b);
   c.strong(43,y+5,trim(siteName(id),188),INK,1);
   c.text(43,y+20,DungeonSites.site(id)!=null?"DUNGEON  /  CHALLENGE ALTAR":"SITE  /  EXPLORATION",SECONDARY,.76f);
   focusMark(c,"site:"+id,32,y,208,32);row++;
  }
  c.text(34,483,"Coordinates from your current world",SECONDARY,.9f);
 }
 private void enemyList(AtlasCanvas c,int mx,int my){
  c.label(34,79,"SEARCH THE ARCHIVE",INK,.73f,1);
  c.fill(34,93,204,23,CREAM);c.fill(34,115,204,1,searchFocus?INK:0xff7c877b);
  String q=query.isEmpty()&&!searchFocus?"Name, universe, faction or origin":query+(searchFocus?"|":"");
  c.text(42,100,trim(q,185),query.isEmpty()?SECONDARY:INK,1);focusMark(c,"search",34,93,204,23);
  for(int i=0;i<REALMS.length;i++)button(c,"realm:"+REALMS[i],34+i*69,121,66,17,REALMS[i].toUpperCase(Locale.ROOT),realm.equals(REALMS[i]),true,false,mx,my);
  String[] facs=activeFactions();
  int cols=facs.length==2?2:facs.length>8?5:4,fw=cols==2?99:cols==5?39:48,step=cols==2?105:cols==5?41:51;
  for(int i=0;i<facs.length;i++)button(c,"faction:"+facs[i],34+i%cols*step,142+i/cols*21,fw,18,facs[i].toUpperCase(Locale.ROOT),faction.equals(facs[i]),true,false,mx,my);
  button(c,"all-types",34,186,99,18,"ALL TYPES",!bosses,true,false,mx,my);
  button(c,"bosses",139,186,99,18,"BOSSES",bosses,true,false,mx,my);
  List<BestiaryCatalog.Entry> list=entries();clampList(list);
  for(int i=0;i<9&&i+listScroll<list.size();i++){
   var e=list.get(i+listScroll);int y=210+i*29;boolean active=e.id().equals(enemy),over=inside(mx,my,32,y,204,27);
   if(active||over)c.fill(32,y,204,27,active?CREAM:0xffd0d3c9);
   c.fill(32,y,active?3:1,27,active?0xff927c4e:0xff7c877b);
   c.strong(42,y+4,trim(e.name(),184),INK,1);
   c.text(42,y+17,e.realm().toUpperCase(Locale.ROOT)+" / "+e.faction().toUpperCase(Locale.ROOT)+" / "+e.role().toUpperCase(Locale.ROOT),SECONDARY,.68f);
   focusMark(c,"enemy:"+e.id(),32,y,204,27);
  }
  if(list.isEmpty()){c.strong(36,225,"No matching enemies",INK,1.05f);c.text(36,244,"Try a name or another faction.",SECONDARY,.95f);}
  scrollbar(c,242,210,261,list.size(),9,listScroll,false);
  c.text(34,483,list.size()+" MATCHES  /  "+BestiaryCatalog.ALL.size()+" TOTAL",SECONDARY,.9f);
 }
 private void centerPanel(AtlasCanvas c,GuiGraphics g,float partial,int mx,int my){
  var e=selected();c.label(275,92,ruin()?"FACTION LANDMARK":e==null?"ENEMY MODEL":e.realm().toUpperCase(Locale.ROOT)+" / "+e.faction().toUpperCase(Locale.ROOT),INK,1,1.5f);
  c.text(275,111,ruin()?"Overworld exploration":e==null?"":e.role(),SECONDARY,1);
  if(ruin()){
   c.title(285,226,siteName(site),INK,1.6f);
   c.text(285,252,"Loot caches and local guards",SECONDARY,1.1f);
   c.line(285,278,555,278,0xff7c877b);
   c.wrap(285,299,"Locate this site to receive its Overworld coordinates.",36,INK);
  }else{
   c.ring(423,384,114,16,0xff8b9388);
   renderPreview(g,c,partial);
   focusMark(c,"model",264,132,318,277);
   c.text(275,419,focused("model")?"ARROWS  Rotate     + / -  Zoom":"DRAG  Rotate     WHEEL  Zoom",SECONDARY,.95f);
   String[] labels={"IDLE","WALK",primaryPoseLabel(),"ABILITY"};
   for(int i=0;i<4;i++)button(c,"pose:"+i,275+i*74,440,69,23,labels[i],pose==i,true,false,mx,my);
   button(c,"pause",275,473,90,20,paused?"RESUME":"PAUSE",paused,true,false,mx,my);
   button(c,"reset",373,473,83,20,"RESET VIEW",false,true,false,mx,my);
   if(tab==0)button(c,"open-bestiary",464,473,107,20,"BESTIARY",false,true,false,mx,my);
   else c.text(470,479,paused?"ANIMATION PAUSED":"LIVE 3D MODEL",SECONDARY,.78f);
  }
 }
 private void renderPreview(GuiGraphics g,AtlasCanvas c,float partial){
  if(preview==null){c.wrap(285,230,previewError.isEmpty()?"Loading model...":previewError,38,INK);return;}
  float extent=Math.max(preview.getBbHeight(),preview.getBbWidth()*1.15f);
  if(enemy.equals("akelous"))extent=4.4f;
  if(enemy.equals("dread_grim")||enemy.equals("nezarec"))extent=Math.max(extent,5.6f);
  if(enemy.equals("oryx_boss"))extent=Math.max(extent,7.8f);
  if(enemy.equals("vex_sphinx"))extent=Math.max(extent,4.6f);
  if(enemy.equals("corpus_shield_osprey"))extent=Math.max(extent,2.2f);
  float pixels=Math.min(64,246/Math.max(1,extent))*zoom;
  // Leave room for the full overhead weapon arc in the Special preview.
  if(pose==3){
   if(enemy.equals("crota")||enemy.equals("alak_hul"))pixels*=.59f;
   else if(enemy.equals("zulmak"))pixels*=.82f;
  }
  scissor(g,264,132,582,409);
  try{
   Quaternionf rotation=new Quaternionf().rotateZ((float)Math.PI).rotateX((float)Math.toRadians(pitch)).rotateY((float)Math.toRadians(yaw));
   InventoryScreen.renderEntityInInventory(g,423,271,pixels,new Vector3f(0,preview.getBbHeight()/2,0),rotation,new Quaternionf().rotateX((float)Math.toRadians(pitch)),preview);
  }catch(RuntimeException ex){if(previewError.isEmpty())org.slf4j.LoggerFactory.getLogger("AgelessArsenals").error("Preview rendering failed for {}",enemy,ex);previewError="This model could not be rendered.";c.text(285,270,previewError,INK,1);}
  finally{g.disableScissor();}
 }
 private void scissor(GuiGraphics g,int l,int t,int r,int b){g.enableScissor(ox+(int)(l*scale),oy+(int)(t*scale),ox+(int)Math.ceil(r*scale),oy+(int)Math.ceil(b*scale));}
 private String trim(String s,int pixels){if(font.width(AtlasCanvas.styled(s))<=pixels)return s;while(!s.isEmpty()&&font.width(AtlasCanvas.styled(s+"..."))>pixels)s=s.substring(0,s.length()-1);return s+"...";}
 private record Line(String text,int color){}
 private void add(List<Line> list,String text,int color){for(var line:font.split(AtlasCanvas.styled(text),298)){StringBuilder b=new StringBuilder();line.accept((i,style,cp)->{b.appendCodePoint(cp);return true;});list.add(new Line(b.toString(),color));}}
 private void heading(List<Line> list,String text){list.add(new Line("",MUTED));add(list,text,GOLD);}
 private void details(AtlasCanvas c,GuiGraphics g,int mx,int my){
  var entry=selected();var location=tab==0?DungeonSites.site(site):null;
  String title=tab==0?siteName(site):entry==null?"Archive":entry.name();
  boolean boss=!ruin()&&entry!=null&&entry.boss();
  c.fill(598,80,338,422,CARD);
  c.fill(598,80,338,64,boss?0xffd1bb86:0xffd8dbd0);
  c.label(614,91,tab==0?(ruin()?"EXPLORATION":"DUNGEON"):boss?"BOSS DOSSIER":"ENEMY DOSSIER",INK,.78f,1.5f);
  float titleScale=1.7f;if(font.width(AtlasCanvas.styled(title))*titleScale>302)titleScale=1.3f;
  c.title(614,110,trim(title,(int)(302/titleScale)),INK,titleScale);
  List<Line> lines=new ArrayList<>();
  if(tab==0){
   if(location!=null){add(lines,location.origin()+" / compact encounter",MUTED);add(lines,"Upper structure: "+siteSize(site)+". "+location.habitat()+".",MUTED);}
   else{add(lines,siteDetail(site),WHITE);add(lines,siteSize(site)+". Explore the site for a supply cache.",MUTED);}
   var result=AtlasClient.RESULTS.get(site);heading(lines,"SIGNAL / COORDINATES");
   if(result==null)add(lines,"Select Locate nearby to scan this world's structure generation within 4,096 blocks.",WHITE);
   else{add(lines,result.message(),WHITE);var confirmed=AtlasClient.CONFIRMED.get(site);if(confirmed!=null){add(lines,"X "+confirmed.x()+"   Y ~"+confirmed.y()+"   Z "+confirmed.z(),GOLD);add(lines,"Overworld. Y is the structure's center; find the entrance at ground level.",MUTED);}}
  }
  if(!ruin()&&entry!=null){
   heading(lines,"ENGAGEMENT");add(lines,entry.spawn(),WHITE);
   heading(lines,"MECHANICS / THIS MOD");int n=1;for(String step:entry.mechanics())add(lines,(n++)+". "+step,WHITE);
   heading(lines,"THREATS & COUNTERPLAY");for(String s:entry.threats())add(lines,"- "+s,MUTED);
   heading(lines,"REWARDS & RESET");add(lines,entry.reward(),WHITE);
   // Loot tags are synchronized by Minecraft. Resolve the actual source at render time
   // so a server pack can move salvage between species without changing the guide.
   var entityType=BuiltInRegistries.ENTITY_TYPE.get(FrontierContent.id(entry.id()));
   for (var part : com.jax.destiny2mc.ageless.materials.FactionComponents.ALL) {
    if (entityType.is(part.source())) {
     add(lines,"Salvage: "+part.item().getDescription().getString(),WHITE);
     break;
    }
   }
   if(!entry.origin().equals("Ageless Arsenals")){heading(lines,"REFERENCE");add(lines,entry.origin(),MUTED);}
  }
  int top=156,bottom=tab==0?413:462;guideVisible=Math.max(1,(bottom-top)/13);guideLines=lines.size();
  guideScroll=Math.clamp(guideScroll,0,Math.max(0,guideLines-guideVisible));
  scissor(g,608,top,928,bottom);
  for(int i=0;i<guideVisible&&i+guideScroll<lines.size();i++){Line line=lines.get(i+guideScroll);c.text(614,top+i*13,line.text,line.color,1);}
  g.disableScissor();scrollbar(c,928,top,bottom-top,lines.size(),guideVisible,guideScroll,true);
  focusMark(c,"guide",608,151,318,bottom-151);
  if(tab==0){
   button(c,"locate",614,433,146,27,scanning()?"CANCEL SCAN":"LOCATE NEARBY",true,true,true,mx,my);
   button(c,"track",770,433,150,27,"TRACK SIGNAL",false,AtlasClient.CONFIRMED.containsKey(site),true,mx,my);
   button(c,"clear",614,471,146,21,"CLEAR WAYPOINT",false,AtlasClient.waypoint!=null,true,mx,my);
   c.text(774,478,"SCROLL TO READ",MUTED,.86f);
  }else if(entry!=null&&!entry.site().isEmpty())button(c,"dungeon",614,474,220,22,"LOCATE THIS DUNGEON",true,true,true,mx,my);
  else c.text(614,481,"SCROLL FOR THE COMPLETE GUIDE",MUTED,.86f);
 }
 private void scrollbar(AtlasCanvas c,int x,int y,int h,int total,int visible,int at,boolean dark){
  if(total<=visible)return;c.fill(x,y,2,h,dark?0xff4f594e:0xff7c877b);int thumb=Math.max(12,h*visible/total);
  c.fill(x,y+(h-thumb)*at/Math.max(1,total-visible),2,thumb,dark?GOLD:INK);
 }

 /** Native widgets supply keyboard traversal, activation and narrator labels. */
 private final class AtlasControl extends AbstractWidget {
  final String id;final Runnable action;
  AtlasControl(String id,String name,int x,int y,int w,int h,boolean enabled,Runnable action){
   super(ox+Math.round(x*scale),oy+Math.round(y*scale),Math.max(1,Math.round(w*scale)),Math.max(1,Math.round(h*scale)),Component.literal(name));
   this.id=id;this.action=action;active=enabled;
  }
  @Override protected void renderWidget(GuiGraphics g,int x,int y,float p){}
  @Override public void onClick(double x,double y){activate();}
  private void activate(){if(active){searchFocus=id.equals("search");action.run();controlSignature="";}}
  @Override public boolean keyPressed(int key,int scan,int mods){if(active&&(key==GLFW.GLFW_KEY_ENTER||key==GLFW.GLFW_KEY_KP_ENTER||key==GLFW.GLFW_KEY_SPACE)){activate();return true;}return false;}
  @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
 }
 private void control(String id,String label,int x,int y,int w,int h,boolean enabled,Runnable action){AtlasControl a=new AtlasControl(id,label,x,y,w,h,enabled,action);controls.add(a);addWidget(a);}
 private void clampList(List<BestiaryCatalog.Entry> list){listScroll=Math.clamp(listScroll,0,Math.max(0,list.size()-9));}
 private void syncControls(){
  List<BestiaryCatalog.Entry> list=entries();clampList(list);
  String sig=tab+"/"+site+"/"+enemy+"/"+query+"/"+realm+"/"+faction+"/"+bosses+"/"+listScroll+"/"+paused+"/"+scanning()+"/"+AtlasClient.CONFIRMED.containsKey(site)+"/"+(AtlasClient.waypoint!=null);
  if(sig.equals(controlSignature))return;
  String focus=getFocused() instanceof AtlasControl a?a.id:"";
  clearWidgets();controls.clear();controlSignature=sig;
  control("locator","Locator page",608,19,148,29,true,this::openLocator);
  control("bestiary","Bestiary page",764,19,172,29,true,this::openBestiary);
  if(tab==0){int row=0;for(String id:sites()){control("site:"+id,siteName(id),32,140+row++*36,208,32,true,()->chooseSite(id));}}
  else{
   control("search","Search enemies. Type a name, universe, faction or origin",34,93,204,23,true,()->searchFocus=true);
   for(int i=0;i<REALMS.length;i++){String r=REALMS[i];control("realm:"+r,"Universe: "+r,34+i*69,121,66,17,true,()->{realm=r;faction="all";listScroll=0;});}
   String[] facs=activeFactions();
   int cols=facs.length==2?2:facs.length>8?5:4,fw=cols==2?99:cols==5?39:48,step=cols==2?105:cols==5?41:51;
   for(int i=0;i<facs.length;i++){String f=facs[i];control("faction:"+f,"Filter: "+f,34+i%cols*step,142+i/cols*21,fw,18,true,()->{faction=f;listScroll=0;});}
   control("all-types","Show all enemy types",34,186,99,18,true,()->{bosses=false;listScroll=0;});
   control("bosses","Show bosses",139,186,99,18,true,()->{bosses=true;listScroll=0;});
   for(int i=0;i<9&&i+listScroll<list.size();i++){var e=list.get(i+listScroll);control("enemy:"+e.id(),e.name()+", "+e.realm()+", "+e.faction()+", "+e.role(),32,210+i*29,204,27,true,()->chooseEnemy(e.id()));}
  }
  if(!ruin()){
   control("model","Model preview. Arrow keys rotate; plus and minus zoom",264,132,318,277,true,()->{});
   String[] labels={"Idle animation","Walk animation",enemy.equals("hive_thrall")?"Left and right claw combo":primaryPoseLabel().toLowerCase(Locale.ROOT)+" animation","Ability animation"};
   for(int i=0;i<4;i++){int p=i;control("pose:"+i,labels[i],275+i*74,440,69,23,true,()->pose=p);}
   control("pause",paused?"Resume animation":"Pause animation",275,473,90,20,true,()->paused=!paused);
   control("reset","Reset model view",373,473,83,20,true,this::resetView);
   if(tab==0)control("open-bestiary","Open this boss in the bestiary",464,473,107,20,true,this::openBestiary);
  }
  control("guide","Encounter guide. Arrow keys and Page Up or Page Down scroll",608,151,318,tab==0?262:311,true,()->{});
  if(tab==0){
   control("locate",scanning()?"Cancel structure scan":"Locate nearby structure",614,433,146,27,true,()->request(scanning()?4:0));
   control("track","Track confirmed structure",770,433,150,27,AtlasClient.CONFIRMED.containsKey(site),()->request(1));
   control("clear","Clear waypoint",614,471,146,21,AtlasClient.waypoint!=null,()->request(2));
  }else{var e=selected();if(e!=null&&!e.site().isEmpty())control("dungeon","Locate this dungeon",614,474,220,22,true,()->{tab=0;chooseSite(e.site());});}
  if(!focus.isEmpty())for(var a:controls)if(a.active&&a.id.equals(focus)){setFocused(a);break;}
 }
 @Override public boolean mouseClicked(double px,double py,int button){
  syncControls();if(button!=0)return super.mouseClicked(px,py,button);
  searchFocus=false;boolean handled=super.mouseClicked(px,py,button);
  if(!ruin()&&inside(bx(px),by(py),264,132,318,277)){dragging=true;return true;}
  return handled;
 }
 @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){if(dragging&&b==0){yaw+=(float)(dx/scale)*.8f;pitch=Math.clamp(pitch+(float)(dy/scale)*.5f,-40,40);return true;}return super.mouseDragged(x,y,b,dx,dy);}
 @Override public boolean mouseReleased(double x,double y,int b){dragging=false;return super.mouseReleased(x,y,b);}
 @Override public boolean mouseScrolled(double px,double py,double dx,double dy){
  int x=bx(px),y=by(py);
  if(!ruin()&&inside(x,y,264,132,318,277)){zoom=Math.clamp(zoom+(float)dy*.1f,.55f,1.65f);return true;}
  if(x<248&&tab==1){listScroll+=dy>0?-2:2;clampList(entries());controlSignature="";return true;}
  if(x>=598){guideScroll=Math.clamp(guideScroll+(dy>0?-3:3),0,Math.max(0,guideLines-guideVisible));return true;}return false;
 }
 @Override public boolean charTyped(char c,int mods){if(searchFocus&&query.length()<64&&!Character.isISOControl(c)){query+=c;listScroll=0;controlSignature="";return true;}return super.charTyped(c,mods);}
 @Override public boolean keyPressed(int key,int scan,int mods){
  syncControls();
  if(key==GLFW.GLFW_KEY_TAB){
   searchFocus=false;
   List<AtlasControl> enabled=controls.stream().filter(a->a.active).toList();
   if(!enabled.isEmpty()){
    int current=enabled.indexOf(getFocused()),direction=(mods&GLFW.GLFW_MOD_SHIFT)!=0?-1:1;
    int next=current<0?(direction>0?0:enabled.size()-1):Math.floorMod(current+direction,enabled.size());
    setFocused(enabled.get(next));searchFocus=focused("search");
   }
   return true;
  }
  if(searchFocus){
   if(key==GLFW.GLFW_KEY_BACKSPACE){if(!query.isEmpty())query=query.substring(0,query.length()-1);listScroll=0;return true;}
   if(key==GLFW.GLFW_KEY_ESCAPE||key==GLFW.GLFW_KEY_ENTER){searchFocus=false;setFocused(null);return true;}
   if(key==GLFW.GLFW_KEY_A&&(mods&GLFW.GLFW_MOD_CONTROL)!=0){query="";listScroll=0;return true;}
   return super.keyPressed(key,scan,mods);
  }
  if(key==GLFW.GLFW_KEY_ESCAPE)return super.keyPressed(key,scan,mods);
  if(key==49||key==50){if(key==49)openLocator();else openBestiary();setFocused(null);return true;}
  if(key==GLFW.GLFW_KEY_SLASH){if(tab!=1)openBestiary();searchFocus=true;syncControls();for(var a:controls)if(a.id.equals("search"))setFocused(a);return true;}
  if(focused("model")){
   if(key==263||key==262){yaw+=key==263?-8:8;return true;}
   if(key==265||key==264){pitch=Math.clamp(pitch+(key==265?-5:5),-40,40);return true;}
   if(key==61||key==45||key==334||key==333){zoom=Math.clamp(zoom+(key==61||key==334?.1f:-.1f),.55f,1.65f);return true;}
  }
  if(key==266||key==267||focused("guide")&&(key==265||key==264)){
   guideScroll=Math.clamp(guideScroll+(key==266?-12:key==267?12:key==265?-1:1),0,Math.max(0,guideLines-guideVisible));return true;
  }
  if(getFocused()!=null&&(key==257||key==335||key==32))return super.keyPressed(key,scan,mods);
  if(key==32){paused=!paused;return true;}
  if(key==82){resetView();return true;}
  if(key==264||key==265){
   int delta=key==264?1:-1;
   if(tab==0){var list=sites();chooseSite(list.get(Math.clamp(list.indexOf(site)+delta,0,list.size()-1)));}
   else{var list=entries();if(!list.isEmpty()){int n=-1;for(int i=0;i<list.size();i++)if(list.get(i).id().equals(enemy))n=i;int at=Math.clamp(n+delta,0,list.size()-1);chooseEnemy(list.get(at).id());if(at<listScroll)listScroll=at;if(at>=listScroll+9)listScroll=at-8;}}
   return true;
  }
  return super.keyPressed(key,scan,mods);
 }
 @Override public void removed(){
  preview=null;GLFW.glfwSetInputMode(minecraft.getWindow().getWindow(),GLFW.GLFW_CURSOR,GLFW.GLFW_CURSOR_NORMAL);super.removed();
 }
}
