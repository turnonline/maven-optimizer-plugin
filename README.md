# Web resource optimizer

Web resource optimizer plugin optimize css and javascript files. It has two separate configuration files. One for css and one for javascript.

## 1. CSS configuration file
With this configuration you can configure folowing:

- define ouput css groups (one group is one output css file)
- define if css will be minified (Non minified css will only encode images to base64 and concatenate every css in group)
- output css file name
- path to external css (must be located inside of the class path)
- path to project style (relative to project root)

### 1.1 Minified css-config.xml example:
```xml
<groups>
  <group name="style.min.css"> <!-- Output css file name -->
    <!-- Path to external css (path must be located inside in the class-path) -->
    <external-css>/org/ctoolkit/wicket/comvai/component/external-styles.css</external-css>
    <!-- Path to project style css -->
    <css>/src/main/webapp/styles/not-minified.css</css>
  </group>
</groups>
```

### 1.2 Not minified css-config.xml example (used for already minified css):
```xml
<groups>
    <group name="style.min.css" minify="false"> <!-- Output css file name (output css will not be minified) -->
        <!-- Path to project style css -->
        <css>/src/main/webapp/styles/styles.css</css>
    </group>
</groups>
```

## 2. JS configuration file

With this configuration you can configure folowing:

- define ouput javascript groups (one group is one minified output js file)
- output javascript file name
- path to common javascript (relative to project root) - this file wil be appended to every group
- path to project javascript (relative to project root)
```xml
<groups>
  <common>
    <!-- Path to common project java-script. This file will be appended to every group -->
    <js>/src/main/webapp/scripts/script.js</js>
  </common>
  <group name="script.min.css"> <!-- Output java-script file name -->
    <!-- Path to project java-script -->
    <js>/src/main/webapp/scripts/script.js</js>
  </group>
</groups>
```

## Plugin configuration in maven pom.xml file

Configuration properties:

- **cssPathToXml** - path to  css configuration xml file
- **jsPathToXml** - path to javascript configuration xml file
- **cssOutputPath** - css output path
- **jsOutputPath** - javascript output path

```xml
<build>
  <plugins>           
    <plugin>
      <groupId>org.ctoolkit.maven.plugins</groupId>
      <artifactId>maven-optimizer-plugin</artifactId>
      <version>1.0</version>
      <executions>
        <execution>
          <phase>compile</phase>
          <goals>
            <goal>optimize</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <cssPathToXml>${project.basedir}/src/main/webapp/WEB-INF/css-config.xml</cssPathToXml>          <!-- path to css configuration file -->
        <jsPathToXml>${project.basedir}/src/main/webapp/WEB-INF/js-config.xml</jsPathToXml>             <!-- path to js configuration file -->
        <cssOutputPath>${project.basedir}/target/${project.build.finalName}/styles/</cssOutputPath>     <!-- css output path -->
        <jsOutputPath>${project.basedir}/target/${project.build.finalName}/scripts/</jsOutputPath>      <!-- js output path -->
        
        <!-- If you do not want to minify css use empty elements instead -->
        <cssPathToXml/>        
        <cssOutputPath/>

        <!-- If you do not want to minify js use empty elements instead -->
        <jsPathToXml/>
        <jsOutputPath/>        
 
      </configuration>
    </plugin>
  </plugins>
</build>
```