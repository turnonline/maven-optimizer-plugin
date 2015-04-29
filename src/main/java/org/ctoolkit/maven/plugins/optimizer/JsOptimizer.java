package org.ctoolkit.maven.plugins.optimizer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
import org.apache.maven.settings.Settings;
import org.ctoolkit.maven.plugins.util.FileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>JsOptimizer does followings:</p>
 * <ul>
 * <li>minify and merge all js files defined in group</li>
 * </ul>
 * <p/>
 * <p>Example of configuration xml</p>
 * <pre>
 *  &lt;groups&gt;
 *      &lt;common&gt;
 *          &lt;!-- Path to common project java-script. This file will be appended to every group --&gt;
 *          &lt;js&gt;src/main/webapp/scripts/script.js&lt;/js&gt;
 *      &lt;/common&gt;
 *      &lt;group name="script.min.css"&gt; &lt;!-- Output java-script file name --&gt;
 *          &lt;!-- Path to project java-script --&gt;
 *          &lt;js&gt;src/main/webapp/scripts/script.js&lt;/js&gt;
 *      &lt;/group&gt;
 *  &lt;/groups&gt;
 * </pre>
 *
 * @author <a href="mailto:pohorelec@comvai.com">Jozef Pohorelec</a>
 */
public class JsOptimizer
{
    private static Logger log = Logger.getLogger( JsOptimizer.class.getName() );

    /**
     * Closure root directory
     */
    private static String closureRoot = new File( JsOptimizer.class.getResource( "/org/ctoolkit/maven/plugins/optimizer/gc_closure/goog/base.js" ).getPath() ).getParent();

    /**
     * Closure library holder list
     */
    private static List<String> closureLibraryList = new ArrayList<String>();

    /**
     * Init closure libraries
     */
    static
    {
        addClosureLib( "base.js" );
        addClosureLib( "dom" + File.separator + "dom.js" );
        addClosureLib( "array" + File.separator + "array.js" );
        addClosureLib( "dom" + File.separator + "tagname.js" );
        addClosureLib( "dom" + File.separator + "classes.js" );
        addClosureLib( "math" + File.separator + "coordinate.js" );
        addClosureLib( "math" + File.separator + "size.js" );
        addClosureLib( "object" + File.separator + "object.js" );
        addClosureLib( "string" + File.separator + "string.js" );
        addClosureLib( "useragent" + File.separator + "useragent.js" );
        addClosureLib( "asserts" + File.separator + "asserts.js" );
        addClosureLib( "debug" + File.separator + "error.js" );
        addClosureLib( "net" + File.separator + "xhrio.js" );
        addClosureLib( "debug" + File.separator + "logger.js" );
        addClosureLib( "debug" + File.separator + "entrypointregistry.js" );
        addClosureLib( "debug" + File.separator + "errorhandlerweakdep.js" );
        addClosureLib( "events" + File.separator + "eventtarget.js" );
        addClosureLib( "json" + File.separator + "json.js" );
        addClosureLib( "net" + File.separator + "errorcode.js" );
        addClosureLib( "net" + File.separator + "eventtype.js" );
        addClosureLib( "net" + File.separator + "httpstatus.js" );
        addClosureLib( "net" + File.separator + "xmlhttp.js" );
        addClosureLib( "net" + File.separator + "xhrmonitor.js" );
        addClosureLib( "structs" + File.separator + "structs.js" );
        addClosureLib( "structs" + File.separator + "map.js" );
        addClosureLib( "uri" + File.separator + "utils.js" );
        addClosureLib( "uri" + File.separator + "uri.js" );
        addClosureLib( "debug" + File.separator + "logbuffer.js" );
        addClosureLib( "debug" + File.separator + "logrecord.js" );
        addClosureLib( "disposable" + File.separator + "disposable.js" );
        addClosureLib( "events" + File.separator + "events.js" );
        addClosureLib( "timer" + File.separator + "timer.js" );
        addClosureLib( "net" + File.separator + "wrapperxmlhttpfactory.js" );
        addClosureLib( "iter" + File.separator + "iter.js" );
        addClosureLib( "disposable" + File.separator + "idisposable.js" );
        addClosureLib( "events" + File.separator + "browserevent.js" );
        addClosureLib( "events" + File.separator + "event.js" );
        addClosureLib( "events" + File.separator + "eventwrapper.js" );
        addClosureLib( "events" + File.separator + "pools.js" );
        addClosureLib( "net" + File.separator + "xmlhttpfactory.js" );
        addClosureLib( "debug" + File.separator + "debug.js" );
        addClosureLib( "structs" + File.separator + "set.js" );
        addClosureLib( "dom" + File.separator + "browserfeature.js" );
        addClosureLib( "reflect" + File.separator + "reflect.js" );
        addClosureLib( "events" + File.separator + "listener.js" );
        addClosureLib( "structs" + File.separator + "simplepool.js" );
        addClosureLib( "useragent" + File.separator + "jscript.js" );
        addClosureLib( "events" + File.separator + "browserfeature.js" );
        addClosureLib( "events" + File.separator + "eventtype.js" );
        addClosureLib( "fx" + File.separator + "fx.js" );
        addClosureLib( "fx" + File.separator + "dom.js" );
        addClosureLib( "fx" + File.separator + "animation.js" );
        addClosureLib( "fx" + File.separator + "easing.js" );
        addClosureLib( "color" + File.separator + "color.js" );
        addClosureLib( "style" + File.separator + "style.js" );
        addClosureLib( "color" + File.separator + "names.js" );
        addClosureLib( "math" + File.separator + "math.js" );
        addClosureLib( "math" + File.separator + "box.js" );
        addClosureLib( "math" + File.separator + "rect.js" );
        addClosureLib( "net" + File.separator + "cookies.js" );
    }

