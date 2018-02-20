package interfaces;
public interface Commandable {

    String getUsage();

    String getCommandName();

    String getDescription();

    void run (String [] args);
}
