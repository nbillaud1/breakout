package breakout.view;

import java.util.Random;

import breakout.model.Game;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;

/**
 * Les décors. Chacun se dessine en code (aucune image) sur le terrain de Game.W x Game.H,
 * et s'anime avec le temps t en secondes.
 */
public enum Theme {

	CLASSIQUE("Classique", "#ffffff", "#4dd8ff", "#4dd8ff", "#5b8cff", "#a36bff", "#ff5fa8", "#ffb347") {
		@Override
		void paintScene(GraphicsContext g, double t) {
			sky(g, 0, H, "#060a1c", "#2a1250");
			glow(g, 180 + 40 * Math.sin(t * 0.07), 260, 280, Color.web("#7b3fe4", 0.22));
			glow(g, 560, 520 + 30 * Math.cos(t * 0.05), 320, Color.web("#1fb5ff", 0.14));
			for (double[] d : dots) {
				double alpha = 0.3 + 0.7 * (0.5 + 0.5 * Math.sin(t * (1 + 2 * d[4]) + d[3] * 6.28));
				double size = 1 + d[2] * 2;
				g.setFill(Color.gray(1, alpha));
				g.fillOval(d[0] * W, d[1] * H, size, size);
			}
			// Étoile filante toutes les 7 secondes.
			double p = t % 7;
			if (p < 0.9) {
				double x = 120 + p * 620, y = 60 + p * 220;
				g.setStroke(new LinearGradient(x - 120, y - 42, x, y, false, CycleMethod.NO_CYCLE,
						new Stop(0, Color.TRANSPARENT), new Stop(1, Color.WHITE)));
				g.setLineWidth(2);
				g.strokeLine(x - 120, y - 42, x, y);
			}
			// Planète à anneau.
			glow(g, 620, 700, 200, Color.web("#ff5fa8", 0.18));
			g.setFill(new RadialGradient(0, 0, 580, 655, 150, false, CycleMethod.NO_CYCLE,
					new Stop(0, Color.web("#ff9ad0")), new Stop(1, Color.web("#4a1760"))));
			g.fillOval(510, 590, 220, 220);
			g.setStroke(Color.web("#ffd1ec", 0.55));
			g.setLineWidth(4);
			g.strokeOval(430, 675, 380, 50);
		}
	},

	MONTAGNE("Montagne", "#ff7a59", "#ff7a59", "#bfe3f5", "#7cc0e8", "#4f86c6", "#6a5acd", "#c44569") {
		private final double[] far = { 0, 520, 90, 400, 170, 470, 260, 360, 360, 450, 450, 330, 560, 440, 640, 380, 700, 420 };
		private final double[] mid = { 0, 600, 110, 480, 200, 560, 300, 450, 420, 580, 520, 470, 620, 560, 700, 500 };
		private final double[] near = { 0, 690, 80, 620, 170, 680, 280, 600, 380, 670, 500, 610, 600, 680, 700, 630 };

		@Override
		void paintScene(GraphicsContext g, double t) {
			sky(g, 0, H, "#5d9bd6", "#ffd3b0");
			glow(g, 520, 380, 170, Color.web("#fff3c4", 0.55));
			g.setFill(Color.web("#fff6d8"));
			g.fillOval(470, 330, 100, 100);
			for (int i = 0; i < 4; i++) {
				cloud(g, (i * 230 + t * (8 + i * 3)) % (W + 260) - 130, 90 + i * 55, 0.75 - i * 0.1);
			}
			mountains(g, far, "#a5b8d8");
			snowCaps(g, far);
			mountains(g, mid, "#6b7fa8");
			snowCaps(g, mid);
			mountains(g, near, "#34405f");
			for (double[] d : dots) {
				double x = d[0] * W + Math.sin(t * 0.8 + d[3] * 6.28) * 15;
				double y = (d[1] * H + t * (25 + d[4] * 45)) % H;
				double r = 1.2 + d[2] * 2.2;
				g.setFill(Color.gray(1, 0.85));
				g.fillOval(x - r, y - r, 2 * r, 2 * r);
			}
		}
	},

