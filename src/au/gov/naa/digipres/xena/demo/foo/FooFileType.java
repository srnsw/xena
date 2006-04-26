package au.gov.naa.digipres.xena.demo.foo;

import au.gov.naa.digipres.xena.kernel.type.FileType;


public class FooFileType extends FileType {
	
    @Override
    public String getName() {
		return "Foo";
	}

    @Override
	public String getMimeType() {
		return "text/plain";
	}
}
