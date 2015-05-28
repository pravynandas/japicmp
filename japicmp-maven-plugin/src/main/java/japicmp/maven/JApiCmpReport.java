package japicmp.maven;

import com.google.common.base.Optional;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * @goal cmp-report
 * @phase site
 */
public class JApiCmpReport extends AbstractMavenReport {
	/**
	 * @parameter
	 * @required
	 */
	private Version oldVersion;

	/**
	 * @parameter
	 * @required
	 */
	private Version newVersion;

	/**
	 * @parameter
	 */
	private Parameter parameter;

	/**
	 * @parameter
	 */
	private List<Dependency> dependencies;

	/**
	 * @parameter
	 */
	private String skip;

	/**
	 * Directory where reports will go.
	 *
	 * @parameter property="project.reporting.outputDirectory"
	 * @required
	 * @readonly
	 */
	private String outputDirectory;

	/**
	 * @component
	 * @required
	 */
	private ArtifactFactory artifactFactory;

	/**
	 * @component
	 * @required
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * @parameter default-value="${localRepository}"
	 * @required
	 */
	private ArtifactRepository localRepository;

	/**
	 * @parameter default-value="${project.remoteArtifactRepositories}"
	 * @required
	 */
	private List<ArtifactRepository> artifactRepositories;

	/**
	 * @parameter default-value="${project}"
	 * @required
	 */
	private MavenProject mavenProject;

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		try {
			JApiCmpMojo mojo = new JApiCmpMojo();
			MavenParameters mavenParameters = new MavenParameters(artifactRepositories, artifactFactory, localRepository, artifactResolver, mavenProject);
			PluginParameters pluginParameters = new PluginParameters(skip, newVersion, oldVersion, parameter, dependencies, Optional.<File>absent(), Optional.of(outputDirectory));
			mojo.executeWithParameters(pluginParameters, mavenParameters);
			Sink sink = getSink();
			List<String> lines = Files.readAllLines(Paths.get(outputDirectory, "japicmp.html"), Charset.forName("UTF-8"));
			for (String line : lines) {
				line = line.replace("<html>", "");
				line = line.replace("</html>", "");
				line = line.replace("<body>", "");
				line = line.replace("</body>", "");
				line = line.replace("<head>", "");
				line = line.replace("</head>", "");
				sink.rawText(line);
			}
			sink.close();
		} catch (Exception e) {
			throw new MavenReportException("Failed to generate report: " + e.getMessage(), e);
		}
	}

	@Override
	public String getOutputName() {
		return "japicmp-maven-plugin-report";
	}

	@Override
	public String getName(Locale locale) {
		return "japicmp-maven-plugin";
	}

	@Override
	public String getDescription(Locale locale) {
		return "japicmp is a maven plugin that computes the differences between two versions of a jar file/artifact.";
	}
}
