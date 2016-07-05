package Game;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Persistence {
    private static Path saveDirectory = Paths.get("");

    public static Path getSaveDirectory() {
        return saveDirectory;
    }

    public static void setSaveDirectory(Path path){
        Persistence.saveDirectory = path;
    }

    public static void setSaveDirectory(String pathString){
        setSaveDirectory(Paths.get(pathString));
    }

    public static String[] loadMoves(String pathString){
        return loadMoves(Paths.get(pathString));
    }

    private static Path generateNewPath(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime dateTime = LocalDateTime.now();
        return Paths.get(getSaveDirectory().toString(), dateTime.format(formatter));
    }

    public static String[] loadMoves(Path path){
        ArrayList<String> moves = new ArrayList<>(1);
        try {
            Files.lines(path).forEachOrdered(line -> {
                String[] parts = line.split(",");
                moves.add(parts[0]);
                if (parts.length == 2) moves.add(parts[1]);
            });
            return moves.toArray(new String[moves.size()]);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return new String[0];
        } catch (IOException e){
            e.printStackTrace();
            return new String[0];
        }
    }
    public static void saveMoves(String[] moves){
        ArrayList<String> lines = new ArrayList<>(0);
        int i = 0;
        String line;
        while (lines.size()*2<moves.length){
            line = moves[i];
            i ++;
            if (i<moves.length){
                line += ","+moves[i];
                i++;
            }
            lines.add(line);
        }
        int v = 0;
        Path path = generateNewPath();
        while (Files.exists(path)){
            path = Paths.get(path.toString()+"_"+Integer.toString(v++));
        }
        System.out.println(path.toString());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            lines.stream().forEachOrdered(l -> {
                try {
                    writer.write(l);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
