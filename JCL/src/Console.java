import general.CommandConnector;
import interfaces.Commandable;
import commands.Zip;
import java.util.Map;

public class Console {

    public static void main(String[] args) {

        Map<String, Commandable> commands = CommandConnector.getCommands();

       for (Map.Entry entry: commands.entrySet()){
            System.out.println("command: " + entry.getKey() + "       " + entry.getValue());

        }

        System.out.println("size: " + commands.size());

        commands.get("zip").run(new String[]{"u", "D://Music//Notepad++"}); // zip command test
        //commands.get("zip").run(new String[]{String.valueOf(0), "D://Music//default.mp3"}); // zip command test
        //Zip.unzip("D://Music//b.zip");


    }
}

