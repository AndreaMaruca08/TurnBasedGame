package logic.livelli;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Level {
    int lvl;
    int livello_raccomandato;
    int xp;
    List<Round> round;
}