	PLAGE("Plage", "#ffffff", "#ff6f61", "#ffd166", "#06d6a0", "#118ab2", "#ef476f", "#8338ec") {
		@Override
		void paintScene(GraphicsContext g, double t) {
			sky(g, 0, 560, "#2fa9ee", "#ffd9a6");
			// Soleil et rayons qui tournent lentement.
			g.save();
			g.translate(560, 140);
			g.rotate(t * 8);
			g.setStroke(Color.web("#fff3b0", 0.55));
			g.setLineWidth(4);
			g.setLineCap(StrokeLineCap.ROUND);
			for (int i = 0; i < 12; i++) {
				double a = i * Math.PI / 6;
				g.strokeLine(Math.cos(a) * 72, Math.sin(a) * 72, Math.cos(a) * 100, Math.sin(a) * 100);
			}
			g.restore();
			glow(g, 560, 140, 130, Color.web("#fff3b0", 0.5));
			g.setFill(Color.web("#fff6c9"));
			g.fillOval(505, 85, 110, 110);
			cloud(g, (t * 12) % (W + 260) - 130, 240, 0.6);
			// Mouettes.
			g.setStroke(Color.web("#3b4a5a"));
			g.setLineWidth(2.5);
			for (int i = 0; i < 3; i++) {
				double x = (t * (35 + i * 8) + i * 260) % (W + 80) - 40, y = 180 + i * 40 + Math.sin(t * 2 + i) * 8;
				double flap = Math.sin(t * 6 + i) * 4;
				g.strokePolyline(new double[] { x - 12, x, x + 12 }, new double[] { y - flap, y + 3, y - flap }, 3);
			}
			// Mer et vagues.
			sky(g, 560, 700, "#1673c9", "#3cc7e8");
			g.setStroke(Color.web("#ffffff", 0.35));
			g.setLineWidth(2);
			for (int i = 0; i < 4; i++) {
				g.beginPath();
				for (double x = 0; x <= W; x += 10) {
					double y = 578 + i * 30 + Math.sin(x * 0.035 + t * (1.2 + i * 0.3) + i) * 4;
					if (x == 0) g.moveTo(x, y);
					else g.lineTo(x, y);
				}
				g.stroke();
			}
			// Sable, avec l'écume qui avance et recule.
			int n = (int) (W / 10) + 1;
			double[] xs = new double[n + 2], ys = new double[n + 2];
			for (int i = 0; i < n; i++) {
				xs[i] = i * 10;
				ys[i] = 690 + Math.sin(i * 0.4 + t * 1.6) * 6;
			}
			xs[n] = W; ys[n] = H;
			xs[n + 1] = 0; ys[n + 1] = H;
			g.setFill(Color.web("#f3d39b"));
			g.fillPolygon(xs, ys, n + 2);
			g.setStroke(Color.web("#ffffff", 0.85));
			g.setLineWidth(4);
			g.strokePolyline(xs, ys, n);
			g.setFill(Color.web("#d9b273"));
			for (double[] d : dots) {
				if (d[1] > 0.5) g.fillOval(d[0] * W, 705 + (d[1] - 0.5) * 180, 2.5, 2.5);
			}
			palm(g, t);
		}
	},

