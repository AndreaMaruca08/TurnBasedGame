package logic.entita;

import logic.skills.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entita {
    private String nome;
    private float hp;
    private float xp;
    private float xp_per_livello;
    private int puntiLivello;
    private int lvl;
    private float def;
    private float atk;
    private float evasione;
    private float critico;
    private UpgradePoints points;
    private MaxValues maxValues;
    private SkillAttacco specialAtk;
    private Passiva passiva;
    private Domain domain;
    private List<Domain> domain_acquisiti;
    private List<Passiva> passive_acquisite;

    public Entita(String nome, float hp, float xp, float xp_per_livello, int lvl, float def, float atk,float evasione, float critico, MaxValues maxValues, SkillAttacco specialAtk, Domain domain){
        this.nome = nome;
        this.hp = hp;
        this.xp = xp;
        this.xp_per_livello = xp_per_livello;
        this.lvl = lvl;
        this.def = def;
        this.atk = atk;
        this.evasione = evasione;
        this.critico = critico;
        this.maxValues = maxValues;
        this.specialAtk = specialAtk;
        this.domain = domain;
    }

    public void levelUp() {
        if (xp < xp_per_livello) return;
        while (xp >= xp_per_livello) {
            puntiLivello++;
            lvl++;
            hp *= 1.1F;
            maxValues.setMaxHp( maxValues.getMaxHp() * 1.1F);
            maxValues.setMaxDef( maxValues.getMaxDef() * 1.1F);
            maxValues.setMaxAtk( maxValues.getMaxAtk() * 1.1F);

            def *= 1.1F;
            atk *= 1.1F;
            if(domain != null) {
                domain.setDanno(domain.getDanno() * 1.02F);
                maxValues.setMaxDomain(maxValues.getMaxDomain() * 1.02F);
            }
            xp -= xp_per_livello;
            if (specialAtk != null) {
                specialAtk.setDanno(specialAtk.getDanno() * 1.1F);
                maxValues.setMaxSp( maxValues.getMaxSp() * 1.1F);
            }
            xp_per_livello *= 1.2F;
        }
    }
    public void levelUpDomain(){
        for(int i = 10; i < getLvl(); i++){
            domain.setChance(domain.getChance() + 1);
            domain.setDanno(domain.getDanno() * 1.05F);
        }
    }
}