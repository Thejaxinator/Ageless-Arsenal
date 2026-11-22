import com.jax.destiny2mc.binder.difficulty.EncounterPreset;

public final class EncounterFormulaCheck {
    private static void close(float actual, float expected) {
        if(Math.abs(actual-expected)>0.0001f)throw new AssertionError(actual+" != "+expected);
    }
    public static void main(String[] args){
        var patrol=EncounterPreset.PATROL;
        close(patrol.incoming(4.5f,false,false),1.71f); // Dreg shock bolt
        close(patrol.incoming(4.5f,false,true),1.1115f);
        close(patrol.incoming(10,true,false),4.2f);    // Valus melee
        close(patrol.incoming(200,true,false),6f);      // hard boss impact cap
        close(patrol.incoming(200,false,false),4f);
        for(var preset:EncounterPreset.values()){
            if(EncounterPreset.find(preset.id)!=preset)throw new AssertionError(preset);
            if(preset.incoming(6,true,true)>preset.incoming(6,true,false))throw new AssertionError(preset);
        }
        close(EncounterPreset.ORIGINAL.incoming(100,true,true),100);
        if(EncounterPreset.find("bogus")!=null)throw new AssertionError();
        System.out.println("Passed: four preset identities, hit caps, rapid follow-up, Dreg bolt and Valus melee.");
    }
}
