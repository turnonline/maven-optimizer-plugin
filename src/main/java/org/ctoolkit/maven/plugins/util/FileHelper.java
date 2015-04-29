package org.ctoolkit.maven.plugins.util;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File helper
 *
 * @author <a href="mailto:pohorelec@comvai.com">Jozef Pohorelec</a>
 */
public class FileHelper
{
    private static Logger log = Logger.getLogger( FileHelper.class.getName() );

    /**
     * Return output directory - ${user.dir} + File.separator + output/path
     *
     * @param path output path
     * @return output directory
     */
    public static String getOutputDirectory( String path )
    {
        return System.getProperty( "user.dir" ) + File.separator + path;
    }

    /**
     * Creates output file
     *
     * @param path     output path
     * @param fileName output file name
     * @param content  content of output file
     * @return full path of created output file
     */
    public static String createOutputFile( String path, String fileName, String content )
    {
        String fullPathFileName = path + fileName;

        File file = new File( path );
        boolean created = file.mkdirs();
        if ( created || file.exists() )
        {
            File f = new File( fullPathFileName );
            writeToFile( f, content );

            return f.getAbsolutePath();
        }

        return fullPathFileName;
    }

    /**
     * Write content into file
     *
     * @param file    file to write
     * @param content content of file
     * @return full path of created file
     */
    public static String writeToFile( File file, String content )
    {
        try
        {
            Files.write( content.getBytes(), new File( file.getPath() ) );
        } catch ( IOException e ) {
            log.log( Level.SEVERE, "Error occurred during creating file '" + file.getAbsolutePath() + "'", e );
        }

        return file.getPath();
    }
}
