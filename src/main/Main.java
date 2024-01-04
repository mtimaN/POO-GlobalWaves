package main;

import app.audio.LibrarySingleton;
import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.input.LibraryInput;
import app.player.AudioPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        LibraryInput library = objectMapper.readValue(new File(CheckerConstants.TESTS_PATH
                    + "library/library.json"), LibraryInput.class);
        LibrarySingleton myLibrary = LibrarySingleton.getInstance();
        myLibrary.getInput(library);
        ArrayNode outputs = objectMapper.createArrayNode();

        HashMap<String, AudioPlayer> audioPlayers = myLibrary.getAudioPlayers();
        AudioPlayer currentPlayer;

        Command[] commands = objectMapper.readValue(
                new File(CheckerConstants.TESTS_PATH + "/" + filePath1),
                Command[].class);

        for (Command command : commands) {
            if (audioPlayers.get(command.getUsername()) == null) {
                currentPlayer = new AudioPlayer(command);
                audioPlayers.put(command.getUsername(), currentPlayer);
            } else {
                currentPlayer = audioPlayers.get(command.getUsername());
            }
            command.execute(outputs, currentPlayer, objectMapper);
        }
        outputs.add(objectMapper
                .valueToTree(myLibrary.endProgram(commands[commands.length - 1])));
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), outputs);
        myLibrary.deleteLibrary();
    }
}
