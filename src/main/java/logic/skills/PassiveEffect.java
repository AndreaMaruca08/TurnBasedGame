package logic.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public enum PassiveEffect {
    POISON(4),
    BLEEDING(7),
    BURNING(10),
    //DEBUFF
    DEF_DEBUFF(15),
    GREAT_DEF_DEBUFF(30),

    ATK_DEBUFF(15),
    GREAT_ATK_DEBUFF(30),

    OVERALL_DEBUFF(20),
    GREAT_OVERALL_DEBUFF(35),

    NOEFFECT(0);

    @Getter
    private final int valore;

    public static List<PassiveEffect> effetti(){
        return List.of(NOEFFECT, BLEEDING, BURNING, DEF_DEBUFF, ATK_DEBUFF, OVERALL_DEBUFF, POISON);
    }

}
