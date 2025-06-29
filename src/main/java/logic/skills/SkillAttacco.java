package logic.skills;

import logic.enums.PassiveEffect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillAttacco {
    private String nomeAttacco;
    private float danno;
    private int utilizzi;
    private PassiveEffect passiveEffect;
}
