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
	private static int prefix = 8, buffer = 4;

	public World() {
		File file = new File(root+"input");
		if (!file.exists()) {
			System.out.println("MUST HAVE VALID ROOT AND INPUT FILE");
			return;
		}
		long l = file.length()+prefix;
		long p = l % buffer == 0? l/buffer : l/buffer+1;
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

		final int pixelLength = buffer;

		int len = 0;
		len += (((int) pixels[0] & 0xff) << 24); // alpha
		len += ((int) pixels[1] & 0xff); // blue
		len += (((int) pixels[2] & 0xff) << 8); // green
		len += (((int) pixels[3] & 0xff) << 16); // red
		int len2 = 0;
		len2 += (((int) pixels[4] & 0xff) << 24); // alpha
		len2 += ((int) pixels[5] & 0xff); // blue
		len2 += (((int) pixels[6] & 0xff) << 8); // green
		len2 += (((int) pixels[7] & 0xff) << 16); // red
		System.out.println(len);
		System.out.println(len2);
		System.out.println("===");

		long length = (long) len << 32 | len2 & 0xFFFFFFFFL;
		int mod = (int) (length % buffer);
		System.out.println("TOTAL PIXELS "+pixels.length);
		System.out.println("LENGTH "+length);
		System.out.println("MOD "+mod);
		for (int pixel = prefix; pixel < prefix+length; pixel += pixelLength) {
			byte[] buffer = new byte[pixelLength];
			buffer[0] = (byte) (pixels[pixel] & 0xff); // alpha
			buffer[3] = (byte) (pixels[pixel + 1] & 0xff); // blue
			buffer[2] = (byte) (pixels[pixel + 2] & 0xff); // green
			buffer[1] = (byte) (pixels[pixel + 3] & 0xff); // red
			if (pixel + pixelLength >= prefix+length) {
				fos.write(buffer, 0, mod);
			} else {
				fos.write(buffer);
			}
		}

		fos.flush();
		fos.close();
	}

	public void read(File file, Consumer consumer) {
		FileInputStream fileInputStream = null;

		byte[] buffer = new byte[World.buffer];

		try {
			fileInputStream = new FileInputStream(file);

			long ab = file.length();
			System.out.println("LENGTH "+ab);
			int c = (int) (ab >> 32);
			int d = (int) ab;
			System.out.println(c);
			System.out.println(d);
			System.out.println("===");
			consumer.write(c);
			consumer.write(d);
			while ((fileInputStream.read(buffer)) > 0) {
				int hex = (0xff & buffer[0]) << 24 | (0xff & buffer[1]) << 16 | (0xff & buffer[2]) << 8 | (0xff & buffer[3]);
//				 System.out.println(String.format("[0]: %1$d, [1]: %2$d, [2]: %3$d, [3]: %4$d", buffer[0],buffer[1], buffer[2], buffer[3]));
				consumer.write(hex);
				buffer = new byte[World.buffer];
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
