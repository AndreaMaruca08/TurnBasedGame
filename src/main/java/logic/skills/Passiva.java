package logic.skills;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passiva {
    String nome;
    List<Skill> skills = List.of();;
}
