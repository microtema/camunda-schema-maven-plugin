package de.microtema.maven.plugin.camunda.schema;

import lombok.SneakyThrows;
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
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.VALIDATE)
public class CamundaDbSchemaGeneratorMojo extends AbstractMojo {

    static final String RESOURCE_PATTERN = "classpath:org/camunda/bpm/engine/db/create/activiti.%s.*.sql";

    static final Pattern CREATE_TABLE_PATTERN = Pattern.compile("create table\\s([A-za-z0-9\\.\\$\\{\\}]*)\\s.*");

    static final String SQL_SCRIPT_PATTERN = "activiti.%s.create.%s.sql";

    static final List<String> CATEGORIES = Arrays.asList(
            "engine",
            "history",
            "identity",
            "case.engine",
            "case.history",
            "decision.engine",
            "decision.history"
    );

    static final String GRANT_TABLE_TEMPLATE = "GRANT SELECT,INSERT,UPDATE,DELETE ON %s TO %s;";

    final Log log = new SystemStreamLog();

    final Set<String> tableNames = new HashSet<>();

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

    @SneakyThrows
    void generateSQlScriptFile(String sql) {
        try (PrintWriter out = new PrintWriter(outputPath)) {
            out.println(sql);
        }
    }

    @SneakyThrows
    void preExecute() {

        File currentFile = new File("").getCanonicalFile();

        File outputPathFile = new File(currentFile, outputPath);

        File parentFile = outputPathFile.getParentFile();

        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        outputPath = outputPathFile.getAbsolutePath();
    }

    void logMessage(String message) {
        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }

    @SneakyThrows
    Collection<Resource> getResources() {

        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver(DelegateExecutionContext.class.getClassLoader());

        Map<String, Resource> map = new HashMap<>();

        String resourcePattern = String.format(RESOURCE_PATTERN, schema);

        Stream.of(patternResolver.getResources(resourcePattern)).forEach(it -> map.put(it.getFilename(), it));

        List<Resource> resources = CATEGORIES.stream().map(this::getSqlScriptName).map(map::get).collect(Collectors.toList());

        resources.forEach(it -> log.info("Read SQL script from component '" + getComponent(it.getFilename(), schema) + "' with resource -> " + it.getFilename()));

        return resources;
    }

    String getSqlScriptName(String categoryName) {

        return String.format(SQL_SCRIPT_PATTERN, schema, categoryName);
    }

    String getGrantTableScript() {

        List<String> orderedTableNames = new ArrayList<>(tableNames);

        Collections.sort(orderedTableNames);

        return orderedTableNames.stream().map(it -> String.format(GRANT_TABLE_TEMPLATE, it, userName)).collect(Collectors.joining("\n"));
    }

    String getComponent(String filename, String schema) {

        return filename.replace("activiti." + schema + ".create.", "").replace(".sql", StringUtils.EMPTY);
    }

    @SneakyThrows
    String getSqlScript(Collection<Resource> resources) {

        StringBuilder builder = new StringBuilder();

        for (Resource resource : resources) {

            try (InputStream is = resource.getInputStream()) {

                String sql = IOUtils.toString(is, Charset.defaultCharset());

                String[] lines = sql.split("\n");

                Stream.of(lines).map(CREATE_TABLE_PATTERN::matcher).filter(Matcher::matches).map(it -> it.group(1)).forEach(tableNames::add);

                builder.append(sql);
            }
        }

        String grantTableScript = getGrantTableScript();

        builder.append("\n");
        builder.append(grantTableScript);

        return builder.toString();
    }
}
