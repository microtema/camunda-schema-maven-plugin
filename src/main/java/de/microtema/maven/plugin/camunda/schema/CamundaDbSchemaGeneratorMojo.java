package de.microtema.maven.plugin.camunda.schema;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.camunda.bpm.engine.context.DelegateExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.VALIDATE)
public class CamundaDbSchemaGeneratorMojo extends AbstractMojo {

    static final String RESOURCE_PATTERN = "classpath:org/camunda/bpm/engine/db/create/activiti.%s.*.sql";

    static final List<String> SQL_SCRIPT_ORDER = Arrays.asList(
            "activiti.oracle.create.engine.sql",
            "activiti.oracle.create.history.sql",
            "activiti.oracle.create.identity.sql",
            "activiti.oracle.create.case.engine.sql",
            "activiti.oracle.create.case.history.sql",
            "activiti.oracle.create.decision.engine.sql",
            "activiti.oracle.create.decision.history.sql"
    );

    static final String GRANT_TABLE_TEMPLATE = "" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_PROPERTY TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_BYTEARRAY TO userName;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_SCHEMA_LOG TO userName;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DEPLOYMENT TO userName;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXECUTION TO userName;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOB TO userName;             \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOBDEF TO userName;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_PROCDEF TO userName;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_TASK TO userName;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_IDENTITYLINK TO userName;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_VARIABLE TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EVENT_SUBSCR TO userName;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_INCIDENT TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_AUTHORIZATION TO userName;   \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_FILTER TO userName;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_METER_LOG TO userName;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXT_TASK TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_BATCH TO userName;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_PROCINST TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ACTINST TO userName;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_TASKINST TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_VARINST TO userName;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_DETAIL TO userName;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_IDENTITYLINK TO userName;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_COMMENT TO userName;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ATTACHMENT TO userName;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_OP_LOG TO userName;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_INCIDENT TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_JOB_LOG TO userName;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_BATCH TO userName;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_EXT_TASK_LOG TO userName;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_GROUP TO userName;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_MEMBERSHIP TO userName;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_USER TO userName;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_INFO TO userName;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT TO userName;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT_MEMBER TO userName;   \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_CASE_DEF TO userName;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_DEF TO userName;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_REQ_DEF TO userName;\n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_CASE_EXECUTION TO userName; ";

    private final Log log = new SystemStreamLog();

    @Parameter(property = "schema", defaultValue = "oracle")
    String schema = "oracle";

    @Parameter(property = "user-name", defaultValue = "userName")
    String userName = "userName";

    @Parameter(property = "output-path", defaultValue = "/src/main/resources/db/migration/V1.0__Create_Tables.sql")
    String outputPath = "/src/main/resources/db/migration/V1.0__Create_Tables.sql";

    @Override
    public void execute() {

        preExecute();

        Collection<Resource> resources = getResources();

        String sql = getSqlScript(resources);

        generateSQlScriptFile(sql);

        logMessage("Generate Camunda DB Schema " + schema + " for user " + userName + " -> " + outputPath);
    }

    void generateSQlScriptFile(String sql) {
        try (PrintWriter out = new PrintWriter(outputPath)) {
            out.println(sql);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    void preExecute() {

        try {
            File currentFile = new File("").getCanonicalFile();

            File outputPathFile = new File(currentFile, outputPath);

            File parentFile = outputPathFile.getParentFile();

            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            outputPath = outputPathFile.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    void logMessage(String message) {
        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }

    Collection<Resource> getResources() {

        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver(DelegateExecutionContext.class.getClassLoader());

        Map<String, Resource> map = new HashMap<>();

        String resourcePattern = String.format(RESOURCE_PATTERN, schema);

        try {

            Arrays.asList(patternResolver.getResources(resourcePattern)).forEach(it -> map.put(it.getFilename(), it));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Resource> resources = SQL_SCRIPT_ORDER.stream().map(map::get).collect(Collectors.toList());

        resources.forEach(it -> log.info("Read SQL script from component '" + getComponent(it.getFilename(), schema) + "' with resource -> " + it.getFilename()));

        return resources;
    }

    String getComponent(String filename, String schema) {

        return filename.replace("activiti." + schema + ".create.", "").replace(".sql", StringUtils.EMPTY);
    }

    String getSqlScript(Collection<Resource> resources) {

        StringBuilder builder = new StringBuilder();

        for (Resource resource : resources) {

            try (InputStream is = resource.getInputStream()) {
                String sql = IOUtils.toString(is, Charset.defaultCharset());
                builder.append(sql);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        String grantTableTemplate = getGrantTableTemplate();

        builder.append("\n");
        builder.append(grantTableTemplate);

        return builder.toString();
    }

    String getGrantTableTemplate() {

        return GRANT_TABLE_TEMPLATE.replaceAll("userName", userName);
    }
}
