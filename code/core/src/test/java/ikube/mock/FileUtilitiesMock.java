package ikube.mock;

import ikube.toolkit.FileUtilities;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = FileUtilities.class)
public class FileUtilitiesMock {

	private static ByteArrayOutputStream CONTENTS;

	@Mock
	public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength) {
		return CONTENTS;
	}

	public static void setContents(final ByteArrayOutputStream contents) {
		FileUtilitiesMock.CONTENTS = contents;
	}
}