    // Closure arguments
    private static final String ARG_MANAGE_CLOSURE_DEPENDENCIES = "--manage_closure_dependencies";
    private static final String ARG_COMPILATION_LEVEL = "--compilation_level";
    private static final String ARG_JS_OUTPUT_FILE = "--js_output_file";
    private static final String ARG_JS = "--js";

    /**
     * Process js optimization
     *
     * @param pathToXml    path to java-script configuration xml
     * @param jsOutputPath java-script output path
     * @param settings     maven {@link Settings}
     */
    public static void process( String pathToXml, String jsOutputPath, Settings settings )
    {
        String outputDirectory = FileHelper.getOutputDirectory( jsOutputPath );

        if ( pathToXml == null || pathToXml.trim().isEmpty() )
        {
            log.info( "Javascript path to xml is not set. Skipping javascript optimization." );
            return;
        }

        if ( !new File( pathToXml ).exists() )
        {
            throw new IllegalArgumentException( "Javascript path to xml does not exists: " + pathToXml );
        }

        log.info( "Starting to optimize javascript..." );

        try
        {
            for ( JSConfig JSConfig : getJsConfigList( pathToXml ) )
            {
                String jsFile = JSConfig.getOutputJavascriptName();
                final String fullJsFilePath = outputDirectory + jsFile;

                new File( outputDirectory ).mkdirs();

                List<String> args = new ArrayList<>();

                for ( String js : JSConfig.getJsList() )
                {
                    args.add( ARG_JS );
                    args.add( js );
                }

                args.add( ARG_MANAGE_CLOSURE_DEPENDENCIES );
                args.add( ARG_COMPILATION_LEVEL );
                args.add( "ADVANCED_OPTIMIZATIONS" );
                args.add( ARG_JS_OUTPUT_FILE );
                args.add( fullJsFilePath );

                try
                {
                    // make an ugly security manager override to be able prevent js closure exit
                    System.setSecurityManager( new MySecurityManager() );
                    CommandLineRunner.main( args.toArray( new String[args.size()] ) );
                }
                catch ( NoStopException e )
                {
                    String jsOutputString = Files.toString( new File( fullJsFilePath ), Charsets.UTF_8 );

                    log.info( "JS minified output:\n===\n" + jsOutputString + "===\n" );
                    log.info( ">>> Javascript optimization finished successfully. Optimized js file can be found at: " + fullJsFilePath );
                }
                finally
                {
                    System.setSecurityManager( null );
                }
            }
        }
        catch ( Exception e )
        {
            log.log( Level.SEVERE, "Error occurred during processing js: ", e );
        }
    }