	FORET("Forêt", "#ffe066", "#ffb703", "#b7e4c7", "#74c69d", "#f4a261", "#e76f51", "#9d4edd") {
		@Override
		void paintScene(GraphicsContext g, double t) {
			sky(g, 0, H, "#163a30", "#7cb594");
			glow(g, 540, 150, 120, Color.web("#f4ffd6", 0.35));
			g.setFill(Color.web("#f1f7d6"));
			g.fillOval(510, 120, 60, 60);
			// Rayons de lumière qui respirent.
			for (int i = 0; i < 3; i++) {
				double a = 0.05 + 0.03 * Math.sin(t * 0.5 + i * 2);
				g.setFill(Color.web("#fffbe0", a));
				double x = 80 + i * 150;
				g.fillPolygon(new double[] { x, x + 70, x + 250, x + 150 }, new double[] { 0, 0, H, H }, 4);
			}
			for (double x = -20; x < W + 40; x += 46) {
				pine(g, x + 10 * Math.sin(x), 610, 150 + 50 * Math.abs(Math.sin(x * 1.7)), "#2e6b52");
			}
			for (double x = -30; x < W + 60; x += 64) {
				pine(g, x + 14 * Math.sin(x * 3), 700, 200 + 60 * Math.abs(Math.sin(x * 0.9)), "#1b4535");
			}
			g.setFill(Color.web("#0c2118"));
			g.fillRect(0, 690, W, H - 690);
			pine(g, 20, 830, 400, "#0a1d14");
			pine(g, 685, 840, 440, "#0a1d14");
			// Lucioles.
			for (int i = 0; i < 30; i++) {
				double[] d = dots[i];
				double x = d[0] * W + Math.sin(t * (0.3 + d[4] * 0.5) + d[3] * 6.28) * 40;
				double y = 260 + d[1] * 480 + Math.cos(t * 0.4 + d[3] * 6.28) * 25;
				double a = Math.max(0, Math.sin(t * (1.5 + d[4]) + d[3] * 6.28));
				glow(g, x, y, 12, Color.web("#ffe066", 0.6 * a));
				g.setFill(Color.web("#fff3a8", a));
				g.fillOval(x - 1.5, y - 1.5, 3, 3);
			}
		}
	};

	static final double W = Game.W, H = Game.H;

	public final String label;
	public final Color ball, accent;
	private final Color[] bricks;
	/** Points tirés une fois pour toutes (x, y, taille, phase, vitesse, entre 0 et 1) : étoiles, neige, lucioles... */
	final double[][] dots = new double[90][5];

	Theme(String label, String ball, String accent, String... brickColors) {
		this.label = label;
		this.ball = Color.web(ball);
		this.accent = Color.web(accent);
		this.bricks = new Color[brickColors.length];
		for (int i = 0; i < brickColors.length; i++) bricks[i] = Color.web(brickColors[i]);
		Random random = new Random(label.hashCode());
		for (double[] d : dots) for (int i = 0; i < d.length; i++) d[i] = random.nextDouble();
	}

	/** Couleur d'une brique selon ses points de vie (1 à 5). */
	public Color brickColor(int hp) {
		return bricks[Math.max(1, Math.min(hp, bricks.length)) - 1];
	}

	abstract void paintScene(GraphicsContext g, double t);

	/** Dessine le décor pour qu'il couvre w x h en entier, recadré au besoin (comme background-size: cover). */
	public void paintCover(GraphicsContext g, double w, double h, double t) {
		double s = Math.max(w / W, h / H);
		g.save();
		g.translate((w - W * s) / 2, (h - H * s) / 2);
		g.scale(s, s);
		paintScene(g, t);
		g.restore();
	}

