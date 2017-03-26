package org.tegeltech.jenkins;

import com.tegeltech.jenkinscommits.domain.Changes;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

public class ChangesBuilder extends Builder implements SimpleBuildStep {

    private final String mainsrcdirs;
    private final String testsrcdirs;

    private ChangesWriter changesWriter;
    private ChangesRetriever changesRetriever;

    @DataBoundConstructor
    public ChangesBuilder(String mainsrcdirs, String testsrcdirs) {
        this.mainsrcdirs = mainsrcdirs;
        this.testsrcdirs = testsrcdirs;
    }


    public String getMainsrcdirs() {
        return mainsrcdirs;
    }

    public String getTestsrcdirs() {
        return testsrcdirs;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        PrintStream logger = listener.getLogger();

        String externalizableId = build.getExternalizableId();
        String jobName = externalizableId.split("#")[0];

        int buildNumber = build.getNumber();
        Changes input = getChangesRetriever().retrieve(jobName, buildNumber, mainsrcdirs, testsrcdirs);

        List<String> changedSources = input.getChangedSources();
        List<String> changedTests = input.getChangedTests();

        logger.println(String.format("Found %s sources.", changedSources.size()));
        logger.println(String.format("Found %s tests.", changedTests.size()));

        logger.println();
        logger.println("--------------------------");
        logger.println("       Sources found      ");
        logger.println("--------------------------");
        input.getChangedSources().forEach(logger::println);

        logger.println();
        logger.println("--------------------------");
        logger.println("        Tests found       ");
        logger.println("--------------------------");
        changedTests.forEach(logger::println);

        String changesPath = workspace.getRemote() + File.separator + "changes.csv";
        getChangesWriter().saveChanges(changedSources, changedTests, changesPath, logger);
    }

    private String squash(List<String> strings) {
        return strings.stream().collect(Collectors.joining(","));
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link ChangesBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p>
     * <p>
     * See {@code src/main/resources/hudson/plugins/hello_world/ChangesBuilder/*.jelly}
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {


        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }


        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Collect changes";
        }

    }

    public ChangesRetriever getChangesRetriever() {
        if (changesRetriever == null) {
            changesRetriever = new ChangesRetriever();
        }
        return changesRetriever;
    }

    public ChangesWriter getChangesWriter() {
        if (changesWriter == null) {
            changesWriter = new ChangesWriter();
        }
        return changesWriter;
    }

    /**
     * Setter for testing, because stapler doesn't know how to inject these.
     */
    public void setChangesRetriever(ChangesRetriever changesRetriever) {
        this.changesRetriever = changesRetriever;
    }

    /**
     * Setter for testing, because stapler doesn't know how to inject these.
     */
    public void setChangesWriter(ChangesWriter changesWriter) {
        this.changesWriter = changesWriter;
    }
}