    /**
     * Return list of {@link JSConfig} objects which contains name of output java-script file and source java-script files
     *
     * @param pathToXml path to xml
     * @return list of {@link JSConfig} objects
     * @throws Exception if exception occurs
     */
    private static List<JSConfig> getJsConfigList( String pathToXml ) throws Exception
    {
        List<JSConfig> list = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse( pathToXml );

        Element groups = ( Element ) document.getElementsByTagName( "groups" ).item( 0 );
        NodeList groupList = groups.getElementsByTagName( "group" );
        for ( int i = 0; i < groupList.getLength(); i++ )
        {
            JSConfig jsConfig = new JSConfig();

            Element group = ( Element ) groupList.item( i );
            jsConfig.setOutputJavascriptName( group.getAttribute( "name" ) );

            // add closure js
            for ( String closureJs : closureLibraryList )
            {
                jsConfig.addJs( closureJs );
            }

            // add common javascripts
            parseCommonJs( groups, jsConfig );

            // add custom javascripts
            NodeList jsList = group.getElementsByTagName( "js" );
            for ( int j = 0; j < jsList.getLength(); j++ )
            {
                Element js = ( Element ) jsList.item( j );

                File file = new File( js.getTextContent() );
                if ( file.exists() )
                {
                    jsConfig.addJs( file.getAbsolutePath() );

                }
                else
                {
                    log.warning( "Javascript file does not exists - therefore will be skipped for optimization: " + js.getTextContent() );
                }
            }

            list.add( jsConfig );
        }

        return list;
    }

    /**
     * Parse common java-script files
     *
     * @param groups   groups
     * @param jsConfig js config
     */
    private static void parseCommonJs( Element groups, JSConfig jsConfig )
    {
        Element common = ( Element ) groups.getElementsByTagName( "common" ).item( 0 );
        NodeList commonJsList = common.getElementsByTagName( "js" );
        for ( int k = 0; k < commonJsList.getLength(); k++ )
        {
            Element commonJs = ( Element ) commonJsList.item( k );

            File file = new File( commonJs.getTextContent() );
            if ( file.exists() )
            {
                jsConfig.addJs( file.getAbsolutePath() );
            }
            else
            {
                log.warning( "Javascript file does not exists - therefore will be skipped for optimization: " + commonJs.getTextContent() );
            }
        }
    }

    /**
     * Java-script configuration class
     */
    private static class JSConfig
    {
        private String outputJavascriptName;

        private List<String> jsList = new ArrayList<>();

        public void setOutputJavascriptName( String outputJavascriptName )
        {
            this.outputJavascriptName = outputJavascriptName;
        }

        public String getOutputJavascriptName()
        {
            return outputJavascriptName;
        }

        public List<String> getJsList()
        {
            return jsList;
        }

        public void addJs( String js )
        {
            jsList.add( js );
        }
    }

    /**
     * Add closure library java-script file
     *
     * @param jsLib relative path to closure java-script file
     */
    private static void addClosureLib( String jsLib )
    {
        closureLibraryList.add( closureRoot + File.separator + jsLib );
    }

    private static class MySecurityManager
            extends SecurityManager
    {

        @Override
        public void checkPermission( Permission perm )
        {
            // do nothing
        }

        @Override
        public void checkExit( int status )
        {
            if ( status == 0 )
            {
                throw new NoStopException();
            }
        }
    }

    private static class NoStopException
            extends RuntimeException
    {
    }
}
