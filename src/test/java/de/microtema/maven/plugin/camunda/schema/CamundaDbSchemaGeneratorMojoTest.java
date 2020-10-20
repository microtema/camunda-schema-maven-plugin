package de.microtema.maven.plugin.camunda.schema;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        outputPath.delete();
    }

    @AfterEach
    void tearDown() {
        outputPath.delete();
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
    void getSqlScriptName() {

        assertEquals("activiti.oracle.create.engine.sql", sut.getSqlScriptName("engine"));
    }

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "0,  engine",
            "1,  history",
            "2,  identity",
            "3,  case.engine",
            "4,  case.history",
            "5,  decision.engine",
            "6,  decision.history"
    })
    void verifyCategory(int index, String expected) {

        assertEquals(expected, CamundaDbSchemaGeneratorMojo.CATEGORIES.get(index));
    }
}
