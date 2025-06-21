package logic;

import logic.entita.Entita;
import logic.entita.MaxValues;
import logic.enums.PassiveEffect;
import logic.enums.ReturnValues;
import logic.skills.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Objects;

import static logic.enums.PassiveEffect.*;
import static logic.enums.ReturnValues.*;
import static logic.enums.TypeOfSkill.ONE_TIME;
import static logic.enums.TypeOfSkill.REPEAT;

@Slf4j
@Data
@AllArgsConstructor
public class CombatSystem {
    private Entita attaccante;
    private Entita difensore;

    /**
     * Esegue un attacco base, calcolando il danno come differenza tra atk e def.
     */
    public String attaccoBase(boolean speciale) {
        String messaggio = "";
        //se evade
        if (Math.random() * 100 < difensore.getEvasione())
            return "EVASIONE " + difensore.getNome() + " ha evaso l'attacco di " + attaccante.getNome();
        float atk = 0.0F;
        if(speciale){
            atk = attaccante.getSpecialAtk().getDanno() * 1.20F;
            attaccante.getSpecialAtk().setUtilizzi(attaccante.getSpecialAtk().getUtilizzi() - 1);
        }else{
             atk = attaccante.getAtk() * 1.20F;
        }
        float difesa = difensore.getDef();

        //se critico ignora 50% difesa
        if (Math.random() * 100 < attaccante.getCritico()) {
            difesa = difesa * 0.5F;
            messaggio += "🔥COLPO CRITICO DI " + attaccante.getNome();
        }

        float danno = atk - difesa;
        if (danno < 0) danno = 0;
        difensore.setHp(difensore.getHp() - danno);
        if(!speciale)
            return messaggio + (" 🤜" + attaccante.getNome() + " ha inflitto " + danno + " a " + difensore.getNome());
        return messaggio + ("💥ATTACCO SPECIALE di " + attaccante.getNome() + " ha inflitto " + danno + " a " + difensore.getNome());
    }

    /**
     * Esegue un attacco speciale, calcolando il danno dalla skill attacco.
     */
    public String attaccoSpeciale() {
        return attaccoBase(true);
    }

    public ReturnValues domainExpansion(List<Entita> nemici){
        if(attaccante.getDomain() == null)
            return NULL;

        switch (attaccante.getDomain().getTarget()){
            case SINGLE_TARGET -> {return attaccoDomain(List.of(difensore));}
            case AOE -> {return attaccoDomain(nemici);}
        }
        return ERROR;
    }
    public ReturnValues attaccoDomain(List<Entita> nemici){
        Domain domainAtt = attaccante.getDomain();
        if(domainAtt == null)
            return NULL;
        else if(domainAtt.getCooldown() != 0)
            return COOLDOWN;
        else if(domainAtt.getChance() < (int)(Math.random() * 100))
            return CHANCE;
        else if(domainAtt.isActive())
            return ATTIVO;


        for(Entita entita : nemici){
            float danno = domainAtt.getDanno() - entita.getDef();
            System.out.println(danno);
            if (danno < 0) danno = 0;
            entita.setHp(entita.getHp() - danno);
        }
        domainAtt.setActive(true);
        if(applicaEffectDomain(nemici) == FINE_DOMAIN)
            return FINE_DOMAIN;
        else if(domainAtt.getDurata() > 0)
            return SUCCESS;
        else
            domainAtt.setCooldown(domainAtt.getMaxCooldown());
        return SUCCESS;
    }

