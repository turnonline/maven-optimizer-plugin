package org.ctoolkit.maven.plugins.optimizer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * Web resource optimizer mojo
 *
 * @author <a href="mailto:pohorelec@comvai.com">Jozef Pohorelec</a>
 * @goal optimize
 * @phase compile
 */
public class ResourceOptimizerMojo
        extends AbstractMojo
{
    /**
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    protected Settings settings;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter cssPathToXml
     */
    private String cssPathToXml;

    /**
     * @parameter cssOutputPath
     */
    private String cssOutputPath;

    /**
     * @parameter jsPathToXml
     */
    private String jsPathToXml;

    /**
     * @parameter jsOutputPath
     */
    private String jsOutputPath;

    /**
     * Set path to css configuration xml
     *
     * @parameter cssPathToXml
     */
    public void setCssPathToXml( String cssPathToXml )
    {
        this.cssPathToXml = cssPathToXml;
    }

    /**
     * Set css output path
     *
     * @parameter
     */
    protected void setCssOutputPath( String cssOutputPath )
    {
        this.cssOutputPath = cssOutputPath;
    }

    /**
     * Set path to java-script configuration xml
     *
     * @param jsPathToXml path to java-script configuration xml
     */
    public void setJsPathToXml( String jsPathToXml )
    {
        this.jsPathToXml = jsPathToXml;
    }

    /**
     * Set java-script output path
     *
     * @param jsOutputPath java-script output path
     */
    public void setJsOutputPath( String jsOutputPath )
    {
        this.jsOutputPath = jsOutputPath;
    }

    /**
     * Set maven settings object
     *
     * @param settings maven settings object
     */
    public void setSettings( Settings settings )
    {
        this.settings = settings;
    }

    /**
     * Set maven project
     *
     * @param project maven project
     */
    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    /**
     * Execute optimize mojo
     *
     * @throws MojoExecutionException if {@link MojoExecutionException} occurs
     * @throws MojoFailureException   if {@link MojoFailureException} occurs
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        CssOptimizer.process( cssPathToXml, cssOutputPath, project.getBasedir().getAbsolutePath() );
        JsOptimizer.process( jsPathToXml, jsOutputPath, settings );
    }
}
