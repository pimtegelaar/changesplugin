package org.tegeltech.jenkins;

import com.tegeltech.jenkinscommits.domain.Changes;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.PrintStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangesBuilderTest {

    private static final String MAIN_DIRS = "src/main/java";
    private static final String TEST_DIRS = "src/test/java";

    @Mock
    private Run<?, ?> build;
    @Mock
    private Launcher launcher;
    @Mock
    private TaskListener listener;
    @Mock
    private File file;
    @Mock
    private ChangesRetriever changesRetriever;
    @Mock
    private ChangesWriter changesWriter;

    private ChangesBuilder changesBuilder;

    @Before
    public void setUp() throws Exception {
        changesBuilder = new ChangesBuilder(MAIN_DIRS, TEST_DIRS);
        changesBuilder.setChangesRetriever(changesRetriever);
        changesBuilder.setChangesWriter(changesWriter);
    }

    @Test
    public void perform() throws Exception {
        String changesPath = "/var/lib/jenkins/workspace/jobname/changes.csv";

        PrintStream logger = System.out;

        Changes changes = new Changes();
        changes.addSource("com.java.SomeClass");
        changes.addTest("com.java.SomeClassTest");

        when(file.getPath()).thenReturn("/var/lib/jenkins/workspace/jobname/");
        FilePath workspace = new FilePath(file);

        when(listener.getLogger()).thenReturn(logger);
        when(build.getExternalizableId()).thenReturn("jobname#3");
        when(build.getNumber()).thenReturn(3);
        when(changesRetriever.retrieve("jobname", 3, MAIN_DIRS, TEST_DIRS)).thenReturn(changes);

        changesBuilder.perform(build, workspace, launcher, listener);

        verify(changesWriter).saveChanges(changes.getChangedSources(), changes.getChangedTests(), changesPath, logger);
    }

}