package logic.entita;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaxValues {
    private float maxHp;
    private float maxDef;
    private float maxAtk;
    private float maxSp;
    private float maxEva;
    private float maxCrit;
    private float maxDomain;
}
