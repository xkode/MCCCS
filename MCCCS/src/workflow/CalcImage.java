package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;

/**
 * Create image by calculating the difference, sum, division or multiplication of two images.
 * 
 * @param image
 *           A
 * @param image
 *           B
 * @param output
 *           file
 * @param one
 *           of this: +,-,*,/, absdiff, and
 * @param RGB
 *           or Float mode
 * @return difference image according to operation mode
 * @author Christian Klukas
 */
public class CalcImage {
	
	private enum Operation {
		plus, minus, times, divide, absolute_of_difference, max, min, logratio, and
	};
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 5) {
			System.err.println(
					"Params: [image A (float)] [image B (float)] [target file] [-,+,*,/,absdiff,max,min,logratio, and] [Mode: 1 - float, 2 - RGB] ! Return Code 1");
			System.exit(1);
		} else {
			File f_a = new File(args[0]);
			File f_b = new File(args[1]);
			File f_out = new File(args[2]);
			String arg3 = args[3];
			String arg4 = args[4];
			Operation op = null;
			if (arg3.equals("-"))
				op = Operation.minus;
			if (arg3.equals("+"))
				op = Operation.plus;
			if (arg3.equals("*"))
				op = Operation.times;
			if (arg3.equals("/"))
				op = Operation.divide;
			if (arg3.equalsIgnoreCase("absdiff"))
				op = Operation.absolute_of_difference;
			if (arg3.equalsIgnoreCase("max"))
				op = Operation.max;
			if (arg3.equalsIgnoreCase("min"))
				op = Operation.min;
			if (arg3.equalsIgnoreCase("logratio"))
				op = Operation.logratio;
			if (arg3.equalsIgnoreCase("AND"))
				op = Operation.and;
			if (op == null) {
				System.err.println("Error - Operation  '" + args[3] + "' is unknown! Return Code 3");
				System.exit(3);
			}
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_a.exists()) {
					System.err.println("File image A '" + f_a.getName() + "' could not be found! Return Code 4");
					System.exit(4);
				} else {
					if (!f_b.exists()) {
						System.err.println("File image B '" + f_b.getName() + "' could not be found! Return Code 5");
						System.exit(5);
					} else {
						Image imgA = new Image(FileSystemHandler.getURL(f_a));
						Image imgB = new Image(FileSystemHandler.getURL(f_b));
						
						if (arg4.matches("1")) {
							float[] out = processFloat(op, imgA, imgB);
							new Image(imgA.getWidth(), imgA.getHeight(), out).saveToFile(f_out.getPath());
						} else {
							ImagePlus out = processRGB(op, imgA, imgB);
							new Image(out).saveToFile(f_out.getPath());
						}
					}
				}
		}
	}
	
	private static ImagePlus processRGB(Operation op, Image imgA, Image imgB) {
		ImagePlus imgplusA = imgA.getAsImagePlus();
		ImagePlus imgplusB = imgB.getAsImagePlus();
		ImagePlus out = new ImagePlus(op.toString(), imgplusA.getProcessor());
		
		ImageCalculator ic = new ImageCalculator();
		
		switch (op) {
			case logratio:
				break;
			case absolute_of_difference:
				break;
			case and:
				out = ic.run("AND create", imgplusA, imgplusB);
				break;
			case divide:
				break;
			case max:
				break;
			case min:
				break;
			case minus:
				break;
			case plus:
				break;
			case times:
				break;
			default:
				break;
		}
		
		return out;
	}
	
	private static float[] processFloat(Operation op, Image imgA, Image imgB) {
		float[] imgAf = imgA.getAs1float();
		float[] imgBf = imgB.getAs1float();
		if (imgAf.length != imgBf.length) {
			System.err.println("Error - Int file dimensions do not match! Return Code 3");
			System.exit(3);
		}
		float[] out = new float[imgAf.length];
		switch (op) {
			case logratio:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = (float) (Math.log(imgAf[i]) - Math.log(imgBf[i]));
				break;
			case max:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = Math.max(imgAf[i], imgBf[i]);
				break;
			case min:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = Math.min(imgAf[i], imgBf[i]);
				break;
			case absolute_of_difference:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = Math.abs(imgAf[i] - imgBf[i]);
				break;
			case divide:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = imgAf[i] / imgBf[i];
				break;
			case minus:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = imgAf[i] - imgBf[i];
				break;
			case plus:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = imgAf[i] + imgBf[i];
				break;
			case times:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = imgAf[i] * imgBf[i];
				break;
			case and:
				for (int i = 0; i < imgAf.length; i++)
					out[i] = (int) imgAf[i] & (int) imgBf[i];
				break;
		}
		return out;
	}
}
