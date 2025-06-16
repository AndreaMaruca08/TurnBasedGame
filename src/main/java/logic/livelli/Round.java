package logic.livelli;

import logic.entita.Entita;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Round {
    int round;
    int turni;
    List<Entita> nemici;
}
