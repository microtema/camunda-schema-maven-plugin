package de.microtema.maven.plugin.camunda.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CamundaDbSchemaGeneratorMojoTest {

    @InjectMocks
    CamundaDbSchemaGeneratorMojo sut;

    File outputPath;

    @BeforeEach
    void setUp() {
        outputPath = new File(sut.outputPath).getParentFile().getParentFile().getParentFile();
    }

    @Test
    void execute() {

        sut.execute();

        File outputPath = new File(sut.outputPath);

        assertTrue(outputPath.exists());
    }

    @Test
    void getResources() {

        Collection<Resource> resources = sut.getResources();

        assertEquals(7, resources.size());
    }

    @Test
    void getComponent() {

        String answer = sut.getComponent("activiti.oracle.create.activity.sql", "oracle");

        assertEquals("activity", answer);
    }

    @Test
    void getGrantTableTemplate() {

        sut.userName = "camunda_user";

        String answer = sut.getGrantTableTemplate();

        assertEquals("GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_PROPERTY TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_BYTEARRAY TO camunda_user;       \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_GE_SCHEMA_LOG TO camunda_user;      \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DEPLOYMENT TO camunda_user;      \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXECUTION TO camunda_user;       \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOB TO camunda_user;             \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_JOBDEF TO camunda_user;          \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_PROCDEF TO camunda_user;         \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_TASK TO camunda_user;            \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_IDENTITYLINK TO camunda_user;    \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_VARIABLE TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EVENT_SUBSCR TO camunda_user;    \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_INCIDENT TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_AUTHORIZATION TO camunda_user;   \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_FILTER TO camunda_user;          \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_METER_LOG TO camunda_user;       \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_EXT_TASK TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_BATCH TO camunda_user;           \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_PROCINST TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ACTINST TO camunda_user;         \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_TASKINST TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_VARINST TO camunda_user;         \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_DETAIL TO camunda_user;          \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_IDENTITYLINK TO camunda_user;    \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_COMMENT TO camunda_user;         \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_ATTACHMENT TO camunda_user;      \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_OP_LOG TO camunda_user;          \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_INCIDENT TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_JOB_LOG TO camunda_user;         \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_BATCH TO camunda_user;           \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_HI_EXT_TASK_LOG TO camunda_user;    \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_GROUP TO camunda_user;           \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_MEMBERSHIP TO camunda_user;      \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_USER TO camunda_user;            \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_INFO TO camunda_user;            \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT TO camunda_user;          \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_ID_TENANT_MEMBER TO camunda_user;   \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_CASE_DEF TO camunda_user;        \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_DEF TO camunda_user;    \n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RE_DECISION_REQ_DEF TO camunda_user;\n" +
                "GRANT SELECT,INSERT,UPDATE,DELETE ON ACT_RU_CASE_EXECUTION TO camunda_user; ", answer);
    }
}
