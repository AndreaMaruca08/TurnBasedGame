package logic.save;

import logic.entita.Entita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameSave {
    private Entita personaggio;
    private List<Integer> livelliSuperati;
}