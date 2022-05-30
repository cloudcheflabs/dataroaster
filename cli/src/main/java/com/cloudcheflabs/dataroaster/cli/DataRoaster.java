package com.cloudcheflabs.dataroaster.cli;

import com.cloudcheflabs.dataroaster.cli.command.Console;
import picocli.CommandLine;

public class DataRoaster {
    public static void main(String... args) {

        CommandLine.IExecutionExceptionHandler errorHandler = new CommandLine.IExecutionExceptionHandler() {
            public int handleExecutionException(Exception ex,
                                                CommandLine commandLine,
                                                CommandLine.ParseResult parseResult) {
                commandLine.getErr().println(ex.getMessage());
                commandLine.usage(commandLine.getErr());
                return commandLine.getCommandSpec().exitCodeOnExecutionException();
            }
        };

        CommandLine commandLine = new CommandLine(new Console());
        commandLine.setExecutionStrategy(new CommandLine.RunAll());
        commandLine.setExecutionExceptionHandler(errorHandler);
        commandLine.execute(args);

        if (args.length == 0) { commandLine.usage(System.out); }
    }
}
