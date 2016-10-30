import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class World extends JPanel {

	private BufferedImage canvas;

	private static String root = "";

	public World() {
		File file = new File(root+"input");
		if (!file.exists()) {
			System.out.println("MUST HAVE VALID ROOT AND INPUT FILE");
			return;
		}
		long l = file.length()+4;
		long p = l % 4 == 0? l/4 : l/4+1;
		int w = (int) Math.sqrt(p) +1;

		canvas = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);

		read(file, new Consumer() {

			int x=0,y=0;

			@Override
			public void write(int hex) {
				canvas.setRGB(x, y, hex);
				x++;
				if (x >= canvas.getWidth()) {
					y++;
					x=0;
				}
			}
		});

		write(canvas);

		try {
			BufferedImage im = ImageIO.read(new File(root+"output.png"));
			decode(im);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	interface Consumer {
		public void write(int hex);
	}

	private void decode(BufferedImage image) throws IOException {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		FileOutputStream fos = new FileOutputStream(root+"generated");

		int zeros = 0;
		for (int x=pixels.length-1; x>0; x--) {
			if (pixels[x] == 0) {
				zeros ++;
			} else {
				break;
			}
		}

		final int pixelLength = 4;

		int len = 0;
		len += (((int) pixels[0] & 0xff) << 24); // alpha
		len += ((int) pixels[1] & 0xff); // blue
		len += (((int) pixels[2] & 0xff) << 8); // green
		len += (((int) pixels[3] & 0xff) << 16); // red
		//        System.out.println(argb);
		for (int pixel = 4; pixel < pixels.length - zeros; pixel += pixelLength) {
			byte[] buffer = new byte[4];
			buffer[0] = (byte) (pixels[pixel] & 0xff); // alpha
			buffer[3] = (byte) (pixels[pixel + 1] & 0xff); // blue
			buffer[2] = (byte) (pixels[pixel + 2] & 0xff); // green
			buffer[1] = (byte) (pixels[pixel + 3] & 0xff); // red
			if (pixel + 4 >= pixels.length - zeros) {
				fos.write(buffer, 0, len);
			} else {
				fos.write(buffer);
			}
		}

		fos.flush();
		fos.close();
	}

	public void read(File file, Consumer consumer) {
		FileInputStream fileInputStream = null;

		byte[] buffer = new byte[4];

		try {
			fileInputStream = new FileInputStream(file);
			int trailing = (int)((file.length()%4));
			consumer.write(trailing);
			while ((fileInputStream.read(buffer)) > 0) {
				int hex = (buffer[0] << 24) | (buffer[1] << 16) | (buffer[2] << 8) | buffer[3];
				//				System.out.println(Integer.toHexString(hex));
				consumer.write(hex);
				buffer = new byte[4];
			}
			fileInputStream.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//		System.out.println("===================");
	}

	public void write(RenderedImage im) {
		try {
			File file = new File(root+"output.png");
			ImageIO.write(im, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		try {
			return new Dimension(canvas.getWidth(), canvas.getHeight());
		} catch (NullPointerException e) {
			return new Dimension(0,0);
		}
	}

	@Override
	public void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D g = (Graphics2D) graphics;
		g.drawImage(canvas,0,0,this);
	}
}
