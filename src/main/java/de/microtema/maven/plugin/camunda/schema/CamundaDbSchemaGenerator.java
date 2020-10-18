package de.microtema.maven.plugin.camunda.schema;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.camunda.bpm.engine.context.DelegateExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CamundaDbSchemaGenerator {

    static final String RESOURCE_PATTERN = "classpath:org/camunda/bpm/engine/db/create/activiti.oracle.*.sql";
    /**
     * Select 'GRANT SELECT,INSERT,UPDATE,DELETE ON '||Table_Name||' TO CAMUNDA_app;'  From All_Tables Where Owner='CAMUNDA_DATA'
     */
    static final String GRANT_TABLE_TEMPLATE = "" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_PROPERTY TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_BYTEARRAY TO CAMUNDA_app;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_SCHEMA_LOG TO CAMUNDA_app;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DEPLOYMENT TO CAMUNDA_app;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXECUTION TO CAMUNDA_app;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOB TO CAMUNDA_app;             \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOBDEF TO CAMUNDA_app;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_PROCDEF TO CAMUNDA_app;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_TASK TO CAMUNDA_app;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_IDENTITYLINK TO CAMUNDA_app;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_VARIABLE TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EVENT_SUBSCR TO CAMUNDA_app;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_INCIDENT TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_AUTHORIZATION TO CAMUNDA_app;   \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_FILTER TO CAMUNDA_app;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_METER_LOG TO CAMUNDA_app;       \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXT_TASK TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_BATCH TO CAMUNDA_app;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_PROCINST TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ACTINST TO CAMUNDA_app;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_TASKINST TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_VARINST TO CAMUNDA_app;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_DETAIL TO CAMUNDA_app;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_IDENTITYLINK TO CAMUNDA_app;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_COMMENT TO CAMUNDA_app;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ATTACHMENT TO CAMUNDA_app;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_OP_LOG TO CAMUNDA_app;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_INCIDENT TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_JOB_LOG TO CAMUNDA_app;         \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_BATCH TO CAMUNDA_app;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_EXT_TASK_LOG TO CAMUNDA_app;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_GROUP TO CAMUNDA_app;           \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_MEMBERSHIP TO CAMUNDA_app;      \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_USER TO CAMUNDA_app;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_INFO TO CAMUNDA_app;            \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT TO CAMUNDA_app;          \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT_MEMBER TO CAMUNDA_app;   \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_CASE_DEF TO CAMUNDA_app;        \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_DEF TO CAMUNDA_app;    \n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_REQ_DEF TO CAMUNDA_app;\n" +
            "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_CASE_EXECUTION TO CAMUNDA_app; ";

    static final List<String> SQL_SCRIPT_ORDER = Arrays.asList(
            "activiti.oracle.create.engine.sql",
            "activiti.oracle.create.history.sql",
            "activiti.oracle.create.identity.sql",
            "activiti.oracle.create.case.engine.sql",
            "activiti.oracle.create.case.history.sql",
            "activiti.oracle.create.decision.engine.sql",
            "activiti.oracle.create.decision.history.sql"
    );

    public static void main2(String[] args) throws Exception {

        Validate.isTrue(args.length > 0, "Output Path is required!");

        String outputPath = args[0];

        Collection<Resource> resources = getResources();

        resources.forEach(it -> System.out.println("Read SQL script from component '" + getComponent(it.getFilename()) + "' with resource -> " + it.getFilename()));

        String sql = getSqlScript(resources);

        try (PrintWriter out = new PrintWriter(outputPath)) {
            out.println(sql);
        }

        System.out.println("Create DB Migration scripts -> " + outputPath);
    }

    static String getComponent(String filename) {

        return filename.replace("activiti.oracle.create.", "").replace(".sql", "");
    }


    static String getSqlScript(Collection<Resource> resources) throws IOException {

        StringBuilder builder = new StringBuilder();

        for (Resource resource : resources) {
            String sql = IOUtils.toString(resource.getInputStream());
            builder.append(sql);
        }

        builder.append("\n");
        builder.append(GRANT_TABLE_TEMPLATE);

        return builder.toString();
    }

    private static Collection<Resource> getResources() {

        PathMatchingResourcePatternResolver PATTERN_RESOLVER = new PathMatchingResourcePatternResolver(DelegateExecutionContext.class.getClassLoader());

        Map<String, Resource> map = new HashMap<>();

        try {

            Arrays.asList(PATTERN_RESOLVER.getResources(RESOURCE_PATTERN)).forEach(it -> map.put(it.getFilename(), it));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return SQL_SCRIPT_ORDER.stream().map(map::get).collect(Collectors.toList());
    }
}