    public ReturnValues applicaEffect(){
        if(attaccante.getSpecialAtk().getPassiveEffect() == null)
            return NULL;

        switch (attaccante.getSpecialAtk().getPassiveEffect()){
            case POISON -> difensore.setHp(difensore.getHp() - (difensore.getMaxValues().getMaxHp() / 100 * POISON.getValore()));
            case DEF_DEBUFF -> difensore.setDef(difensore.getDef() - (difensore.getDef() / 100 * DEF_DEBUFF.getValore()));
            case GREAT_DEF_DEBUFF -> difensore.setDef(difensore.getDef() - (difensore.getDef() / 100 * GREAT_DEF_DEBUFF.getValore()));
            case ATK_DEBUFF -> difensore.setAtk(difensore.getAtk() - (difensore.getAtk() / 100 * ATK_DEBUFF.getValore()));
            case GREAT_ATK_DEBUFF -> difensore.setAtk(difensore.getAtk() - (difensore.getAtk() / 100 * GREAT_ATK_DEBUFF.getValore()));
            case BURNING -> difensore.setHp(difensore.getHp() - (difensore.getMaxValues().getMaxHp() / 100 * BURNING.getValore()));
            case BLEEDING -> difensore.setHp(difensore.getHp() - (difensore.getMaxValues().getMaxHp() / 100 * BLEEDING.getValore()));
            case OVERALL_DEBUFF -> {
                difensore.setDef(difensore.getDef() - (difensore.getDef() / 100 * OVERALL_DEBUFF.getValore()));
                difensore.setAtk(difensore.getAtk() - (difensore.getAtk() / 100 * OVERALL_DEBUFF.getValore()));
            }
        }

        return SUCCESS;
    }
    public ReturnValues applicaEffectDomain(List<Entita> nemici){
        if(attaccante.getDomain() == null)
            return NULL;
        Domain domainAtt = attaccante.getDomain();
        if(domainAtt.getDurata() <= 0)
            return FINE_DOMAIN;
        for(Entita entita : nemici){
            if(nemici.isEmpty() || entita == null) {
                continue;
            }
            MaxValues maxValues = entita.getMaxValues();
            for(PassiveEffect passiveEffect : attaccante.getDomain().getPassiveEffects()){
                switch (passiveEffect){
                    case POISON ->           entita.setHp(entita.getHp() - (maxValues.getMaxHp() / 100 * POISON.getValore()));
                    case BURNING ->          entita.setHp(entita.getHp() - (maxValues.getMaxHp() / 100 * BURNING.getValore()));
                    case BLEEDING ->         entita.setHp(entita.getHp() - (maxValues.getMaxHp() / 100 * BLEEDING.getValore()));

                    case DEF_DEBUFF ->       entita.setDef(entita.getDef() - (entita.getDef() / 100 * DEF_DEBUFF.getValore()));
                    case GREAT_DEF_DEBUFF -> entita.setDef(entita.getDef() - (entita.getDef() / 100 * GREAT_DEF_DEBUFF.getValore()));

                    case ATK_DEBUFF ->       entita.setAtk(entita.getAtk() - (entita.getAtk() / 100 * ATK_DEBUFF.getValore()));
                    case GREAT_ATK_DEBUFF -> entita.setAtk(entita.getAtk() - (entita.getAtk() / 100 * GREAT_ATK_DEBUFF.getValore()));

                    case OVERALL_DEBUFF -> {
                        entita.setDef(entita.getDef() - (entita.getDef() / 100 * OVERALL_DEBUFF.getValore()));
                        entita.setAtk(entita.getAtk() - (entita.getAtk() / 100 * OVERALL_DEBUFF.getValore()));
                    }
                }
            }
        }


        return SUCCESS;
    }
    /**
     * Activates skills for the attacking entity by parsing skill effects and applying conditional stat modifications.
     * This method iterates over the attacker's skills, parses the effects for target identification,
     * stat modifications, and conditions to determine whether to apply the skill. Depending on the parsed
     * effect, it either directly modifies the targeted entity's stats or checks conditions before applying changes.
     *<br><br>
     * The skill effects follow a predefined structure that dictates:
     * - The target to be affected (e.g., ATTACCANTE or DIFENSORE).
     * - The stat to modify (e.g., HP, ATTACK, DEFENSE).
     * - The operation to perform (e.g., increase or decrease by a percentage).
     * - The condition to check before activation (e.g., specific stat or name comparison).
     *<br><br>
     * The method uses helper methods like `applicaSkill` and `condizione` to perform the necessary stat modifications
     * and conditional checks, ensuring proper activation logic based on the parsed effect details.
     */
    public String attivaSkills(List<Entita> nemici) {
        //EXAMPLE: "ATTACCANTE DEFENSE + 10 WHEN ATTACCANTE HP < 40",
        //              0         1    2  3  4       5      6  7  8
        //for each skill in his passive
        if(attaccante.getPassiva() == null)
            return "";
        for(Skill skill : attaccante.getPassiva().getSkills()){

            if(skill.getEffect() == null || skill.getEffect().isEmpty() )
                continue;
            String kit = skill.getEffect().toUpperCase();
            var parts = kit.split(" ");


            //WHO GETS THE BUFF
            Entita target = switch (parts[0]){              //ATTACCANTE
                case "ATTACCANTE", "ATTACKER", "PLAYER", "EROE" -> attaccante;
                case "DIFENSORE", "DEFENDER", "NEMICO", "ENEMY" -> difensore;
                default -> throw new IllegalStateException("Unexpected TARGET " + parts[0]);
            };
            String statToIncrease = parts[1];               // DEFENSE
            char sign = parts[2].charAt(0);                 // +
            float percentage = Float.parseFloat(parts[3]);  // 10
            //ENTITA RESPONSIBLE FOR                        // WHEN
            Entita entCondizione = switch (parts[5]){       // ATTACCANTE
                case "ATTACCANTE", "ATTACKER", "PLAYER", "EROE" -> attaccante;
                case "DIFENSORE", "DEFENDER", "NEMICO", "ENEMY" -> difensore;
                case "ALWAYS" -> attaccante;
                default -> throw new IllegalStateException("Unexpected ENTITY CONDITION: " + parts[5]);
            };
            String nameCondition = "";
            if(parts.length == 6 && skill.getTipoDiUtilizzo() == REPEAT) {
                return applicaSkill(target, percentage, statToIncrease, sign, skill, false);
            }
            else if(parts.length == 6 && skill.getTipoDiUtilizzo() == ONE_TIME && !skill.isAttivo()){
                return applicaSkill(target, percentage, statToIncrease, sign, skill, false);
            }
            else if(skill.isAttivo() && skill.getTipoDiUtilizzo() == ONE_TIME )
                continue;
            else if(statToIncrease.equals("HP") && skill.isAttivo())
                continue;

            String sign2 = parts[7] ;                       // <
            float percentageToCheck = 50;
            try {                                           //40
                percentageToCheck = Float.parseFloat(parts[8]);
            }catch (Exception e){
                nameCondition = parts[8];
            }

            //controllo quale statistica o proprietà è da guardare
            switch (parts[6]){                              // HP
                case "NAME", "NOME" -> {
                    if(Objects.equals(entCondizione.getNome().toUpperCase(), nameCondition) && Objects.equals(parts[7], "IS"))
                        return applicaSkill(target, percentage, statToIncrease, sign, skill, false);
                }
                case "HP" -> {return condizione(entCondizione.getHp(), parts[6] , sign2, percentageToCheck, target,entCondizione, percentage, statToIncrease, sign, skill);}
                case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" -> {return condizione(entCondizione.getAtk(), parts[6], sign2, percentageToCheck, target,entCondizione ,percentage, statToIncrease, sign, skill);}
                case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" -> {return condizione(entCondizione.getDef(), parts[6],sign2, percentageToCheck, target,entCondizione, percentage, statToIncrease, sign, skill);}

            }
        }
        return "Controlla che le tue skill siano scritte correttamente " + attaccante.getNome();
    }

