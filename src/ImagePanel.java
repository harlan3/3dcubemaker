
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private BufferedImage image;
	private BufferedImage unscaledImage;
	private int stencilFacePixels = 128;
	private Point stencilOrigin;

	ImagePanel() {

		stencilOrigin = new Point();

		setStencilOrigin();
	}

	void setStencilOrigin() {

		stencilOrigin.x = 2 * stencilFacePixels;
		stencilOrigin.y = 2 * stencilFacePixels;
	}

	public void updateStencilFacePixels(int stencilFacePixels) {

		this.stencilFacePixels = stencilFacePixels;
		setStencilOrigin();
		repaint();
	}

	public void updateStencilOrigin(Point offset) {

		stencilOrigin.x += offset.x;
		stencilOrigin.y -= offset.y;
		repaint();
	}

	public void updateImage(String fileName) {
		try {
			if (fileName != null) {
				image = ImageIO.read(new File(fileName));
				unscaledImage = image;
				repaint();
			}
		} catch (IOException e) {
		}
	}

	public void rotateImage() {

		if (image == null)
			return;

		double rads = Math.toRadians(90);
		double sin = Math.abs(Math.sin(rads));
		double cos = Math.abs(Math.cos(rads));
		int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
		int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
		BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
		AffineTransform at = new AffineTransform();
		at.translate(w / 2, h / 2);
		at.rotate(rads, 0, 0);
		at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
		AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		rotateOp.filter(image, rotatedImage);
		image = rotatedImage;
		unscaledImage = image;
		repaint();
	}

	public void scaleImage(double factor) {

		if (unscaledImage == null)
			return;

		final int w = unscaledImage.getWidth();
		final int h = unscaledImage.getHeight();
		BufferedImage scaledImage = new BufferedImage((int) (w * factor), (int) (h * factor),
				BufferedImage.TYPE_INT_ARGB);
		final AffineTransform at = AffineTransform.getScaleInstance(factor, factor);
		final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		scaledImage = ato.filter(unscaledImage, scaledImage);
		image = scaledImage;
		repaint();
	}

	private void addImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {

		Graphics2D g2d = buff1.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
		g2d.drawImage(buff2, x, y, null);
		g2d.dispose();
	}

	public void generateTexture() {

		BufferedImage bufferedOutputImage = new BufferedImage(stencilFacePixels * 4, stencilFacePixels * 2,
				ColorSpace.TYPE_RGB);
		Graphics2D graphics = bufferedOutputImage.createGraphics();

		Color rgb = new Color(0, 0, 0);
		graphics.setColor(rgb);
		graphics.fillRect(0, 0, bufferedOutputImage.getWidth(), bufferedOutputImage.getHeight());

		try {
			BufferedImage subimage = image.getSubimage(stencilOrigin.x, stencilOrigin.y - stencilFacePixels,
					stencilFacePixels * 3, stencilFacePixels * 2);
			addImage(bufferedOutputImage, subimage, (float) 1.0, 0, 0);
		} catch (Exception e) {
			System.out.println("An Exception occured. Is the Stencil completely within the image?");
		}

		File outFile = new File("output.png");
		try {
			ImageIO.write(bufferedOutputImage, "png", outFile);
		} catch (IOException e) {
		}
	}

	public void drawFace(Graphics g, boolean fill) {

		if (fill)
			g.fillRect(0, 0, (int) stencilFacePixels, (int) stencilFacePixels);
		else
			g.drawRect(0, 0, (int) stencilFacePixels, (int) stencilFacePixels);
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (image != null)
			g.drawImage(image, 0, 0, this);

		// Set origin
		g.translate(stencilOrigin.x, stencilOrigin.y);

		g.translate((int) (0.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, true);

		g.translate((int) (-3.0 * stencilFacePixels), (int) (-1.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, false);

		g.translate((int) (1.0 * stencilFacePixels), (int) (0.0 * stencilFacePixels));
		drawFace(g, true);
	}
}
