package logic.entita;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpgradePoints {
    private int punti_hp;
    private int punti_def;
    private int punti_atk;
    private int punti_domain;
    private int punti_atkSp;
}
