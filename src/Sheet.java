import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

public class Sheet extends JPanel {

	private static final long serialVersionUID = 1L;
	private int size = 0;
	private BufferedImage image;
	
	public Sheet() {
		Lightbeam.of(new File("C:\\...\\generated.pptx"))
				.encode()
				.peekImage(im -> {
					size = im.getWidth();
					image = im;
				})
				.writeToImage("image.png");
		
		try {
			Lightbeam.of(new File("C:\\...\\image.png"))
			.decode("ppt.pptx");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(size, size);
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		if (image != null)
			g.drawImage(image,0,0,this);
	}
}
