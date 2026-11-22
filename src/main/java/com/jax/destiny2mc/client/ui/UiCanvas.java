package com.jax.destiny2mc.client.ui;
/** Shared drawing contract: the game and offline layout previews use these same layouts. */
public interface UiCanvas {
 void fill(int x,int y,int w,int h,int colour);
 void text(int x,int y,String text,int colour,float scale);
 void image(String name,int x,int y,int w,int h);
 void item(int id,int x,int y,int size);
 default void line(int x1,int y1,int x2,int y2,int c){
  int n=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));
  for(int i=0;i<=n;i++){float t=n==0?0:(float)i/n;fill(Math.round(x1+(x2-x1)*t),Math.round(y1+(y2-y1)*t),1,1,c);}
 }
 default void outline(int x,int y,int w,int h,int c){fill(x,y,w,1,c);fill(x,y+h-1,w,1,c);fill(x,y,1,h,c);fill(x+w-1,y,1,h,c);}
 default void ring(int x,int y,int rx,int ry,int c){for(int i=0;i<72;i++){double a=i*Math.PI/36,b=(i+1)*Math.PI/36;line(x+(int)(Math.cos(a)*rx),y+(int)(Math.sin(a)*ry),x+(int)(Math.cos(b)*rx),y+(int)(Math.sin(b)*ry),c);}}
 default void diamond(int x,int y,int r,int c){line(x-r,y,x,y-r,c);line(x,y-r,x+r,y,c);line(x+r,y,x,y+r,c);line(x,y+r,x-r,y,c);}
 default void wrap(int x,int y,String s,int chars,int colour){
  String line="";for(String word:s.split(" ")){if(line.length()+word.length()+1>chars){text(x,y,line,colour,1);y+=12;line="";}line+=(line.isEmpty()?"":" ")+word;}if(!line.isEmpty())text(x,y,line,colour,1);
 }
}