    /**
     * Activates a specific skill based on a condition involving a comparison of a stat value.
     * This method evaluates the given condition and, if true, applies a skill to a target entity.
     *
     * @param statToCheck The stat value to be compared.
     * @param sign The comparison operator used in the condition ("<", ">", "=", "<=", ">=", "!=").
     * @param percentage The value against which the stat is compared.
     * @param target The entity to which the skill will be applied if the condition is met.
     * @param percentageIncrease The percentage value that modifies the target's stat upon skill activation.
     * @param statToIncrease The stat to be increased or decreased ("HP", "ATK", or "DEF").
     * @param sign1 The operator indicating an increase or decrease in the target's stat ('+' or '-').
     */
    //condizione di attivazione
    public String condizione(float statToCheck, String statToCheckString , String sign, float percentage, Entita target,Entita condTarget,  float percentageIncrease, String statToIncrease, char sign1, Skill skill){

        //converto da percentuale a valore in corrispondenza della statistica
        float convertedPercentage = switch (statToCheckString){
            case "HP" -> percentToFloat(condTarget.getMaxValues().getMaxHp(), percentage);
            case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" -> percentToFloat(condTarget.getMaxValues().getMaxAtk(), percentage);
            case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" -> percentToFloat(condTarget.getMaxValues().getMaxDef(), percentage);
            default -> throw new IllegalStateException("Unexpected value: " + statToCheckString);
        };
        switch (sign){
            case "<", "MINORE" -> {
                System.out.println("MINORE");
                if(statToCheck < convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck < convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck < convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case ">", "MAGGIORE" -> {
                System.out.println("ENTITA : " + condTarget.getNome());
                System.out.println("IEJWRJNFIENFERGNE " + statToCheck + " percentage "  +  convertedPercentage );
                System.out.println("MAGGIORE");
                if(statToCheck > convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck > convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck > convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case "=", "UGUALE" -> {
                System.out.println("UGUALE");
                if(statToCheck == convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck == convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck == convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case ">=", "MAGGIORE_O_UGUALE" -> {
                if(statToCheck >= convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck >= convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck >= convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case "<=", "MINORE_O_UGUALE" -> {
                if(statToCheck <= convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck <= convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck <= convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case "!=", "DIVERSO" -> {
                if(statToCheck != convertedPercentage && skill.isAttivo())
                    break;
                else if(!(statToCheck != convertedPercentage) && skill.isAttivo()){
                    skill.setAttivo(false);
                    return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, true);
                }
                if(statToCheck != convertedPercentage) return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }
            case "ALWAYS", "SEMPRE", "SI" -> {
                return applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill, false);
            }

            default -> log.error("Sign not found in condizione");
        }
        return "";
    }
    /**
     * Applies a skill to a target entity by increasing or decreasing
     * a specified stat (HP, attack, defense) by a certain percentage of
     * the target's maximum value for that stat.
     *
     * @param target        The entity to which the skill effect is applied.
     * @param percentage    The percentage of the stat's maximum value to be added or subtracted.
     * @param statToIncrease The stat to be modified (e.g., "HP", "ATTACCO", "DEFESA").
     * @param sign           A character indicating the operation: '+' to increase, '-' to decrease.
     */
    //attivazione e incremento delle stats
    public String applicaSkill(Entita target, float percentage, String statToIncrease, char sign, Skill skill, boolean disattivare){
        skill.setAttivo(true);
        String valueDaStampare = "";
        var maxHp = target.getMaxValues().getMaxHp();
        var maxAtk = target.getMaxValues().getMaxAtk();
        var maxDef = target.getMaxValues().getMaxDef();
        if(skill.getTipoDiUtilizzo() == REPEAT && skill.isAttivo() && !disattivare){
            maxHp = target.getHp();
            maxAtk = target.getAtk();
            maxDef = target.getDef();
        }
        switch (statToIncrease){
            case "HP", "VITA"-> {
                valueDaStampare = "✦Hp " + target.getHp() + " -> ";
                switch (sign){
                    case '+' -> {
                        if(disattivare) {
                            target.setHp(target.getHp() - percentToFloat(maxHp, percentage));
                            skill.setAttivo(false);
                        }
                        else target.setHp(target.getHp() + percentToFloat(maxHp, percentage));
                    }
                    case '-' -> {
                        if(disattivare) {
                            target.setHp(target.getHp() + percentToFloat(maxHp, percentage));
                            skill.setAttivo(false);
                        }
                        else target.setHp(target.getHp() - percentToFloat(maxHp, percentage));
                    }
                }
                valueDaStampare += target.getHp();
            }
            case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" ->{
                valueDaStampare = "✦Atk " + target.getAtk() + " -> ";
                switch (sign){
                    case '+' -> {
                        if(disattivare) {
                            target.setAtk(target.getAtk() - percentToFloat(maxAtk, percentage));
                            skill.setAttivo(false);
                        }
                        else target.setAtk(target.getAtk() + percentToFloat(maxAtk, percentage));
                    }
                    case '-' -> {
                        if(disattivare) {
                            target.setAtk(target.getAtk() + percentToFloat(maxAtk, percentage));
                            skill.setAttivo(false);
                        }
                        else target.setAtk(target.getAtk() - percentToFloat(maxAtk, percentage));
                    }
                }
                valueDaStampare += target.getAtk();
            }
            case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" ->{
                valueDaStampare = "✦Def " + target.getDef() + " -> ";
                switch (sign){
                    case '+' -> {
                        if(disattivare) {
                            target.setDef(target.getDef() - percentToFloat(maxDef, percentage));
                            skill.setAttivo(false);
                        }
                        else target.setDef(target.getDef() + percentToFloat(maxDef, percentage));
                    }
                    case '-' -> {
                        if(disattivare) {
                            target.setDef(target.getDef() + percentToFloat(maxDef, percentage)); // Ripristina
                            skill.setAttivo(false);
                        }
                        else target.setDef(target.getDef() - percentToFloat(maxDef, percentage));
                    }
                }
                valueDaStampare += target.getDef();
            }
        }
        if(!disattivare)
            return "☆" + attaccante.getNome() + " "+ valueDaStampare + "✦ skill: " + statToIncrease + " " + sign + " " + percentage  + " ATTIVA✅";
        return "☆" + attaccante.getNome() + " " + valueDaStampare + "✦ skill: " + statToIncrease + " " + sign + " " + percentage  + " DISATTIVATA❌";
    }

    public float percentToFloat(float maxValue, float percentage){
        return maxValue * (percentage / 100);
    }

    public ReturnValues disattivaSkills() {
        if(attaccante.getDomain() != null)
            attaccante.getDomain().setActive(false);
        attaccante.setDef(attaccante.getMaxValues().getMaxDef());
        attaccante.setAtk(attaccante.getMaxValues().getMaxAtk());
        attaccante.setHp(attaccante.getMaxValues().getMaxHp());
        if(attaccante.getPassiva() == null)
            return NULL;
        for(Skill skill : attaccante.getPassiva().getSkills()){
            skill.setAttivo(false);
        }
        return NULL;
    }



}