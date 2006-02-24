package au.gov.naa.digipres.xena.plugin.dataset;
public class TrimCsvToXenaDatasetNormaliser extends CsvToXenaDatasetNormaliser {
	public TrimCsvToXenaDatasetNormaliser() {
		this.setHeaderFieldDelimiter(':');
		this.setFirstRowFieldNames(true);
		this.setOneFieldHeader(true);
	}
}
