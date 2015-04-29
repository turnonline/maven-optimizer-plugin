package org.ctoolkit.maven.plugins.optimizer;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:pohorelec@comvai.com">Jozef Pohorelec</a>
 */
public class ResourceOptimizerMojoTest
{
    private static final String LOCAL_REPOSITORY = System.getenv( "M2" );

    @Test
    public void testExecute() throws Exception
    {
        ResourceOptimizerMojo mojo = createMojo();

        mojo.execute();
    }

    private ResourceOptimizerMojo createMojo()
    {
        ResourceOptimizerMojo mojo = new ResourceOptimizerMojo();

        mojo.setCssPathToXml( "src/test/webapp/WEB-INF/css-config.xml" );
        mojo.setJsPathToXml( "src/test/webapp/WEB-INF/js-config.xml" );

        mojo.setCssOutputPath( "target/styles/" );
        mojo.setJsOutputPath( "target/scripts/" );

        Settings settings = new Settings();
        settings.setLocalRepository( LOCAL_REPOSITORY );
        mojo.setSettings( settings );

        MavenProject mavenProject = new MavenProject(  );
        mavenProject.setFile( new File(new File("pom.xml").getAbsolutePath().substring( 0,  new File("pom.xml").getAbsolutePath().lastIndexOf( File.separator )) + File.separator + "pom.xml") );
        mojo.setProject( mavenProject );

        return mojo;
    }
}