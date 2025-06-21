package logic.skills;

import logic.enums.TypeOfSkill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Skill {
    private String effect;
    private boolean attivo;
    private TypeOfSkill tipoDiUtilizzo;
}
