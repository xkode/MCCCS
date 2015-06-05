package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.operation.channels.ChannelProcessing;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create color channel files in diverse color spaces. input: R/G/B image files
 * output:channel_xyz_x.png, ... (diverse set of color channels)
 * 
 * @author Christian Klukas
 */
public class RGB2ALL {

	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 4) {
			System.err
					.println("Params: [r] [g] [b] [source bit range 8/16/..] ! Return Code 1");
			System.exit(1);
		} else {
			File f_r = new File(args[0]);
			File f_g = new File(args[1]);
			File f_b = new File(args[2]);
			if (!f_r.exists()) {
				System.err.println("File RGB - R '" + f_r.getName()
						+ "' could not be found! Return Code 2");
				System.exit(2);
			} else if (!f_g.exists()) {
				System.err.println("File RGB - G '" + f_g.getName()
						+ "' could not be found! Return Code 2");
				System.exit(2);
			} else if (!f_b.exists()) {
				System.err.println("File RGB - B '" + f_b.getName()
						+ "' could not be found! Return Code 2");
				System.exit(2);
			} else {
				Image r = new Image(FileSystemHandler.getURL(f_r));
				Image g = new Image(FileSystemHandler.getURL(f_g));
				Image b = new Image(FileSystemHandler.getURL(f_b));
				float divistorFor8bitRangeTarget = (float) (Math.pow(2,
						Float.parseFloat(args[3])) / 256);
				ChannelProcessing cp = new ChannelProcessing(r.getAs1float(),
						g.getAs1float(), b.getAs1float(), r.getWidth(),
						r.getHeight(), divistorFor8bitRangeTarget);
				cp.get(Channel.XYZ_X)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_xyz_x.tif");
				cp.get(Channel.XYZ_Y)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_xyz_y.tif");
				cp.get(Channel.XYZ_Z)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_xyz_z.tif");

				cp.get(Channel.HSV_H)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_hsv_h.tif");
				cp.get(Channel.HSV_S)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_hsv_s.tif");
				cp.get(Channel.HSV_V)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_hsv_v.tif");

				cp.get(Channel.LAB_L)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_lab_l.tif");
				cp.get(Channel.LAB_A)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_lab_a.tif");
				cp.get(Channel.LAB_B)
						.getImage()
						.saveToFile(
								f_r.getParent() + File.separator
										+ "channel_lab_b.tif");
			}
		}
	}
}