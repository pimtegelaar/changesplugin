package org.tegeltech.jenkins;

import com.tegeltech.jenkinscommits.JenkinsApiClient;
import com.tegeltech.jenkinscommits.domain.Changes;
import com.tegeltech.jenkinscommits.domain.JenkinsJob;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangesRetrieverTest {

    @Mock
    private JenkinsApiClient jenkinsApiClient;

    private ChangesRetriever changesRetriever;

    @Before
    public void setUp() throws Exception {
        changesRetriever = new ChangesRetriever(jenkinsApiClient);
    }

    @Test
    public void retrieve() throws Exception {
        String jobName = "commons-io";
        int buildNumber = 3;
        String mainsrcdrs = "src/main/java";
        String testsrcdirs = "src/test/java";
        JenkinsJob jenkinsJob = new JenkinsJob(jobName);
        String src1 = "src/main/java/org/apache/commons/io/FilenameUtils.java";
        String src2 = "src/main/java/org/apache/commons/io/serialization/package.html";
        String test1 = "src/test/java/org/apache/commons/io/monitor/AbstractMonitorTestCase.java";
        String test2 = "src/test/java/org/apache/commons/io/ByteOrderMarkTestCase.java";
        String test3 = "src/test/java/org/apache/commons/io/FileUtilsFileNewerTestCase.java";
        String other = "changes/changes.xml";
        String commits = buildCommitResponse(src1,src2,test1,test2,test3,other);

        when(jenkinsApiClient.fetchCommits(jenkinsJob,buildNumber)).thenReturn(commits);

        Changes changes = changesRetriever.retrieve(jobName, buildNumber, mainsrcdrs, testsrcdirs);

        assertThat(changes, is(notNullValue()));
    }


    private String buildCommitResponse(String... paths) {
        StringBuilder response = new StringBuilder();
        response.append("<CommitsResponse>");
        Arrays.stream(paths).forEach(path -> response.append(String.format("<affectedPath>%s</affectedPath>", path)));
        response.append("</CommitsResponse>");
        return response.toString();
    }
}