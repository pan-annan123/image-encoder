import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

public class Lightbeam {

	private BufferedImage encoded;

	private static final int prefix = 8, buf = 4;
	private static final boolean debug = false;

	private File file;

	public Lightbeam(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("Invalid file path");
		}
		this.file = file;
		
		//Find a square able to fit the bytes from the file
		long length = prefix + file.length();
		long pixels = length % buf == 0 ? length/buf : length/buf+1;
		int width = (int) Math.sqrt(pixels) + 1;

		//Create a new image
		encoded = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
	}
	
	public static Lightbeam of(File file) {
		return new Lightbeam(file);
	}
	
	public Lightbeam encode() {
		//track pixel writing
		int i = 0;
		int[] pixels = ((DataBufferInt) encoded.getRaster().getDataBuffer()).getData();
		
		//Prepare an input stream
		FileInputStream fis = null;
		byte[] buffer = new byte[buf];
	
		try {
			fis = new FileInputStream(file);
	
			//Obtain length information
			long length = file.length();
			int part1 = (int) (length >> 32);
			int part2 = (int) length;
			
			//Print original length and data from parts
			if (debug) {
				System.out.println("Original: "+length);
				System.out.println(part1);
				System.out.println(part2);
				System.out.println("===");
			}
			
			//Write length information to image
			pixels[i++] = part1;
			pixels[i++] = part2;
			
			//Read file and encode to image
			while ((fis.read(buffer)) > 0) {
				int hex = (0xff & buffer[0]) << 24 | (0xff & buffer[1]) << 16 | (0xff & buffer[2]) << 8 | (0xff & buffer[3]);
				pixels[i++] = hex;
				buffer = new byte[buf];
			}
			
			//Close the stream
			fis.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public BufferedImage getImage() {
		return encoded;
	}
	
	public Lightbeam peekImage(Consumer<BufferedImage> action) {
		action.accept(encoded);
		return this;
	}
	
	/**
	 * 
	 * @param output - filename (including extension)
	 */
	public void writeToImage(String output) {
		try {
			//Write image to file
			File imagefile = new File(file.getParentFile(), output);
			ImageIO.write(encoded, "png", imagefile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param output - filename (including extension)
	 */
	public void decode(String output) throws IOException {
		BufferedImage image = ImageIO.read(file);
		
		//Obtain image pixels in bytes
		final byte[] pixels;
		try {
			pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException("Lightbeam file is not an image");
		}
		
		//Prepare output stream
		FileOutputStream fos = new FileOutputStream(new File(this.file.getParentFile(), output));
		byte[] buffer = new byte[buf];

		//Read first 2 pixels to establish file length
		int len = getPixel(pixels, 0);
		int len2 = getPixel(pixels, 4);
		long length = (long) len << 32 | len2 & 0xFFFFFFFFL;
		
		//Calculate mod 4 to establish the pixels with partially written information
		int mod = (int) (length % buf);
		
		//Print decoding results
		if (debug) {
			System.out.println(len);
			System.out.println(len2);
			System.out.println("===");
			System.out.println("TOTAL PIXELS "+pixels.length);
			System.out.println("LENGTH "+length);
			System.out.println("MOD "+mod);
		}
		
		//from [prefix, prefix+length[, increment by pixel
		for (int pixel = prefix; pixel < prefix+length; pixel += buf) {
			buffer[0] = (byte) (pixels[pixel] & 0xff); // alpha
			buffer[3] = (byte) (pixels[pixel + 1] & 0xff); // blue
			buffer[2] = (byte) (pixels[pixel + 2] & 0xff); // green
			buffer[1] = (byte) (pixels[pixel + 3] & 0xff); // red
			
			//If the next pixel is not part of the file
			if (pixel + buf > prefix+length) {
				fos.write(buffer, 0, mod);
			} else {
				fos.write(buffer);
			}
		}

		//finalize and close stream
		fos.flush();
		fos.close();
	}
	
	private int getPixel(byte[] pixels, int start) {
		int pixel = 0;
		pixel += ((pixels[start  ] & 0xff) << 24); // alpha
		pixel += ((pixels[start+3] & 0xff) << 16); // red
		pixel += ((pixels[start+2] & 0xff) << 8); // green
		pixel +=  (pixels[start+1] & 0xff); // blue
		return pixel;
	}
	
}
