package logic.skills;

import logic.entita.Entita;
import logic.enums.PassiveEffect;
import logic.enums.Target;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Domain {
    private String nome;
    private float danno;
    private int cooldown;
    private int maxCooldown;
    private int durata;
    private int durataMax = durata;
    private int chance;
    private boolean isActive;
    private Target target;
    private List<PassiveEffect> passiveEffects;
    private int lvl;

    public Domain evoluzione(Entita personaggio){
        if(this.getNome().isBlank())
            return this;
        if(personaggio.getLvl() >= 40 && !this.getNome().contains("EVOLUTO")){
            this.setNome(this.getNome() + " EVOLUTO");
            this.setMaxCooldown(this.getMaxCooldown() - 1);
            this.setDurataMax(this.getDurataMax() + 2);
            this.setDanno(this.getDanno() * 1.3F);
            if(passiveEffects == null)
                return this;
            for(PassiveEffect passiv : passiveEffects) {
                switch (passiv) {
                    case DEF_DEBUFF -> {
                        passiveEffects.remove(passiv);
                        passiveEffects.add(PassiveEffect.GREAT_DEF_DEBUFF) ;
                    }
                    case ATK_DEBUFF ->{
                        passiveEffects.remove(passiv);
                        passiveEffects.add(PassiveEffect.GREAT_ATK_DEBUFF) ;
                    }
                }
            }
            personaggio.levelUpDomain();
        }
        return this;
    }
}
