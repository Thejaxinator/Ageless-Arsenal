package com.jax.destiny2mc.binder.progression;

/** No game dependencies. Compile with CareerCurve and run under Java 21. */
public class CareerCurveCheck {
    static void check(boolean result) {
        if (!result) throw new AssertionError("Career curve regression");
    }
    public static void main(String[] args) {
        int maximum = CareerCurve.threshold(50);
        for (int xp=0; xp<=maximum; xp++) {
            int level=CareerCurve.level(xp);
            check(CareerCurve.threshold(level)<=xp);
            check(level==50 || xp<CareerCurve.threshold(level+1));
        }
        for (int i=1;i<50;i++) {
            check(CareerCurve.threshold(i+1)-CareerCurve.threshold(i)==40+12*(i-1));
            check(CareerCurve.healthBonus(i+1)>CareerCurve.healthBonus(i));
            check(CareerCurve.veteranChance(i+1)>=CareerCurve.veteranChance(i));
        }
        check(CareerCurve.healthBonus(1)==0 && CareerCurve.healthBonus(20)==20 && CareerCurve.healthBonus(50)==40);
        check(CareerCurve.armorBonus(50)==5 && CareerCurve.veteranChance(50)==28);
        check(CareerCurve.clampPoints(Long.MAX_VALUE)==maximum && CareerCurve.clampPoints(-1)==0);
        System.out.println("CAREER CURVE PASS: all "+(maximum+1)+" XP totals map to the correct level");
        for (int n:new int[]{1,10,20,35,50}) System.out.printf("L%d %d XP, %.2f max HP, +%d armor, %d%% veteran chance%n",
            n,CareerCurve.threshold(n),20+CareerCurve.healthBonus(n),CareerCurve.armorBonus(n),CareerCurve.veteranChance(n));
    }
}
