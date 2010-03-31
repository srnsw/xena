package au.gov.naa.digipres.xena.demo.foo;

import au.gov.naa.digipres.xena.kernel.type.XenaFileType;

public class XenaFooFileType extends XenaFileType {

	@Override
	public String getTag() {
		return FooNormaliser.FOO_OPENING_ELEMENT_QUALIFIED_NAME;
	}

	@Override
	public String getNamespaceUri() {
		return FooNormaliser.FOO_URI;
	}
}
