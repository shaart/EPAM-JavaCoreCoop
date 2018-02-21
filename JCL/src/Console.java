import interfaces.Commandable;

import java.util.Map;

public class Console {

    public static void main(String[] args) {

        Map<String, Commandable> commands = CommandConnector.getCommands();

       for (Map.Entry entry: commands.entrySet()){
            System.out.println("command: " + entry.getKey() + "       " + entry.getValue());

        }

        System.out.println("size: " + commands.size());

        commands.get("zip").run(new String[]{}); // command test

    }
}

