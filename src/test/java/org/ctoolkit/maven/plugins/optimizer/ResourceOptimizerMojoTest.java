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