	static void sky(GraphicsContext g, double y0, double y1, String top, String bottom) {
		g.setFill(new LinearGradient(0, y0, 0, y1, false, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web(top)), new Stop(1, Color.web(bottom))));
		g.fillRect(0, y0, W, y1 - y0);
	}

	static void glow(GraphicsContext g, double x, double y, double r, Color color) {
		g.setFill(new RadialGradient(0, 0, x, y, r, false, CycleMethod.NO_CYCLE,
				new Stop(0, color), new Stop(1, Color.TRANSPARENT)));
		g.fillOval(x - r, y - r, 2 * r, 2 * r);
	}

	static void cloud(GraphicsContext g, double x, double y, double alpha) {
		g.setFill(Color.gray(1, alpha));
		g.fillOval(x, y, 90, 34);
		g.fillOval(x + 25, y - 18, 70, 42);
		g.fillOval(x + 55, y, 80, 30);
	}

	/** Chaîne de montagnes : pts = x0, y0, x1, y1... remplie jusqu'en bas. */
	static void mountains(GraphicsContext g, double[] pts, String color) {
		int n = pts.length / 2;
		double[] xs = new double[n + 2], ys = new double[n + 2];
		for (int i = 0; i < n; i++) {
			xs[i] = pts[2 * i];
			ys[i] = pts[2 * i + 1];
		}
		xs[n] = W; ys[n] = H;
		xs[n + 1] = 0; ys[n + 1] = H;
		g.setFill(Color.web(color));
		g.fillPolygon(xs, ys, n + 2);
	}

	/** Neige sur chaque sommet (point plus haut que ses deux voisins). */
	static void snowCaps(GraphicsContext g, double[] pts) {
		g.setFill(Color.web("#f4f8ff"));
		for (int i = 1; i < pts.length / 2 - 1; i++) {
			double px = pts[2 * i], py = pts[2 * i + 1];
			double ax = pts[2 * i - 2], ay = pts[2 * i - 1], bx = pts[2 * i + 2], by = pts[2 * i + 3];
			if (py >= ay || py >= by) continue;
			double lx = px + (ax - px) * 0.3, ly = py + (ay - py) * 0.3;
			double rx = px + (bx - px) * 0.3, ry = py + (by - py) * 0.3;
			g.fillPolygon(new double[] { px, rx, (px + rx) / 2, px, (px + lx) / 2, lx },
					new double[] { py, ry, (py + ry) / 2 + 6, py + (ly - py) * 0.8, (py + ly) / 2 + 6, ly }, 6);
		}
	}

	static void pine(GraphicsContext g, double x, double baseY, double h, String color) {
		g.setFill(Color.web(color));
		g.fillRect(x - h * 0.03, baseY - h * 0.15, h * 0.06, h * 0.15);
		for (int k = 0; k < 3; k++) {
			double top = baseY - h + k * h * 0.25, bottom = top + h * 0.45, half = h * (0.14 + k * 0.07);
			g.fillPolygon(new double[] { x, x - half, x + half }, new double[] { top, bottom, bottom }, 3);
		}
	}

	static void palm(GraphicsContext g, double t) {
		double sway = Math.sin(t * 0.8) * 6, topX = 105 + sway, topY = 500;
		g.setStroke(Color.web("#8a5a2b"));
		g.setLineWidth(16);
		g.setLineCap(StrokeLineCap.ROUND);
		g.beginPath();
		g.moveTo(70, H);
		g.quadraticCurveTo(40, 650, topX, topY);
		g.stroke();
		g.setFill(Color.web("#2f8f4e"));
		double[] angles = { -170, -135, -100, -60, -20, 20 };
		for (int k = 0; k < angles.length; k++) {
			double a = Math.toRadians(angles[k] + Math.sin(t * 1.2 + k) * 4);
			double tipX = topX + Math.cos(a) * 110, tipY = topY + Math.sin(a) * 110 + 30;
			double midX = (topX + tipX) / 2, midY = (topY + tipY) / 2 - 18;
			double px = -Math.sin(a) * 12, py = Math.cos(a) * 12;
			g.beginPath();
			g.moveTo(topX, topY);
			g.quadraticCurveTo(midX + px, midY + py, tipX, tipY);
			g.quadraticCurveTo(midX - px, midY - py, topX, topY);
			g.fill();
		}
		g.setFill(Color.web("#5d3a1a"));
		g.fillOval(topX - 12, topY - 2, 13, 13);
		g.fillOval(topX + 1, topY + 2, 13, 13);
	}
}
