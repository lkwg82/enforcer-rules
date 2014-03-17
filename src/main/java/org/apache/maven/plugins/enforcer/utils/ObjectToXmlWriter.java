package org.apache.maven.plugins.enforcer.utils;

import com.thoughtworks.xstream.XStream;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

public final class ObjectToXmlWriter {
    public static void writeXmlReport(File file, Object report) {

        if (null == file) {
            return; // skip this, xml report file is not set
        }

        final XStream xStream = new XStream();

        try {
            FileUtils.fileWrite(file.getAbsolutePath(), xStream.toXML(report));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}