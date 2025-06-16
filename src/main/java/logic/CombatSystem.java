package logic;

import logic.entita.Entita;
import logic.entita.MaxValues;
import logic.skills.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Objects;

import static logic.skills.PassiveEffect.*;
import static logic.ReturnValues.*;
import static logic.skills.TypeOfSkill.ONE_TIME;
import static logic.skills.TypeOfSkill.REPEAT;

@Slf4j
@Data
@AllArgsConstructor
public class CombatSystem {
    private Entita attaccante;
    private Entita defender;

    /**
     * Esegue un attacco base, calcolando il danno come differenza tra atk e def.
     */
    public void attaccoBase() {
        float danno = attaccante.getAtk() - defender.getDef();
        if (danno < 0) danno = 0;
        defender.setHp(defender.getHp() - danno);
    }
    /**
     * Esegue un attacco speciale, calcolando il danno dalla skill attacco.
     */
    public void attaccoSpeciale() {
        float danno = attaccante.getSpecialAtk().getDanno() - defender.getDef();
        if (danno < 0) danno = 0;
        defender.setHp(defender.getHp() - danno);
    }

    public ReturnValues domainExpansion(List<Entita> nemici){
        if(attaccante.getDomain() == null)
            return NULL;

        switch (attaccante.getDomain().getTarget()){
            case SINGLE_TARGET -> {return attaccoDomain(List.of(defender));}
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
            case POISON -> defender.setHp(defender.getHp() - (defender.getMaxValues().getMaxHp() / 100 * POISON.getValore()));
            case DEF_DEBUFF -> defender.setDef(defender.getDef() - (defender.getDef() / 100 * DEF_DEBUFF.getValore()));
            case GREAT_DEF_DEBUFF -> defender.setDef(defender.getDef() - (defender.getDef() / 100 * GREAT_DEF_DEBUFF.getValore()));
            case ATK_DEBUFF -> defender.setAtk(defender.getAtk() - (defender.getAtk() / 100 * ATK_DEBUFF.getValore()));
            case GREAT_ATK_DEBUFF -> defender.setAtk(defender.getAtk() - (defender.getAtk() / 100 * GREAT_ATK_DEBUFF.getValore()));
            case BURNING -> defender.setHp(defender.getHp() - (defender.getMaxValues().getMaxHp() / 100 * BURNING.getValore()));
            case BLEEDING -> defender.setHp(defender.getHp() - (defender.getMaxValues().getMaxHp() / 100 * BLEEDING.getValore()));
            case OVERALL_DEBUFF -> {
                defender.setDef(defender.getDef() - (defender.getDef() / 100 * OVERALL_DEBUFF.getValore()));
                defender.setAtk(defender.getAtk() - (defender.getAtk() / 100 * OVERALL_DEBUFF.getValore()));
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
    public void attivaSkills(List<Entita> nemici) {
        //EXAMPLE: "ATTACCANTE DEFENSE + 10 WHEN ATTACCANTE HP < 40",
        //              0         1    2  3  4       5      6  7  8
        //for each skill in his passive
        if(attaccante.getPassiva() == null)
            return;
        for(Skill skill : attaccante.getPassiva().getSkills()){

            if(skill.getEffect() == null || skill.getEffect().isEmpty() )
                continue;
            String kit = skill.getEffect().toUpperCase();
            var parts = kit.split(" ");
            if(!Objects.equals(parts[5], "ALWAYS")  && skill.isAttivo())
                continue;
            //WHO GETS THE BUFF
            Entita target = switch (parts[0]){              //ATTACCANTE
                case "ATTACCANTE", "ATTACKER", "PLAYER", "EROE" -> attaccante;
                case "DIFENSORE", "DEFENDER", "NEMICO", "ENEMY" -> defender;
                default -> throw new IllegalStateException("Unexpected TARGET " + parts[0]);
            };
            String statToIncrease = parts[1];               // DEFENSE
            char sign = parts[2].charAt(0);                 // +
            float percentage = Float.parseFloat(parts[3]);  // 10
            //ENTITA RESPONSIBLE FOR                        // WHEN
            Entita entCondizione = switch (parts[5]){       // ATTACCANTE
                case "ATTACCANTE", "ATTACKER", "PLAYER", "EROE" -> attaccante;
                case "DIFENSORE", "DEFENDER", "NEMICO", "ENEMY" -> defender;
                case "ALWAYS" -> attaccante;
                default -> throw new IllegalStateException("Unexpected ENTITY CONDITION: " + parts[5]);
            };
            String nameCondition = "";
            if(parts.length == 6 && skill.getTipoDiUtilizzo() == REPEAT) { // HP
                applicaSkill(target, percentage, statToIncrease, sign, skill);
                continue;
            }
            else if(parts.length == 6 && skill.getTipoDiUtilizzo() == ONE_TIME && !skill.isAttivo()){
                applicaSkill(target, percentage, statToIncrease, sign, skill);
                continue;
            }
            else if(parts.length == 6 && skill.getTipoDiUtilizzo() == ONE_TIME)
                continue;

            String sign2 = parts[7] ;                       // <
            float percentageToCheck = 50;
            try {                                           //40
                percentageToCheck = Float.parseFloat(parts[8]);
            }catch (Exception e){
                nameCondition = parts[8];
            }

            //controllo quale statistica o proprietà è da guardare
            switch (parts[6]){
                case "NAME", "NOME" -> {
                    if(Objects.equals(entCondizione.getNome().toUpperCase(), nameCondition) && Objects.equals(parts[7], "IS"))
                        applicaSkill(target, percentage, statToIncrease, sign, skill);
                }
                case "HP", "VITA" -> condizione(entCondizione.getHp(), parts[6] , sign2, percentageToCheck, target, percentage, statToIncrease, sign, skill);
                case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" -> condizione(entCondizione.getAtk(), parts[6], sign2, percentageToCheck, target, percentage, statToIncrease, sign, skill);
                case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" -> condizione(entCondizione.getDef(), parts[6],sign2, percentageToCheck, target, percentage, statToIncrease, sign, skill);

            }
        }
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
    public void condizione(float statToCheck, String statToCheckString , String sign, float percentage, Entita target, float percentageIncrease, String statToIncrease, char sign1, Skill skill){

        //converto da percentuale a valore in corrispondenza della statistica
        float convertedPercentage = switch (statToCheckString){
            case "HP", "VITA" -> percentToFloat(target.getMaxValues().getMaxHp(), percentage);
            case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" -> percentToFloat(target.getMaxValues().getMaxAtk(), percentage);
            case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" -> percentToFloat(target.getMaxValues().getMaxDef(), percentage);
            default -> throw new IllegalStateException("Unexpected value: " + statToCheckString);
        };
        System.out.println(convertedPercentage + "   CONVERTED PERCENTAGE\n");
        switch (sign){
            case "<", "MINORE" -> {
                if(statToCheck < convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case ">", "MAGGIORE" -> {
                if(statToCheck > convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case "=", "UGUALE" -> {
                if(statToCheck == convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case ">=", "MAGGIORE_O_UGUALE" -> {
                if(statToCheck >= convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case "<=", "MINORE_O_UGUALE" -> {
                if(statToCheck <= convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case "!=", "DIVERSO" -> {
                if(statToCheck != convertedPercentage) applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);
            }
            case "ALWAYS", "SEMPRE", "SI" -> applicaSkill(target,percentageIncrease, statToIncrease, sign1, skill);

            default -> log.error("Sign not found in condizione");
        }
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
    public void applicaSkill(Entita target, float percentage, String statToIncrease, char sign, Skill skill){
        skill.setAttivo(true);
        switch (statToIncrease){
            case "HP", "VITA"-> {
                switch (sign){
                    case '+' -> target.setHp(target.getHp() + percentToFloat(target.getMaxValues().getMaxHp(), percentage));
                    case '-' -> target.setHp(target.getHp() - percentToFloat(target.getMaxValues().getMaxHp(), percentage));
                }
            }
            case "ATTACCO", "ATK", "ATTACK", "DAMAGE", "DANNO" ->{
                switch (sign){
                case '+' -> target.setAtk(target.getAtk() + percentToFloat(target.getMaxValues().getMaxAtk(), percentage));
                case '-' -> target.setAtk(target.getAtk() - percentToFloat(target.getMaxValues().getMaxAtk(), percentage));
                }
            }
            case "DIFESA", "DEF", "DEFENSE", "CORAZZA", "CORPO", "RESISTENZA" ->{
                switch (sign){
                    case '+' -> target.setDef(target.getDef() + percentToFloat(target.getMaxValues().getMaxDef(), percentage));
                    case '-' -> target.setDef(target.getDef() - percentToFloat(target.getMaxValues().getMaxDef(), percentage));
                }
            }
        }
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