import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRProperties;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.apache.log4j.Logger;
import org.junit.Test;


public class TestPdf {

	Logger log = Logger.getLogger(TestPdf.class);
	private final File pdfPath = new File(System.getProperty("java.io.tmpdir"), "test.pdf");
	
	@Test
	public void jasper() throws SQLException, ParseException, JRException, FileNotFoundException {
		for(JRProperties.PropertySuffix x : JRProperties.getProperties("")) {
			log.info(x.getKey() + " = " + x.getValue());
		}
		JRSwapFileVirtualizer virtualizer = null;
		try {
			JasperReport jasperReport = JasperCompileManager.compileReport(TestPdf.class.getClassLoader().getResourceAsStream("test.jrxml"));
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			JRDataSource dataSource = new JRXmlDataSource(TestPdf.class.getClassLoader().getResourceAsStream("test.xml"), "/zb/doc");
			virtualizer = new JRSwapFileVirtualizer(51, new JRSwapFile(System.getProperty("java.io.tmpdir"), 2048, 1024));
			virtualizer.setReadOnly(false);
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRPdfExporterParameter.FORCE_LINEBREAK_POLICY, true);
			// exporter.setProperty(JRPdfExporterParameter.PROPERTY_FORCE_LINEBREAK_POLICY, Boolean.TRUE);
			exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, new JRExportProgressMonitor() {
				int count;
				@Override
				public void afterPageExport() {
					++count;
				}
			});
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			FileOutputStream out = new FileOutputStream(pdfPath);
			System.out.println("PDF: " + pdfPath);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();
		} finally {
			if (virtualizer != null) {
				virtualizer.cleanup();
			}
		}		
	}

}
