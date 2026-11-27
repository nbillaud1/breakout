package breakout.model;

import java.util.List;

/**
 * Un niveau : sa disposition de briques (voir Wall), le temps entre deux descentes du mur
 * et la vitesse de départ de la balle.
 */
public record Level(String name, double descentEvery, double ballSpeed, String... rows) {

	public static final List<Level> ALL = List.of(
			new Level("Échauffement", 32, 330,
					"..222222..",
					".22222222.",
					"1111111111",
					"1111111111"),
			new Level("Pyramide", 28, 350,
					"....33....",
					"...3223...",
					"..322223..",
					".32211223.",
					"3221111223"),
			new Level("Damier", 24, 370,
					"4.4.4.4.4.",
					".3.3.3.3.3",
					"2222222222",
					"1.1.1.1.1.",
					".1.1.1.1.1"),
			new Level("Le mur d'origine", 24, 390,
					"5555555555",
					"4444444444",
					"3333333333",
					"2222222222",
					"1111111111"),
			new Level("Forteresse", 22, 410,
					"5555555555",
					"5........5",
					"5.444444.5",
					"5.4.33.4.5",
					"5.444444.5",
					"5..2222..5",
					"1111111111"));
}
