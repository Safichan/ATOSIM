/**
 * RESPONSIBILITY:
 * - Initializes the standalone Palladio/Threesim environment.
 * - Executes a single simulation run and serializes the result to JSON.
 *
 * KEY CHANGE:
 * - Each experiment now produces its own result file containing:
 *   (1) config_id,
 *   (2) full input configuration,
 *   (3) raw simulation results.
 *
 * WHY THIS WAS NECESSARY:
 * - Guarantees traceability and reproducibility of every experiment.
 */


package org.palladiosimulator.blockchainsystems.atosim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

import org.palladiosimulator.blockchainsystems.core.simulation.abstractions.SimulationParameters;
import org.palladiosimulator.blockchainsystems.threesim.simulation.results.ThreesimSimulationResultSerializer;
import org.palladiosimulator.blockchainsystems.core.simulation.MonteCarloSimulationParameters;
import org.palladiosimulator.blockchainsystems.core.simulation.SimulationType;
import org.palladiosimulator.blockchainsystems.core.simulation.SingleSimulationParameters;

public class BlockchainATOSIMStandalone {

    private final Logger logger =
            Logger.getLogger(BlockchainATOSIMStandalone.class);

    private String modelProjectName;
    private Class<? extends Plugin> modelProjectActivator;
    private Path outputDir;
    
    
    // Initialize simulation environment
    public BlockchainATOSIMStandalone(String modelProjectName,
                                        Class<? extends Plugin> modelProjectActivator,
                                        Path outputDir) {
        this.modelProjectName = modelProjectName;
        this.modelProjectActivator = modelProjectActivator;
        this.outputDir = outputDir;
    }

    public boolean initAnalysis() {
        EcorePlugin.ExtensionProcessor.process(null);
        return initStandalone();
    }
    
    /**
     * Runs one simulation and writes a dedicated JSON file for this configuration.
     */

