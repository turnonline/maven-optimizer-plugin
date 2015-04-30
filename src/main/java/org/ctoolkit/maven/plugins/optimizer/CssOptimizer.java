/*
 * Comvai maven optimizer plugin
 * Copyright (C) 2015 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.maven.plugins.optimizer;

import com.google.common.base.Charsets;
import com.google.common.css.compiler.commandline.ClosureCommandLineCompiler;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.ctoolkit.maven.plugins.util.FileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>CssOptimizer does followings:</p>
 * <ul>
 * <li>Encode images in css files to base64</li>
 * <li>Minify and merge all css files defined in group</li>
 * </ul>
 * <p>Example of configuration xml:</p>
 * <pre>
 *  &lt;groups&gt;
 *      &lt;group name="style.min.css"&gt; &lt;!-- Output css file name --&gt;
 *          &lt;!-- Path to external css (path must be located inside in the class-path) --&gt;
 *          &lt;external-css&gt;/org/apache/commons/io/test.css&lt;/external-css&gt;
 *          &lt;!-- Path to project style css --&gt;
 *          &lt;css&gt;src/main/webapp/styles/style.css&lt;/css&gt;
 *      &lt;/group&gt;
 *  &lt;/groups&gt;
 * </pre>
 *
 * @author <a href="mailto:pohorelec@comvai.com">Jozef Pohorelec</a>
 */
public class CssOptimizer
{
    private static Logger log = Logger.getLogger( CssOptimizer.class.getName() );

    private static final String URL_DATA_IMAGE_BASE64 = "url(\"data:image/{0};base64,{1}\")";

    private static final String DATA_IMAGE_PNG_BASE64 = "data:image";

    /**
     * Process css optimization
     *
     * @param pathToXml     path to css configuration xml
     * @param cssOutputPath css output path
     */
    public static void process( String pathToXml, String cssOutputPath, String baseDir )
    {
        if ( pathToXml == null || pathToXml.trim().isEmpty() )
        {
            log.info( "Css path to xml is not set. Skipping css optimization." );
            return;
        }

        if ( !new File( pathToXml ).exists() )
        {
            throw new IllegalArgumentException( "Css path to xml does not exists: " + pathToXml );
        }

        log.info( "Starting to optimize css..." );

        try
        {
            for ( CSSConfig cssConfig : getCssConfigList( pathToXml, baseDir ) )
            {
                String cssOutputFileName = cssConfig.getCssOutputName();
                List<String> cssPaths = new ArrayList<>();

                // create external css files
                for ( String externalCssContent : cssConfig.getExternalCssList() )
                {
                    String externalCssFileName = FileHelper.writeToFile( File.createTempFile( "externalCssFile", ".css" ), externalCssContent );
                    cssPaths.add( externalCssFileName );
                }

                // encode images in css
                for ( String cssCustomInputFileName : cssConfig.getCssList() )
                {
                    File cssCustomFile = new File( cssCustomInputFileName );

                    if ( cssCustomFile.exists() )
                    {
                        String cssCustomString = encodeImages( cssCustomFile );
                        String encodedCssFileName = FileHelper.writeToFile( File.createTempFile( "encodedCssFile", ".css" ), cssCustomString );

                        cssPaths.add( encodedCssFileName );
                    }
                }

                // minify css
                String cssOutputString;
                if ( cssConfig.getMinfy() )
                {
                    cssPaths.add( "-o" ); // set output file parameter as temp file (we will load its content into cssOutputString
                    File cssMinifiedFile = File.createTempFile( "minifiedCssFile", "css" );
                    cssPaths.add( cssMinifiedFile.getAbsolutePath() );
                    ClosureCommandLineCompiler.main( cssPaths.toArray( new String[cssPaths.size()] ) );

                    cssOutputString = Files.toString(cssMinifiedFile, Charsets.UTF_8);

                    log.info( "CSS minified output:\n===\n" + cssOutputString + "\n===\n");
                }
                else
                {
                    StringBuilder output = new StringBuilder();
                    for ( String cssPath : cssPaths )
                    {
                        output.append( Files.toString( new File( cssPath ), Charsets.UTF_8 ) );
                    }

                    cssOutputString = output.toString();
                }


                // create final css file
                String optimizedCss = FileHelper.createOutputFile( cssOutputPath, cssOutputFileName, cssOutputString );

                log.info( ">>> Css optimization finished successfully. Optimized css file can be found at: " + optimizedCss );
            }
        }
        catch ( Exception e )
        {
            log.log( Level.SEVERE, "Error occurred during processing css: ", e );
        }
    }

