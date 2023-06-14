package com.codecool.marsexploration.mapexplorer.exploration;

import com.codecool.marsexploration.mapexplorer.analizer.AllOutcomeAnalyzer;
import com.codecool.marsexploration.mapexplorer.configuration.ConfigurationParameters;
import com.codecool.marsexploration.mapexplorer.configuration.ConfigurationValidator;
import com.codecool.marsexploration.mapexplorer.logger.Logger;
import com.codecool.marsexploration.mapexplorer.maploader.MapLoader;
import com.codecool.marsexploration.mapexplorer.rovers.Rover;
import com.codecool.marsexploration.mapexplorer.rovers.RoverPlacement;

public class ExplorationSimulator {

    private final AllOutcomeAnalyzer allOutcomeAnalyzer;
    private ConfigurationParameters configurationParameters;
    private MapLoader mapLoader;
    private ConfigurationValidator configurationValidator;
    private RoverPlacement roverPlacement;
    private Rover rover;
    private MovementService movementService;

    private Logger logger;

    public ExplorationSimulator(ConfigurationParameters configurationParameters, MapLoader mapLoader, ConfigurationValidator configurationValidator, RoverPlacement roverPlacement, Rover rover, MovementService movementService, AllOutcomeAnalyzer allOutcomeAnalyzer, Logger logger) {
        this.configurationParameters = configurationParameters;
        this.mapLoader = mapLoader;
        this.configurationValidator = configurationValidator;
        this.roverPlacement = roverPlacement;
        this.allOutcomeAnalyzer = allOutcomeAnalyzer;
        this.rover = rover;
        this.movementService = movementService;
        this.logger = logger;
    }

    public void runSimulation(ConfigurationParameters configurationParameters) {
        Simulation simulation = new Simulation(0, configurationParameters.maxSteps(), rover,
                configurationParameters.spaceshipLandingPoint(),
                mapLoader.load(configurationParameters.mapPath()), configurationParameters.symbols(), null);
        SimulationStepsLogging simulationStepsLogging = new SimulationStepsLogging(simulation, logger, allOutcomeAnalyzer);

        while (simulation.explorationOutcome() == null && simulation.numberOfSteps() < configurationParameters.maxSteps()) {
            movementService.move();
            configurationParameters.symbols().forEach(symbol -> {
                rover.checkForResourcesAround(symbol);
            });
            rover.addScannedCoordinates();
            ExplorationOutcome explorationOutcome = allOutcomeAnalyzer.analyze(simulation);
            simulation.setNumberOfSteps(simulation.numberOfSteps() + 1);
            if (explorationOutcome != null) {
                simulation.setExplorationOutcome(explorationOutcome);
            }
            System.out.println(simulation);
            simulationStepsLogging.logSteps();
        }

        // IN LOOP

        //Movement. The rover needs to determine an adjacent empty spot of the chart to move

        // Scanning. The rover needs to scan the area for resources based on how far it can see (its sight).

        // Analysis. After the information is gathered, you need to determine whether an outcome is reached.

        // Log. Write the current state of events in the simulation to the log file.

        // Step increment. Increment the context step variable by one.


    }


    public AllOutcomeAnalyzer getAllOutcomeAnalyzer() {
        return allOutcomeAnalyzer;
    }
}
