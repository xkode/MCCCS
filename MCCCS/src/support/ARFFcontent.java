package support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import workflow.Settings;

/**
 * @author klukas, pape
 */
public class ARFFcontent {
	
	private String relationName = "";
	private List<String> headerLines = new ArrayList<>();
	private ArrayList<File> columnData = new ArrayList<File>();
	private ArrayList<Long> columnDataLineCount = new ArrayList<Long>();
	
	public void appendColumnData(File f) throws IOException {
		BufferedReader tf = new BufferedReader(new FileReader(f));
		
		File tempF = File.createTempFile("tmp_", "", new File(f.getParent()));
		tempF.deleteOnExit();
		
		BufferedWriter dataLines = new BufferedWriter(new FileWriter(tempF));
		
		boolean header = true;
		String l = null;
		long lines = 0;
		while ((l = tf.readLine()) != null) {
			if (header) {
				if (!l.startsWith("%"))
					if (l.contains("@relation")) {
						String rn = l.substring(l.indexOf("@relation") + "@relation".length());
						rn = rn.trim();
						if (rn.startsWith("'"))
							rn = rn.substring(1, rn.length() - 1);
						if (relationName.length() > 0)
							relationName += "_";
						relationName += rn;
					} else
						if (!l.contains("@data"))
							headerLines.add(l);
						else
							header = false;
			} else {
				if (!l.startsWith("%")) {
					dataLines.write(l);
					dataLines.newLine();
					lines++;
				}
			}
		}
		columnData.add(tempF);
		columnDataLineCount.add(lines);
		
		tf.close();
		dataLines.close();
	}
	
	public void writeTo(File file, HashSet<Integer> removeColumns, String addLast) throws IOException {
		FileWriter tf = new FileWriter(file, true);
		
		tf.write("%" + System.lineSeparator());
		
		tf.write("@relation '" + relationName + "'" + System.lineSeparator());
		
		{
			int colIdx = 0;
			for (String hl : headerLines) {
				if (hl.startsWith("@attribute")) {
					colIdx++;
					if (!removeColumns.contains(colIdx))
						tf.write(hl + System.lineSeparator());
				} else
					tf.write(hl + System.lineSeparator());
			}
		}
		
		// add class info in case of addLst == ?
		if (addLast.contains("?"))
		{
			tf.write("@attribute class\t{");
			for (int idx = 0; idx < Settings.numberOfClasses; idx++) {
				if (idx < Settings.numberOfClasses - 1)
					tf.write("class" + idx + ",");
				else
					tf.write("class" + idx);
			}
			tf.write("}" + System.lineSeparator());
		}
		
		tf.flush();
		
		long maxLines = 0;
		for (Long lineCount : columnDataLineCount)
			if (lineCount > maxLines)
				maxLines = lineCount;
		
		tf.write("@data" + System.lineSeparator());
		tf.flush();
		
		LinkedHashMap<File, Stream<String>> file2stream = new LinkedHashMap<>();
		LinkedHashMap<File, Iterator<String>> file2input = new LinkedHashMap<>();
		
		for (File f : columnData) {
			file2stream.put(f, null);
			file2input.put(f, null);
		}
		
		for (long line = 0; line < maxLines; line++) {
			StringBuilder sb = new StringBuilder();
			
			for (File f : file2input.keySet()) {
				if (file2input.get(f) != null && !file2input.get(f).hasNext()) {
					file2stream.get(f).close();
					file2stream.remove(f);
					file2input.remove(f);
				}
				if (file2input.get(f) == null) {
					file2stream.put(f, Files.lines(f.toPath()));
					file2input.put(f, file2stream.get(f).iterator());
				}
				Iterator<String> s = file2input.get(f);
				if (s == null || !s.hasNext()) {
					s = Files.lines(f.toPath()).iterator();
				}
				if (sb.length() > 0)
					sb.append(",");
				if (s != null && s.hasNext())
					sb.append(s.next());
				else {
					break;
				}
			}
			
			String val = sb.toString();
			if (removeColumns.size() > 0) {
				String[] valArr = val.split(",");
				sb = new StringBuilder();
				for (int colIdx = 1; colIdx <= valArr.length; colIdx++) {
					if (!removeColumns.contains(colIdx)) {
						if (sb.length() > 0)
							sb.append(",");
						sb.append(valArr[colIdx - 1]);
					}
				}
				val = sb.toString();
			}
			
			tf.write(val);
			
			// append last
			if (addLast.length() > 0)
				tf.write("," + addLast);
			
			tf.write(System.lineSeparator());
		}
		
		for (File f : file2stream.keySet())
			file2stream.get(f).close();
		
		tf.write("%" + System.lineSeparator());
		
		tf.close();
	}
}