    /**
     * Encode images in css file and return encoded css content
     *
     * @param cssCustomFile css custom file
     * @return css content with encoded images to base64
     * @throws IOException if IOException occurs
     */
    private static String encodeImages( File cssCustomFile ) throws IOException
    {
        String cssCustomString = Files.toString( cssCustomFile, Charsets.UTF_8 );

        // match all images paths
        Matcher m = Pattern.compile( "url\\(.*\\)" ).matcher( cssCustomString );

        StringBuffer sb = new StringBuffer();
        while ( m.find() )
        {
            String imgSrc = m.group( 0 ).replace( "url(\"", "" ).replace( "\")", "" );

            if ( imgSrc.startsWith( DATA_IMAGE_PNG_BASE64 ) ) // image is already encoded
            {
                continue;
            }

            File file = cssCustomFile.getParentFile();

            for ( final String next : Arrays.asList( imgSrc.split( "/" ) ) )
            {
                if ( "..".equals( next ) )
                {
                    file = file.getParentFile();
                }
                else
                {
                    File[] files = file.listFiles( new FilenameFilter()
                    {
                        @Override
                        public boolean accept( File dir, String name )
                        {
                            return name.equals( next );
                        }
                    } );

                    if ( files.length == 0 )
                    {
                        continue;
                    }

                    file = files[0];
                }

                if ( file.isFile() )
                {
                    break;
                }

            }

            if ( file.isFile() )
            {
                String extension = file.getName().substring( file.getName().indexOf( "." ) + 1 );
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write( ImageIO.read( file ), extension, baos );

                String encodedImage = DatatypeConverter.printBase64Binary( baos.toByteArray() );

                m.appendReplacement( sb, MessageFormat.format( URL_DATA_IMAGE_BASE64, extension, encodedImage ) );
            }
        }
        m.appendTail( sb );

        return sb.toString();
    }

    /**
     * Return list of {@link CSSConfig} objects which contains name of output css file and source css files
     *
     * @param pathToXml path to configuration xml
     * @return list of {@link CSSConfig} objects
     * @throws Exception if exception occurs
     */
    private static List<CSSConfig> getCssConfigList( String pathToXml, String baseDir ) throws Exception
    {
        List<CSSConfig> list = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse( pathToXml );

        Element groups = ( Element ) document.getElementsByTagName( "groups" ).item( 0 );
        NodeList groupList = groups.getElementsByTagName( "group" );
        for ( int i = 0; i < groupList.getLength(); i++ )
        {
            CSSConfig cssConfig = new CSSConfig();

            Element group = ( Element ) groupList.item( i );
            cssConfig.setCssOutputName( group.getAttribute( "name" ) );

            String minfy = group.getAttribute( "minify" );
            if ( "".equals( minfy ) )
            {
                cssConfig.setMinfy( true );
            }
            else
            {
                cssConfig.setMinfy( Boolean.valueOf( minfy ) );
            }

            // add external css
            NodeList cssList = group.getElementsByTagName( "external-css" );
            for ( int j = 0; j < cssList.getLength(); j++ )
            {
                Element cs = ( Element ) cssList.item( j );

                InputStream cssExternalResource = CssOptimizer.class.getResourceAsStream( cs.getTextContent() );
                if ( cssExternalResource != null )
                {
                    cssConfig.addExternalCss( CharStreams.toString( new InputStreamReader( cssExternalResource, Charsets.UTF_8 ) ) );
                }
                else
                {
                    log.warning( "External css does not exists: " + cs.getTextContent() );
                }
            }

            // add custom css
            cssList = group.getElementsByTagName( "css" );
            for ( int j = 0; j < cssList.getLength(); j++ )
            {
                Element cs = ( Element ) cssList.item( j );

                String fullPath = baseDir + cs.getTextContent();
                File file = new File( fullPath );
                if ( file.exists() )
                {
                    log.info( "Adding css path: " + fullPath );
                    cssConfig.addCss( fullPath );
                }
                else
                {
                    log.warning( "Css file does not exists - therefore will be skipped for optimization: " + fullPath );
                }
            }

            list.add( cssConfig );
        }

        return list;
    }

    /**
     * CSS configuration class
     */
    private static class CSSConfig
    {
        /**
         * Name of ouptut class name
         */
        private String cssOutputName;

        /**
         * List of absolute paths to css to minify
         */
        private List<String> cssList = new ArrayList<>();

        /**
         * List of absolute paths to external css to minify
         */
        private List<String> externalCssList = new ArrayList<>();

        /**
         * Flag for minifying css
         */
        private Boolean minfy;

        public void setCssOutputName( String cssOutputName )
        {
            this.cssOutputName = cssOutputName;
        }

        public String getCssOutputName()
        {
            return cssOutputName;
        }

        public List<String> getCssList()
        {
            return cssList;
        }

        public void addCss( String css )
        {
            cssList.add( css );
        }

        public List<String> getExternalCssList()
        {
            return externalCssList;
        }

        public void addExternalCss( String externalCss )
        {
            externalCssList.add( externalCss );
        }

        public Boolean getMinfy()
        {
            return minfy;
        }

        public void setMinfy( Boolean minfy )
        {
            this.minfy = minfy;
        }

        @Override
        public String toString()
        {
            return "CSSConfig{" +
                    "cssOutputName='" + cssOutputName + '\'' +
                    ", cssList=" + cssList +
                    ", externalCssList=" + externalCssList +
                    ", minfy=" + minfy +
                    '}';
        }
    }
}
