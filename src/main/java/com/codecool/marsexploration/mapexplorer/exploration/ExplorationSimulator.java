package com.codecool.marsexploration.mapexplorer.exploration;

import com.codecool.marsexploration.mapexplorer.analizer.AllOutcomeAnalyzer;
import com.codecool.marsexploration.mapexplorer.commandCenter.CommandCenter;
import com.codecool.marsexploration.mapexplorer.configuration.ConfigurationParameters;
import com.codecool.marsexploration.mapexplorer.logger.Logger;
import com.codecool.marsexploration.mapexplorer.maploader.MapLoader;
import com.codecool.marsexploration.mapexplorer.maploader.model.Coordinate;
import com.codecool.marsexploration.mapexplorer.maploader.model.Symbol;
import com.codecool.marsexploration.mapexplorer.repository.ExplorationsDto;
import com.codecool.marsexploration.mapexplorer.repository.ExplorationsRepository;
import com.codecool.marsexploration.mapexplorer.rovers.Rover;
import com.codecool.marsexploration.mapexplorer.rovers.RoverStatus;
import com.codecool.marsexploration.mapexplorer.service.CoordinateCalculatorService;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.codecool.marsexploration.mapexplorer.rovers.RoverStatus.*;

public class ExplorationSimulator {

    private final ExplorationResultDisplay explorationResultDisplay;
    private final MapLoader mapLoader;
    private final AllOutcomeAnalyzer allOutcomeAnalyzer;
    private final Logger logger;
    private final ExplorationsRepository explorationsRepository;
    private MovementService movementService;

    public ExplorationSimulator(ExplorationResultDisplay explorationResultDisplay,
                                MapLoader mapLoader,
                                MovementService movementService,
                                AllOutcomeAnalyzer allOutcomeAnalyzer,
                                Logger logger,
                                ExplorationsRepository explorationsRepository) {
        this.explorationResultDisplay = explorationResultDisplay;
        this.mapLoader = mapLoader;
        this.movementService = movementService;
        this.allOutcomeAnalyzer = allOutcomeAnalyzer;
        this.logger = logger;
        this.explorationsRepository = explorationsRepository;
    }

    public Simulation runSimulation(ConfigurationParameters configurationParameters, List<Rover> rovers) {
        Simulation simulation = new Simulation(
                configurationParameters.maxSteps(),
                rovers,
                configurationParameters.spaceshipLandingPoint(),
                mapLoader.load(configurationParameters.mapPath()),
                configurationParameters.symbols()
        );

        SimulationStepsLogging simulationStepsLogging = new SimulationStepsLogging(simulation, logger, allOutcomeAnalyzer);

        while (simulation.explorationOutcome() == null) {
            movementService.move();

            configurationParameters.symbols().forEach(symbol -> rovers.forEach(rover -> rover.checkForObjectsAround(symbol)));

            rovers.forEach(Rover::addScannedCoordinates);

            ExplorationOutcome explorationOutcome = allOutcomeAnalyzer.analyze(simulation);

            simulationStepsLogging.logSteps();

            if (explorationOutcome != null) {
                int numberOfResources = rovers.get(0).getObjectsPoints().values().stream().mapToInt(Set::size).sum();
                ExplorationsDto explorationsDto = new ExplorationsDto(simulation.numberOfSteps(), numberOfResources, explorationOutcome);
                explorationsRepository.saveInDatabase(explorationsDto);
                simulation.setExplorationOutcome(explorationOutcome);
            }

            simulation.setNumberOfSteps(simulation.numberOfSteps() + 1);
        }

        explorationResultDisplay.displayExploredMap(simulation);
        return simulation;
    }
}
