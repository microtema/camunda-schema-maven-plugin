package de.microtema.maven.plugin.camunda.schema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CamundaDbSchemaGeneratorMojoTest {

    @InjectMocks
    CamundaDbSchemaGeneratorMojo sut;

    @Test
    void execute() {

        File outputPath = new File(sut.outputPath);
        outputPath.delete();

        assertFalse(outputPath.exists());

        sut.execute();

        outputPath = new File(sut.outputPath);

        assertTrue(outputPath.exists());
    }
}
