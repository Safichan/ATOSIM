package org.palladiosimulator.blockchainsystems.atosim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ATOSIMSimulator {
	
    // ----------------------------------------------------
    // DEFAULT LOCATIONS (can be overridden via CLI args)
    // ----------------------------------------------------
    private static final Path DEFAULT_TESTMODELS_DIR =
            Paths.get("testmodels");
    
    // Configuration that is used in the simulations
    private static final Path DEFAULT_BASE_CONFIG_JSON =
            DEFAULT_TESTMODELS_DIR.resolve("configuration.json");
    
    // Configurations for the attacker
    private static final Path DEFAULT_CSV =
            Paths.get("run_configurations_selfish.csv");
    
    // Folder for the results
    private static final Path DEFAULT_OUTPUT_DIR =
            Paths.get("indiv_json");

    public static void main(String[] args) {

        // Parse named flags from command line from any position; remaining args are positional.
        String cliAttackType = null;
        Path outputDir = DEFAULT_OUTPUT_DIR;
        Integer rowIndex = null;
        List<String> positional = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if ("--attack-type".equals(args[i]) && i + 1 < args.length) {
                cliAttackType = args[++i];
            } else if ("--output-dir".equals(args[i]) && i + 1 < args.length) {
                outputDir = Paths.get(args[++i]);
            } else if ("--row-index".equals(args[i]) && i + 1 < args.length) {
                // 0-indexed into the CSV's data rows; runs only that single row.
                // Lets a Slurm job array fan a CSV out across separate jobs via
                // --array=0-N-1 and --row-index $SLURM_ARRAY_TASK_ID.
                rowIndex = Integer.parseInt(args[++i]);
            } else {
                positional.add(args[i]);
            }
        }
        
        // If there are no arguments use the defaults
        Path csvPath        = positional.size() >= 1 ? Paths.get(positional.get(0)) : DEFAULT_CSV;
        Path testmodelsDir  = positional.size() >= 2 ? Paths.get(positional.get(1)) : DEFAULT_TESTMODELS_DIR;
        Path baseConfigJson = positional.size() >= 3 ? Paths.get(positional.get(2)) : DEFAULT_BASE_CONFIG_JSON;
        
        
        // Initialize the simulator
        BlockchainATOSIMStandalone simulator =
                new BlockchainATOSIMStandalone(
                        "org.palladiosimulator.blockchainsystems.atosim",
                        Activator.class,
                        outputDir);

        if (!simulator.initAnalysis()) {
            System.err.println("❌ Unable to initialize simulator");
            return;
        }

        System.out.println("✔ Simulator initialized");
        System.out.println("CSV: " + csvPath.toAbsolutePath());
        System.out.println("testmodels/: " + testmodelsDir.toAbsolutePath());
        System.out.println("Base config: " + baseConfigJson.toAbsolutePath());
        System.out.println("Output dir:  " + outputDir.toAbsolutePath());
        if (cliAttackType != null) {
            System.out.println("Attack type (CLI override): " + cliAttackType);
        }

        try {
        	// Load configurations into java map/list
            Map<String, String> baseConfig = loadJsonConfig(baseConfigJson);
            List<Map<String, String>> allRows = loadCsv(csvPath);

            List<Map<String, String>> rows;
            if (rowIndex != null) {
                if (rowIndex < 0 || rowIndex >= allRows.size()) {
                    throw new IllegalArgumentException(
                            "--row-index " + rowIndex + " out of bounds (CSV has " + allRows.size() + " row(s))");
                }
                rows = List.of(allRows.get(rowIndex));
                System.out.println("Row filter: --row-index " + rowIndex + " (of " + allRows.size() + " rows)");
            } else {
                rows = allRows;
            }

            int rowCounter = 0;

            for (Map<String, String> row : rows) {
                rowCounter++;

                // Validate CSV row BEFORE running anything
                validateCsvColumns(row);

                // Start from base configuration.json
                Map<String, String> config = new LinkedHashMap<>(baseConfig);

                // Preserve config_id from CSV if present, else use row counter
                String configId = row.getOrDefault("config_id", String.valueOf(rowCounter));
                config.put("config_id", configId);
                config.put("id", configId);

                // Copy CSV parameters as-is
                config.putAll(row);

                // CLI --attack-type overrides JSON and CSV values
                if (cliAttackType != null) {
                    config.put("attack_type", cliAttackType);
                }

                Path systemModelPath = pickSystemModelPath(testmodelsDir, row.get("system_config_id"));
                config.put("blockchainSystemModelFilePath", systemModelPath.toString());

                Path attackModelPath = pickAttackModelPath(
                        testmodelsDir, row.get("attack_strategy"), row.get("attacker_config_id"));
                config.put("attackModelFilePath", attackModelPath.toString());

                // runId == config_id (not a sequential counter) so that separate
                // Slurm array tasks each writing result_run_<runId>.json never collide.
                int runId = toRunId(configId, rowCounter);

                System.out.println("\n▶ Run " + runId + " | config_id=" + configId);
                System.out.println("   System model: " + systemModelPath.toAbsolutePath());
                System.out.println("   Attack model: " + attackModelPath.toAbsolutePath());
                System.out.println("   Attack type  = " + config.getOrDefault("attack_type", "SELFISH_MINING (default)"));
                System.out.println("   Monte-Carlo rounds = "
                        + config.getOrDefault("numberOfMonteCarloRounds", "?"));

                simulator.runSimulation(config, runId);
            }

            System.out.println("\n✔ All runs completed");

        } catch (Exception e) {
            System.err.println("❌ Batch execution failed");
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------
    // Load base configuration.json
    // ----------------------------------------------------
    private static Map<String, String> loadJsonConfig(Path jsonPath) throws IOException {
        if (!Files.exists(jsonPath)) {
            throw new IllegalArgumentException(
                    "Base configuration.json not found: " + jsonPath.toAbsolutePath());
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();

        try (Reader r = Files.newBufferedReader(jsonPath)) {
            Map<String, String> m = gson.fromJson(r, type);
            if (m == null || m.isEmpty()) {
                throw new IllegalArgumentException(
                        "Base configuration.json is empty/unreadable: "
                                + jsonPath.toAbsolutePath());
            }
            return m;
        }
    }

    // ----------------------------------------------------
    // Load CSV configurations
    // ----------------------------------------------------
    private static List<Map<String, String>> loadCsv(Path csvPath) throws IOException {
        if (!Files.exists(csvPath)) {
            throw new IllegalArgumentException(
                    "CSV not found: " + csvPath.toAbsolutePath());
        }

        List<Map<String, String>> rows = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath)) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException(
                        "CSV is empty: " + csvPath.toAbsolutePath());
            }

            String[] headers = headerLine.split(",");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",", -1);
                Map<String, String> row = new LinkedHashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "No rows found in CSV: " + csvPath.toAbsolutePath());
        }

        return rows;
    }

    // ----------------------------------------------------
    // CSV validation
    // ----------------------------------------------------
    private static void validateCsvColumns(Map<String, String> row) {

        List<String> required = List.of(
                "config_id",
                "system_config_id",
                "validator_count",
                "node_degree",
                "bandwidth",
                "block_creation_interval",
                "max_block_size",
                "attacker_config_id",
                "attack_strategy"
        );

        for (String key : required) {
            if (!row.containsKey(key) || row.get(key).isBlank()) {
                throw new IllegalArgumentException(
                        "❌ Missing or empty required CSV column: " + key
                                + " in row: " + row);
            }
        }
    }

    private static int toRunId(String configId, int fallback) {
        try {
            return Integer.parseInt(configId.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ----------------------------------------------------
    // Two-stage model resolution
    // ----------------------------------------------------
    // generate_models_two_stage.py lays models out as:
    //   <testmodelsDir>/system_models/sys-<system_config_id>/Net.blockchainsystem (+ siblings)
    //   <testmodelsDir>/attack_models/<attack_strategy>/atk-<attacker_config_id>/Net.attackmodel
    // The two are combined at load time (see BlockchainSystemModelLoader); attack models
    // generated against one reference system model's NodeSystem ids are repaired there too,
    // so any system_config_id may be paired with any attacker_config_id/attack_strategy.

    private static Path pickSystemModelPath(Path testmodelsDir, String systemConfigId) {
        Path modelPath =
                testmodelsDir
                        .resolve("system_models")
                        .resolve("sys-" + systemConfigId)
                        .resolve("Net.blockchainsystem");

        if (!Files.exists(modelPath)) {
            throw new IllegalArgumentException(
                    "❌ System model not found for system_config_id=" + systemConfigId +
                    " at " + modelPath.toAbsolutePath());
        }

        return modelPath;
    }

    private static Path pickAttackModelPath(Path testmodelsDir, String attackStrategy, String attackerConfigId) {
        Path modelPath =
                testmodelsDir
                        .resolve("attack_models")
                        .resolve(attackStrategy)
                        .resolve("atk-" + attackerConfigId)
                        .resolve("Net.attackmodel");

        if (!Files.exists(modelPath)) {
            throw new IllegalArgumentException(
                    "❌ Attack model not found for attack_strategy=" + attackStrategy +
                    ", attacker_config_id=" + attackerConfigId + " at " + modelPath.toAbsolutePath());
        }

        return modelPath;
    }
}