    public void runSimulation(Map<String, String> configuration, int runId) {
    	
    	// To measure memory and time later on
    	var startTime = System.nanoTime();
    	
    	Runtime runtime = Runtime.getRuntime();
    	runtime.gc();
    	long before = runtime.totalMemory() - runtime.freeMemory();
    	
    	// Convert to simulation configurations java object
        var simulationParameters =
                getSimulationParametersFromConfiguration(configuration);
        
        // Create a simulation runner with all parameters
        var simulationFactory =
                new ATOSIMSimulationFactory(simulationParameters, configuration);
        
        // Run simulation
        var result = simulationFactory.run();

        var serializer = new ThreesimSimulationResultSerializer();

        boolean writeChainReorganizationsAsCsv = Boolean.parseBoolean(
                configuration.getOrDefault("writeChainReorganizationsAsCsv", "false"));

        String simulationJson;
        String chainReorganizationsCsv = null;
        if (writeChainReorganizationsAsCsv) {
            simulationJson = serializer.serialize(result, false);
            chainReorganizationsCsv = serializer.serializeChainReorganizationsCsv(result);
        } else {
            simulationJson = serializer.serialize(result);
        }

        var stopTime = System.nanoTime();

        long after = runtime.totalMemory() - runtime.freeMemory();
        var memoryUsed = (after - before) / (1024 * 1024);

        Map<String, Object> finalResult = new LinkedHashMap<>();
        finalResult.put("runId", runId);
        finalResult.put("config_id", configuration.get("config_id"));
        finalResult.put("inputParameters", configuration);
        finalResult.put("simulationResult",
                JsonParser.parseString(simulationJson));
        finalResult.put("startSimulationTime", startTime);
        finalResult.put("stopSimulationTime", stopTime);
        finalResult.put("simulationTime", stopTime - startTime);
        finalResult.put("memoryUsed", memoryUsed);

        String jsonResult = new GsonBuilder()
                .create()
                .toJson(finalResult);

        try {
            Path outputFile = createOutputPath(runId);
            Files.createDirectories(outputFile.getParent());

            writeAtomically(outputFile, jsonResult);

            if (chainReorganizationsCsv != null) {
                Path chainReorganizationsFile = createChainReorganizationsCsvPath(runId);
                writeAtomically(chainReorganizationsFile, chainReorganizationsCsv);
            }

            System.out.println("✔ Result saved: " + outputFile.toAbsolutePath());

        } catch (IOException e) {
            logger.error("Failed to write simulation result", e);
        }
    }

/**
 * Writes content to a temp file in the same directory, flushes and closes it, then
 * atomically renames it into place. A process kill mid-write (e.g. a SLURM OOM or
 * time-limit SIGKILL, which bypasses try-with-resources entirely) then leaves at worst a
 * stray ".tmp" file -- never a truncated result_run_*.json -- since ATOMIC_MOVE is
 * guaranteed all-or-nothing by the filesystem, unlike writing straight to the final path.
 */
    private void writeAtomically(Path targetFile, String content) throws IOException {
        Path tmpFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmpFile)) {
            writer.write(content);
        }
        Files.move(tmpFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

/**
 * Builds a unique, human-readable result filename per configuration.
 * Keeps results colocated with the corresponding model folder.
 */

    private Path createOutputPath(int runId) {
        return outputDir.resolve("result_run_" + runId + ".json");
    }

/**
 * By default, chainReorganizations is embedded directly in result_run_<runId>.json (see
 * ThreesimJsonSerializer). Only when the writeChainReorganizationsAsCsv config.json flag is set
 * to "true" is it written as this sibling CSV file instead (see
 * ThreesimChainReorganizationsCsvSerializer) -- useful at production scale, where the embedded
 * form dominates JSON file size. result_run_<runId>.json always carries the cheap per-round
 * chainReorganizationDepths summary either way.
 */

    private Path createChainReorganizationsCsvPath(int runId) {
        return outputDir.resolve("result_run_" + runId + ".chainReorganizations.csv");
    }

    private boolean initStandalone() {
        try {
            StandaloneInitializerBuilder.builder()
                    .registerProjectURI(
                            this.modelProjectActivator,
                            this.modelProjectName)
                    .registerProjectURI(org.glassfish.hk2.osgiresourcelocator.Activator.class,
                            "org.glassfish.hk2.osgi-resource-locator")
                    .build()
                    .init();

            logger.info("Successfully initialized standalone environment.");
            return true;

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone environment.", e);
            System.out.println(e.toString());
            return false;
        }
    }
    
    // Converts configuration into Monte Carlo or simple parameters (java objects)
    private SimulationParameters getSimulationParametersFromConfiguration(
            Map<String, String> configuration) {

        SimulationType simulationType = SimulationType.Single;
        if ("Monte-Carlo".equals(configuration.getOrDefault("simulationType", ""))) {
            simulationType = SimulationType.MonteCarlo;
        }

        long maxAllowedBlockchainLength =
                Long.parseLong(
                        configuration.getOrDefault(
                                "maxAllowedBlockchainLength", "30"));

        int numberOfMonteCarloRounds =
                Integer.parseInt(
                        configuration.getOrDefault(
                                "numberOfMonteCarloRounds", "1"));

        int monteCarloParallelism =
                Integer.parseInt(
                        configuration.getOrDefault(
                                "monteCarloParallelism", "0"));

        String blockchainSystemModelFilePath =
                configuration.getOrDefault(
                        "blockchainSystemModelFilePath", "");

        return (simulationType == SimulationType.MonteCarlo)
                ? new MonteCarloSimulationParameters(
                maxAllowedBlockchainLength,
                numberOfMonteCarloRounds,
                monteCarloParallelism,
                blockchainSystemModelFilePath)
                : new SingleSimulationParameters(
                maxAllowedBlockchainLength,
                blockchainSystemModelFilePath);
    }
}
