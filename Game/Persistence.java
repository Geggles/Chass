package Game;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Stream;

public class Persistence {
    public static String[] loadMoves(String pathString){
        return loadMoves(Paths.get(pathString));
    }

    public static String[] loadMoves(Path path){
        LinkedList<String> moves = new LinkedList<>();
        try {
            Files.lines(path).forEachOrdered(line -> {
                if (!line.equals("")) {
                    String[] parts = line.split(",");
                    moves.add(parts[0]);
                    if (parts.length == 2) moves.add(parts[1]);
                }
            });
            return moves.toArray(new String[moves.size()]);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Path generateNewPath(Path basePath){
        return generateNewPath(basePath.toString());
    }

    public static Path generateNewPath(String basePath){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH-mm-ss");
        LocalDateTime dateTime = LocalDateTime.now();
        return Paths.get(basePath, dateTime.format(formatter) + ".cgn");
    }

    public static String[] moveChain(String moveSeries){
        LinkedList<String> moves = new LinkedList<>();
        for (String line: moveSeries.split("\n")) {
            if (!line.equals("")) {
                String[] parts = line.split(",");
                moves.add(parts[0]);
                if (parts.length == 2) moves.add(parts[1]);
            }
        }
        return moves.toArray(new String[moves.size()]);
    }

    public static String moveSeries(String[] moves){
        String result = "";
        boolean white = true;
        for(String move: moves){
            result += move;
            result += white? ',': '\n';
            white = !white;
        }
        return result.substring(0, result.length());
    }

    public static Path saveMoves(String[] moves, Path path){
        boolean replaceExisting = true;
        File file = new File(path.toUri());
        if (file.isDirectory()) {
            file = new File(generateNewPath(path).toUri());
            replaceExisting = false;
        }

        int v = 0;
        while (!replaceExisting && file.exists()){
            file = new File(path.toString()+"_"+Integer.toString(v++));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            System.out.println(moveSeries(moves));
            writer.write(moveSeries(moves));
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            return file.toPath();
        }
    }
}
