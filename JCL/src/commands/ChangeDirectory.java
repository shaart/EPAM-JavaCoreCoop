package commands;

import interfaces.Commandable;

public class ChangeDirectory implements Commandable {
    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getCommandName() {
        return "cd";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void run(String[] args) {

    }
}
